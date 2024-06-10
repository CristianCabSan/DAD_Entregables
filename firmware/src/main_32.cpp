#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <PubSubClient.h>
#include <WiFiUdp.h>
#include <string>

const int BOARD_ID = 1;
const int DEVICE_ID = 1;
const int GROUP_ID = 1;
const int numberOfValues = 2;
const int actuatorPin = 25;
const int sensorPin = 34;

int test_delay = 1000; // so we don't spam the API
boolean describe_tests = true;
boolean active = true;
HTTPClient http;

// Replace WifiName and WifiPassword by your WiFi credentials
  /*
  #define STASSID "Redmi"           //"Your_Wifi_SSID"
  #define STAPSK "1234abcd"         //"Your_Wifi_PASSWORD"
  String serverName = "http://192.168.43.236:8084/";
  */
  
  #define STASSID "DIGIFIBRA-9sYQ"  //"Your_Wifi_SSID"
  #define STAPSK "EyU4QyukeDHK"     //"Your_Wifi_PASSWORD"
  String serverName = "http://192.168.1.141:8084/";
  

// MQTT configuration
WiFiClient espClient;
PubSubClient client(espClient);

// Server IP, where de MQTT broker is deployed
const char *MQTT_BROKER_ADRESS = "192.168.1.141"; //Mi IP
const uint16_t MQTT_PORT = 1883;

// Name for this MQTT client. La libreria PubSubClient lo necesita
// Debe ser unico para cada placa
const char *MQTT_CLIENT_NAME = "Placa_1";

// Callback a ejecutar cuando se recibe un mensaje
void OnMqttReceived(char *topic, byte *payload, unsigned int length)
{
  Serial.print("Received on ");
  Serial.print(topic);
  Serial.print(": ");

  String content = "";
  for (size_t i = 0; i < length; i++)
  {
    content.concat((char)payload[i]);
  }
  if (strcmp(topic, "group_1") == 0) {
    if(content == "ON"){
      digitalWrite(actuatorPin, HIGH);
    } else if (content == "OFF"){
      digitalWrite(actuatorPin, LOW);
    } else if (content == "start"){
        active = true;
    } else if (content = "stop"){
        active = false;
    }
  }
  Serial.print(content);
  Serial.println();
}

void InitMqtt()
{
  client.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
  client.setCallback(OnMqttReceived);
}

// Setup
void setup()
{
  Serial.begin(9600);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);
  pinMode(actuatorPin, OUTPUT);
  pinMode(sensorPin, INPUT);

  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  InitMqtt();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  client.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
  Serial.println("Setup!");
  
}

// conecta o reconecta al MQTT
// consigue conectar -> suscribe a topic y publica un mensaje
// no -> espera 5 segundos
void ConnectMqtt()
{
  Serial.print("Starting MQTT connection...");
  if (client.connect(MQTT_CLIENT_NAME))
  {
    //Ejmplo de suscripcion
    Serial.print("Connected");
    client.subscribe("group_1");
    client.subscribe("group_1/sensor_1");
    client.subscribe("group_2");
  }
  else
  {
    Serial.print("Failed MQTT connection, rc=");
    Serial.print(client.state());
    Serial.println(" try again in 5 seconds");
    delay(5000);
  }
}

void HandleMqtt()
{
  if (!client.connected())
  {
    ConnectMqtt();
  }
  client.loop();
}

String response;

String serializeSensorValueBody(int ID, int boardID, int groupID, double value, String type, long date)
{
  DynamicJsonDocument doc(2048);
  doc["ID"] = ID;
  doc["boardID"] = boardID;
  doc["groupID"] = groupID;
  doc["value"] = value;
  doc["type"] = type;
  doc["date"] = date;
  doc["removed"] = false;

  String output;
  serializeJson(doc, output);
  Serial.println(output);

  return output;
}

String serializeActuatorStatusBody(int ID, int boardID, int groupID, double value, String type, long date)
{
  DynamicJsonDocument doc(2048);
  doc["ID"] = ID;
  doc["boardID"] = boardID;
  doc["groupID"] = groupID;
  doc["date"] = date;
  doc["value"] = value;
  doc["type"] = type;
  doc["date"] = date;
  doc["removed"] = false;

  String output;
  serializeJson(doc, output);
  Serial.println(output);

  return output;
}

String serializeDeviceBody(int ID, long date)
{
  DynamicJsonDocument doc(2048);
  doc["ID"] = ID;
  doc["date"] = date;

  String output;
  serializeJson(doc, output);
  Serial.println(output);

  return output;
}

void deserializeActuatorStatusBody(String responseJson)
{
  if (responseJson != "")
  {
    DynamicJsonDocument doc(2048);
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    int ID = doc["ID"];
    int boardID = doc["boardID"];
    int groupID = doc["groupID"];
    double value = doc["value"];
    String type = doc["type"];
    long date = doc["date"];

    Serial.println(("Sensor deserialized: [sensorID: " + String(ID) + ", boardID: " + boardID + ", groupID: " + groupID + ", value: " + value + ", type: " + type + ", date: " + date  + "]").c_str());
  }
}

void deserializeDeviceBody(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    DynamicJsonDocument doc(2048);

    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    JsonArray array = doc.as<JsonArray>();
    for (JsonObject board : array)
    {
      int boardID = board["ID"];
      long date = board["date"];

      Serial.println(("Device deserialized: [boardID: " + String(boardID) + ", date: " + String(date) + "]").c_str());
    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void deserializeSensorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }
    
    JsonArray array = doc.as<JsonArray>();
    for (JsonObject sensor : array)
    {
      int sensorID = sensor["ID"];
      int boardID = sensor["boardID"];
      int groupID = sensor["groupID"];
      String type = sensor["type"];
      double value = sensor["value"];
      long date = sensor["date"];

      Serial.println(("Sensor deserialized: [sensorID: " + String(sensorID) + ", boardID: " + String(boardID) + ", groupID: " + String(groupID) + ", type: " + type + ", value: " + value + ", date: " + date  + "]").c_str());
    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void deserializeActuatorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    JsonArray array = doc.as<JsonArray>();
    for (JsonObject actuator : array)
    {
      int actuatorID = actuator["ID"];
      int boardID = actuator["boardID"];
      String type = actuator["type"];
      double value = actuator["value"];
      long timestamp = actuator["date"];

      Serial.println(("Actuator deserialized: [actuatorID: " + String(actuatorID) + ", boardID: " + boardID + ", type: " + type + ", value: " + value + ", date: " + timestamp  + "]").c_str());
    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void test_response(int httpResponseCode)
{
  delay(test_delay);
  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String payload = http.getString();
    Serial.println(payload);
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}

void GET_tests()
{
  String serverPath = serverName + "api/sensors/" + String(GROUP_ID) +  "/" + String(BOARD_ID);
  describe("----Todos los sensores de la placa 1----");
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());
  describe("----------------------------");

  describe("----Todos los sensores----");
  serverPath = serverName + "api/sensors/sensor/all";
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());
  describe("----------------------------");
  
  describe("----Sensor concreto----");
  serverPath = serverName + "api/sensors/" + String(GROUP_ID) +  "/" + String(BOARD_ID) + "/" + String(DEVICE_ID);
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());
  describe("----------------------------");

  describe("----Ultimos 2 valores de un sensor concreto----");
  serverPath = serverName + "api/sensors/" + String(GROUP_ID) +  "/"  + String(BOARD_ID) + "/" + String(DEVICE_ID) + "/" + String(numberOfValues);
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());
  describe("----------------------------");

  //--------------------------------//
  
  describe("----Todos los actuadores de la placa 1----");
  serverPath = serverName + "api/actuators/" + String(GROUP_ID) +  "/"  + String(BOARD_ID);
  http.begin(serverPath.c_str());
  // test_response(http.GET());
  deserializeActuatorsFromDevice(http.GET());
  describe("----------------------------");

  describe("----Todos los actuadores----");
  serverPath = serverName + "api/actuators/actuator/all";
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());
  describe("----------------------------");
  
 
  describe("----Actuador concreto----");
  serverPath = serverName + "api/actuators/" + String(GROUP_ID) +  "/"  + String(BOARD_ID) + "/" + String(DEVICE_ID);
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());
  describe("----------------------------");

  describe("----Ultimos 2 valores de un actuador concreto----");
  serverPath = serverName + "api/actuators/" + String(GROUP_ID) +  "/"  + String(BOARD_ID) + "/" + String(DEVICE_ID) + "/" + String(numberOfValues);
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());
  describe("----------------------------");

  //--------------------------------//

  describe("----Ultimos 2 valores de entre todos los actuadores de la placa 1----");
  serverPath = serverName + "api/boards/actuators/" + String(GROUP_ID) +  "/"  + String(BOARD_ID) + "/" + String(numberOfValues);
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());
  describe("----------------------------");

  describe("----Ultimos 2 valores de entre todos los sensores de la placa 1----");
  serverPath = serverName + "api/boards/sensors/" + String(GROUP_ID) +  "/"  + String(BOARD_ID) + "/" + String(numberOfValues);
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());
  describe("----------------------------");
}

void POST_tests()
{
  String serverPath;
  describe("Test POST with sensor value");
  String sensor_value_body = serializeSensorValueBody(DEVICE_ID, BOARD_ID, GROUP_ID, 10.0, "SensorPrueba", millis());
  serverPath = serverName + "api/sensors";
  http.begin(serverPath.c_str());
  test_response(http.POST(sensor_value_body));
}

unsigned long previousMillis = 0;
const long interval = 10000; //Tiempo en ms entre ejecucion

void loop()
{
  
    HandleMqtt();

  unsigned long currentMillis = millis();
  unsigned long diferencia = currentMillis - previousMillis;

  //Añadir comprobacion de conexion antes de empezar a enviar
  if (diferencia >= interval && active == true) {
    previousMillis = currentMillis;
    int value = analogRead(sensorPin);
    String sensor_value_body = serializeSensorValueBody(DEVICE_ID, BOARD_ID, GROUP_ID, value , "SensorPrueba", millis());
    String serverPath = serverName + "api/sensors";
    http.begin(serverPath.c_str());
    test_response(http.POST(sensor_value_body));
  }
}

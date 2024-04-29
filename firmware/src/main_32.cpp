#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <WiFiUdp.h>
#include <list>
#include <PubSubClient.h>
//#include <RestClient.h>



// Replace 0 by ID of this current device
const int BOARD_ID = 1;
const int DEVICE_ID = 1;
const int GROUP_ID = 1;
const int numberOfValues = 2;

WiFiClient espClient;
PubSubClient pubsubClient(espClient);
char msg[50];


int test_delay = 1000; // so we don't spam the API
boolean describe_tests = true;

// Replace 0.0.0.0 by your server local IP (ipconfig [windows] or ifconfig [Linux o MacOS] gets IP assigned to your PC)
HTTPClient http;

// Replace WifiName and WifiPassword by your WiFi credentials
  #define STASSID "Redmi"           //"Your_Wifi_SSID"
  #define STAPSK "1234abcd"         //"Your_Wifi_PASSWORD"
  String serverName = "http://192.168.43.236:8084/";

  /*
  #define STASSID "DIGIFIBRA-9sYQ"  //"Your_Wifi_SSID"
  #define STAPSK "EyU4QyukeDHK"     //"Your_Wifi_PASSWORD"
  String serverName = "http://192.168.43.236:8084/";
  */


// Setup
void setup()
{
  pubsubClient.setServer("192.168.0.111", 1883);
  pubsubClient.setCallback(callback);



  Serial.begin(9600);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);

  /* Explicitly set the ESP32 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");
}

void callback(char* topic, byte* payload, unsigned int length) {
	Serial.print("Mensaje recibido [");  
	Serial.print(topic);  
	Serial.print("] ");
	String message = String((char *) payload);  
	Serial.print(message);  
	Serial.println();  

	// Trabajar con el mensaje
}

void reconnect() {  
	while (! pubsubClient.connected()) {    
		Serial.print("Conectando al servidor MQTT");    
		if (pubsubClient.connect("ESP8266Client")) {      
			Serial.println("Conectado");      
			pubsubClient.publish("topic_2", "Hola a todos");      
			pubsubClient.subscribe("topic_1");    
		} else {      
			Serial.print("Error, rc=");      
			Serial.print(pubsubClient.state());      
			Serial.println(" Reintentando en 5 segundos");      
			delay(5000);    
		}  
	}
}


String response;

String serializeSensorValueBody(int ID, int boardID, int groupID, double value, String type, long date)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  DynamicJsonDocument doc(2048);

  // Add values in the document
  //
  doc["ID"] = ID;
  doc["boardID"] = boardID;
  doc["groupID"] = groupID;
  doc["value"] = value;
  doc["type"] = type;
  doc["date"] = date;
  doc["removed"] = false;

  // Generate the minified JSON and send it to the Serial port.
  //
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

    // Deserialize the JSON document
    DeserializationError error = deserializeJson(doc, responseJson);

    // Test if parsing succeeds.
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // Fetch values.
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
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    
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
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
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
  
  describe("----Todos los sensores de la placa 1----");
  String serverPath = serverName + "api/sensors/" + String(GROUP_ID) +  "/" + String(BOARD_ID);
  http.begin(serverPath.c_str());
  // test_response(http.GET());
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
  
  String actuator_states_body = serializeActuatorStatusBody(random(1, 10), random(1, 10), random(1, 10), 10.0, "ActuadorPrueba", millis());
  describe("Test POST with actuator state");
  String serverPath = serverName + "api/actuators";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

  String sensor_value_body = serializeSensorValueBody(random(1, 10), random(1, 10), random(1, 10), 10.0, "SensorPrueba", millis());
  describe("Test POST with sensor value");
  serverPath = serverName + "api/sensors";
  http.begin(serverPath.c_str());
  test_response(http.POST(sensor_value_body));

  // String device_body = serializeDeviceBody(String(DEVICE_ID), ("Name_" + String(DEVICE_ID)).c_str(), ("mqtt_" + String(DEVICE_ID)).c_str(), 12);
  // describe("Test POST with path and body and response");
  // serverPath = serverName + "api/device";
  // http.begin(serverPath.c_str());
  // test_response(http.POST(actuator_states_body));
  
}

bool seguir = true;

// Run the tests!
void loop()
{
  if(seguir == true) {
    GET_tests();
    POST_tests();
  }
  seguir = false;

  if (!pubsubClient.connected()) {
		reconnect();
	}

	pubsubClient.loop();
	delay(5000);
	snprintf (msg, 75, "Son las %ld", millis());
	pubsubClient.publish("topic_2", msg);	

}

package es.us.lsi.dad;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class MqttClientVerticle extends AbstractVerticle {
	
	Gson gson;

	public void start(Promise<Void> startFuture) {
		gson = new Gson();
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "localhost", s -> {
			
			//En nuestro proyecto no meter estas 2 funciones dentro del connect (no meterlas para nada de hecho)
			//, simplemente poner mensaje de "Conexion correcta"
			//subscribe(<nombre_topic>,<QoS>,<handler>)
			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					System.out.println("Suscripción " + mqttClient.clientId());
				}
			});
			
			mqttClient.subscribe("topic_3", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					System.out.println("Suscripción " + mqttClient.clientId());
				}
			});

			mqttClient.publishHandler(handler -> {
				System.out.println("Mensaje recibido:");
				System.out.println("    Topic: " + handler.topicName().toString());
				System.out.println("    Id del mensaje: " + handler.messageId());
				System.out.println("    Contenido: " + handler.payload().toString());
				try {
				Sensor sen = gson.fromJson(handler.payload().toString(), Sensor.class);
				System.out.println("    Sensor: " + sen.toString());
				}catch (JsonSyntaxException e) {
					System.out.println("    No es un Sensor. ");
				}
			});
			//Solo este, en el POST
			//Ejemplo nombre topic -> "group_" + sensor.getGroupId(),
			//publish("nombre_topic", Buffer.buffer("Mensaje"), MqttQoS.AT_LEAST_ONCE, <duplicated>, <retain>);
			mqttClient.publish("topic_1", Buffer.buffer("ON"), MqttQoS.AT_LEAST_ONCE, false, false);
		});

	}

}

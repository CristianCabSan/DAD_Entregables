package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import io.vertx.core.http.ServerWebSocket;

public class RestServer extends AbstractVerticle {
    Gson gson;
    MySQLPool mySqlClient;
    MqttClient mqttClient;
    private String latestMqttMessage = "";
    private List<ServerWebSocket> webSockets = new ArrayList<>();
    int threshold = 3500;
    String topic = "group_1";

    public void start(Promise<Void> startPromise) {
        // Configure and connect the MQTT client
        mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
        
        mqttClient.connect(1883, "localhost", s -> {
            if (s.succeeded()) {
                System.out.println("Connected to MQTT broker");
                // Subscribe to the topic
                mqttClient.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value(), ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Subscribed to topic: " + topic);
                    } else {
                        System.out.println("Failed to subscribe to topic");
                    }
                });
            } else {
                System.out.println("Failed to connect to MQTT broker");
            }
        });

        // Set up the MQTT message callback
        mqttClient.publishHandler(message -> {
            // Debug lines
            // System.out.println("Received message on topic " + message.topicName());
            // System.out.println("Message payload: " + message.payload().toString());
            latestMqttMessage = message.payload().toString();
            if (latestMqttMessage.startsWith("N")) {
                String numericPart = latestMqttMessage.substring(1);
                threshold = Integer.parseInt(numericPart);
            } else if (latestMqttMessage.startsWith("G")) {
                topic = latestMqttMessage.substring(1);
                mqttClient.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value(), ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Subscribed to topic: " + topic);
                    } else {
                        System.out.println("Failed to subscribe to topic");
                    }
                });
            }
            // Broadcast the message to all connected WebSocket clients
            broadcastMessage(latestMqttMessage);
        });

        // Set up the MySQL client
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
            .setPort(3306)
            .setHost("localhost")
            .setDatabase("dad")
            .setUser("usuario")
            .setPassword("usuario");

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

        // Instantiating a Gson serialize object using specific date format
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        // Defining the router object
        Router router = Router.router(vertx);

        // WebSocket handler
        vertx.createHttpServer().webSocketHandler(ws -> {
            webSockets.add(ws);
            ws.closeHandler(v -> webSockets.remove(ws));
        }).requestHandler(router).listen(8084, result -> {
            if (result.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(result.cause());
            }
        });

        // Defining URI paths for each method in RESTful interface, including body handling
        router.route("/api/sensors*").handler(BodyHandler.create());
        router.get("/api/sensors/sensor/all").handler(this::getAllSen);
        router.get("/api/sensors/:groupid/:boardid").handler(this::getAllSenFromBoard);
        router.get("/api/sensors/:groupid/:boardid/:sensorid").handler(this::getOneSen);
        router.get("/api/sensors/:groupid/:boardid/:sensorid/:numberofvalues").handler(this::getLastNValuesFromSen);
        router.post("/api/sensors").handler(this::addOneSen);
        router.delete("/api/sensors/:groupid/:boardid/:sensorid").handler(this::deleteOneSen);
        router.put("/api/sensors/:groupid/:boardid/:sensorid").handler(this::putOneSen);

        router.route("/api/actuators*").handler(BodyHandler.create());
        router.get("/api/actuators/actuator/all").handler(this::getAllAct);
        router.get("/api/actuators/:groupid/:boardid").handler(this::getAllActFromBoard);
        router.get("/api/actuators/:groupid/:boardid/:actuatorid").handler(this::getOneAct);
        router.get("/api/actuators/:groupid/:boardid/:actuatorid/:numberofvalues").handler(this::getLastNValuesFromAct);
        router.post("/api/actuators").handler(this::addOneAct);
        router.delete("/api/actuators/:groupid/:actuatorid").handler(this::deleteOneAct);
        router.put("/api/actuators/:groupid/:boardid/:actuatorid").handler(this::putOneAct);

        router.get("/api/boards/sensors/:groupid/:boardid/:numberofvalues").handler(this::getLastNSenValuesFromBoa);
        router.get("/api/boards/actuators/:groupid/:boardid/:numberofvalues").handler(this::getLastNActValuesFromBoa);

        router.get("/api/groups/sensors/:groupid/:numberofvalues").handler(this::getLastNSenValuesFromGroup);
        router.get("/api/groups/actuators/:groupid/:numberofvalues").handler(this::getLastNActValuesFromGroup);

        router.route("/api/config*").handler(BodyHandler.create());
        router.get("/api/config").handler(this::config);
        router.get("/api/config/threshold").handler(this::getThreshold);
        router.get("/api/config/topic").handler(this::getTopic);
        router.post("/api/config/action").handler(this::boton);
    }

    private void broadcastMessage(String message) {
        for (ServerWebSocket ws : webSockets) {
            ws.writeTextMessage(message);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        try {
            stopPromise.complete();
        } catch (Exception e) {
            stopPromise.fail(e);
        }
        super.stop(stopPromise);
    }
    
    private void config(RoutingContext routingContext) {
        vertx.fileSystem().readFile("src/main/java/es/us/lsi/dad/config.html", result -> {
            if (result.succeeded()) {
                String html = result.result().toString();
                html = html.replace("{{threshold}}", String.valueOf(threshold));
                routingContext.response()
                    .putHeader("content-type", "text/html")
                    .end(html);
            } else {
                routingContext.response()
                    .setStatusCode(500)
                    .end("Failed to load configuration page");
            }
        });
    }

    private void getThreshold(RoutingContext routingContext) {
        JsonObject response = new JsonObject().put("threshold", threshold);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(response.encode());
    }
    
    private void getTopic(RoutingContext routingContext) {
        JsonObject response = new JsonObject().put("topic", topic);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(response.encode());
    }

    private void boton(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        if (json == null) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", "Invalid or missing JSON body").encode());
            return;
        }

        String action = json.getString("action");
        mqttClient.publish(topic, Buffer.buffer(action), MqttQoS.AT_LEAST_ONCE, false, false);

        routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("status", "success").encode());
    }

	//Gets all values from every value
	private void getAllSen(RoutingContext routingContext) {
		mySqlClient.query("SELECT * FROM dad.sensors;").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				List<Sensor> result = new ArrayList<>();
				for(Row elem : resultSet) {
					result.add(new Sensor(
							elem.getInteger("ID"),
							elem.getInteger("boardID"), 
							elem.getInteger("groupID"),
							elem.getInteger("value"), 
							elem.getString("type"), 
							elem.getLong("date"))
							);
				}
				routingContext.request().response().end(gson.toJson(result));
			} else {
				//System.out.println("Error" + res.cause().getLocalizedMessage());
				routingContext.request().response().setStatusCode(400).end();
			}
		});
	}
	
	//Gets all the values over time of a certain sensor (both sensorid and boardid must be specified)
	private void getOneSen(RoutingContext routingContext) {
	    int targetSensorID = Integer.parseInt(routingContext.request().getParam("sensorid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.sensors WHERE ID = ? AND boardID = ? AND groupID = ?")
	                    .execute(Tuple.of(targetSensorID, targetBoardID, targetGroupID), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Sensor> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Sensor(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"), 
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	//Gets the last(higher date) N values from a certain sensor (both sensorid and boardid must be specified)
	private void getLastNValuesFromSen(RoutingContext routingContext) {
	    int targetSensorID = Integer.parseInt(routingContext.request().getParam("sensorid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int numberOfValues = Integer.parseInt(routingContext.request().getParam("numberofvalues"));

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.sensors WHERE ID = ? AND boardID = ? AND groupID = ? ORDER BY date DESC LIMIT ?")
	                    .execute(Tuple.of(targetSensorID, targetBoardID, targetGroupID, numberOfValues), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Sensor> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Sensor(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"), 
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	//Gets all the sensor values from a certain board
	private void getAllSenFromBoard(RoutingContext routingContext) {
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.sensors WHERE boardID = ? AND groupID")
	                    .execute(Tuple.of(targetBoardID, targetGroupID), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Sensor> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Sensor(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"),
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	//Creates a new instance of sensor value
	private void addOneSen(RoutingContext routingContext) {
		final Sensor sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
		String value;
		if(sensor.getValue() >= threshold) {
			value = "ON";
		} else  {
			value = "OFF";
		}
		mqttClient.publish(topic, Buffer.buffer(value), MqttQoS.AT_LEAST_ONCE, false, false);
		mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("INSERT INTO sensors (id, boardID, groupID, value, type, date) VALUES (?, ?, ?, ?, ?, ?)")
	                    .execute(Tuple.of(sensor.getID(), sensor.getBoardID(), sensor.getGroupID(), sensor.getValue(), sensor.getType(), sensor.getDate()), res -> {
	                        if (res.succeeded()) {
	                            routingContext.response().setStatusCode(201).end("Data inserted successfully");
	                        } else {
	                            routingContext.response().setStatusCode(500).end("Failed to insert data into the database");
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.response().setStatusCode(500).end("Failed to connect to the database");
	        }
	    });
	}
	
	//Deletes a instace of sensor value
	private void deleteOneSen(RoutingContext routingContext) {
		int targetSensorID = Integer.parseInt(routingContext.request().getParam("sensorid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("DELETE FROM sensors WHERE ID = ? AND boardID = ? AND groupID = ?")
	                    .execute(Tuple.of(targetSensorID, targetBoardID, targetGroupID), res -> {
	                        if (res.succeeded()) {
	                            routingContext.response().setStatusCode(200).end("Data deleted successfully");
	                        } else {
	                            routingContext.response().setStatusCode(500).end("Failed to delete data from the database");
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.response().setStatusCode(500).end("Failed to connect to the database");
	        }
	    });
	}
	
	//Updates a instace of sensor value
	private void putOneSen(RoutingContext routingContext) {
		int targetSensorID = Integer.parseInt(routingContext.request().getParam("sensorid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    Sensor sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
	    
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("UPDATE sensors SET ID = ?, boardID = ?, groupID = ?, value = ?, type = ?, date = ? WHERE ID = ? AND boardID = ? AND groupID = ?")
	                    .execute(Tuple.of(sensor.getID(), sensor.getBoardID(), sensor.getGroupID(), sensor.getValue(), sensor.getType(), sensor.getDate(), 
	                    		targetSensorID, targetBoardID, targetGroupID), res -> {
	                        if (res.succeeded()) {
	                            routingContext.response().setStatusCode(200).end("Data updated successfully");
	                        } else {
	                            routingContext.response().setStatusCode(500).end("Failed to update data in the database");
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.response().setStatusCode(500).end("Failed to connect to the database");
	        }
	    });
	}
	
	/*--------------------------------------------------------------------------------*/
	//For reference see sensors endpoints//
	
	private void getAllAct(RoutingContext routingContext) {
		mySqlClient.query("SELECT * FROM dad.actuators;").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				List<Actuator> result = new ArrayList<>();
				for(Row elem : resultSet) {
					result.add(new Actuator(
							elem.getInteger("ID"),
							elem.getInteger("boardID"), 
							elem.getInteger("groupID"),
							elem.getInteger("value"), 
							elem.getString("type"), 
							elem.getLong("date"))
							);
				}
				routingContext.request().response().end(gson.toJson(result));
			} else {
				System.out.println("Error" + res.cause().getLocalizedMessage());
				routingContext.request().response().setStatusCode(400).end();
			}
		});
	}
	
	private void getLastNValuesFromAct(RoutingContext routingContext) {
		int targetActuatorID = Integer.parseInt(routingContext.request().getParam("actuatorid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int numberOfValues = Integer.parseInt(routingContext.request().getParam("numberofvalues"));

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.actuators WHERE ID = ? AND boardID = ? AND groupID = ? ORDER BY date DESC LIMIT ?")
	                    .execute(Tuple.of(targetActuatorID, targetBoardID, targetGroupID, numberOfValues), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Actuator> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Actuator(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"), 
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	private void getOneAct(RoutingContext routingContext) {
		int targetActuatorID = Integer.parseInt(routingContext.request().getParam("actuatorid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.actuators WHERE ID = ? AND boardID = ? AND groupID = ?")
	                    .execute(Tuple.of(targetActuatorID, targetBoardID, targetGroupID), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Actuator> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Actuator(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"),
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	private void getAllActFromBoard(RoutingContext routingContext) {
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.actuators WHERE boardID = ? AND groupID = ?")
	                    .execute(Tuple.of(targetBoardID, targetGroupID), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Actuator> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Actuator(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"), 
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	private void addOneAct(RoutingContext routingContext) {
		final Actuator actuator = gson.fromJson(routingContext.getBodyAsString(), Actuator.class);
		mqttClient.publish("group_" + actuator.getGroupID(), Buffer.buffer(actuator.getValue().toString()), MqttQoS.AT_LEAST_ONCE, false, false);
		mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("INSERT INTO actuators (id, boardID, groupID, value, type, date) VALUES (?, ?, ?, ?, ?, ?)")
	                    .execute(Tuple.of(actuator.getID(), actuator.getBoardID(), actuator.getGroupID(), actuator.getValue(), actuator.getType(), actuator.getDate()), res -> {
	                        if (res.succeeded()) {
	                            routingContext.response().setStatusCode(201).end("Data inserted successfully");
	                        } else {
	                            routingContext.response().setStatusCode(500).end("Failed to insert data into the database");
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.response().setStatusCode(500).end("Failed to connect to the database");
	        }
	    });
	}
	
	private void deleteOneAct(RoutingContext routingContext) {
		int targetActuatorID = Integer.parseInt(routingContext.request().getParam("actuatorid"));
		int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("DELETE FROM actuators WHERE ID = ? AND boardID = ? AND groupID = ?")
	                    .execute(Tuple.of(targetActuatorID, targetBoardID, targetGroupID), res -> {
	                        if (res.succeeded()) {
	                            routingContext.response().setStatusCode(200).end("Data deleted successfully");
	                        } else {
	                            routingContext.response().setStatusCode(500).end("Failed to delete data from the database");
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.response().setStatusCode(500).end("Failed to connect to the database");
	        }
	    });
	}
	
	private void putOneAct(RoutingContext routingContext) {
		int targetActuatorID = Integer.parseInt(routingContext.request().getParam("actuatorid"));
		int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    Actuator actuator = gson.fromJson(routingContext.getBodyAsString(), Actuator.class);
	    
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("UPDATE actuators SET ID = ?, boardID = ?, groupID = ?, value = ?, type = ?, date = ? WHERE ID = ? and boardID = ?")
	                    .execute(Tuple.of(actuator.getID(), actuator.getBoardID(), actuator.getGroupID(), actuator.getValue(), actuator.getType(), actuator.getDate(), 
	                    		targetActuatorID, targetBoardID, targetGroupID), res -> {
	                        if (res.succeeded()) {
	                            routingContext.response().setStatusCode(200).end("Data updated successfully");
	                        } else {
	                            routingContext.response().setStatusCode(500).end("Failed to update data in the database");
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.response().setStatusCode(500).end("Failed to connect to the database");
	        }
	    });
	}
	
	/*--------------------------------------------------------------------------------*/
	
	
	private void getLastNSenValuesFromBoa(RoutingContext routingContext) {
		int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
		int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int numberOfValues = Integer.parseInt(routingContext.request().getParam("numberofvalues"));

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.sensors WHERE boardID = ? and groupID = ? ORDER BY date DESC LIMIT ?")
	                    .execute(Tuple.of(targetBoardID, targetGroupID, numberOfValues), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Sensor> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Sensor(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"), 
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	private void getLastNActValuesFromBoa(RoutingContext routingContext) {
	    int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int numberOfValues = Integer.parseInt(routingContext.request().getParam("numberofvalues"));

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.actuators WHERE boardID = ? and groupID = ? ORDER BY date DESC LIMIT ?")
	                    .execute(Tuple.of(targetBoardID, targetGroupID, numberOfValues), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Actuator> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Actuator(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"), 
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	/*--------------------------------------------------------------------------------*/
	
	private void getLastNSenValuesFromGroup(RoutingContext routingContext) {
		int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int numberOfValues = Integer.parseInt(routingContext.request().getParam("numberofvalues"));

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.sensors WHERE groupID = ? ORDER BY date DESC LIMIT ?")
	                    .execute(Tuple.of(targetGroupID, numberOfValues), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Sensor> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Sensor(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"),
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
	
	private void getLastNActValuesFromGroup(RoutingContext routingContext) {
	    int targetGroupID = Integer.parseInt(routingContext.request().getParam("groupid"));
	    int numberOfValues = Integer.parseInt(routingContext.request().getParam("numberofvalues"));

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM dad.actuators WHERE groupID = ? ORDER BY date DESC LIMIT ?")
	                    .execute(Tuple.of(targetGroupID, numberOfValues), res -> {
	                    	if(res.succeeded()) {
	            				RowSet<Row> resultSet = res.result();
	            				List<Actuator> result = new ArrayList<>();
	            				for(Row elem : resultSet) {
	            					result.add(new Actuator(
	            							elem.getInteger("ID"),
	            							elem.getInteger("boardID"), 
	            							elem.getInteger("groupID"),
	            							elem.getInteger("value"), 
	            							elem.getString("type"), 
	            							elem.getLong("date"))
	            							);
	            				}
	            				routingContext.request().response().end(gson.toJson(result));
	            			} else {
	                            routingContext.request().response().setStatusCode(400).end();
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            routingContext.request().response().setStatusCode(400).end();
	        }
	    });
	}
}
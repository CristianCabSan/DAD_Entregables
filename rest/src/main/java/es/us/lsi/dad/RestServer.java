package es.us.lsi.dad;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle {

	private List<Sensor> sensors = new ArrayList<Sensor>();
	private List<Actuator> actuators = new ArrayList<Actuator>();
	private List<Board> boards = new ArrayList<Board>();
	private Gson gson;

	public void start(Promise<Void> startFuture) {
		// Creating some synthetic data
		createSomeData(25);
		

		// Instantiating a Gson serialize object using specific date format
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx
		.createHttpServer()
		.requestHandler(router::handle)
		.listen(8084, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*
		router.route("/api/sensors*").handler(BodyHandler.create());
		router.get("/api/sensors").handler(this::getAllWithParamsSen);
		router.get("/api/sensors/sensor/all").handler(this::getAllSen);
		router.get("/api/sensors/:sensorid").handler(this::getOneSen);
		router.post("/api/sensors").handler(this::addOneSen);
		router.delete("/api/sensors/:sensorid").handler(this::deleteOneSen);
		router.put("/api/sensors/:userid").handler(this::putOneSen);
		
		router.route("/api/actuators*").handler(BodyHandler.create());
		router.get("/api/actuators").handler(this::getAllWithParamsAct);
		router.get("/api/actuators/actuator/all").handler(this::getAllAct);
		router.get("/api/actuators/:actuatorid").handler(this::getOneAct);
		router.post("/api/actuators").handler(this::addOneAct);
		router.delete("/api/actuators/:actuatorid").handler(this::deleteOneAct);
		router.put("/api/actuators/:actuatorid").handler(this::putOneAct);
		
		router.route("/api/boards*").handler(BodyHandler.create());
		router.get("/api/boards").handler(this::getAllWithParamsBoa);
		router.get("/api/boards/board/all").handler(this::getAllBoa);
		router.get("/api/boards/:boardid").handler(this::getOneBoa);
		router.post("/api/boards").handler(this::addOneBoa);
		router.delete("/api/boards/:boardid").handler(this::deleteOneBoa);
		router.put("/api/boards/:boardid").handler(this::putOneBoa);
		
	}

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		try {
			sensors.clear();
			stopPromise.complete();
		} catch (Exception e) {
			stopPromise.fail(e);
		}
		super.stop(stopPromise);
	}

	@SuppressWarnings("unused")
	private void getAllSen(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new SensorListWrapper(sensors)));
	}
	
	private void getAllAct(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new ActuatorListWrapper(actuators)));
	}
	
	private void getAllBoa(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new BoardListWrapper(boards)));
	}

	private void getAllWithParamsSen(RoutingContext routingContext) {
		final Integer ID = routingContext.queryParams().contains("ID") ? Integer.parseInt(routingContext.queryParam("ID").get(0))
				: null;	
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new SensorListWrapper(sensors.stream().filter(elem -> {
					boolean res = true;
					res = res && (ID != null ? elem.getID().equals(ID) : true);
					return res;
				}).collect(Collectors.toList()))));
	}
	
	private void getAllWithParamsAct(RoutingContext routingContext) {
		final Integer ID = routingContext.queryParams().contains("ID") ? Integer.parseInt(routingContext.queryParam("ID").get(0))
				: null;	
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new ActuatorListWrapper(actuators.stream().filter(elem -> {
					boolean res = true;
					res = res && (ID != null ? elem.getID().equals(ID) : true);
					return res;
				}).collect(Collectors.toList()))));
	}
	
	private void getAllWithParamsBoa(RoutingContext routingContext) {
		final Integer ID = routingContext.queryParams().contains("ID") ? Integer.parseInt(routingContext.queryParam("ID").get(0))
				: null;	
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new BoardListWrapper(boards.stream().filter(elem -> {
					boolean res = true;
					res = res && (ID != null ? elem.getID().equals(ID) : true);
					return res;
				}).collect(Collectors.toList()))));
	}
	
	private void getOneSen(RoutingContext routingContext) {
		try {
			int targetSensorID = Integer.parseInt(routingContext.request().getParam("sensorid"));

			if (sensors.stream().anyMatch(sen -> sen.getID() == targetSensorID)) {
				Optional<Sensor> targetSensor = sensors.stream()
		                .filter(sen -> sen.getID() == targetSensorID)
		                .findFirst();
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end(gson.toJson(targetSensor.get()));
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end("Sensor with ID:" + targetSensorID + " doesnt exist");
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}
	
	private void getOneAct(RoutingContext routingContext) {
		try {
			int targetActuatorID = Integer.parseInt(routingContext.request().getParam("actuatorid"));

			if (actuators.stream().anyMatch(sen -> sen.getID() == targetActuatorID)) {
				Optional<Actuator> targetActuator= actuators.stream()
		                .filter(sen -> sen.getID() == targetActuatorID)
		                .findFirst();
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end(gson.toJson(targetActuator.get()));
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end("Actuator with ID:" + targetActuatorID + " doesnt exist");
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void getOneBoa(RoutingContext routingContext) {
		try {
			int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));

			if (boards.stream().anyMatch(sen -> sen.getID() == targetBoardID)) {
				Optional<Board> targetBoard = boards.stream()
		                .filter(sen -> sen.getID() == targetBoardID)
		                .findFirst();
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end(gson.toJson(targetBoard.get()));
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end("Board with ID:" + targetBoardID + " doesnt exist");
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}
	
	private void addOneSen(RoutingContext routingContext) {
		final Sensor sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
		sensors.add(sensor.getID(), sensor);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(sensor));
	}
	
	private void addOneAct(RoutingContext routingContext) {
		final Actuator actuator = gson.fromJson(routingContext.getBodyAsString(), Actuator.class);
		actuators.add(actuator.getID(), actuator);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(actuator));
	}

	private void addOneBoa(RoutingContext routingContext) {
		final Board board = gson.fromJson(routingContext.getBodyAsString(), Board.class);
		boards.add(board.getID(), board);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(board));
	}
	
	private void deleteOneSen(RoutingContext routingContext) {
		int targetSensorID = Integer.parseInt(routingContext.request().getParam("sensorid"));
		if (sensors.stream().anyMatch(sen -> sen.getID() == targetSensorID)) {
			Optional<Sensor> targetSensor = sensors.stream()
	                .filter(Sensor -> Sensor.getID() == targetSensorID)
	                .findFirst();
			if (targetSensor.isPresent()) {
		        sensors.removeIf(sen -> sen.getID() == targetSensorID);
		        routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
		                .end(gson.toJson(targetSensor.get()));
			}
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end("Sensor with ID:" + targetSensorID + " doesnt exist");
		}
	}
	
	private void deleteOneAct(RoutingContext routingContext) {
		int targetActuatorID = Integer.parseInt(routingContext.request().getParam("actuatorid"));
		if (actuators.stream().anyMatch(sen -> sen.getID() == targetActuatorID)) {
			Optional<Actuator> targetActuator = actuators.stream()
	                .filter(Actuator -> Actuator.getID() == targetActuatorID)
	                .findFirst();
			if (targetActuator.isPresent()) {
		        actuators.removeIf(sen -> sen.getID() == targetActuatorID);
		        routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
		                .end(gson.toJson(targetActuator.get()));
			}
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end("Actuator with ID:" + targetActuatorID + " doesnt exist");
		}
	}
	
	private void deleteOneBoa(RoutingContext routingContext) {
		int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
		if (boards.stream().anyMatch(sen -> sen.getID() == targetBoardID)) {
			Optional<Board> targetBoard = boards.stream()
	                .filter(Board -> Board.getID() == targetBoardID)
	                .findFirst();
			if (targetBoard.isPresent()) {
		        boards.removeIf(sen -> sen.getID() == targetBoardID);
		        routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
		                .end(gson.toJson(targetBoard.get()));
			}
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end("Board with ID:" + targetBoardID + " doesnt exist");
		}
	}

	private void putOneSen(RoutingContext routingContext) {
		int targetSensorID = Integer.parseInt(routingContext.request().getParam("sensorid"));
		Optional<Sensor> ts = sensors.stream()
                .filter(Sensor -> Sensor.getID() == targetSensorID)
                .findFirst();
		final Sensor element = gson.fromJson(routingContext.getBodyAsString(), Sensor.class); //Sensor a añadir
		if (ts.isPresent()){
			Sensor targetSensor = ts.get();
			
			targetSensor.setID(element.getID());
			targetSensor.setBoardID(element.getBoardID());
			targetSensor.setValue(element.getValue());
			targetSensor.setType(element.getType());
			targetSensor.setDate(element.getDate());
			
		}
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
		.end(gson.toJson(element));
	}
	
	private void putOneAct(RoutingContext routingContext) {
		int targetActuatorID = Integer.parseInt(routingContext.request().getParam("actuatorid"));
		Optional<Actuator> ts = actuators.stream()
                .filter(Actuator -> Actuator.getID() == targetActuatorID)
                .findFirst();
		final Actuator element = gson.fromJson(routingContext.getBodyAsString(), Actuator.class); //Actuator a añadir
		if (ts.isPresent()){
			Actuator targetActuator = ts.get();
			
			targetActuator.setID(element.getID());
			targetActuator.setBoardID(element.getBoardID());
			targetActuator.setActive(element.getActive());
			targetActuator.setType(element.getType());
			targetActuator.setDate(element.getDate());
			
		}
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
		.end(gson.toJson(element));
	}
	
	private void putOneBoa(RoutingContext routingContext) {
		int targetBoardID = Integer.parseInt(routingContext.request().getParam("boardid"));
		Optional<Board> ts = boards.stream()
                .filter(Board -> Board.getID() == targetBoardID)
                .findFirst();
		final Board element = gson.fromJson(routingContext.getBodyAsString(), Board.class); //Board a añadir
		if (ts.isPresent()){
			Board targetBoard = ts.get();
			
			targetBoard.setID(element.getID());
			targetBoard.setAssignedSensors(element.getAssignedSensors());
			targetBoard.setAssignedActuators(element.getAssignedActuators());
			targetBoard.setDate(element.getDate());		
		}
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
		.end(gson.toJson(element));
	}
	
	private void createSomeData(int number) {
		Random rnd = new Random();
		sensors.add(new Sensor(1, 1, 0., "Type_" + 1, new Timestamp(System.currentTimeMillis()+1)));
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			sensors.add(new Sensor(id, 1, 0., "Type_" + id, new Timestamp(System.currentTimeMillis()+id)));
		});
		
		actuators.add(new Actuator(1, 1, true, "Type_" + 1, new Timestamp(System.currentTimeMillis()+1)));
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			actuators.add(new Actuator(id, 1, true, "Type_" + id, new Timestamp(System.currentTimeMillis()+id)));
		});
		
		List<Integer> example = new ArrayList<Integer>();
		example.add(1);example.add(2);example.add(3);
		boards.add(new Board(1, example, example, new Timestamp(System.currentTimeMillis()+1)));
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			boards.add(new Board(id, example, example, new Timestamp(System.currentTimeMillis()+id)));
		});
	}

}
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
		router.get("/api/sensors").handler(this::getAllWithParams);
		router.get("/api/sensors/sensor/all").handler(this::getAll);
		router.get("/api/sensors/:sensorid").handler(this::getOne);
		//router.post("/api/users").handler(this::addOne);
		//router.delete("/api/users/:userid").handler(this::deleteOne);
		//router.put("/api/users/:userid").handler(this::putOne);
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
	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new SensorListWrapper(sensors)));
	}

	private void getAllWithParams(RoutingContext routingContext) {
		final Integer ID = routingContext.queryParams().contains("ID") ? Integer.parseInt(routingContext.queryParam("ID").get(0))
				: null;	
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new SensorListWrapper(sensors.stream().filter(elem -> {
					boolean res = true;
					res = res && (ID != null ? elem.getID().equals(ID) : true);
					return res;
				}).collect(Collectors.toList()))));
	}
	
	private void getOne(RoutingContext routingContext) {
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
						.setStatusCode(204).end();
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	/*
	private void addOne(RoutingContext routingContext) {
		final UserEntity user = gson.fromJson(routingContext.getBodyAsString(), UserEntity.class);
		users.put(user.getIdusers(), user);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(user));
	}

	private void deleteOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("userid"));
		if (users.containsKey(id)) {
			UserEntity user = users.get(id);
			users.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(user));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}

	private void putOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("userid"));
		UserEntity ds = users.get(id);
		final UserEntity element = gson.fromJson(routingContext.getBodyAsString(), UserEntity.class);
		ds.setName(element.getName());
		ds.setSurname(element.getSurname());
		ds.setBirthdate(element.getBirthdate());
		ds.setPassword(element.getPassword());
		ds.setUsername(element.getUsername());
		users.put(ds.getIdusers(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
	*/
	
	private void createSomeData(int number) {
		Random rnd = new Random();
		sensors.add(new Sensor(1, 1, 0., "Type_" + 1, new Timestamp(System.currentTimeMillis()+1)));
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			sensors.add(new Sensor(id, 1, 0., "Type_" + id, new Timestamp(System.currentTimeMillis()+id)));
		});
	}

}
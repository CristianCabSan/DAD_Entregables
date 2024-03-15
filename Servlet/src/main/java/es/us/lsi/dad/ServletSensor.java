package es.us.lsi.dad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletSensor extends HttpServlet {
	private static final long serialVersionUID = -6201150158950823811L;

	private List<Sensor> Sensors;

	public void init() throws ServletException {
		Sensors = new ArrayList<>();
		Sensor prueba = new Sensor();
		prueba.setID(1);
		prueba.setBoardID(1);
		prueba.setType("motor");
		prueba.setValue(30.);
		Sensors.add(prueba);
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer targetSensorID = Integer.parseInt(req.getParameter("ID"));
		Gson gson = new Gson();
		if (Sensors.stream().anyMatch(sen -> sen.getID() == targetSensorID)) {
			Optional<Sensor> targetSensor = Sensors.stream()
	                .filter(Sensor -> Sensor.getID() == targetSensorID)
	                .findFirst();
			resp.getWriter().println(gson.toJson(targetSensor));
		} else {
			response(resp, "Sensor with ID:" + targetSensorID + " doesnt exist");
		}
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		Sensor newSensor = gson.fromJson(reader, Sensor.class);
		if (!Sensors.stream().anyMatch(sen -> sen.getID() == newSensor.getID())) {
			Sensors.add(newSensor);
			resp.getWriter().println(gson.toJson(newSensor));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "ID:" + newSensor.getID() + " is already assigned");
		}
	}
	
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		Sensor targetSensor = gson.fromJson(reader, Sensor.class);
		if (Sensors.stream().anyMatch(Sensor -> Sensor.getID() == targetSensor.getID())) {
			Sensors.removeIf(sen -> sen.getID() == targetSensor.getID());
			resp.getWriter().println(gson.toJson(targetSensor));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "Sensor with ID:" + targetSensor.getID() + " doesnt exist");
		}
	   
	}

	private void response(HttpServletResponse resp, String msg) throws IOException {
		PrintWriter out = resp.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<t1>" + msg + "</t1>");
		out.println("</body>");
		out.println("</html>");
	}
	
}
package es.us.lsi.dad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletSensor extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6201150158950823811L;

	private List<Integer> Sensors;

	public void init() throws ServletException {
		Sensors = new ArrayList<>();
		Sensors.add(1);
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer ID = Integer.parseInt(req.getParameter("ID"));
		if (Sensors.contains(ID)) {
			response(resp, "Sensor with ID:" + ID + " exists");
		} else {
			response(resp, "Sensor with ID:" + ID + " doesnt exist");
		}
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		Sensor sensor = gson.fromJson(reader, Sensor.class);
		if (!Sensors.contains(sensor.getID())) {
			Sensors.add(sensor.getID());
			resp.getWriter().println(gson.toJson(sensor));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "ID is already assigned");
		}
	}
	
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		Sensor sensor = gson.fromJson(reader, Sensor.class);
		if (Sensors.contains(sensor.getID())) {
			Sensors.remove(sensor.getID());
			resp.getWriter().println(gson.toJson(sensor));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "Sensor with ID:" + sensor.getID() + " doesnt exist");
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
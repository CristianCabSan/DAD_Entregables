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

public class ServletActuator extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6201150158950823811L;

	private List<Actuator> Actuators;

	public void init() throws ServletException {
		Actuators = new ArrayList<>();
		Actuator prueba = new Actuator();
		prueba.setID(1);
		prueba.setBoardID(1);
		prueba.setName("prueba");
		prueba.setType("motor");
		prueba.setActive(false);
		Actuators.add(prueba);
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer ID = Integer.parseInt(req.getParameter("ID"));
		Gson gson = new Gson();
		if (Actuators.stream().anyMatch(act -> act.getID() == ID)) {
			Optional<Actuator> foundActuator = Actuators.stream()
	                .filter(Actuator -> Actuator.getID() == ID)
	                .findFirst();
			resp.getWriter().println(gson.toJson(foundActuator));
		} else {
			response(resp, "Actuator with ID:" + ID + " doesnt exist");
		}
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		Actuator newActuator = gson.fromJson(reader, Actuator.class);
		if (!Actuators.stream().anyMatch(act -> act.getID() == newActuator.getID())) {
			Actuators.add(newActuator);
			resp.getWriter().println(gson.toJson(newActuator));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "ID:" + newActuator.getID() + " is already assigned");
		}
	}
	
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		Actuator targetActuator = gson.fromJson(reader, Actuator.class);
		if (Actuators.stream().anyMatch(Actuator -> Actuator.getID() == targetActuator.getID())) {
			Actuators.removeIf(act -> act.getID() == targetActuator.getID());
			resp.getWriter().println(gson.toJson(targetActuator));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "Actuator with ID:" + targetActuator.getID() + " doesnt exist");
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
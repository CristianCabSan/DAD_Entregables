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

public class ServletActuator extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6201150158950823811L;

	private List<Integer> Actuators;

	public void init() throws ServletException {
		Actuators = new ArrayList<>();
		Actuators.add(1);
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer ID = Integer.parseInt(req.getParameter("ID"));
		if (Actuators.contains(ID)) {
			response(resp, "Actuator with ID:" + ID + " exists");
		} else {
			response(resp, "Actuator with ID:" + ID + " doesnt exist");
		}
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		Actuator actuator = gson.fromJson(reader, Actuator.class);
		if (!Actuators.contains(actuator.getID())) {
			Actuators.add(actuator.getID());
			resp.getWriter().println(gson.toJson(actuator));
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
		Actuator actuator = gson.fromJson(reader, Actuator.class);
		if (Actuators.contains(actuator.getID())) {
			Actuators.remove(actuator.getID());
			resp.getWriter().println(gson.toJson(actuator));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "Actuator with ID:" + actuator.getID() + " doesnt exist");
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
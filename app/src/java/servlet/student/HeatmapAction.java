/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.student;

import controller.HeatmapController;
import dao.AppUsageDAO;
import dao.LocationDAO;
import dao.LocationUsageDAO;
import entity.AppUsage;
import entity.Location;
import entity.LocationUsage;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jeremyongts92
 */
@WebServlet(name = "HeatmapAction", urlPatterns = {"/HeatmapAction"})
public class HeatmapAction extends HttpServlet {

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			LocationUsageDAO luDAO = new LocationUsageDAO();

			String dateStr = request.getParameter("date");
			String timeStr = request.getParameter("time");
			out.println(dateStr + ",");
			out.println(timeStr);
			String floor = request.getParameter("floor");
			request.setAttribute("date", dateStr);
			request.setAttribute("time", timeStr);
			request.setAttribute("floor", floor);
			
			
			
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date datetime = dateFormat.parse(dateStr + " " + timeStr, new ParsePosition(0));
			if (datetime == null){
				request.setAttribute("error", "You have entered an invalid date!");
				RequestDispatcher rd = request.getRequestDispatcher("student/heatmap.jsp");
				rd.forward(request, response);
				return;
			}
				
			HeatmapController ctrl = new HeatmapController();
			HashMap<String, ArrayList<LocationUsage>> result = ctrl.generateHeatmap(datetime, floor);
			
//			return HashMap<location,userlist>
			request.setAttribute("heatmap", result);
			RequestDispatcher rd = request.getRequestDispatcher("student/heatmap.jsp");
			rd.forward(request, response);
			

		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

}

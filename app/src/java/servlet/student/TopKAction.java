/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.student;

import controller.*;
import dao.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "TopKAction", urlPatterns = {"/TopKAction"})
public class TopKAction extends HttpServlet {

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
		//This is the choice selection of which of the 3 option the user wants to be processed.
		String selection = request.getParameter("category");
		//Gets the start and end dates as necessary.
		String startDate = request.getParameter("startdate");
		String endDate = request.getParameter("enddate");
		TopkReportController ctrl = new TopkReportController();
		//This Error means NOTHING ELSE is printed
		String errors = "";
		//This error means that data is still printed
		ArrayList<String> warnList = new ArrayList<String>();
                
                if(selection == null){
                    RequestDispatcher rd = request.getRequestDispatcher("top-kreport.jsp");
                    rd.forward(request, response);
                }
                
                //Gets the number of (top) K that the individual wants displayed
		String entry = request.getParameter("entries");
                int topK = -1;
		if(entry == null){
                    entry = "3";
		}else if(entry.length() == 0){
                    errors += ", blank k";
                }else{
                    //Checks for K
                    topK = Utility.parseInt(entry);
                    if (topK > 10 || topK < 1) {
                        errors += ", invalid k";
                    }
                }
                //START DATE VALIDATION
                Date dateFormattedStart = null;
                if (startDate == null) {
                        errors += ", missing startdate";
                } else if (startDate.length() == 0) {
                        errors += ", blank startdate";
                } else {
                    if (startDate.length() != 10) {
                        errors += ", invalid startdate";
                    } else {
                        startDate = Utility.parseString(startDate + " 00:00:00");
                        if (startDate == null || !Utility.checkDate(startDate)) {
                            errors += ", invalid startdate";
                        }else{
                            dateFormattedStart = Utility.parseDate(startDate);
                        }
                    }
                }

                //END DATE VALIDATION
                Date dateFormattedEnd = null;
                if (endDate == null) {
                        errors += ", missing enddate";
                } else if (endDate.length() == 0) {
                        errors += ", blank enddate";
                } else {
                    if (endDate.length() != 10) {
                            errors += ", invalid enddate";
                    } else {
                        endDate = Utility.parseString(endDate + " 23:59:59");
                        if (endDate == null || !Utility.checkDate(endDate)) {
                            errors += ", invalid enddate";
                        }else{
                            dateFormattedEnd = Utility.parseDate(endDate);
                        }
                    }
                }

		//Finally, makes sure the start date if after to add error, as if it is before or similar, no error
		if (dateFormattedStart != null && dateFormattedEnd != null && dateFormattedStart.after(dateFormattedEnd)) {
			errors += ", Your start date should be before your end date!";
		}

		//All the values are from the same select place. It only changes based on the report selected
		String selected = request.getParameter("choice");
		//Checks school/appcategory (Actually this is chosen)
                if (selection.equals("schoolapps")) {
                    if(selected == null){
                        errors += ", missing school";
                    }else if(selected.length() == 0){
                        errors += ", blank school";
                    }else if (!Utility.checkSchools(selected)) {
                        errors += ", invalid school";
                    }
                } else {
                    if(selected == null){
                        errors += ", missing app category";
                    }else if(selected.length() == 0){
                        errors += ", blank app category";
                    }else if (!Utility.checkCategory(selected)) {
                        errors += ", invalid app category";
                    }
                }

		//Delcares the values to return. Declares both in case of 
		ArrayList<HashMap<String, String>> catValues = null;

		//If all checks are passed:
		if (errors.length() == 0) {
			//The switch case divides the chosen choice into the three categories as necessary
			switch (selection) {
				case "schoolapps":
					//This parameter is only for the school function
					catValues = ctrl.getTopkApp(topK, selected, dateFormattedStart, dateFormattedEnd, warnList);
					break;
				case "appstudents":
					//This parameter is only for those who select App Category and return Students
					catValues = ctrl.getTopkStudents(topK, selected, dateFormattedStart, dateFormattedEnd, warnList);
					break;
				default:
					//This parameter is only for those who select App Category and return School
					catValues = ctrl.getTopkSchool(topK, selected, dateFormattedStart, dateFormattedEnd, warnList);
					break;
			}
		} else {
			//Need to substring for multiple errors
			errors = errors.substring(2);
		}
                
		request.setAttribute("catvalues", catValues);
		request.setAttribute("choice", selected);
		request.setAttribute("error", errors);
                if(warnList != null && warnList.size() != 0){
                    request.setAttribute("errors", warnList.get(0));
                }
		request.setAttribute("entries", entry);
		RequestDispatcher rd = null;
		//Divides back into where the request came from.
                switch (selection) {
                        case "schoolapps":
                                //This parameter is only for the school function
                                rd = request.getRequestDispatcher("top-kreport.jsp");
                                break;
                        case "appstudents":
                                //This parameter is only for those who select App Category and return Students
                                rd = request.getRequestDispatcher("top-kstudent.jsp");
                                break;
                        default:
                                //This parameter is only for those who select App Category and return School
                                rd = request.getRequestDispatcher("top-kschool.jsp");
                                break;
                }
		rd.forward(request, response);
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

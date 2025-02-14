/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import dao.ConnectionManager;
import dao.LocationDAO;
import dao.LocationUsageDAO;
import dao.UserDAO;
import dao.Utility;
import entity.Location;
import entity.LocationUsage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Shuwen
 */

/**
 * DeleteLocationController controls all actions relating to DeleteLocation
 * functionality
 */
public class DeleteController {
  
     /**
     * Deletes records from LocationUsage given the parameters
     *
     * @param macAdd The unique id of a user
     * @param startDate The starting date of interest
     * @param endDate The ending date of interest
     * @param startTime The starting time of interest
     * @param endTime The ending time of interest
     * @param locId The unique id of a location
     * @param semanticPl The name of a location
     * @param error The list that stores all errors generated by this request
     * @throws SQLException if there is an SQL error
     * @return an arraylist of LocationUsage
     */
    public ArrayList<LocationUsage> delete(String macAdd, String startDate, String endDate, String startTime, String endTime, String locId, String semanticPl, ArrayList<String> error) throws SQLException {
        ArrayList<LocationUsage> deleted = null;
        Connection conn = ConnectionManager.getConnection();
        conn.setAutoCommit(false);
        String errors = "";
    
        LocationUsageDAO luDao = new LocationUsageDAO();
        
        //Starts the checking here
        //START DATE VALIDATION
        Date dateFormattedStart = null;      
        if (startDate == null) {
            errors += ", missing startdate";
        } else if (startDate.length() == 0){
            errors += ", blank startdate";
        } else {
            if(startDate.length() != 10){
                errors += ", invalid startdate";
            }else{
                if(startTime != null && startTime.length() != 0){
                    startDate += " " + startTime + ":00";
                }else{
                    startDate += " 00:00:00";
                }
                if(!Utility.checkOnlyDate(startDate)){
                    errors += ", invalid startdate";
                } else if (startDate.length() != 19 || !Utility.checkDate(startDate)) { // if they are of the wrong length
                    errors += ", invalid starttime";
                }else{
                    dateFormattedStart = Utility.parseDate(startDate);
                }
            }
        }
        
        //END DATE VALIDATION
        Date dateFormattedEnd = null;      
        if (endDate != null) {
            if (endDate.length() == 0){
                errors += ", blank enddate";
            } else {
                if(endDate.length() != 10){
                    errors += ", invalid enddate";
                }else{
                    if(endTime != null && endTime.length() != 0){
                        endDate += " " + endTime + ":00";
                    }else{
                        endDate += " 00:00:00";
                    }
                    if(!Utility.checkOnlyDate(endDate)){
                        errors += ", invalid enddate";
                    } else if ((endTime != null && endTime.length() != 0) && endDate.length() != 19 || !Utility.checkDate(endDate)) { // if they are of the wrong length
                        errors += ", invalid endtime";
                    }else{
                        dateFormattedEnd = Utility.parseDate(endDate);
                    }
                }
            } 
        }
        
        if(dateFormattedStart != null && dateFormattedEnd != null && dateFormattedStart.after(dateFormattedEnd)){
            errors += ", invalid starttime";
        }

        //MACADDRESS VALIDATION - This one COULD be input as the login person is admin, and therefore not retrieve the user's own macadd like activeness
        if(macAdd != null && macAdd.length() != 0){
            if (!Utility.checkHexadecimal(macAdd)) {
                errors += ", invalid mac address";
                
            //Retrieves the Userlist to check the macAdd
            }else{
                UserDAO userDao = new UserDAO();
                if(!userDao.checkMacAdd(conn, macAdd)){
                    errors += ", invalid mac address";
                }                
            }
        }
        
        //Location id validation
        String place = null;
        int locationId = Utility.parseInt(locId);
        if(locId != null && locId.length() != 0){
            if(locationId < 0){
                errors += ", invalid location-id";
                
                //Here, have to call for locationIdList
            } else{
                LocationDAO lDao = new LocationDAO();
                place = lDao.checkLocationId(conn, locationId);
                if (place == null) {
                    errors += ", invalid location-id";       
                }
            }
        }
        
        //SEMANTIC PLACE VALIDATION
        if(semanticPl != null && semanticPl.length() != 0){
            LocationDAO lDao = new LocationDAO(); 
            Location semPl = lDao.retrieveSemPl(semanticPl);
            if(semPl == null) {
                errors += ", invalid semantic-place"; 
            }

        } 
        
        if(errors.length() == 0){
     
            
            deleted = luDao.delete(conn, macAdd, startDate, endDate, locationId, semanticPl);
           
        }else{
            error.add(errors.substring(2));
          
        }
        return deleted;
    }

}   
    



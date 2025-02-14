/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import com.csvreader.CsvReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import entity.*;
import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 * @author ASUS-PC
 */
/**
 * LocationUsageDAO handles interactions between LocationUsage and Controllers
 */
public class LocationUsageDAO {

    private TreeMap<String, LocationUsage> locationList;
    private TreeMap<String, Integer> duplicate;

    /* Loads the list of Location list and Duplicate list
     */
    public LocationUsageDAO() {
        duplicate = new TreeMap<>();
        locationList = new TreeMap<>();
    }

    /**
     * Inserts rows into LocationUsage in the database
     *
     * @param reader The CSV reader used to read the csv file
     * @param errMap The map that will contain errors messages
     * @param conn The connection to the database
     * @param locationIdList The list of location id that is successfully
     * uploaded to the database
     * @throws IOException An error found
     * @return an array of int, any number above 0 is the row is success
     * updated, otherwise not successfully updated.
     */
    public int insert(CsvReader reader, TreeMap<Integer, String> errMap, Connection conn, HashMap<Integer, String> locationIdList) throws IOException {
        try {
            int index = 2;

            String sql = "insert into locationusage values(STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,?) ON DUPLICATE KEY UPDATE locationid = "
                    + "VALUES(locationid);";
            PreparedStatement stmt = conn.prepareStatement(sql);
            reader.readHeaders();
            String[] headers = reader.getHeaders();

            while (reader.readRecord()) {
                //retrieving per row
                String errorMsg = "";

                //Declare Values
                int locationId = -1;
                String date = null;
                String macAdd = null;

                for (String s : headers) {
                    switch (s) {
                        case "timestamp":
                            //check timestamp
                            date = Utility.parseString(reader.get("timestamp"));
                            if (date == null) {
                                errorMsg += ",blank timestamp";

                            } else {
                                if (!Utility.checkDate(date)) {
                                    errorMsg += ",invalid timestamp";
                                }
                            }
                            break;

                        case "mac-address":
                            //check macAdd
                            macAdd = Utility.parseString(reader.get("mac-address"));
                            if (macAdd == null) {
                                errorMsg += ",blank mac-address";

                            } else if (!Utility.checkHexadecimal(macAdd)) {
                                errorMsg += ",invalid mac address";

                            } else {
                                macAdd = macAdd.toLowerCase();
                            }
                            break;

                        case "location-id":
                            //check locid
                            String locId = Utility.parseString(reader.get("location-id"));
                            if (locId == null) {
                                errorMsg += ",blank location-id";
                            } else {
                                locationId = Utility.parseInt(locId);
                                if (locationId <= 0) {
                                    errorMsg += ",invalid location id";
                                } else if (!locationIdList.containsKey(locationId)) {
                                    errorMsg += ",invalid location";
                                }
                            }
                            break;
                    }
                }

                if (errorMsg.length() == 0) {
                    String key = date + macAdd;
                    Integer exisMac = duplicate.get(key);
                    if (exisMac != null) {
                        errMap.put(exisMac, "duplicate row");
                    }
                    duplicate.put(key, index);
                    //add to list
                    stmt.setString(1, date);
                    stmt.setString(2, macAdd);
                    stmt.setInt(3, locationId);
                    stmt.addBatch();
                } else {

                    errMap.put(index, errorMsg.substring(1));
                }
                index++;
//			if (index % 10000 == 0){
//				stmt.executeBatch();
//			}

            }
            //insert into tables

            stmt.executeBatch();
            conn.commit();
            stmt.close();
        } catch (SQLException e) {

        }
        return duplicate.size();
    }

    /**
     * Add rows into LocationUsage in the database
     *
     * @param reader The CSV reader used to read the csv file
     * @param errMap The map that will contain errors messages
     * @param conn The connection to the database
     * @throws IOException An error found
     * @return number of rows updated
     */
    public int add(CsvReader reader, TreeMap<Integer, String> errMap, Connection conn) throws IOException {
        int updateCounts = 0;
        try {
            int index = 2;
            String sql = "insert into locationusage (timestamp, macaddress, locationid) values(STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,?);";
            PreparedStatement stmt = conn.prepareStatement(sql);
            reader.readHeaders();
            String[] headers = reader.getHeaders();
            while (reader.readRecord()) {
                //retrieving per row
                String errorMsg = "";

                //Declare Values
                int locationId = -1;
                String date = null;
                String macAdd = null;

                for (String s : headers) {
                    switch (s) {
                        case "timestamp":
                            //check timestamp
                            date = Utility.parseString(reader.get("timestamp"));
                            if (date == null) {
                                errorMsg += ",blank timestamp";
                            } else {
                                if (!Utility.checkDate(date)) {
                                    errorMsg += ",invalid timestamp";
                                }
                            }
                            break;

                        case "mac-address":
                            //check macAdd
                            macAdd = Utility.parseString(reader.get("mac-address"));
                            if (macAdd == null) {
                                errorMsg += ",blank mac-address";

                            } else if (!Utility.checkHexadecimal(macAdd)) {
                                errorMsg += ",invalid mac address";

                            } else {
                                macAdd = macAdd.toLowerCase();
                            }
                            break;

                        case "location-id":
                            //check locid
                            String locId = Utility.parseString(reader.get("location-id"));
                            if (locId == null) {
                                errorMsg += ",blank location-id";
                            } else {
                                locationId = Utility.parseInt(locId);
                                if (locationId <= 0) {
                                    errorMsg += ",invalid location id";
                                    //IF LOCATION ID NOT BLANK
                                } else {
                                    String query = "select locationid from location where locationid = ?;";
                                    PreparedStatement pStmt = conn.prepareStatement(query);
                                    pStmt.setInt(1, locationId);
                                    ResultSet rs = pStmt.executeQuery();
                                    if (!rs.next()) {
                                        errorMsg += ",invalid location";

                                    }
                                    pStmt.close();
                                }
                            }
                            break;
                    }
                }

                //IF ALL VALIDATIONS ARE PASSED
                if (errorMsg.length() == 0) {
                    if (duplicate.containsKey(date + macAdd)) {
                        errMap.put(duplicate.get(date + macAdd), "duplicate row");

                    }
                    duplicate.put(date + macAdd, index);
                    locationList.put(date + macAdd, new LocationUsage(date, macAdd, locationId));
                } else {
                    errMap.put(index, errorMsg.substring(1));
                }

                //row number increased
                index++;

            }

            //CHECK FOR DUPLICATES IN DATABASE
            ArrayList<LocationUsage> locList = new ArrayList<LocationUsage>(locationList.values());

            try {
                for (LocationUsage loc : locList) {
                    stmt.setString(1, loc.getTimestamp());
                    stmt.setString(2, loc.getMacAddress());
                    stmt.setInt(3, loc.getLocationId());
                    stmt.addBatch();
                }
                int[] updatedArr = stmt.executeBatch();
                for (int i : updatedArr) {
                    updateCounts += i;
                }
                //CATCH WHEN THERE IS DUPLICATE
            } catch (BatchUpdateException e) {
                int[] updatedArr = e.getUpdateCounts();
                for (int i = 0; i < updatedArr.length; i++) {
                    if (updatedArr[i] == Statement.EXECUTE_FAILED) {
                        // This method retrieves the row fail, and then searches the locationid corresponding and then uses the duplicate TreeMap to find the offending row.
                        int row = duplicate.get(locList.get(i).getTimestamp() + locList.get(i).getMacAddress());
                        String errorMsg = "";
                        if (errMap.containsKey(index)) {
                            errorMsg = errMap.get(index);
                        }
                        if (errorMsg != null && errorMsg.length() != 0) {
                            errorMsg += ",duplicate row";
                        } else {
                            errorMsg += "duplicate row";
                        }
                        errMap.put(row, errorMsg);
                    }
                    if (updatedArr[i] >= 0) {
                        updateCounts += updatedArr[i];
                    }
                }
            }
            conn.commit();
            reader.close();
            stmt.close();
        } catch (SQLException e) {

        }
        return updateCounts;
    }

    /**
     * Delete rows in LocationUsage
     *
     * @param reader The CSV reader used to read the csv file
     * @param conn The connection to the database
     * @throws IOException An error found
     * @return array list of rows deleted. For each row, anything above 0 is the
     * row is success updated, otherwise not successfully updated.
     */
    public int[] delete(CsvReader reader, Connection conn) throws IOException {
        int[] toReturn = new int[2];
        int notFound = 0;
        int found = 0;
        int[] updateCounts = {};
        try {
            String sql = "delete from locationusage where timestamp = STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') and macaddress = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            reader.readHeaders();
            while (reader.readRecord()) {
                //retrieving per row
                //check timestamp
                String date = Utility.parseString(reader.get("timestamp"));
                String macAdd = Utility.parseString(reader.get("mac-address"));
                if (date != null && Utility.checkDate(date) && macAdd != null && Utility.checkHexadecimal(macAdd)) {
                    stmt.setString(1, date);
                    stmt.setString(2, macAdd);
                    stmt.addBatch();
                }
                if (stmt != null) {
                    updateCounts = stmt.executeBatch();
                    conn.commit();
                    for (int i : updateCounts) {
                        if (!(i > 0)) { //Can be 0 or anything else
                            notFound++;
                        } else {
                            found += i;
                        }
                    }
                }
            }
            conn.commit();
            reader.close();
            ConnectionManager.close(conn, stmt);

        } catch (NullPointerException e) {

        } catch (SQLException e) {

        }
        toReturn[0] = found; //Valid Records which have successfully deleted rows in the database
        toReturn[1] = notFound; //Valid Records which are succesful but have not deleted rows in the database
        return toReturn;
    }

    /**
     * Delete rows in LocationUsage
     *
     * @param conn The connection to the database
     * @param macAdd The macAdd of a user
     * @param startDate The start date of interest
     * @param endDate The end date of interest
     * @param locationId The unique id of a location
     * @param semanticPlace The corresponding place of the locationId
     * @throws SQLException An SQL Exception found
     * @return array list LocationUsage
     */
    public ArrayList<LocationUsage> delete(Connection conn, String macAdd, String startDate, String endDate, int locationId, String semanticPlace) throws SQLException {
        ArrayList<LocationUsage> lList = new ArrayList<LocationUsage>();
        int stringCount = 2;
        try {
            String sql = "SELECT lu.locationid, macaddress, lu.timestamp, semanticplace FROM locationusage lu, location l WHERE lu.locationid = l.locationid"
                    + " AND timestamp >= STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')";

            if (endDate != null) {
                sql += " AND timestamp < STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')";
            }
            if (locationId > 0) {
                sql += " AND lu.locationid = ?";
            } else if (semanticPlace != null) {
                sql += " AND semanticplace = ?";
            }
            if (macAdd != null) {
                sql += " AND macaddress = ?";
            }
            sql += " ;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, startDate);
            if (endDate != null) {
                stmt.setString(stringCount, endDate);
                stringCount++;
            }
            if (locationId > 0) {
                stmt.setInt(stringCount, locationId);
                stringCount++;
            } else if (semanticPlace != null) {
                stmt.setString(stringCount, semanticPlace);
                stringCount++;
            }
            if (macAdd != null) {
                stmt.setString(stringCount, macAdd);
            }
            stringCount = 2;
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int locId = rs.getInt(1);
                String macAddress = rs.getString(2);
                String timestamp = Utility.formatDate(rs.getTimestamp(3));
                String semPlace = rs.getString(4);
                lList.add(new LocationUsage(timestamp, macAddress, new Location(locId, semPlace)));
            }
            rs.close();
            stmt.close();
            sql = "DELETE FROM locationusage WHERE timestamp >= STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')";

            if (endDate != null) {
                sql += " AND timestamp < STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')";
            }
            if (locationId > 0) {
                sql += " AND locationid = ?";
            } else if (semanticPlace != null) {
                sql += " AND locationid IN (SELECT locationid FROM location WHERE semanticplace = ?)";
            }
            if (macAdd != null) {
                sql += " AND macaddress = ?";
            }
            sql += " ;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, startDate);
            if (endDate != null) {
                ps.setString(stringCount, endDate);
                stringCount++;
            }
            if (locationId > 0) {
                ps.setInt(stringCount, locationId);
                stringCount++;
            } else if (semanticPlace != null) {
                ps.setString(stringCount, semanticPlace);
                stringCount++;
            }
            if (macAdd != null) {
                ps.setString(stringCount, macAdd);
            }
            ps.addBatch();
            ps.executeBatch();
            conn.commit();

            ConnectionManager.close(conn, stmt);

        } catch (NullPointerException e) {

        } catch (SQLException e) {
        }
        return lList;
    }

    /**
     * Retrieve LocationUsage given the date and location
     *
     * @param date The date of interest
     * @param loc The location
     * @return an arraylist of LocationUsage
     */
    public ArrayList<LocationUsage> retrieve(java.util.Date date, String loc) {
        ArrayList<LocationUsage> result = new ArrayList<LocationUsage>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT timestamp, macaddress, lu.locationid \n"
                    + "FROM (\n"
                    + "SELECT MAX(TIMESTAMP) as timestamp, macaddress, locationid FROM locationusage\n"
                    + "WHERE timestamp >= STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') \n"
                    + "AND timestamp < STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') \n"
                    + "group by macaddress\n"
                    + ") as lu,\n"
                    + "location l\n"
                    + "WHERE \n"
                    + "lu.locationid = l.locationid\n"
                    + "AND semanticplace = ? \n"
                    + ";";

            conn = ConnectionManager.getConnection();
            ps = conn.prepareStatement(sql);

            Date before = new java.sql.Date(date.getTime() - 900000);
            Date after = new java.sql.Date(date.getTime());
            ps.setString(1, Utility.formatDate(before)); //15 minutes before
            ps.setString(2, Utility.formatDate(after));
            ps.setString(3, loc);

            rs = ps.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp(1);
                String macAddress = rs.getString(2);
                String locationId = rs.getString(3);

                LocationUsage curr = new LocationUsage(Utility.formatDate(new Date(timestamp.getTime())), macAddress, Integer.parseInt(locationId));
                result.add(curr);

            }
            ConnectionManager.close(conn, ps, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, ps, rs);
        }

        return result;
    }

    /**
     * Retrieve LocationUsage given the date and floor for each user
     *
     * @param date The date of interest
     * @param floor The floor number of location
     * @return an HashMap of macAddress and its corresponding LocationUsage
     */
    public HashMap<String, LocationUsage> retrieveByFloor(java.util.Date date, String floor) {
        HashMap<String, LocationUsage> result = new HashMap<String, LocationUsage>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "select * \n"
                    + "    from appusage au, locationusage lu,location l \n"
                    + "    where au.macaddress = lu.macaddress\n"
                    + "    and lu.locationid = l.locationid \n"
                    + "    and semanticplace like ?\n"
                    + "    AND lu.timestamp >= ? and lu.timestamp < ?\n"
                    + "    and au.timestamp >= ? and au.timestamp < ?\n"
                    + "    group by lu.macaddress \n"
                    + "    order by lu.macaddress, lu.timestamp;";

            conn = ConnectionManager.getConnection();
            ps = conn.prepareStatement(sql);

            Date before = new java.sql.Date(date.getTime() - (15 * 60 * 1000));
            Date after = new java.sql.Date(date.getTime());
            ps.setString(1, "SMUSIS" + floor + "%");
            ps.setString(2, Utility.formatDate(before)); //15 minutes before
            ps.setString(3, Utility.formatDate(after));
            ps.setString(4, Utility.formatDate(before)); //15 minutes before
            ps.setString(5, Utility.formatDate(after));

            rs = ps.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp(4);
                String macAddress = rs.getString(5);
                int locationId = rs.getInt(6);
                String semanticplace = rs.getString(8);
                LocationUsage curr = new LocationUsage(Utility.formatDate(new Date(timestamp.getTime())), macAddress, new Location(locationId, semanticplace));
                result.put(macAddress, curr);

            }
            ConnectionManager.close(conn, ps, rs);
        } catch (SQLException e) {
        } finally {
            ConnectionManager.close(conn, ps, rs);
        }

        return result;
    }

    /**
     * Retrieve LocationUsage given the date and floor of a specific user
     *
     * @param macAdd The mac address of a specific user
     * @param startDate The start date of interest
     * @param endDate The start date of interest
     * @return an HashMap of LocationUsage
     */
    public ArrayList<LocationUsage> retrieveByUser(String macAdd, java.util.Date startDate, java.util.Date endDate) {

        ArrayList<LocationUsage> result = new ArrayList<LocationUsage>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = " select * from locationusage\n"
                    + " WHERE macaddress = ? \n"
                    + " AND timestamp >= ? AND timestamp <= ? \n"
                    + " ORDER BY timestamp;";

            conn = ConnectionManager.getConnection();
            ps = conn.prepareStatement(sql);

            ps.setString(1, macAdd);
            ps.setString(2, new java.sql.Timestamp(startDate.getTime()).toString());
            ps.setString(3, new java.sql.Timestamp(endDate.getTime()).toString());

            rs = ps.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp(1);
                String macAddress = rs.getString(2);
                String locationId = rs.getString(3);

                LocationUsage curr = new LocationUsage(Utility.formatDate(new Date(timestamp.getTime())), macAddress, Integer.parseInt(locationId));
                result.add(curr);

            }
            ConnectionManager.close(conn, ps, rs);
        } catch (SQLException e) {
        } finally {
            ConnectionManager.close(conn, ps, rs);
        }

        return result;

    }

    /**
     * Create LocationUsage object
     *
     * @param startInLocation The starting location id
     * @param endInLocation The ending location id
     * @param prevLocationId The previous location id
     * @param macaddress The mac address of a specific user
     * @param totalAUList The list of AppUsage
     */
    public void retrieve(java.util.Date startInLocation, java.util.Date endInLocation, int prevLocationId, String macaddress, ArrayList<LocationUsage> totalAUList) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "select * from locationusage\n"
                    + " WHERE timestamp >= ? AND timestamp < ? \n"
                    + " AND locationid = ? \n"
                    + " AND macaddress != ? \n"
                    + " ORDER BY timestamp\n"
                    + " ;";

            conn = ConnectionManager.getConnection();
            ps = conn.prepareStatement(sql);

            ps.setString(1, new java.sql.Timestamp(startInLocation.getTime()).toString());
            ps.setString(2, new java.sql.Timestamp(endInLocation.getTime()).toString());
            ps.setInt(3, prevLocationId);
            ps.setString(4, macaddress);

            rs = ps.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp(1);
                String macAddress = rs.getString(2);
                String locationId = rs.getString(3);

                LocationUsage curr = new LocationUsage(Utility.formatDate(new Date(timestamp.getTime())), macAddress, Integer.parseInt(locationId));
                totalAUList.add(curr);

            }
            ConnectionManager.close(conn, ps, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, ps, rs);
        }

    }

    /* Retrieve LocationUsage given the date and macAdd of a user
     * @param date The date of interest
     * @param macAdd The macAdd of the user
     * @return an HashMap of LocationUsage
     */
    public ArrayList<LocationUsage> retrieveUserLocationUsage(String date, String macAdd) {
        ArrayList<LocationUsage> locList = new ArrayList<>();
        // This method gets a Single user's locationusage
        try {
            //Note the query is already tailored to just check or date
            String sql = "SELECT timestamp, semanticplace, l.locationid \n"
                    + "FROM location l, locationusage lu \n"
                    + "WHERE date(timestamp) = ?\n"
                    + "AND l.locationid = lu.locationid\n"
                    + "AND macaddress = ?\n"
                    + "ORDER BY timestamp;";

            Connection conn = ConnectionManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, date);
            ps.setString(2, macAdd);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString(1);
                String place = rs.getString(2);
                int locId = rs.getInt(3);

                //Location id is NOT impt when you have the place already.
                locList.add(new LocationUsage(timestamp, macAdd, new Location(locId, place)));
            }
            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locList;
    }

    /* Retrieve LocationUsage of all users except the user of the macAddress in the input
     * @param date The date of interest
     * @param macAdd The macAdd of the user
     * @return an HashMap of LocationUsage
     */
    public ArrayList<LocationUsage> retrievePeopleExceptUserLocationUsage(String date, String macAddress) {
        //This is to get EVERYONE's location usage
        ArrayList<LocationUsage> locList = new ArrayList<>();
        try {
            //Note the query is already tailored to just check or date
            String sql = "SELECT timestamp, semanticplace, macaddress, l.locationid \n"
                    + "FROM location l, locationusage lu \n"
                    + "WHERE date(timestamp) = ?\n"
                    + "AND macaddress != ?\n"
                    + "AND l.locationid = lu.locationid\n"
                    + "ORDER BY macaddress, timestamp\n";

            Connection conn = ConnectionManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, date);
            //Ensures that the User's macadd doesnt return
            ps.setString(2, macAddress);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString(1);
                String place = rs.getString(2);
                String macAdd = rs.getString(3);
                int locId = rs.getInt(4);

                //Location id is NOT impt when you have the place already.
                locList.add(new LocationUsage(timestamp, macAdd, new Location(locId, place)));
            }
            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locList;
    }
}

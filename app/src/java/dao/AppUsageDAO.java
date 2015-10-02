package dao;

import entity.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.*;
import com.opencsv.CSVReader;
import java.sql.ResultSet;

public class AppUsageDAO {

	private ArrayList<String> unsuccessful = new ArrayList<>();

	public AppUsageDAO() {
	}

	public void insert(AppDAO aDao, UserDAO uDao, CSVReader reader) throws IOException, SQLException {
		try {
			Connection conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			String sql = "insert into appusage (timestamp, macaddress, appid) values(STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,?);";
			PreparedStatement stmt = conn.prepareStatement(sql);

			String[] arr = null;
			while ((arr = reader.readNext()) != null) {
				//retrieving per row
				boolean err = false;

				//check timestamp
				java.util.Date format = Utility.parseDate(arr[0]);

				if (format == null) {
					err = true;
					unsuccessful.add("invalid timestamp");
				}
				String date = Utility.formatDate(format);

				//check macAdd
				String macAdd = Utility.parseString(arr[1]);
				if (macAdd == null) {
					unsuccessful.add("mac add cannot be blank");
					err = true;
				}
				if (!Utility.checkHexadecimal(macAdd)) {
					unsuccessful.add("invalid mac address");
					err = true;
				}

				if (!uDao.hasMacAdd(macAdd)) {
					unsuccessful.add("no matching mac address");
					err = true;
				}

				//check appid
				int appId = Utility.parseInt(arr[2]);
				if (appId <= 0) {
					unsuccessful.add("app id cannot be blank");
					err = true;
				}

				if (aDao.hasAppId(appId)) {
					unsuccessful.add("invalid app");
					err = true;
				}

				if (!err) {
					//add to list
					stmt.setString(1, date);
					stmt.setString(2, macAdd);
					stmt.setInt(3, appId);
					stmt.addBatch();
					//insert into tables
				}
			}
			//closing
			if (stmt != null) {
				stmt.executeBatch();
				conn.commit();
			}
			reader.close();
			ConnectionManager.close(conn, stmt);
		} catch (NullPointerException e) {
		}
	}

	public ArrayList<AppUsage> retrieve(java.util.Date date, String floor) {
		try {
			Connection conn = ConnectionManager.getConnection();
			PreparedStatement ps = conn.prepareStatement(
					"SELECT lu.timestamp, lu.macaddress, lu.locationid FROM locationUsage lu, location l"
							+ "WHERE lu.locationid = l.locationid"
							+ "AND timestamp < ?"
							+ "AND semanticplace LIKE '?'");
			
			ps.setDate(1, new java.sql.Date(date.getTime()));
			ps.setString(2, "%" +  floor + "%");
			
			ResultSet rs = ps.executeQuery();
			
			ArrayList<AppUsage> result = new ArrayList<AppUsage>();
			while (rs.next()){
				String timestamp = rs.getString(1);
				String macAddress = rs.getString(2);
				String locationId = rs.getString(3);
				System.out.println(timestamp + macAddress + locationId);
				
			}
			
		} catch (SQLException e) {

		}

		return null;
	}

}

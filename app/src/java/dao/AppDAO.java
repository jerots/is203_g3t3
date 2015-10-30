package dao;

import com.csvreader.CsvReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import entity.App;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.TreeMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ASUS-PC
 */
public class AppDAO {

	public int[] insert(CsvReader reader, TreeMap<Integer, String> errMap, Connection conn, HashMap<Integer,String> appIdList) throws IOException, SQLException {
		String sql = "insert into app values(?,?,?) ON DUPLICATE KEY UPDATE appname = appname, appcategory = appcategory;";
		PreparedStatement stmt = conn.prepareStatement(sql);
                //Reads the headers to decide where to go!
                reader.readHeaders();
		//index starts at 2 because the headers count as a row.
		int index = 2;
		while (reader.readRecord()) {
			boolean err = false;

			int appId = Utility.parseInt(reader.get("app-id"));
			if (appId <= 0) {

				String errorMsg = errMap.get(index);
				if (errorMsg == null) {
					errMap.put(index, "invalid app id");
				} else {
					errMap.put(index, errorMsg + "," + "invalid app id");
				}

				err = true;
			}

			String name = Utility.parseString(reader.get("app-name"));
			name = name.replace("\"", "");
			if (name == null) {

				String errorMsg = errMap.get(index);
				if (errorMsg == null) {
					errMap.put(index, "name cannot be blank");
				} else {
					errMap.put(index, errorMsg + "," + "name cannot be blank");
				}
				err = true;
			}

			String cat = Utility.parseString(reader.get("app-category"));
			cat = cat.replace("\"", "");

			if (cat == null) {

				String errorMsg = errMap.get(index);
				if (errorMsg == null) {
					errMap.put(index, "category cannot be blank");
				} else {
					errMap.put(index, errorMsg + "," + "category cannot be blank");
				}

				err = true;

			}

			if (!Utility.checkCategory(cat)) {

				String errorMsg = errMap.get(index);
				if (errorMsg == null) {
					errMap.put(index, "invalid category");
				} else {
					errMap.put(index, errorMsg + "," + "invalid category");
				}
				err = true;
			}

			if (!err) {
				//insert into tables
				appIdList.put(appId,"");
				stmt.setInt(1, appId);
				stmt.setString(2, name);
				stmt.setString(3, cat);
				stmt.addBatch();
			}
			index++;
		}
		//closing

		int[] updatedRecords = stmt.executeBatch();
		conn.commit();
                stmt.close();
		return updatedRecords;
	}

	public App retrieveAppbyId(int appId) {

		String sql = "SELECT * FROM app WHERE appid = ? ";
		try {
			Connection conn = ConnectionManager.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setInt(1, appId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				int appid = rs.getInt(1);
				String appname = rs.getString(2);
				String category = rs.getString(3);


				return new App(appid, appname, category);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public TreeMap<String, ArrayList<Integer>> retrieveByCategory() {

		TreeMap<String, ArrayList<Integer>> result = new TreeMap<String, ArrayList<Integer>>();

		try {
			Connection conn = ConnectionManager.getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT appid, appcategory from app");

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {

				int appid = rs.getInt(1);
				String appcategory = rs.getString(2);
				if (result.containsKey(appcategory)) {
					ArrayList<Integer> value = result.get(appcategory);
					value.add(appid);
					result.put(appcategory, value);
				} else {
					ArrayList<Integer> value = new ArrayList<Integer>();
					value.add(appid);
					result.put(appcategory, value);
				}
			}

		} catch (SQLException e) {

		}

		return result;
	}
}

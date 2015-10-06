package dao;

import entity.App;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import com.opencsv.CSVReader;
import java.util.HashMap;

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

    private HashMap<Integer,App> appList;
    private ArrayList<String> unsuccessful = new ArrayList<>();

    public AppDAO() {
        appList = new HashMap<>();
    }

    public void insert(CSVReader reader) throws IOException, SQLException {
        try {
            Connection conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            String sql = "insert into app values(?,?,?) ON DUPLICATE KEY UPDATE appname = VALUES(appname), appcategory = VALUES(appcategory);";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String[] arr = null;
            while ((arr = reader.readNext()) != null) {
                boolean err = false;

                int appId = Utility.parseInt(arr[0]);
                if (appId <= 0) {
                    unsuccessful.add("invalid app id");
                    err = true;
                }

                String name = Utility.parseString(arr[1]);
                name = name.replace("\"","");
                if (name == null) {
                    unsuccessful.add("Name cannot be blank.");
                    err = true;
                }
                
                String cat = Utility.parseString(arr[2]);
                cat = cat.replace("\"","");

                if (cat == null) {
                    unsuccessful.add("category cannot be blank.");
                    err = true;

                }

                if (!Utility.checkCategory(cat)) {
                    unsuccessful.add("invalid category.");
                    err = true;
                }

                if (!err) {
                    App app = new App(appId, name, cat);
                    appList.put(appId, app);
                    //insert into tables
                    stmt.setInt(1, appId);
                    stmt.setString(2, name);
                    stmt.setString(3, cat);
                    stmt.addBatch();
                }
            }
            //closing
            if (stmt != null) {
                stmt.executeBatch();
                conn.commit();
            }
            reader.close();
            ConnectionManager.close(conn,stmt);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean hasAppId(int aId) {
        return appList.containsKey(aId);
    }
    
    public void add(CSVReader reader) throws IOException, SQLException {
        try {
            Connection conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            int counter = 0;
            String sql = "insert into app values(?,?,?) ON DUPLICATE KEY UPDATE appname = appname, appcategory = appcategory;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String[] arr = null;
            while ((arr = reader.readNext()) != null) {
                boolean err = false;

                int appId = Utility.parseInt(arr[0]);
                if (appId <= 0) {
                    unsuccessful.add("invalid app id");
                    err = true;
                }

                String name = Utility.parseString(arr[1]);
                name = name.replace("\"","");
                if (name == null) {
                    unsuccessful.add("Name cannot be blank.");
                    err = true;
                }
                
                String cat = Utility.parseString(arr[2]);
                cat = cat.replace("\"","");

                if (cat == null) {
                    unsuccessful.add("category cannot be blank.");
                    err = true;

                }

                if (!Utility.checkCategory(cat)) {
                    unsuccessful.add("invalid category.");
                    err = true;
                }

                if (!err) {
                    counter++;
                    App app = new App(appId, name, cat);
                    appList.put(appId, app);
                    //insert into tables
                }
            }
            for(App app: appList.values()){
                stmt.setInt(1, app.getAppId());
                stmt.setString(2, app.getAppName());
                stmt.setString(3, app.getAppCategory());
                stmt.addBatch();
            }
            //closing
            if (stmt != null) {
                stmt.executeBatch();
                conn.commit();
            }
            reader.close();
            ConnectionManager.close(conn,stmt);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}

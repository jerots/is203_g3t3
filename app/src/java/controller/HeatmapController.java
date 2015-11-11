package controller;

import dao.LocationDAO;
import dao.LocationUsageDAO;
import entity.Location;
import entity.LocationUsage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class HeatmapController {

	public TreeMap<String, ArrayList<LocationUsage>> generateHeatmap(Date datetime, String floor) {
		
		LocationUsageDAO luDAO = new LocationUsageDAO();

		//for each location, count unique users
		TreeMap<String, ArrayList<LocationUsage>> result = new TreeMap<String, ArrayList<LocationUsage>>();

		LocationDAO locDAO = new LocationDAO();

		//retrieve all the floors
		ArrayList<String> floorLocationList = locDAO.retrieve(floor);
		HashMap<String,LocationUsage> luMap = luDAO.retrieveByFloor(datetime, floor);
		//get all locationUsage on this floor, latest timing
		//for each location in the floor
		for (int i = 0; i < floorLocationList.size(); i++) {
			String loc = floorLocationList.get(i);

			//instantiate arraylist
			ArrayList<LocationUsage> locList = new ArrayList<LocationUsage>();
			result.put(loc, locList);
			
			Iterator<String> keyIter = luMap.keySet().iterator();
			while (keyIter.hasNext()){
				String macAddress = keyIter.next();
				LocationUsage lu = luMap.get(macAddress);
				Location location = lu.getLocation();
				
				if (loc.equals(location.getSemanticPlace())) {
					locList.add(lu);
				}
			}
		}

		return result;
	}

}

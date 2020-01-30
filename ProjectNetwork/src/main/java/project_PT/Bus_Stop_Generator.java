package project_PT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.matsim.api.core.v01.Coord;

public class Bus_Stop_Generator {
	public static void main(String[] args) throws IOException {
		
		Map<String,Bus_Stop> bus_stops=new HashMap<>();
		
		String fileName="datalarge/bus_stop_dhaka.geojson";
		InputStream is = new FileInputStream(fileName);
	    String jsonTxt = IOUtils.toString(is, "UTF-8");
	    
	    JSONObject json = new JSONObject(jsonTxt);    
	    
	    JSONArray featureArray=json.getJSONArray("features");
	    
	    FileWriter fw= new FileWriter(new File("data/osm_stops.csv"));
	    fw.append("id,name,name_bn,name_en\n");
	    
	    for(int i=0;i<featureArray.length();i++) {
	    	JSONObject feature = featureArray.getJSONObject(i);
	    	JSONObject properties = feature.getJSONObject("properties");
	    	
	    	String id=properties.getString("@id");
	    	String type=id.split("/")[0];
	    	
	    	if(type.equals("node")) {
	    		id=id.split("/")[1];
	    		String name = !properties.isNull("name")?properties.getString("name"):null;
	    		String name_bn = !properties.isNull("name_bn")?properties.getString("name_bn"):null;
	        	String name_en = !properties.isNull("name_en")?properties.getString("name_en"):null;
	        	JSONObject geometry=feature.getJSONObject("geometry");
	        	JSONArray c=geometry.getJSONArray("coordinates");
	        	Coord coord=new Coord(c.getDouble(0),c.getDouble(1));
	        	Bus_Stop stop=new Bus_Stop(id,coord);
	        	stop.setName(name);
	        	stop.setName_en(name_en);
	        	stop.setName_bn(name_bn);
	        	bus_stops.put(id, stop);
	        	fw.append(id+","+name+","+name_bn+","+name_en+"\n");
	        	fw.flush();
	    	}
	    }
	    fw.close();
		System.out.println("Stop map created...");
	}
}

class Bus_Stop{
	private final Coord coord;
	private final String id;
	private String name;
	private String name_bn;
	private String name_en;
	
	public Bus_Stop(String id, Coord coord) {
		this.id=id;
		this.coord=coord;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName_bn() {
		return name_bn;
	}

	public void setName_bn(String name_bn) {
		this.name_bn = name_bn;
	}

	public String getName_en() {
		return name_en;
	}

	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	public Coord getCoord() {
		return coord;
	}
	
	public boolean matchName(String name) {
		if(this.name==null && this.name_bn==null && this.name_en==null) {
			return false;
		}
		if(name.equalsIgnoreCase(this.name)||name.equalsIgnoreCase(this.name_bn)||name.equalsIgnoreCase(this.name_en)) {
			return false;
		}
		return false;
	}
	
}
package DhakaProject.ProjectNetwork;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

public class NetworkGenerator {
public static void main(String[] args) throws IOException, ParseException {
		
	String fileName="data/NetworkDhaka_1.geojson";
	//BufferedReader bf=new BufferedReader(new FileReader(new File(fileName)));
	Network network = NetworkUtils.createNetwork();
	
	InputStream is = new FileInputStream(fileName);
    String jsonTxt = IOUtils.toString(is, "UTF-8");
    
    JSONObject json = new JSONObject(jsonTxt);       
    String crs=json.getJSONObject("crs").getJSONObject("properties").getString("name");
    System.out.println(crs);
    JSONArray featureArray=json.getJSONArray("features");
    for(int i=0;i<featureArray.length();i++) {
    	JSONObject feature = featureArray.getJSONObject(i);
    	JSONObject fp = feature.getJSONObject("properties");
    	
    	String highway = !fp.isNull("highway")?fp.getString("highway"):null;
    	String osmId = !fp.isNull("_id")?fp.getString("_id"):null;
    	String surface = !fp.isNull("surface")?fp.getString("surface"):null;
    	String name_bn = !fp.isNull("name_bn")?fp.getString("name_bn"):null;
    	String name_en = !fp.isNull("name_en")?fp.getString("name_en"):null;
    	Boolean isOneWay =!fp.isNull("oneway")?(fp.getString("oneway").equals("yes")?true:false):false;
    	Boolean access = !fp.isNull("access")?fp.getString("access").equals("yes")?true:false:true;
    	Double maxSpeed = !fp.isNull("maxspeed")?fp.getDouble("maxspeed"):60;//in km/hr
    	String ref = !fp.isNull("ref")?fp.getString("ref"):null;
    	String sideWalk = !fp.isNull("sidewalk")?fp.getString("sidewalk"):null;
    	Integer lanes = !fp.isNull("lanes")?fp.getInt("lanes"):2;
    	Double maxSpeedForward = !fp.isNull("maxspeed_forward")?fp.getDouble("maxspeed_forward"):60;
    	Double maxSpeedBackWard =  !fp.isNull("maxspeed_backward")?fp.getDouble("maxspeed_backward"):60;
    	Integer lanesForward =  !fp.isNull("lanes_forward")?fp.getInt("lanes_forward"):2;
    	Integer lanesBackward =  !fp.isNull("lanes_backward")?fp.getInt("lanes_backward"):2;
    	Boolean rickshaw =  !fp.isNull("rickshaw")?fp.getString("rickshaw").equals("yes")?true:false:false;
    	
    	
    	coordDetails cd=parseCoordinateArray(feature.getJSONObject("geometry").getJSONArray("coordinates"));
    	
    	System.out.println();
    }
}



private static coordDetails parseCoordinateArray(JSONArray coordinateArray) {
	Coord[] coords=new Coord[coordinateArray.length()];
	for(int i=0;i<coordinateArray.length();i++) {
		JSONArray c=coordinateArray.getJSONArray(i);
		Coord coord = new Coord(c.getDouble(0),c.getDouble(1));
		coords[i]=coord;
	}
	return new coordDetails(coords);
}

}

class coordDetails{
	private final Coord[] coords;
	private final double length;
	private final String firstCoordId;
	private final String lastCoordId;
	
	public coordDetails(Coord[] coords) {
		this.coords=coords;
		this.length=this.calcLength();
		this.firstCoordId=Integer.toString((int)coords[0].getX())+"_"+Integer.toString((int)coords[0].getY());
		this.lastCoordId=Integer.toString((int)coords[coords.length-1].getX())+"_"+Integer.toString((int)coords[coords.length-1].getY());
	}
	
	private double calcLength() {
		double length=0;
		for(int i=1;i<this.coords.length;i++) {
			length+=Math.sqrt(Math.pow(this.coords[i-1].getX()-this.coords[i].getX(), 2)+Math.pow(this.coords[i-1].getY()-this.coords[i].getY(), 2));
		}
		return length;
	}

	public Coord[] getCoords() {
		return coords;
	}

	public double getLength() {
		return length;
	}

	public String getFirstCoordId() {
		return firstCoordId;
	}

	public String getLastCoordId() {
		return lastCoordId;
	}
	
	
}

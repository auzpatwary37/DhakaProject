package DhakaProject.ProjectNetwork;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;

public class NetworkGenerator {
public static void main(String[] args) throws IOException, ParseException {
		
	String fileName="data/NetworkDhaka_1.geojson";
	//BufferedReader bf=new BufferedReader(new FileReader(new File(fileName)));
	Network network = NetworkUtils.createNetwork();
	NetworkFactory netfac=network.getFactory();
	InputStream is = new FileInputStream(fileName);
    String jsonTxt = IOUtils.toString(is, "UTF-8");
    
    JSONObject json = new JSONObject(jsonTxt);       
    String crs=json.getJSONObject("crs").getJSONObject("properties").getString("name");
    System.out.println(crs);
    JSONArray featureArray=json.getJSONArray("features");
    
    //modes
    Set<String> modes=new HashSet<>();
    modes.add("car");
    modes.add("bus");
    
    
    for(int i=0;i<featureArray.length();i++) {
    	JSONObject feature = featureArray.getJSONObject(i);
    	JSONObject fp = feature.getJSONObject("properties");
    	
    	Double length = fp.getDouble("Length");
    	String highway = !fp.isNull("highway")?fp.getString("highway"):null;
    	String osmId = !fp.isNull("_id")?fp.getString("_id"):null;
    	String surface = !fp.isNull("surface")?fp.getString("surface"):null;
    	String name_bn = !fp.isNull("name_bn")?fp.getString("name_bn"):null;
    	String name_en = !fp.isNull("name_en")?fp.getString("name_en"):null;
    	Boolean isOneWay =!fp.isNull("oneway")?(fp.getString("oneway").equals("yes")?true:false):true;
    	Boolean access = !fp.isNull("access")?fp.getString("access").equals("yes")?true:false:true;
    	Double maxSpeed = !fp.isNull("maxspeed")?fp.getDouble("maxspeed"):60;//in km/hr
    	String ref = !fp.isNull("ref")?fp.getString("ref"):null;
    	String sideWalk = !fp.isNull("sidewalk")?fp.getString("sidewalk"):null;
    	Integer lanes = !fp.isNull("lanes")?fp.getInt("lanes"):2;
    	Double maxSpeedForward = !fp.isNull("maxspeed_forward")?fp.getDouble("maxspeed_forward"):(maxSpeed!=null?maxSpeed:60);
    	Double maxSpeedBackWard =  !fp.isNull("maxspeed_backward")?fp.getDouble("maxspeed_backward"):(maxSpeed!=null?maxSpeed:60);
    	Integer lanesForward =  !fp.isNull("lanes_forward")?fp.getInt("lanes_forward"):lanes;
    	Integer lanesBackward =  !fp.isNull("lanes_backward")?fp.getInt("lanes_backward"):lanes;
    	Boolean rickshaw =  !fp.isNull("rickshaw")?fp.getString("rickshaw").equals("yes")?true:false:false;
    	
    	coordDetails cd=parseCoordinateArray(feature.getJSONObject("geometry").getJSONArray("coordinates"));
    	
    	//create the nodes and links 
    	Node fromNode = netfac.createNode(Id.createNodeId(cd.getFirstCoordId()), cd.getCoords()[0]);
    	Node toNode = netfac.createNode(Id.createNodeId(cd.getLastCoordId()), cd.getCoords()[cd.getCoords().length-1]);
    	
    	if(!network.getNodes().containsKey(fromNode.getId())) {
    		network.addNode(fromNode);
    	}
    	
    	if(!network.getNodes().containsKey(toNode.getId())) {
    		network.addNode(toNode);
    	}
    	//links convention is  nodeId___nodeId
    	if(isOneWay) {//if one way just add one link 
    		Link link = netfac.createLink(Id.createLinkId(fromNode.getId().toString()+"___"+toNode.getId()), fromNode, toNode);
    		//add the link attributes
    		link.setCapacity(1800);
    		link.setAllowedModes(modes);
    		link.setFreespeed(maxSpeed*1000/3600);
    		link.setLength(length);
    		link.setNumberOfLanes(lanes);
    		
    		// set the attributes keys are all lower case matching the osm
    		link.getAttributes().putAttribute("highway", highway);
    		link.getAttributes().putAttribute("osmid", osmId);
    		link.getAttributes().putAttribute("surface", surface);
    		link.getAttributes().putAttribute("name_bn", name_bn);
    		link.getAttributes().putAttribute("name_en", name_en);
    		link.getAttributes().putAttribute("access", access);
    		link.getAttributes().putAttribute("ref", ref);
    		link.getAttributes().putAttribute("sidewalk", sideWalk);
    		link.getAttributes().putAttribute("rickshaw", rickshaw);
    		
    		removeNullAttribute(link);
    		
    		if(!network.getLinks().containsKey(link.getId())) {
    			network.addLink(link);
    		}else {
    			System.out.println("Debug duplicate link!!!");
    		}
    		
    	}else {//if both way, add two links 
    		Link link1 = netfac.createLink(Id.createLinkId(fromNode.getId().toString()+"___"+toNode.getId()), fromNode, toNode);
    		
    		link1.setCapacity(1800);
    		link1.setAllowedModes(modes);
    		link1.setFreespeed(maxSpeedForward*1000/3600);
    		link1.setLength(length);
    		link1.setNumberOfLanes(lanesForward);
    		
    		// set the attributes keys are all lower case matching the osm
    		link1.getAttributes().putAttribute("highway", highway);
    		link1.getAttributes().putAttribute("osmid", osmId);
    		link1.getAttributes().putAttribute("surface", surface);
    		link1.getAttributes().putAttribute("name_bn", name_bn);
    		link1.getAttributes().putAttribute("name_en", name_en);
    		link1.getAttributes().putAttribute("access", access);
    		link1.getAttributes().putAttribute("ref", ref);
    		link1.getAttributes().putAttribute("sidewalk", sideWalk);
    		link1.getAttributes().putAttribute("rickshaw", rickshaw);
    		
    		removeNullAttribute(link1);
    		network.addLink(link1);
    		
    		Link link2 = netfac.createLink(Id.createLinkId(toNode.getId()+"___"+fromNode.getId().toString()), toNode, fromNode);
    		
    		link2.setCapacity(1800);
    		link2.setAllowedModes(modes);
    		link2.setFreespeed(maxSpeedBackWard*1000/3600);
    		link2.setLength(length);
    		link2.setNumberOfLanes(lanesBackward);
    		
    		// set the attributes keys are all lower case matching the osm
    		link2.getAttributes().putAttribute("highway", highway);
    		link2.getAttributes().putAttribute("osmid", osmId);
    		link2.getAttributes().putAttribute("surface", surface);
    		link2.getAttributes().putAttribute("name_bn", name_bn);
    		link2.getAttributes().putAttribute("name_en", name_en);
    		link2.getAttributes().putAttribute("access", access);
    		link2.getAttributes().putAttribute("ref", ref);
    		link2.getAttributes().putAttribute("sidewalk", sideWalk);
    		link2.getAttributes().putAttribute("rickshaw", rickshaw);
    		
    		removeNullAttribute(link2);
    		
    		network.addLink(link2);
    	}
    	
    }
    new NetworkWriter(network).writeV2("data/DhakMajorRoadNetwork_unclean.xml");
    new NetworkCleaner().run(network);
    
    new NetworkWriter(network).writeV2("data/DhakMajorRoadNetwork.xml");
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

private static void removeNullAttribute(Link link) {
	String[] attList = new String[] {"surface", "name_bn", "name_en", "ref", "sidewalk"};
	for(String s:attList) {
		if(link.getAttributes().getAttribute(s)==null) {
			link.getAttributes().removeAttribute(s);
		}
	}
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

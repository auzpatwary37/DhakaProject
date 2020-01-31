package project_PT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.utils.geometry.CoordUtils;

import com.google.common.collect.Sets;

public class StopToLinkMap {
	public static void main(String[] args) throws IOException {
		String fileName="data/Bus_Stops_31stJan20.csv";
		BufferedReader bf = new BufferedReader(new FileReader(new File(fileName)));
		bf.readLine();//get rid of the header
		
		Map<String,Coord> stops=new HashMap<>();
		String line=null;
		while((line=bf.readLine())!=null) {
			String[] part=line.split(",");
			String name = part[0].trim();
			double x = Double.parseDouble(part[1].trim());
			double y = Double.parseDouble(part[2].trim());
			
			if(!stops.containsKey(name)) {
				stops.put(name, new Coord(x,y));
			}else {
				name =name+"1";
				stops.put(name, new Coord(x,y));
				System.out.println("Non unique stop name for stop: "+name+". Please check!!!");
			}
			
		}
		
		//Read in the network
		Network denseNet=NetworkUtils.createNetwork();
		new NetworkReaderMatsimV2(denseNet).readFile("data/ptNet_Dhaka_Mod.xml");
		
		FileWriter fw = new FileWriter(new File("data/stopToLinkConnection.csv"));
		fw.append("Name,x,y,matched_linkid\n");//append header
		
		for(Entry<String, Coord> e:stops.entrySet()) {
			Id<Link> linkId=getNearestLeftEntryLink(denseNet, e.getValue()).getId();
			
			fw.append(e.getKey()+","+e.getValue().getX()+","+e.getValue().getY()+","+linkId.toString()+"\n");
			fw.flush();
		}
		
		fw.close();
	}
	
	public static List<Id<Link>> getNearestLinksExactlyByMath(Network net, Coord cord, double maxDistance, Set<String> AllowedModes){
		return getNearestLinksExactlyByMath(net, cord, maxDistance, AllowedModes, 300, Collections.emptySet());
	}
	
	/**
	 * Gets the links closest to a Coord<br>
	 * Does not modify net to find links, and is safe for use in threads<br>
	 * Finds the distance from Coord to Link, and then check if once coord shifted by d lies on the link<br>
	 * Gets Links to/from nearest node regardless of search distance after found links within search distance<br>
	 * Max Distance increments currently set at 20<p>
	 * Math: Rotates link by 90deg, denote as vector v<br>
	 * vector r, from cord to link from node<br>
	 * vector d, projection r onto v<br>
	 * cord moves by vector d, check if result within bounding box of link
	 * @param net
	 * @param cord
	 * @param maxDistance
	 * @param AllowedModes 
	 * @param maxSpeedAllowed - freespeed of links must be lower than
	 * @return List of Links
	 * @author JLo
	 */
	public static List<Id<Link>> getNearestLinksExactlyByMath(Network net, Coord cord, double maxDistance, Set<String> AllowedModes, double maxSpeedAllowed, Set<Id<Link>> exclusionLinks){
		
		List<Id<Link>> linkIdList = new ArrayList<Id<Link>>();
		
		while(linkIdList.isEmpty()) {			//to ensure getting links
			for(Node TNode:NetworkUtils.getNearestNodes(net, cord, maxDistance))	{
				for(Link TTLink:TNode.getInLinks().values())
					if(!linkIdList.contains(TTLink.getId())
							&& TTLink.getFreespeed()<=maxSpeedAllowed
							&& !exclusionLinks.contains(TTLink.getId())
							&& Sets.intersection(TTLink.getAllowedModes(), AllowedModes).size()>0 
							 	)
						linkIdList.add(TTLink.getId());
				for(Link TTLink:TNode.getOutLinks().values())
					if(!linkIdList.contains(TTLink.getId())
							&& TTLink.getFreespeed()<=maxSpeedAllowed
							&& !exclusionLinks.contains(TTLink.getId())
							&& Sets.intersection(TTLink.getAllowedModes(), AllowedModes).size()>0 
							 	)
						linkIdList.add(TTLink.getId());
			}
			for(Link TLink:net.getLinks().values()) {
				if(!linkIdList.contains(TLink.getId())
						&& TLink.getFreespeed()<=maxSpeedAllowed														
						&& !exclusionLinks.contains(TLink.getId())
						&& Sets.intersection(TLink.getAllowedModes(), AllowedModes).size()>0) {	//check allowed modes	
//					if(TLink==Id.createLinkId("401420_401419"))		//for debug certain links
//						maxDistance=maxDistance;
					//math stuff
					double x1 = TLink.getFromNode().getCoord().getX();
					double y1 = TLink.getFromNode().getCoord().getY();
					double x2 = TLink.getToNode().getCoord().getX();
					double y2 = TLink.getToNode().getCoord().getY();
					double absv = Math.sqrt(Math.pow(x2-x1, 2)+Math.pow(y2-y1, 2));		//the magnitude of vector v
					double unitvdotr = (x2-x1)*(y1-cord.getY())-(x1-cord.getX())*(y2-y1);	//unit vector v dot r
					double d = Math.abs(unitvdotr)/absv;
					double ddir = unitvdotr/Math.abs(unitvdotr)*-1;		//*unitvdotr/Math.abs(unitvdotr) is a terrible solution to +-vector d, but it works
					
					if(d<maxDistance) {
						double x3 = cord.getX()+d*(y2-y1)/absv*ddir;	//shifts cord by vector d
						double y3 = cord.getY()-d*(x2-x1)/absv*ddir;
						if((Math.max(x1,x2)>=x3 && Math.min(x1,x2)<=x3) && (Math.max(y1,y2)>=y3 && Math.min(y1,y2)<=y3)) 	
							//check if new point lies in-between the link, assumes new point is correctly calculated on the link
							linkIdList.add(TLink.getId());
					}
//					if(TLink.getId()==Id.createLinkId("401420_401419"))		//for debug certain links
//						continue;
				}
			}
			maxDistance = maxDistance+20;		//not sure the proper distance
		} 			
		
		//get nearest node, as a safety mechanism		!!!BAD IDEA!!!
//		Node TNode0 = NetworkUtils.getNearestNode(net, cord);
//		for(Id<Link> TTLink:TNode0.getInLinks().keySet())
//			if(!linkIdList.contains(TTLink)
//					&& Sets.intersection(net.getLinks().get(TTLink).getAllowedModes(), AllowedModes).size()>0 && !tunnelsId.contains(TTLink.toString()) )
//				linkIdList.add(TTLink);
//		for(Id<Link> TTLink:TNode0.getOutLinks().keySet())
//			if(!linkIdList.contains(TTLink)
//					&& Sets.intersection(net.getLinks().get(TTLink).getAllowedModes(), AllowedModes).size()>0 && !tunnelsId.contains(TTLink.toString()) )
//				linkIdList.add(TTLink);
		
		return linkIdList;
	}
	
	  /**
     * Finds the (approx.) nearest link to a given point on the map,
     * such that the point lies on the right side of the directed link,
     * if such a link exists.
	 *
     * It searches first for the nearest node, and then for the nearest link
     * originating or ending at that node and fulfilling the above constraint.
     * <p>
     * <b>Special cases:</b> {@code nodes:o ; links:<-- ; coord:x}
     * <i>No right entry link exists</i>
     * <pre>
	 * {@code
     * o<-1--o returning
     * | . . ^ nearest left
     * |2 . 4| entry link
     * v .x. | (link.id=3)
     * o--3->o<br>
	 * }
     * </pre>
     * <i>No right entry link exists but more than one nearest left entry link exist</i>
	 * <pre>
	 * {@code
	 * o<-1--o returning
     * | . . ^ nearest left
     * |2 x 4| entry link with the
     * v . . | lowest link id
     * o--3->o (link.id=1)
	 * }
	 * </pre>
     * <i>More than one nearest right entry link exist</i>
	 * <pre>
	 * {@code
     * o--1->o returning
     * ^ . . | nearest right
     * |2 x 4| entry link with the
     * | . . v lowest link id
     * o<-3--o (link.id=1)
	 *
     * o<----7&8--x->o (link.id=7)
	 * }
	 * </pre>
     *
     * @param coord
     *          the coordinate for which the closest link should be found
     * @return the link found closest to <code>coord</code> and oriented such that the
     * point lies on the right of the link.
     */
    // TODO [balmermi] there should be only one 'getNearestLink' method
    // which returns either the nearest 'left' or 'right' entry link, based on a global
    // config param.
    public static Link getNearestLeftEntryLink(Network network, final Coord coord) {
        Link nearestRightLink = null;
        Link nearestOverallLink = null;
        Node nearestNode = NetworkUtils.getNearestNode((network),coord);

        double[] coordVector = new double[2];
        coordVector[0] = nearestNode.getCoord().getX() - coord.getX();
        coordVector[1] = nearestNode.getCoord().getY() - coord.getY();

        // now find nearest link from the nearest node
        double shortestRightDistance = Double.MAX_VALUE; // reset the value
        double shortestOverallDistance = Double.MAX_VALUE; // reset the value
        List<Link> incidentLinks = new ArrayList<>(nearestNode.getInLinks().values());
        incidentLinks.addAll(nearestNode.getOutLinks().values());
        for (Link link : incidentLinks) {
		double dist = CoordUtils.distancePointLinesegment(link.getFromNode().getCoord(), link.getToNode().getCoord(), coord);
            if (dist <= shortestRightDistance) {
                // Generate a vector representing the link
                double[] linkVector = new double[2];
                linkVector[0] = link.getToNode().getCoord().getX()
                        - link.getFromNode().getCoord().getX();
                linkVector[1] = link.getToNode().getCoord().getY()
                        - link.getFromNode().getCoord().getY();

                // Calculate the z component of cross product of coordVector and the link
                double crossProductZ = coordVector[0]*linkVector[1] - coordVector[1]*linkVector[0];
                // If coord lies to the right of the directed link, i.e. if the z component
                // of the cross product is negative, set it as new nearest link
                if (crossProductZ > 0) {//Just turned the sigh
                    if (dist < shortestRightDistance) {
                        shortestRightDistance = dist;
                        nearestRightLink = link;
                    }
                    else { // dist == shortestRightDistance
                        if (link.getId().compareTo(nearestRightLink.getId()) < 0) {
                            shortestRightDistance = dist;
                            nearestRightLink = link;
                        }
                    }
                }
            }
            if (dist < shortestOverallDistance) {
                shortestOverallDistance = dist;
                nearestOverallLink = link;
            }
            else if (dist == shortestOverallDistance) {
                if (link.getId().compareTo(nearestOverallLink.getId()) < 0) {
                    shortestOverallDistance = dist;
                    nearestOverallLink = link;
                }
            }
        }

        // Return the nearest overall link if there is no nearest link
        // such that the given coord is on the right side of it
        if (nearestRightLink == null) {
            return nearestOverallLink;
        }
        return nearestRightLink;
    }
}

package DhakaProject.ProjectNetwork;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import org.matsim.core.utils.io.OsmNetworkReader;

public class osmAutomatic {
public static void main(String[] args) {
	
	GeotoolsTransformation transformation = new GeotoolsTransformation("WGS84","epsg:3106");
	
	Network network= NetworkUtils.createNetwork();
	
	OsmNetworkReader netReader = new OsmNetworkReader(network, transformation);
	
	netReader.parse("datalarge/highways_dhaka_major_ways.osm");
	
	new NetworkWriter(network).writeV2("data/automaticOSM.xml");
	new NetworkCleaner().run(network);
	new NetworkWriter(network).writeV2("data/automaticOSM_clean.xml");
	
}
}

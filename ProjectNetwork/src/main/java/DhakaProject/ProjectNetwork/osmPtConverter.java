package DhakaProject.ProjectNetwork;

import org.matsim.api.core.v01.network.Network;
import org.matsim.pt2matsim.config.OsmConverterConfigGroup;
import org.matsim.pt2matsim.osm.OsmMultimodalNetworkConverter;
import org.matsim.pt2matsim.osm.lib.OsmData;
import org.matsim.pt2matsim.osm.lib.OsmDataImpl;
import org.matsim.pt2matsim.osm.lib.OsmFileReader;
import org.matsim.pt2matsim.tools.NetworkTools;

public class osmPtConverter {
public static void main(String[] args) {
	OsmConverterConfigGroup osmConfig = OsmConverterConfigGroup.createDefaultConfig();
	osmConfig.setOutputCoordinateSystem("EPSG:3106");
	osmConfig.setOsmFile("datalarge/highways_major_Dhaka.osm");
	osmConfig.setOutputNetworkFile("data/ptNet_Dhaka.xml");
	osmConfig.setMaxLinkLength(1000);
	
	osmConfig.setKeepTagsAsAttributes(true);
	osmConfig.setKeepHighwaysWithPT(true);
	osmConfig.setKeepPaths(true);
	

	// read OSM file
	OsmData osm = new OsmDataImpl();
	new OsmFileReader(osm).readFile(osmConfig.getOsmFile());

	// convert
	OsmMultimodalNetworkConverter converter = new OsmMultimodalNetworkConverter(osm);
	converter.convert(osmConfig);

	Network network = converter.getNetwork();
	
	
	// write file
	NetworkTools.writeNetwork(network, osmConfig.getOutputNetworkFile());
	
	
}
}

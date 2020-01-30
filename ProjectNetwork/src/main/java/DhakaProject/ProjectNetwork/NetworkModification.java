package DhakaProject.ProjectNetwork;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class NetworkModification {
public static void main(String[] args) {
	Config config = ConfigUtils.createConfig();
	config.network().setInputFile("data/ptNet_Dhaka.xml");
	Scenario scenario=ScenarioUtils.loadScenario(config);
	Network network=scenario.getNetwork();
	Link link= network.getLinks().get(Id.createLinkId("3444"));
	Node toNode=network.getNodes().get(Id.createNodeId("6904829215"));
	link.setToNode(toNode);
	
	new NetworkWriter(network).writeV2("data/ptNet_Dhaka_Mod.xml");
}
}

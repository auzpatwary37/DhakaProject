package PopulationGeneration;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleWriterV1;
import org.matsim.vehicles.Vehicles;
import org.matsim.vehicles.VehiclesFactory;



public class popGen {
public static void main(String[] args) {
	Config config=ConfigUtils.createConfig();
	config.network().setInputFile("data/ptNet_Dhaka.xml");
	Network network = ScenarioUtils.loadScenario(config).getNetwork();
	
	double minX=522375;
	double minY=2608127;
	double maxX=563481;
	double maxY=2649535;
	
	config.controler().setLastIteration(100);

	ActivityParams home = new ActivityParams("home");
	home.setTypicalDuration(16 * 60 * 60);
	config.planCalcScore().addActivityParams(home);
	ActivityParams work = new ActivityParams("work");
	work.setTypicalDuration(8 * 60 * 60);
	config.planCalcScore().addActivityParams(work);
	
	config.controler().setOutputDirectory("output");
	
	config.strategy().addParam("ModuleProbability_1", "0.8");
	config.strategy().addParam("Module_1", "ChangeExpBeta");
	config.strategy().addParam("ModuleProbability_2", "0.05");
	config.strategy().addParam("Module_2", "ReRoute");
	config.strategy().addParam("ModuleProbability_3", "0.1");
	config.strategy().addParam("Module_3", "TimeAllocationMutator");


	config.planCalcScore().setWriteExperiencedPlans(true);
	config.planCalcScore().setPerforming_utils_hr(100);

	config.global().setNumberOfThreads(7);

	Scenario scenario = ScenarioUtils.loadScenario(config);
	

	// Create vehicles
	Vehicles vehicles = scenario.getVehicles();
	VehiclesFactory vf = vehicles.getFactory();
	VehicleType vt = vf.createVehicleType(Id.create("car", VehicleType.class));
	vt.setPcuEquivalents(1);
	vt.setMaximumVelocity(50);
	vehicles.addVehicleType(vt);

	// new MatsimNetworkReader(scenario.getNetwork()).readFile("data/network.xml");
	int segments = 1;
	int populations = 10000;
	for (int i = 0; i < segments; i++) {
		fillScenario(scenario, vt, minX, maxX, minY, maxY, populations / segments,
				(i - segments / 2) * 10 * 60);
	}

	new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write("output/plan.xml");
	new VehicleWriterV1(scenario.getVehicles()).writeFile("output/vehicles.xml");

	Controler controler = new Controler(scenario);
	

	
	
	controler.getConfig().controler()
			.setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
	controler.run();

	
}


private static Population fillScenario(Scenario scenario, VehicleType vt, double x_min, double x_max, double y_min,
		double y_max, int NoAgents, int offset) {
	Population population = scenario.getPopulation();
	double x_mid = (x_min + x_max) / 2;
	double y_mid = (y_min + y_max) / 2;

	int grid_size = (int) Math.sqrt(NoAgents);

	for (int i = 0; i < NoAgents; i++) { 
		Coord coord = new Coord((double) (x_min + Math.random()*(x_max-x_min)),
				(double) (y_min + Math.random()*(y_max-y_min)));
		
		double theta=Math.random()*2*Math.PI;
		double r=6000*(1+Math.random());
		
		Coord coordWork = new Coord(
				(double) (coord.getX()+r*Math.cos(theta)),
				(double) (coord.getY()+r*Math.sin(theta)));
		createOnePerson(scenario, population, vt, i, coord, coordWork, offset);
	}
	return population;
}

private static void createOnePerson(Scenario scenario, Population population, VehicleType vt, int i, Coord coord,
		Coord coordWork, int time_offset) {
	String personId = "p_" + i + "_" + time_offset / 60;
	Person person = population.getFactory().createPerson(Id.createPersonId(personId)); // Create person

	// Create and add vehicle for this person
	Vehicles vehicles = scenario.getVehicles();
	VehiclesFactory vf = vehicles.getFactory();
	Vehicle v = vf.createVehicle(Id.createVehicleId(personId), vt);
	vehicles.addVehicle(v);

	Plan plan = population.getFactory().createPlan();

	Activity home = population.getFactory().createActivityFromCoord("home", coord);
	home.setEndTime(9 * 60 * 60 + time_offset);
	plan.addActivity(home);

	Leg hinweg;
	if (i % 1 == 0) {// all will be assigned car
		hinweg = population.getFactory().createLeg("car");
	} else {
		hinweg = population.getFactory().createLeg("pt");
	}
	plan.addLeg(hinweg);

	Activity work = population.getFactory().createActivityFromCoord("work", coordWork);
	// work.setStartTime(9*60*60+time_offset);
	work.setEndTime(17 * 60 * 60 + time_offset);
	plan.addActivity(work);

	Leg rueckweg;
	if (i % 2 == 0) {
		rueckweg = population.getFactory().createLeg("car");
	} else {
		rueckweg = population.getFactory().createLeg("pt");
	}
	plan.addLeg(rueckweg);

	Activity home2 = population.getFactory().createActivityFromCoord("home", coord);
	plan.addActivity(home2);

	person.addPlan(plan);
	population.addPerson(person);
}

}

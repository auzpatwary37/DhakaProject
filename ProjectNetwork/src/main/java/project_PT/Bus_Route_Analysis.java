package project_PT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class Bus_Route_Analysis {
	public static void main(String[] args) throws IOException {
	
		String fileLoc= "data/bus_route_stop data_app.csv";
		
		BufferedReader bufferedReader= new BufferedReader(new FileReader(new File(fileLoc)));
		
		bufferedReader.readLine();//get rid of the header
		
		String line=null;
		
		HashSet<String> stops=new HashSet<>();
		HashSet<String> routeId=new HashSet<>();
		
		while((line=bufferedReader.readLine())!=null) {
			
			String[] part=line.split(",");
			String name=part[0].trim();
			String route=part[1].trim();
			if(route.equals("na")) {
				route=part[3].trim()+"_"+part[part.length-1].trim();
			}
			if(!routeId.contains(route)) {
				routeId.add(route);
			}
			for(int i=2;i<part.length;i++) {
				if(!stops.contains(part[i].trim())) {
					stops.add(part[i].trim());
				}
			}
			
			
		}
		
		bufferedReader.close();
		
		FileWriter fw= new FileWriter(new File("data/stops_trial.csv"));
		String sep="";
		for(String s:stops) {
			fw.append(sep+s);
			sep=",";
		}
		fw.append("\n");
		sep="";
		for(String s:routeId) {
			fw.append(sep+s);
			sep=",";
		}
		fw.append("\n");
		fw.flush();
		fw.close();
		
	}
}

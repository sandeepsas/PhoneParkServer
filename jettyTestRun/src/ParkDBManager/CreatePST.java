/*//DO NOT RUN THIS FILE

package ParkDBManager;


import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParserException;

import Database.LoadPST;
import MapDatabase.DirectedEdge;
import MapDatabase.GraphNode;
import MapDatabase.ParseOSM;
import MapDatabase.RoadGraph;

public class CreatePST {

	public static void main (String[] args0){

		LoadPST pst = new LoadPST();
		//Fetch the parking availability values from PST database for the street Block

		ParseOSM parseOSM = null;
		try {
			parseOSM = new ParseOSM();
		} catch (IOException | XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		RoadGraph roadGraph = parseOSM.getRoadGraph();

		LinkedList<GraphNode> nodes = roadGraph.nodes;
		LinkedList<DirectedEdge> edges = roadGraph.edges ;

		Iterator<DirectedEdge> edge_itr = edges.iterator();

		int streetID = 1;

		while(edge_itr.hasNext()){

			DirectedEdge e = edge_itr.next();
			double length = e.getLength();
			
			int totalSpaces;
			
			if(length<0.015){
				totalSpaces = 0;
			}else{
				totalSpaces = (int)(150*length);
			}

			pst.writePST(streetID,totalSpaces,totalSpaces);
			streetID++;
		}
	}

}
*/
/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package map;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

import timer.Timer;
import tools.Astar;
import tools.Route;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class MapAgent extends Agent {
	
	private static final int PATH_SIZE = 12;
	private static final int FIGURE_SIZES = 32;
	private Map map;
	private Map original_map;
	private String map_path;
	private ArrayList<AID> map_subscribers;
	private Viewer viewer;
	private int boxes_in_goal = 0;
	private int moves = 0;
	timer.Timer timer = new Timer();

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! BoxAgent " + getAID().getName() + " is running.");
		map_subscribers = new ArrayList<AID>();

		// Loading arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			map_path = args[0].toString();
			
			// Register the map service for sending the map to other agents through the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("map");
			sd.setName("Chokoban");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			map = loader();
			original_map = loader();
			
			// Add the behaviour serving queries from mover agents
			addBehaviour(new MapSubscribeServer());
			
			addBehaviour(new MapUpdateServer());
			
			createAgents();
			
			viewer = new Viewer(map.map);
			
			viewer.drawMap(map.map);
			
			timer.restart();
			
		}
		else {
			// Make the agent terminate
			System.out.println("No arguments was passed");
			doDelete();
		}
	}
	
	void simulateRoute(String route, String mover, int wait)
	{
		int moverID = Integer.parseInt(mover);
		int[] solverposition = findSolver(map.map, mover);
		
		moves++;

		for (int i = 0; i<route.length(); i++)
		{
			if (route.charAt(i) == 'u' || route.charAt(i) == 'U')
			{
				if (map.map[solverposition[0]][solverposition[1]-1] >= 1000){
						map.map[solverposition[0]][solverposition[1]-1] = moverID + 40;
						boxes_in_goal++;
						System.out.println("Boxes: " + boxes_in_goal + "Time: " + timer.timeSinceStartInSeconds());
						System.out.println("Moves per box: " + moves/boxes_in_goal);
						System.out.println("Boxes per minute: " + boxes_in_goal/(timer.timeSinceStartInSeconds()/60));
				}
				else{
					if (map.map[solverposition[0]][solverposition[1]] >= 50){
						map.map[solverposition[0]][solverposition[1]-1] = moverID + 40;

					}
					else
						map.map[solverposition[0]][solverposition[1]-1] = moverID;
				}
				
				if (original_map.map[solverposition[0]][solverposition[1]] >= 100 && original_map.map[solverposition[0]][solverposition[1]] < 1000)
					map.map[solverposition[0]][solverposition[1]] = original_map.map[solverposition[0]][solverposition[1]];
				else
					map.map[solverposition[0]][solverposition[1]] = 2;
				solverposition[1] -= 1;
			}
			else if (route.charAt(i) == 'd' || route.charAt(i) == 'D')
			{
				if (map.map[solverposition[0]][solverposition[1]+1] >= 1000){
						map.map[solverposition[0]][solverposition[1]+1] = moverID + 40;
						boxes_in_goal++;
						System.out.println("Boxes: " + boxes_in_goal + "Time: " + timer.timeSinceStartInSeconds());
						System.out.println("Moves per box: " + moves/boxes_in_goal);
						System.out.println("Boxes per minute: " + boxes_in_goal/(timer.timeSinceStartInSeconds()/60));


				}
				else{
					if (map.map[solverposition[0]][solverposition[1]] >= 50){
						map.map[solverposition[0]][solverposition[1]+1] = moverID + 40;

					}
					else
						map.map[solverposition[0]][solverposition[1]+1] = moverID;
				}
				if (original_map.map[solverposition[0]][solverposition[1]] >= 100 && original_map.map[solverposition[0]][solverposition[1]] < 1000)
					map.map[solverposition[0]][solverposition[1]] = original_map.map[solverposition[0]][solverposition[1]];
				else
					map.map[solverposition[0]][solverposition[1]] = 2;
				solverposition[1] += 1;
			}
			else if (route.charAt(i) == 'l' || route.charAt(i) == 'L')
			{
				if (map.map[solverposition[0]-1][solverposition[1]] >= 1000){
						map.map[solverposition[0]-1][solverposition[1]] = moverID + 40;
						boxes_in_goal++;
						System.out.println("Boxes: " + boxes_in_goal + "Time: " + timer.timeSinceStartInSeconds());
						System.out.println("Moves per box: " + moves/boxes_in_goal);
						System.out.println("Boxes per minute: " + boxes_in_goal/(timer.timeSinceStartInSeconds()/60));


				}
				else{
					if (map.map[solverposition[0]][solverposition[1]] >= 50){
						map.map[solverposition[0]-1][solverposition[1]] = moverID + 40;
					}
					else
						map.map[solverposition[0]-1][solverposition[1]] = moverID;
				}
				if (original_map.map[solverposition[0]][solverposition[1]] >= 100 && original_map.map[solverposition[0]][solverposition[1]] < 1000)
					map.map[solverposition[0]][solverposition[1]] = original_map.map[solverposition[0]][solverposition[1]];
				else
					map.map[solverposition[0]][solverposition[1]] = 2;
				solverposition[0] -= 1;
			}
			else if (route.charAt(i) == 'r' || route.charAt(i) == 'R')
			{
				if (map.map[solverposition[0]+1][solverposition[1]] >= 1000){
						map.map[solverposition[0]+1][solverposition[1]] = moverID + 40;
						boxes_in_goal++;
						System.out.println("Boxes: " + boxes_in_goal + "Time: " + timer.timeSinceStartInSeconds());
						System.out.println("Moves per box: " + moves/boxes_in_goal);
						System.out.println("Boxes per minute: " + boxes_in_goal/(timer.timeSinceStartInSeconds()/60));
				}
				else{
					if (map.map[solverposition[0]][solverposition[1]] >= 50){
						map.map[solverposition[0]+1][solverposition[1]] = moverID + 40;

					}
					else
						map.map[solverposition[0]+1][solverposition[1]] = moverID;
				}
				if (original_map.map[solverposition[0]][solverposition[1]] >= 100 && original_map.map[solverposition[0]][solverposition[1]] < 1000)
					map.map[solverposition[0]][solverposition[1]] = original_map.map[solverposition[0]][solverposition[1]];
				else
					map.map[solverposition[0]][solverposition[1]] = 2;
				solverposition[0] += 1;
			}
			
			viewer.updateMap(map.map);
					
			if (wait != 0)
			{
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
		for(int i = 0; i < map.map.length ; i++)
			for(int j = 0; j < map.map[0].length; j++)
				if(map.map[i][j] > 49 && map.map[i][j] < 100 && original_map.map[i][j] > 99 && original_map.map[i][j] < 1000)
					map.map[i][j] -= 40;
		/*
		for(int i = 0; i < map.map.length ; i++){
			for(int j = 0; j < map.map[0].length; j++){
				System.out.print(map.map[i][j] + " ");
			}
			System.out.println("");
		}*/
		

				
		

	} 

	private int[] findSolver(int[][] map, String mover)
	{
		int[] r = new int[2];
		for (int i = 0; i<map.length; i++)
			for (int j = 0; j<map[0].length; j++)
				if (map[i][j] == Integer.parseInt(mover) || map[i][j] == Integer.parseInt(mover) + 40 )
				{
					r[0] = i;
					r[1] = j;
					return r;
				}
		r[0] = -1;
		r[1] = -1;
		return r;
	}
	
	/**
	   Inner class MapSubscribeServer.
	 */
	private class MapSubscribeServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// SUBSCRIBE Message received. Process it
				String title = msg.getContent();
				map_subscribers.add(msg.getSender());
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.INFORM);
				reply.setConversationId("map_conv");
				try {
					reply.setContentObject(map);
					myAgent.send(reply);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				block();
			}
		}
	}  // End of inner class MapSubscribeServer
	
	/**
	   Inner class MapSubscribeServer.
	 */
	private class MapUpdateServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getConversationId().equals("map_update"))
				{
					String route = msg.getContent().substring(0, msg.getContent().length() - 2);
					String mover = msg.getContent().substring(msg.getContent().length() - 2, msg.getContent().length());
					simulateRoute(route, mover, 0);
					//System.out.println("Mover: " + mover + " Route: " + route);
					
					ACLMessage update = new ACLMessage(ACLMessage.INFORM);
					for (int j = 0; j < map_subscribers.size(); j++)
						update.addReceiver(map_subscribers.get(j));

					update.setPerformative(ACLMessage.INFORM);
					update.setConversationId("map_conv");
					
					try {
						update.setContentObject(map);
						myAgent.send(update);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					viewer.updateMap(map.map);
				}
			}
			else {
				block();
			}
		}
	}  // End of inner class MapSubscribeServer
	
	private int[][] emptyMap(int[][] mapIn){
		for(int i = 0 ; i < mapIn.length; i++)
			for(int j = 0; j < mapIn[0].length; j++)
				mapIn[i][j] = 0;
		return mapIn;
	}
	
	private void startNewAgent(String className,String agentName,Object[] arguments) throws StaleProxyException 
	{
		((AgentController)getContainerController().createNewAgent(agentName,className,arguments)).start();
	}
	
	private void createAgents()
	{
		int i;
		for (i = 0; i < map.no_of_movers; i++)
		{
			Object[] temp = new Object[1];
			temp[0] = new Integer(10+i);
			try {
				startNewAgent("mover.MoverAgent", "MoverAgent" + (10+i), temp);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (i = 0; i < map.no_of_boxes1; i++)
		{
			Object[] temp = new Object[1];
			temp[0] = new Integer(1000+i);
			try {
				startNewAgent("box.BoxAgent", "BoxAgent" + (1000+i), temp);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (i = 0; i < map.no_of_boxes2; i++)
		{
			Object[] temp = new Object[1];
			temp[0] = new Integer(2000+i);
			try {
				startNewAgent("box.BoxAgent", "BoxAgent" + (2000+i), temp);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (i = 0; i < map.no_of_boxes3; i++)
		{
			Object[] temp = new Object[1];
			temp[0] = new Integer(3000+i);
			try {
				startNewAgent("box.BoxAgent", "BoxAgent" + (3000+i), temp);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (i = 0; i < map.no_of_boxes4; i++)
		{
			Object[] temp = new Object[1];
			temp[0] = new Integer(4000+i);
			try {
				startNewAgent("box.BoxAgent", "BoxAgent" + (4000+i), temp);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Object[] temp = new Object[1];
		temp[0] = new Integer(map.no_of_goals);
		try {
			startNewAgent("goal.GoalAgent", "GoalAgent", temp);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private Map loader(){
		
		Map r = new Map();
		int movers = 10;
		int boxes1 = 1000;
		int boxes2 = 2000;
		int boxes3 = 3000;
		int boxes4 = 4000;
		
		int goals = 100;
		
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(map_path);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  strLine = br.readLine();
			  
			  // Do something to the map specs
			  String delimiter = " ";
			  String[] temp = strLine.split(delimiter);
			 
			  if (temp.length == 3)
			  {
				  r.map_width = Integer.parseInt(temp[0]);
				  r.map_height = Integer.parseInt(temp[1]);
				  r.no_of_goals = Integer.parseInt(temp[2]);				  
			  }
			  else
				  System.out.println("ERROR: Map specs has wrong format");


			  
			  r.map = new int[r.map_width][r.map_height];
			  r.map = emptyMap(r.map);
			  
			  int j = 0;
			  //Read File Line By Line
			  
			  while ((strLine = br.readLine()) != null)   {
				  // Print the content on the console
				 // System.out.println (strLine);
				  for( int i = 0 ; i < strLine.length()  ; i++){
					  switch (strLine.charAt(i)) {
					case 'X':
					case 'x':
						r.map[i][j] = 1;
						break;
						  
					case '.':
						r.map[i][j] = 2;
						break;
						
					case 'M':
					case 'm':
						r.map[i][j] = movers++;
						break;
						
					case 'G':
					case 'g':
						r.map[i][j] = goals++;
						break;
						
					case '1':
						r.map[i][j] = boxes1++;
						break;
						
					case '2':
						r.map[i][j] = boxes2++;
						break;
						
					case '3':
						r.map[i][j] = boxes3++;
						break;
						
					case '4':
						r.map[i][j] = boxes4++;
						break;
						
					default:
						break;
					}
				  }
				  j++;
			  }
			  //Close the input stream
			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
		r.no_of_boxes1 = boxes1 - 1000;
		r.no_of_boxes2 = boxes2 - 2000;
		r.no_of_boxes3 = boxes3 - 3000;
		r.no_of_boxes4 = boxes4 - 4000;

		r.no_of_goals = goals - 100;
		r.no_of_movers = movers - 10;
		return r;
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("BoxAgent " + getAID().getName() + " terminating.");
	}
}

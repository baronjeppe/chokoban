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
	private String map_path;
	private ArrayList<AID> map_subscribers;
	private Viewer viewer;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! BoxAgent " + getAID().getName() + " is running.");
		map_subscribers = new ArrayList<AID>();

		// Loading arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			map_path = (String) args[0];
			
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
			
			// Add the behaviour serving queries from mover agents
			addBehaviour(new MapSubscribeServer());
			
			addBehaviour(new MapUpdateServer());
			
			createAgents();
			
			viewer = new Viewer(map.map_width, map.map_height);
			
			viewer.drawMap(map.map);
			
		}
		else {
			// Make the agent terminate
			System.out.println("No arguments was passed");
			doDelete();
		}
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
					String updates = msg.getContent();
					String[] commands = updates.split(" ");
					for (int i = 0; i < commands.length; i++)
					{
						System.out.println(commands[i]);
						String[] coordinates = commands[i].split(",");
						map.map[Integer.parseInt(coordinates[3])][Integer.parseInt(coordinates[2])] = map.map[Integer.parseInt(coordinates[1])][Integer.parseInt(coordinates[0])];
						map.map[Integer.parseInt(coordinates[1])][Integer.parseInt(coordinates[0])] = 2;
					}
					
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
					viewer.drawMap(map.map);
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
		}
		for (i = 0; i < map.no_of_boxes; i++)
		{
			Object[] temp = new Object[1];
			temp[0] = new Integer(1000+i);
			try {
				startNewAgent("box.BoxAgent", "BoxAgent" + (1000+i), temp);
			} catch (StaleProxyException e) {
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
		int boxes = 1000;
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
						
					case 'J':
					case 'j':
						r.map[i][j] = boxes++;
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
		r.no_of_boxes = boxes - 1000;
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

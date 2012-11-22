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

package box;

import java.util.LinkedList;
import java.util.PriorityQueue;

import timer.Timer;
import box.nodeHashing.NodeHashTable;
import box.Node;
import box.State;
import map.Map;
import map.Viewer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class BoxAgent extends Agent {
	
	private int id;
	private AID mapAgent;
	private Map map;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! BoxAgent " + getAID().getName() + " is running.");

		// Loading arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			id = Integer.parseInt(args[0].toString());
			
			// Register the box-route service for sending desired route to the movers in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("box-route");
			sd.setName("Chokoban");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// Add the behaviour serving queries from mover agents
			addBehaviour(new RoutePriceRequestsServer());
			
			// Add the behaviour serving queries from mover agents
			addBehaviour(new RouteRequestServer());
			
			addBehaviour(new MapSubscriber());
			
			// Find map-agent and subscriber to map
			addBehaviour(new OneShotBehaviour(this) {
				public void action() {
					//System.out.println("Requesting MapAgent");
					// find the map agent
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("map");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						if (result.length > 0)
						{
							mapAgent = result[0].getName();
							ACLMessage sub = new ACLMessage(ACLMessage.SUBSCRIBE);
							sub.addReceiver(mapAgent);
							sub.setContent("subscribe_to_map");
							sub.setConversationId("map_conv");
							sub.setReplyWith("sub"+System.currentTimeMillis()); // Unique value
							myAgent.send(sub);
							
						}
						else
							System.out.println("FAILED");
						//System.out.println("mapAgent: " + mapAgent.getName());
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
			} );
			
		}
		else {
			// Make the agent terminate
			System.out.println("No arguments was passed");
			doDelete();
		}
	}
	
	private int[][] makeOriginalMap(int[][] map)
	{
		int[][] ret = new int[map.length][map[0].length];
		for (int i = 0; i<map.length; i++)
			for (int j = 0; j<map[0].length;j++){
				if (map[i][j] == 100)
					ret[i][j] = 2;
				else if (map[i][j] == 10)
					ret[i][j] = 2;
				else if (map[i][j] == 1000)
					ret[i][j] = 100;
				else
					ret[i][j] = map[i][j];
			}
		return ret;
	}
	
	private int[][] reverseMap(int[][] map)
	{
		int[][] ret = new int[map.length][map[0].length];
		for (int i = 0; i<map.length; i++)
			for (int j = 0; j<map[0].length;j++){
				if (map[i][j] == 100)
					ret[i][j] = 1000;
				else if (map[i][j] == 1000)
					ret[i][j] = 100;
				else
					ret[i][j] = map[i][j];
			}
		return ret;
	}

	public String solve(int[][] map, int[] mover)
	{
		String ret = new String();
		
		PriorityQueue<Node> openList = new PriorityQueue<Node>();
		
		NodeHashTable closedList = new NodeHashTable();
		NodeHashTable openListHash = new NodeHashTable();
		
		LinkedList<Node> temp = new LinkedList<Node>();

		Timer timer = new Timer();
		
		State.original_map = makeOriginalMap(map);
		State initialState = new State(reverseMap(map), mover);
		
		Node initialNode = new Node(null,'-',initialState);
		
		openList.add(initialNode);
		openListHash.insert(initialNode);
		
		while(true) {
		    if (openList.isEmpty() ) {
		    	//System.out.println("No Solution Found\n");
		    	ret = "-";
		    	break;
		    }
		    
		    Node currentNode = openList.peek();
		    
		    if (currentNode.state.equalToMap(map)) {
		    //	System.out.println("Solution Found\n");
		    	//System.out.println("Collisions: " + closedList.getCollisions());
		    	ret = currentNode.printSolution();
		    	//System.out.println("Price: " + currentNode.state.price);
		    	//System.out.println("Nodes expanded: " + Integer.toString( Node.nodesExpanded ) );
		    	break;
		    }
		    temp = currentNode.expand();
		    
		    while(!temp.isEmpty()){
		    	if (!openListHash.contains(temp.getFirst()) && !closedList.contains(temp.getFirst())) {
		    		openList.add(temp.getFirst());
		    		openListHash.insert(temp.pollFirst());
		    	}
		    	else {
		    		temp.removeFirst();
		    	}
		    }
		    if (timer.timeSinceStartInNS() > 1000000000)
		    {
		    	System.out.println("Time elapsed: " + timer.timeSinceStartInSeconds() + " seconds \t Nodes expanded: " + Node.nodesExpanded + "\tOpenlist: " + openList.size());
		    }
		    
		    if (!closedList.contains(currentNode))
		    	closedList.insert(currentNode);
		    openList.remove(currentNode);
		    openListHash.remove(currentNode);
		}
		return ret;
	}
	
	String calcRoute()
	{
		String ret;
		int[] mover = new int[2];
		
		int[][] solverMap = new int[map.map_width][map.map_height];
		
		
		for (int i = 0; i < map.map_width; i++)
			for (int j = 0; j < map.map_height; j++)
			{
				if (map.map[i][j] >= 1000)
					solverMap[i][j] = 1;
				else if (map.map[i][j] == 10)
				{
					mover[0] = i;
					mover[1] = j;
					solverMap[i][j] = 10;
				}
				else if(map.map[i][j] >= 100 && map.map[i][j] < 1000){
					solverMap[i][j] = 100;
				}
				else
					solverMap[i][j] = map.map[i][j];
				
				if (map.map[i][j] == id)
					solverMap[i][j] = 1000;
			}
		/*
		for (int i = 0; i < solverMap[0].length; i++){
			for (int j = 0; j < solverMap.length; j++){
				System.out.print(solverMap[j][i] + "\t");
			}
			System.out.println("");
		}*/
		
		ret = solve(solverMap,mover);
		
		return ret;
	}
	
	/**
	   Inner class RoutePriceRequestsServer.
	 */
	private class RoutePriceRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it				
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				// Calc route price and propose
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setConversationId("route_conv");
				reply.setContent(calcRoute());
				//System.out.println(myAgent.getName() + " has been requested");

				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	/**
	   Inner class RoutePriceRequestsServer.
	 */
	private class MapSubscriber extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getConversationId().equals("map_conv"))
				{
					// CFP Message received. Process it	
					try {
						map = (Map) msg.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println("map_height:" + map.map_height);
				}

			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	
	/**
	   Inner class RouteRequestServer.
	 */
	private class RouteRequestServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.INFORM);
				reply.setConversationId("route_conv");
				reply.setContent(calcRoute());

				myAgent.send(reply);
				
				//doDelete();
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

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

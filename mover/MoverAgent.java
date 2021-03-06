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

package mover;

import java.util.Random;

import map.Map;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MoverAgent extends Agent {
	private int id;
	private String queue = "";
	private AID[] boxAgents;
	private AID mapAgent;
	private AID goalAgent;
	private ACLMessage goalMessage;
	private Map map;
	private boolean findMyself = false;
	private Integer[] endPos = new Integer[2];
	private Integer[] curPos = new Integer[2];
	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! BoxAgent " + getAID().getName() + " is running.");

		// Loading arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			
			// Register mover agent service in yellow pages.
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("mover-agent");
			sd.setName("Chokoban");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			id = Integer.parseInt(args[0].toString());
			
			addBehaviour(new MapSubscriber());
					
			addBehaviour(new MoveBehaviour(this, 400));
						
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
							System.out.println("FAILED - Found no MapAgents");
						//System.out.println("mapAgent: " + mapAgent.getName());
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
				}
			} );
			
			addBehaviour(new RequestBoxRouteServer());

			
			/*addBehaviour(new OneShotBehaviour() {
				
				@Override
				public void action() {
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("goal-agent");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						if (result.length > 0)
							goalAgent = result[0].getName();
						
						else
							System.out.println("FAILED - Found no MapAgents");
						//System.out.println("mapAgent: " + mapAgent.getName());
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
				}
			}) ;*/
		
		}
		else {
			// Make the agent terminate
			System.out.println("No arguments was passed");
			doDelete();
		}
	}
	
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
					if(!findMyself){
						findMyself = true;
						for(int i = 0; i < map.map.length; i++)
							for(int j = 0; j < map.map[0].length; j++){
								if(map.map[i][j] == id){
									endPos[0] = j;
									endPos[1] = i;
									curPos[0] = j;
									curPos[1] = i;
								}
									
							}
					}
						
				}

			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	
	/**
	   Inner class RequestBoxRoutePerformer.
	   This is the behaviour used by the Mover agents to request routes from the boxes 
	   and find which box to move.
	 */
	private class RequestBoxRoutePerformer extends Behaviour {
		
		private RequestBoxRoutePerformer(String boxType, ACLMessage replyIn){
			boxTypes = boxType;
			replyBox = replyIn;
		}
		
		private AID boxWithBestRoute; // The agent who provides the best offer 
		private int bestPrice;  // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
		private String bestRoute;
		private String boxTypes;
		private ACLMessage replyBox;
		private int askedBoxes = 0;
		

		public void action() {
			switch (step) {
			case 0:
					askedBoxes = 0;
					// Send the cfp to all boxes
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < boxAgents.length; ++i) {
						if(boxAgents[i].getName().substring(8,9).equals(boxTypes)){
							cfp.addReceiver(boxAgents[i]);
							askedBoxes++;
						}
					} 
					
					cfp.setContent("Request_route_price-" + endPos[1].toString() + "-" + endPos[0].toString());
					System.out.println("Content: " + cfp.getContent());
					cfp.setConversationId("route_conv");
					cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("route_conv"),
							MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
				
					
				break;
			case 1:
				// Receive all proposals/refusals from boxes
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getConversationId().equals("route_conv"))
					{
						if (reply.getPerformative() == ACLMessage.PROPOSE && !reply.getContent().equals("-")) {
							// This is an offer 
							int price = reply.getContent().length();
							Random random = new Random();
							int temp = random.nextInt(3);
							if (boxWithBestRoute == null || temp == 2) {
								// This is the best offer at present
								bestPrice = price;
								bestRoute = reply.getContent();
								boxWithBestRoute = reply.getSender();
							}
						}

						repliesCnt++;
						if (repliesCnt >= askedBoxes) {
							ACLMessage order = null;
							// We received all replies
					
							if(bestRoute != null){
								MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
								order = new ACLMessage(ACLMessage.CFP);
								order.addReceiver(goalAgent);
								order.setContent(queue + "-" +  bestRoute + "-" + boxWithBestRoute.getName());
								order.setConversationId("route_req");
							}
							else{
								order.setContent("-");
							}
							myAgent.send(order);

							
							step = 2; 
							
						}
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// Request the route from the selected box
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(boxWithBestRoute);
				order.setContent("Request_route" + "-" + endPos[1] + "-" + endPos[0]);
				order.setConversationId("route_conv");
				order.setReplyWith("route_request"+System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("route_conv"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// Receive the route order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getConversationId().equals("route_conv"))
					{
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// Purchase successful. We can terminate
							System.out.println("Route successfully recieved from agent "+reply.getSender().getName());
							System.out.println("Route = " + reply.getContent().toString());
							//myAgent.doDelete();
							
						}
						else {
							System.out.println("Attempt failed: requested book already sold.");
						}
						step = 4;
					}
				}
				else {
					block();
				}
				break;
			}  
			
			//System.out.println(getName() + "Found route: " + bestRoute + " From: " + boxWithBestRoute.getName());
			
		}
		
		

		public boolean done() {
			if (step == 2 && boxWithBestRoute == null) {
				System.out.println("Attempt failed: not available for sale");
			}
			return ((step == 2 && boxWithBestRoute == null) || step == 4);
		}
	}  // End of inner class RequestPerformer
	
	private class MoveBehaviour extends TickerBehaviour{
		
		public MoveBehaviour(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}

		private MessageTemplate mt; // The template to receive replies
		
		public void walkRoute(String route, String mover)
		{			
			//System.out.println("Walk route: " + route);

			for (int i = 0; i<route.length(); i++)
			{
				ACLMessage order = new ACLMessage(ACLMessage.INFORM);
				order.addReceiver(mapAgent);
				//System.out.println("Trying to walk with mover: " + mover.substring(10,12));
				order.setContent(route.substring(i, i+1) + id );
				//System.out.println(route.substring(i, i+1));
				order.setConversationId("map_update");
				order.setReplyWith("map_update"+System.currentTimeMillis());
				myAgent.send(order);
			}
		}

		@Override
		protected void onTick() {
			if(queue.length() > 0){
				char nexMove = queue.charAt(0);
				
				int[] temp = {curPos[0], curPos[1]};
				
				switch(nexMove) {
				case 'u':
				case 'U':
					temp[0]--;
					break;
					
				case 'd':
				case 'D':
					temp[0]++;
					break;
					
				case 'r':
				case 'R':
					temp[1]++;
					break;
					
				case 'l':
				case 'L':
					temp[1]--;
					break;
				default:
					//queue = queue.substring(1);
					System.out.println("False move..!");
					break;
				}
				
				if(map.map[temp[1]][temp[0]] >= 10 && map.map[temp[1]][temp[0]] < 100){
					switch(nexMove) {
					case 'u':
					case 'U':
						if(map.map[curPos[1]-1][curPos[0]] == 2 || (map.map[curPos[1]-1][curPos[0]] >= 100 && map.map[curPos[1]-1][curPos[0]] < 1000)) 
							if(map.map[curPos[1]-1][curPos[0]-1] == 2 || (map.map[curPos[1]-1][curPos[0]-1] >= 100 && map.map[curPos[1]-1][curPos[0]-1] < 1000)){
								queue = "LUR" + queue.substring(1);
							}
						
						break;
						
					case 'd':
					case 'D':
						if(map.map[curPos[1]+1][curPos[0]] == 2 || (map.map[curPos[1]+1][curPos[0]] >= 100 && map.map[curPos[1]+1][curPos[0]] < 1000)) 
							if(map.map[curPos[1]+1][curPos[0]+1] == 2 || (map.map[curPos[1]+1][curPos[0]+1] >= 100 && map.map[curPos[1]+1][curPos[0]+1] < 1000)) 
								queue = "RDL" + queue.substring(1);
						break;
						
					case 'r':
					case 'R':
						if(map.map[curPos[1]][curPos[0]-1] == 2 || (map.map[curPos[1]][curPos[0]-1] >= 100 && map.map[curPos[1]][curPos[0]-1] < 1000)) 
							if(map.map[curPos[1]+1][curPos[0]-1] == 2 || (map.map[curPos[1]+1][curPos[0]-1] >= 100 && map.map[curPos[1]+1][curPos[0]-1] < 1000)) 
								queue = "URD" + queue.substring(1);
						break;
						
					case 'l':
					case 'L':
						if(map.map[curPos[1]][curPos[0]+1] == 2 || (map.map[curPos[1]][curPos[0]+1] >= 100 && map.map[curPos[1]][curPos[0]+1] < 1000)) 
							if(map.map[curPos[1]-1][curPos[0]+1] == 2 || (map.map[curPos[1]-1][curPos[0]+1] >= 100 && map.map[curPos[1]-1][curPos[0]+1] < 1000)) 
								queue = "DLU" + queue.substring(1);
						break;
					}
				}
				else{
					walkRoute(queue.substring(0,1), myAgent.getName());
					queue = queue.substring(1);
					curPos[0] = temp[0];
					curPos[1] = temp[1];

				}
			}
			
		}
		
	}
	
	
	private class RequestBoxRouteServer extends CyclicBehaviour {
		
		private MessageTemplate mt; // The template to receive replies
		
		public String calcMoverRoutes(String boxTypes, ACLMessage reply){
			String ret = "";
			
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("box-route");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template); 
				//System.out.println("Found the following boxes:");
				boxAgents = new AID[result.length];
				for (int i = 0; i < result.length; ++i) {
					boxAgents[i] = result[i].getName();
					//System.out.println(boxAgents[i].getName());
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			
			myAgent.addBehaviour(new RequestBoxRoutePerformer(boxTypes, reply));

			return ret;
		}
		
		
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getConversationId().equals("route_req")) {
					// CFP Message received. Process it				
					String title = msg.getContent();
					ACLMessage reply = msg.createReply();
					//System.out.println("Moveragent: " + myAgent.getName() + " recieved: " + title );
					goalMessage = msg;
					goalAgent = msg.getSender();
					//System.out.println(getName() + " recieved request for finding boxroute");
					calcMoverRoutes(title, reply);
				}
				else if(msg.getConversationId().equals("move_req")){
					String content = msg.getContent();
					if(content.length() > 0){
						queue += content;
						calcEndPos(content);
						
					}
				}
				block();
			}
			else
				block();
			
		}
	}  // End of inner class OfferRequestsServer
	
	private void calcEndPos(String route){
		for(int i = 0; i < route.length(); i++){
			switch(route.charAt(i)){
			case 'u':
			case 'U':
				endPos[0]--;
				break;
			case 'd':
			case 'D':
				endPos[0]++;
				break;
			case 'l':
			case 'L':
				endPos[1]--;
				break;
			case 'r':
			case 'R':
				endPos[1]++;
				break;

			}
		}
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("BoxAgent " + getAID().getName() + " terminating.");
	}
}

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

package goal;

import java.security.SecureRandom;
import java.util.Random;

import tools.Astar;

import map.Map;
import jade.content.onto.basic.Result;
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

public class GoalAgent extends Agent {

	private int id;
	private AID mapAgent;
	private Map map;
	private AID[] moverAgents;
	private AID[] boxAgents;
	int step = 0;
	int[] sequence = {1,4,3,2,1,2,3,4};
	int sequenceCounter = 0;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! BoxAgent " + getAID().getName() + " is running.");
		
		

		// Loading arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			id = Integer.parseInt(args[0].toString());
			
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("goal-agent");
			sd.setName("Chokoban");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			addBehaviour(new MapSubscriber());
			
			addBehaviour(new TickerBehaviour(this, 2000) {
				
				@Override
				protected void onTick() {
					
					// Find moverAgents in the map
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("mover-agent");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
							if(result.length > 0){
								moverAgents = new AID[result.length];
								for (int i = 0; i < result.length; ++i) {
									moverAgents[i] = result[i].getName();
								}
						}
						else
							System.out.println("FAILED - Found no moverAgents");
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
					step = 0;

					addBehaviour(new RequestBoxMovingPerformer());
					
				}
			});
			
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
							System.out.println("FAILED - Found no MapAgent");
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
	
	
	private class RequestBoxMovingPerformer extends Behaviour{

		private static final long serialVersionUID = 1L;
		int stepz = 0;
		MessageTemplate mt; // The template to receive replies
		int replyCnt = 0;
		String bestRoute = "";
		AID moverWithBestRoute;
		String boxWithBestRoute;
		
		public void walkRoute(String route, String mover)
		{			
			//System.out.println("Walk route: " + route);

			for (int i = 0; i<route.length(); i++)
			{
				ACLMessage order = new ACLMessage(ACLMessage.INFORM);
				order.addReceiver(mapAgent);
				//System.out.println("Trying to walk with mover: " + mover.substring(10,12));
				order.setContent(route.substring(i, i+1) + mover.substring(10,12) );
				//System.out.println(route.substring(i, i+1));
				order.setConversationId("map_update");
				order.setReplyWith("map_update"+System.currentTimeMillis());
				myAgent.send(order);
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void action() {
			//Random random = new Random();
			//SecureRandom random = new SecureRandom();
			//Integer randomInt = random.nextInt(4) + 1;
			Integer randomInt = sequence[sequenceCounter];
			switch (stepz) {
			case 0:
				System.out.println(randomInt);
				// Send the cfp to all boxes
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < moverAgents.length; ++i) {
					cfp.addReceiver(moverAgents[i]);
				} 
				cfp.setContent(randomInt.toString());
				cfp.setConversationId("route_req");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("route_req"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				replyCnt = 0;
				stepz = 1;
				bestRoute = "";
				
			case 1:
				mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage reply1 = myAgent.receive(mt);
				if (reply1 != null) {
					if (reply1.getConversationId().equals("route_req"))
					{
						if (reply1.getPerformative() == ACLMessage.CFP && !reply1.getContent().equals("-")) {
							// This is an offer 
							//int price = reply.getContent().length();
							//System.out.println("Content: " + reply1.getContent() + " Recieved from: " + reply1.getSender().getName());
							//walkRoute(reply1.getContent());
							replyCnt++;
							
							 /* String to split. */
							  String str = reply1.getContent();
							  String[] temp;
							 
							  /* delimiter */
							  String delimiter = "-";
							  /* given string will be split by the argument delimiter provided. */
							  temp = str.split(delimiter);
							
							if(bestRoute.equals("") || bestRoute.length() > reply1.getContent().length()){
								bestRoute = temp[0];
								moverWithBestRoute = reply1.getSender();
								boxWithBestRoute = temp[1];
							}
								
						}
					}
				}
				
				if(moverAgents.length <= replyCnt){
					String lastMove = bestRoute.substring(bestRoute.length()-1, bestRoute.length());
					switch (lastMove) {
					case "u":
					case "U":
						bestRoute += "D";
						break;
						
					case "d":
					case "D":
						bestRoute += "U";
						break;
						
					case "r":
					case "R":
						bestRoute += "L";
						break;
						
					case "l":
					case "L":
						bestRoute += "R";
						break;

					default:
						break;
					}
					stepz = 2;
					System.out.println("Best route: " + bestRoute + " Recieved from: " + moverWithBestRoute);
					System.out.println(boxWithBestRoute);
					int[] box = findBox(map.map, Integer.parseInt(boxWithBestRoute.substring(8,12)));
					int[] goal = findGoal(map.map);
					//bestRoute += Astar.calcRoute(map.map, box[0], box[1], goal[0], goal[1]);
					
					// Find boxagent that has to be terminated
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("box-route");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						if (result.length > 0){
							for(int i = 0; i < result.length ; i++){
							//	System.out.println("Trrying to match: " + boxWithBestRoute + " With: " + result[i].getName());
								if(result[i].getName().equals(boxWithBestRoute)){
									ACLMessage mes = new ACLMessage(ACLMessage.CANCEL);
									mes.addReceiver(result[i].getName());
									mes.setContent("terminate");
									myAgent.send(mes);

								}
							}
							
						}
						else
							System.out.println("FAILED - Found no box agents");
						//System.out.println("mapAgent: " + mapAgent.getName());
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
					
					walkRoute(bestRoute, moverWithBestRoute.getName());
					sequenceCounter++;
				}
				
				break;
			}
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	int[] findGoal(int[][] map){
		int[] ret = new int[2];
		
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if(map[i][j] >= 100 && map[i][j] < 1000){
					ret[0] = i;
					ret[1] = j;
				}
			}
		}
		
		return ret;
	}
	
	int[] findBox(int[][] map, int box){
		int[] ret = new int[2];
		
		for(int i = 0; i < map.length ; i++)
			for(int j = 0 ; j < map[0].length; j++)
				if(map[i][j] == box){
					ret[0] = i;
					ret[1] = j;
				}
		return ret;
	}


	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("BoxAgent " + getAID().getName() + " terminating.");
	}
}

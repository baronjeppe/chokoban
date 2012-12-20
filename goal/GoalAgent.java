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
	int ticks = 0;

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
			
			addBehaviour(new TickerBehaviour(this, 1500) {				
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void onTick() {
					ticks++;
					
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

					if(ticks > 5)
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
		String bestReply = "";
		ACLMessage moverReply;
		AID moverWithBestRoute;
		String boxWithBestRoute;
		
		

		@Override
		public void action() {
			Random random = new Random();
			//SecureRandom random = new SecureRandom();
			Integer randomInt = random.nextInt(4) + 1;
			//Integer randomInt = sequence[sequenceCounter];
			
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
			
			if(boxAgents.length == 0){
				System.out.println("Goal agent terminating, due to no more boxes");
				doDelete();
			}
			
			boolean foundBox = false;
			
			while(!foundBox){
				randomInt = random.nextInt(4)+1;
				for(int i = 0; i < boxAgents.length; i++){
					if(Integer.parseInt(boxAgents[i].getName().substring(8,9)) == randomInt)
						foundBox = true;
				}
			}
			
			if(true){
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
							if (reply1.getPerformative() == ACLMessage.CFP) {
								replyCnt++;
								System.out.println("Sender: " + reply1.getSender().getName() + " send route: " + reply1.getContent());
								if(!reply1.getContent().equals("-")){
									// This is an offer 
									//int price = reply.getContent().length();
									//System.out.println("Content: " + reply1.getContent() + " Recieved from: " + reply1.getSender().getName());
									//walkRoute(reply1.getContent());
									
									 /* String to split. */
									  String str = reply1.getContent();
									  String[] temp;
									 
									  /* delimiter */
									  String delimiter = "-";
									  /* given string will be split by the argument delimiter provided. */
									  temp = str.split(delimiter);
									  
									
									if(bestRoute.equals("") || bestRoute.length() > (temp[1]).length()+1){
										bestRoute = temp[0] + "-" + temp[1];
										moverWithBestRoute = reply1.getSender();
										boxWithBestRoute = temp[2];
										bestReply = temp[1];
										//System.out.println(temp[2]);
										moverReply = reply1.createReply();
									}
								}
							}
						}
					}
					
					if(moverAgents.length <= replyCnt) {
						if(!bestRoute.equals("")){
							char lastMove = bestRoute.charAt(bestRoute.length()-1);
							switch (lastMove) {
							case 'u':
							case 'U':
								bestReply += "D";
								break;
								
							case 'd':
							case 'D':
								bestReply += "U";
								break;
								
							case 'r':
							case 'R':
								bestReply += "L";
								break;
								
							case 'l':
							case 'L':
								bestReply += "R";
								break;
		
							default:
								break;
							}
							stepz = 2;
							System.out.println("Best route: " + bestRoute + " Recieved from: " + moverWithBestRoute);
							System.out.println(boxWithBestRoute);
							//int[] box = findBox(map.map, Integer.parseInt(boxWithBestRoute.substring(8,12)));
							//int[] goal = findGoal(map.map);
							//bestRoute += Astar.calcRoute(map.map, box[0], box[1], goal[0], goal[1]);
							
							//Send reply to mover that it has to move
							moverReply.setConversationId("move_req");
							template = new DFAgentDescription();
							moverReply.setContent(bestReply);
							myAgent.send(moverReply);
							moverReply.removeReceiver(moverWithBestRoute);
							
							for(int i = 0; i < moverAgents.length; i++){
								if(!moverAgents[i].equals(moverWithBestRoute))
									moverReply.addReceiver(moverAgents[i]);
							}
							moverReply.setContent("");
							myAgent.send(moverReply);
							
							// Find boxagent that has to be terminated
							sd = new ServiceDescription();
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
							
							//walkRoute(bestRoute, moverWithBestRoute.getName());
							sequenceCounter++;
						}
						break;
						
						
					}
				}
			}
			else	{
				System.out.println("Asked for nonexisting box");
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

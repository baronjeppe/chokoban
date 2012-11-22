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

public class GoalAgent extends Agent {

	private int id;
	private AID mapAgent;
	private Map map;
	private AID[] moverAgents;
	int step = 0;

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
			
			addBehaviour(new TickerBehaviour(this, 5000) {
				
				@Override
				protected void onTick() {
					
					
					
					// Find moverAgents in the map
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("mover-agent");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						moverAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							moverAgents[i] = result[i].getName();
						}
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
		
		public void walkRoute(String route)
		{			
			for (int i = 0; i<route.length(); i++)
			{
				ACLMessage order = new ACLMessage(ACLMessage.INFORM);
				order.addReceiver(mapAgent);
				order.setContent(route.substring(i, i+1));
				System.out.println(route.substring(i, i+1));
				order.setConversationId("map_update");
				order.setReplyWith("map_update"+System.currentTimeMillis());
				myAgent.send(order);
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void action() {
			Random random = new Random();
			Integer randomInt = random.nextInt(4) + 1;
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
							System.out.println("Content: " + reply1.getContent() + " Recieved from: " + reply1.getSender().getName());
							//walkRoute(reply1.getContent());
							replyCnt++;
							
							if(bestRoute.equals("") || bestRoute.length() > reply1.getContent().length()){
								bestRoute = reply1.getContent();
							}
								
						}
					}
				}
				
				if(moverAgents.length <= replyCnt){
					stepz = 2;
					System.out.println(bestRoute);
					walkRoute(bestRoute);

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


	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("BoxAgent " + getAID().getName() + " terminating.");
	}
}

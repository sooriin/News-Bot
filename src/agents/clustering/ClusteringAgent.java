package agents.clustering;

import java.io.IOException;
import java.util.HashMap;
import agents.AgentsEnums.MessageKey;
import agents.AgentsEnums.MessageValue;
import agents.conversation.ConversationAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * Agent used for clustering news articles.
 * */
public class ClusteringAgent extends Agent
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void setup()
	{
		addBehaviour(new ClusteringAgentBehaviour());
	}
	
	/**
	 * Sends a response message to the conversation agent, containing the results from clustering an article.
	 * @param clusterResult The result of performing the clustering.
	 */
	void respondToClusterRequest(String clusterResult)
	{
		HashMap<MessageKey, Object> aclContent = new HashMap<MessageKey, Object>();

		aclContent.put(MessageKey.INTENT, MessageValue.RESPONSE_CLUSTER_ARTICLE);
		aclContent.put(MessageKey.INTENT_DATA, clusterResult);

		ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);

		aclMessage.addReceiver(new AID(ConversationAgent.class.getSimpleName(), AID.ISLOCALNAME));
		
		try
		{
			aclMessage.setContentObject(aclContent);
			send(aclMessage);
		}
		catch(IOException e)
		{
			//If the message cannot be sent to the conversation agent, there's not much that can
			//be done at this point; just print the trace.
			e.printStackTrace();
		}
	}
}
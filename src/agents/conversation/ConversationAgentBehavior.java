package agents.conversation;

import java.util.HashMap;
import agents.AgentsEnums.MessageKey;
import agents.AgentsEnums.MessageValue;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The behavior class for the Conversation Agent. It receives ACL messages and reacts accordingly.
 * @author Sorin
 *
 */
class ConversationAgentBehavior extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	public void action()
	{
		final ACLMessage message = getAgent().receive();
		final ConversationAgent agent = ((ConversationAgent)getAgent());
		
		if(message != null)
		{
			try
			{
				HashMap<MessageKey, Object> messageMap = (HashMap<MessageKey, Object>)message.getContentObject();
				MessageValue intent = (MessageValue)messageMap.get(MessageKey.INTENT);
				
				if(intent != null)
				{
					switch(intent)
					{
					case RESPONSE_CLUSTER_ARTICLE:
					case RESPONSE_EXTRACT_FROM_ARTICLE:
					case RESPONSE_GET:
					{
						final String data = (String)messageMap.get(MessageKey.INTENT_DATA);
						
						agent.sendTelegramMessage(data);
						break;
					}
					default:
						//TODO
						break;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
package agents.informationextractor;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The behavior class for the Information Extraction Agent. It receives ACL messages and reacts accordingly.
 * @author Sorin
 *
 */
class InformationExtractorBehavior extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;

	@Override
	public void action()
	{
		ACLMessage message = getAgent().receive();
		
		if(message != null)
		{
			//TODO
		}
	}
}
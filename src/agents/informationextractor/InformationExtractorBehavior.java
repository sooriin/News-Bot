package agents.informationextractor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import agents.AgentsEnums.MessageKey;
import agents.AgentsEnums.MessageValue;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
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
	private static final String EXTRACT_EXCEPTION = "It seems that I was not able to perform this action.";
	private static final String EXTRACTION_FAILURE = "I could not extract any information from the article.";
	private static final String EXTRACTION_SUCCESS = "Information extraction completed.\nWhat information would you like to know about it?";
	private static final String NO_ARTICLE_PROCESSED = "I haven't been given an article to process yet.";
	private static final String GET_FAIL = "I could not find any information about that in the last processed article.";
	private static final String GET_SUCCESS = "Here is what I could find from the last processed article...\n\n";
	
	//Strings with the names of the relevant annotations.
	private static final String ANNOTATION_PERSON = "Person", ANNOTATION_DATE = "Date", ANNOTATION_LOCATION = "Location",
			ANNOTATION_ORGANIZATION = "Organization", ANNOTATION_MONEY = "Money", ANNOTATION_PERCENT = "Percent";
	
	//A list with the names of the relevant annotations.
	private static final List<String> ALLOWED_ANNOTATIONS = Arrays.asList(ANNOTATION_PERSON, ANNOTATION_DATE, ANNOTATION_LOCATION,
			ANNOTATION_ORGANIZATION, ANNOTATION_MONEY, ANNOTATION_PERCENT);
	
	//A hash map holding annotation names for each MessageValue requesting an annotation.
	private static final HashMap<MessageValue, String> INTENT_VALUE_TO_ANNOTATION;
	
	static
	{
		INTENT_VALUE_TO_ANNOTATION = new HashMap<MessageValue, String>();
		
		INTENT_VALUE_TO_ANNOTATION.put(MessageValue.GET_EXTRACTED_PERSON, ANNOTATION_PERSON);
		INTENT_VALUE_TO_ANNOTATION.put(MessageValue.GET_EXTRACTED_DATE, ANNOTATION_DATE);
		INTENT_VALUE_TO_ANNOTATION.put(MessageValue.GET_EXTRACTED_LOCATION, ANNOTATION_LOCATION);
		INTENT_VALUE_TO_ANNOTATION.put(MessageValue.GET_EXTRACTED_ORGANIZATION, ANNOTATION_ORGANIZATION);
		INTENT_VALUE_TO_ANNOTATION.put(MessageValue.GET_EXTRACTED_MONEY, ANNOTATION_MONEY);
		INTENT_VALUE_TO_ANNOTATION.put(MessageValue.GET_EXTRACTED_PERCENT, ANNOTATION_PERCENT);
	}
	
	//A hash map holding all the values of each relevant annotation.
	private HashMap<String, HashSet<String>> _annotations;
	
	//Handle to GATE used for extracting information from articles.
	private final GATEHandle GATE_HANDLE;
	
	private boolean _articleProcessed;
	
	public InformationExtractorBehavior()
	{
		GATE_HANDLE = new GATEHandle();
		_articleProcessed = false;
		
		initializeAnnotationsMap();
	}

	private void initializeAnnotationsMap()
	{
		_annotations = new HashMap<>();
		
		_annotations.put(ANNOTATION_PERSON, new HashSet<String>());
		_annotations.put(ANNOTATION_DATE, new HashSet<String>());
		_annotations.put(ANNOTATION_LOCATION, new HashSet<String>());
		_annotations.put(ANNOTATION_ORGANIZATION, new HashSet<String>());
		_annotations.put(ANNOTATION_MONEY, new HashSet<String>());
		_annotations.put(ANNOTATION_PERCENT, new HashSet<String>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void action()
	{
		final ACLMessage message = getAgent().receive();
		final InformationExtractorAgent agent = ((InformationExtractorAgent)getAgent());
		
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
					case EXTRACT_FROM_ARTICLE:
					{
						String article = (String)messageMap.get(MessageKey.INTENT_DATA);
						String extractionResult = performExtraction(article);
						
						agent.respondToRequest(MessageValue.RESPONSE_EXTRACT_FROM_ARTICLE, extractionResult);
						break;
					}
					case GET_EXTRACTED_PERSON:
					case GET_EXTRACTED_DATE:
					case GET_EXTRACTED_LOCATION:
					case GET_EXTRACTED_ORGANIZATION:
					case GET_EXTRACTED_MONEY:
					case GET_EXTRACTED_PERCENT:
					{
						String result = getAnnotations(intent);
						
						agent.respondToRequest(MessageValue.RESPONSE_GET, result);
						
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
				agent.respondToRequest(MessageValue.RESPONSE_EXTRACT_FROM_ARTICLE, EXTRACT_EXCEPTION);
			}
		}
	}

	/**
	 * Gets the annotations for a specific intent requesting an annotation.
	 * @param mv The intent value requesting a specific annotation.
	 * @return The string with the annotations, or an error string.
	 */
	private String getAnnotations(MessageValue mv)
	{
		String result = null;
		
		if(_articleProcessed)
		{
			final String setName = INTENT_VALUE_TO_ANNOTATION.get(mv);
			
			if(setName != null)
			{
				final String annotations = getDataFromSet(setName);
				
				if(annotations != null)
				{
					result = GET_SUCCESS + annotations;
				}
				else
				{
					result = GET_FAIL;
				}
			}
			else
			{
				result = GET_FAIL;
			}
		}
		else
		{
			result = NO_ARTICLE_PROCESSED;
		}
		
		return result;
	}

	/**
	 * Obtains relevant information from a news article and stores it in memory.
	 * @param article A news article's text.
	 * @return A string with the result of performing the extraction: Either indicating success, or failure.
	 */
	private String performExtraction(String article)
	{
		String ret = EXTRACTION_FAILURE;
		AnnotationSet annotations = GATE_HANDLE.getAnnotations(article);
		
		_articleProcessed = false;
		
		try
		{
			if(annotations != null && !annotations.isEmpty())
			{
				initializeAnnotationsMap(); //Create a new map for the incoming annotations.
				
				ret = EXTRACTION_SUCCESS;

				Document doc = Factory.newDocument(article);
				
				//Loop through each annotation in the document and add its value to the correct set.
				for(Annotation annotation : annotations)
				{
					final String type = annotation.getType();
					
					//Only focus on relevant annotations.
					if(ALLOWED_ANNOTATIONS.contains(type))
					{
						final String annotationValue = gate.Utils.stringFor(doc, annotation); 
						
						_annotations.get(type).add(annotationValue);
					}
				}
				
				_articleProcessed = true;
			}
		}
		catch(Exception e)
		{
			ret = EXTRACTION_FAILURE;
			_articleProcessed = false;
		}
		
		return ret;
	}
	
	/**
	 * Generates a string with all the values from an annotations set contained in annotations.
	 * @param setName The name of the set from which to get the annotations.
	 * @return A string with all the values in the set with name setName, or null on error.
	 */
	private String getDataFromSet(String setName)
	{
		String data = null;
		HashSet<String> set = _annotations.get(setName);
		
		if(set != null && !set.isEmpty())
		{
			data = "";
			
			for(String s : set)
			{
				data += s + "\n";
			}
		}
		
		return data;
	}
}
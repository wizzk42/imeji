package de.mpg.imeji.metadata.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.richfaces.json.JSONCollection;
import org.richfaces.json.JSONException;

import de.mpg.jena.vo.MetadataProfile;
import de.mpg.jena.vo.Statement;

import thewebsemantic.LocalizedString;

public class SuggestBean
{
	private Map<URI, Suggest> suggests = null;

	public void init(MetadataProfile profile)
	{
		suggests = new HashMap<URI,Suggest>();
		
		for (Statement s : profile.getStatements())
		{
			suggests.put(s.getName(), new Suggest(s));
		}
	}

	public Map<URI, Suggest> getSuggests() {
		return suggests;
	}
	
	public void setSuggests(Map<URI, Suggest> suggests) {
		this.suggests = suggests;
	}

	public class Suggest
	{
		private Statement statement = null;
		
		public Suggest(Statement s) 
		{
			statement = s;
		}
		
		public List<Object> autoComplete(Object suggest)
	    {
	        if (statement.getLiteralConstraints() != null && statement.getLiteralConstraints().size() > 0)
	        {
	            List<String> suggestions = new ArrayList<String>();
	            List<String> literals = new ArrayList<String>();
	            for (LocalizedString str : statement.getLiteralConstraints())
	                literals.add(str.toString());
	            for (String str : literals)
	                if (str.toLowerCase().contains(suggest.toString().toLowerCase()))
	                    suggestions.add(str);
	            String json = "[";
	            for (String str : suggestions)
	                json += "{\"http_purl_org_dc_elements_1_1_title\" : \"" + str + "\"},";
	            json = json.substring(0, json.length() - 1) + "]";
	            JSONCollection jsc;
	            try
	            {
	                jsc = new JSONCollection(json);
	            }
	            catch (JSONException e)
	            {
	                return null;
	            }
	            return Arrays.asList(jsc.toArray());
	        }
	        else if (statement.getVocabulary() != null)
	        {
	            if (suggest.toString().isEmpty())
	                suggest = "a";
	            if (!suggest.toString().isEmpty())
	            {
	                try
	                {
	                    HttpClient client = new HttpClient();
	                    GetMethod getMethod = new GetMethod(statement.getVocabulary().toString()
	                            + "?format=json&n=10&m=full&q=" + suggest);
	                    client.executeMethod(getMethod);
	                    JSONCollection jsc = new JSONCollection(getMethod.getResponseBodyAsString());
	                    return Arrays.asList(jsc.toArray());
	                }
	                catch (Exception e)
	                {
	                    throw new RuntimeException(e);
	                }
	            }
	        }
	        return null;
	    }
		
		public boolean getDoAutoComplete()
		{
			if (statement != null) 
	        {
	        	 if (statement.getLiteralConstraints() != null && statement.getLiteralConstraints().size() > 0)
	        	 {
	        		 return true;
	        	 }
	             if (statement.getVocabulary() != null)
	             {
	            	 return true;
	             }
	        }
	        return false;
		}
	}
}
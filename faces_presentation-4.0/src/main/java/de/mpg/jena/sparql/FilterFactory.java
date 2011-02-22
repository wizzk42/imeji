package de.mpg.jena.sparql;

import java.util.List;
import java.util.Map;

import de.mpg.jena.controller.SearchCriterion;
import de.mpg.jena.controller.SearchCriterion.Filtertype;
import de.mpg.jena.controller.SearchCriterion.ImejiNamespaces;
import de.mpg.jena.vo.Grant;
import de.mpg.jena.vo.Grant.GrantType;
import de.mpg.jena.vo.User;

public class FilterFactory 
{
	public static String getFilter(List<SearchCriterion> scList, Map<String, QueryElement> els, User user, String specificFilter)
	{
		String sf = generateSearchFilters(scList, els);
		String uf = generateUserFilters(user);
		
		String filter = " .FILTER( " + uf + " ) ";
		if (!" ( ) ".equals(sf))
		{
			filter += "  .FILTER( " + sf + " )";
		}
		if (!"".equals(specificFilter))
		{
			filter += " .FILTER( " + specificFilter + " )";
		}
		
		return filter;
	}
	
	private static String generateSearchFilters(List<SearchCriterion> scList, Map<String, QueryElement> els)
	{
		String filter =" (";
		
		for(SearchCriterion sc : scList)
		{
			if (!sc.getChildren().isEmpty())
			{
				filter += generateSearchFilters(sc.getChildren(), els);
			}
			String newFilter = getFilterString(sc, els.get(sc.getNamespace().getNs()).getName());
			if (!" (".equals(filter) && !"".equals(newFilter))
			{
				filter += getOperatorString(sc);
			}
			filter += newFilter;
		}
		
		filter += " ) ";
		
		return filter;
	}
	
	private static String generateUserFilters(User user)
	{
		String f = "?visibility = <http://imeji.mpdl.mpg.de/image/visibility/PUBLIC>";
		
		if (user != null && user.getGrants() != null && !user.getGrants().isEmpty())
		{
			for (Grant g : user.getGrants())
			{
				if (	GrantType.CONTAINER_ADMIN.equals(g.getGrantType())
					|| 	GrantType.CONTAINER_EDITOR.equals(g.getGrantType())
					||	GrantType.PRIVILEGED_VIEWER.equals(g.getGrantType()))
				f += " || ?coll=<" + g.getGrantFor() + ">";
			}
		}
		
		return f;
	}
	
	private static String getOperatorString(SearchCriterion sc)
	{
		if (sc.getOperator().equals(SearchCriterion.Operator.AND))
		{
			return " && ";
		}
        else if (sc.getOperator().equals(SearchCriterion.Operator.OR))
        {
        	return " || ";
        }
        else if (sc.getOperator().equals(SearchCriterion.Operator.MINUS))
        {
        	return " NOT EXISTS ";
        }
		return " && ";
	}
	
	
	private static String getFilterString(SearchCriterion sc, String variable)
	{
		String filter ="";
		variable = "?" + variable;
		
		if (sc.getValue() != null)
        {
			if (sc.getFilterType().equals(Filtertype.URI) && !sc.getNamespace().equals(ImejiNamespaces.ID_URI))
            {
                filter += "str(" + variable + ")='" + sc.getValue() + "'";
            }
            else if (sc.getFilterType().equals(Filtertype.URI) && sc.getNamespace().equals(ImejiNamespaces.ID_URI))
            {
                filter += "?s" + "=<" + sc.getValue() + ">";
            }
            else if (sc.getFilterType().equals(Filtertype.REGEX))
            {
                filter += "regex(" + variable + ", '"  + sc.getValue() + "', 'i')";
            }
            else if (sc.getFilterType().equals(Filtertype.EQUALS))
            {
                filter += variable + "='" + sc.getValue()+ "'";
            }
            else if (sc.getFilterType().equals(Filtertype.BOUND))
            {
                filter += "bound(" + variable + ")=" + sc.getValue() + "";
            }
            else if (sc.getFilterType().equals(Filtertype.EQUALS_NUMBER))
            {
            	try{ Double d = Double.valueOf(sc.getValue());
            	filter += variable + "='" + d + "'^^<http://www.w3.org/2001/XMLSchema#double>";}
            	catch (Exception e) {/* Not a double*/}
            }
            else if (sc.getFilterType().equals(Filtertype.GREATER_NUMBER))
            {
            	try{ Double d = Double.valueOf(sc.getValue());
            	filter += variable + ">='" + d + "'^^<http://www.w3.org/2001/XMLSchema#double>";}
            	catch (Exception e) {/* Not a double*/}
            }
            else if (sc.getFilterType().equals(Filtertype.LESSER_NUMBER))
            {
            	try{ Double d = Double.valueOf(sc.getValue());
            	filter += variable + "<='" + d + "'^^<http://www.w3.org/2001/XMLSchema#double>";}
            	catch (Exception e) {/* Not a double*/}
            }
            else if (sc.getFilterType().equals(Filtertype.EQUALS_DATE))
            {
                filter += variable + "='" + sc.getValue() + "'^^<http://www.w3.org/2001/XMLSchema#dateTime>";
            }
            else if (sc.getFilterType().equals(Filtertype.GREATER_DATE))
            {
                filter += variable + ">='" + sc.getValue() + "'^^<http://www.w3.org/2001/XMLSchema#dateTime>";
            }
            else if (sc.getFilterType().equals(Filtertype.LESSER_DATE))
            {
                filter += variable + "<='" + sc.getValue() + "'^^<http://www.w3.org/2001/XMLSchema#dateTime>";
            }
        }
		return filter;
	}
}
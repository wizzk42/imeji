package de.mpg.jena.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import de.mpg.escidoc.services.framework.PropertyReader;
import de.mpg.escidoc.services.framework.ServiceLocator;
import de.mpg.imeji.collection.CollectionBean;
import de.mpg.imeji.util.LoginHelper;
import de.mpg.jena.ImejiBean2RDF;
import de.mpg.jena.ImejiJena;
import de.mpg.jena.ImejiRDF2Bean;
import de.mpg.jena.sparql.ImejiSPARQL;
import de.mpg.jena.sparql.QuerySPARQL;
import de.mpg.jena.sparql.query.QuerySPARQLImpl;
import de.mpg.jena.util.MetadataFactory;
import de.mpg.jena.util.ObjectHelper;
import de.mpg.jena.vo.CollectionImeji;
import de.mpg.jena.vo.Grant;
import de.mpg.jena.vo.Image;
import de.mpg.jena.vo.ImageMetadata;
import de.mpg.jena.vo.Image.Visibility;
import de.mpg.jena.vo.Properties.Status;
import de.mpg.jena.vo.User;

public class ImageController extends ImejiController
{
    private String additionalQuery = "";
    private static Logger logger = null;

    private static ImejiRDF2Bean imejiRDF2Bean = new ImejiRDF2Bean(ImejiJena.imageModel);
	private static ImejiBean2RDF imejiBean2RDF = new ImejiBean2RDF(ImejiJena.imageModel);
    
    public ImageController(User user)
    {
        super(user);
        logger = Logger.getLogger(ImageController.class);
    }
   
    public void create(Image img, URI coll) throws Exception
    {
        CollectionController cc = new CollectionController(user);
        CollectionImeji ic = cc.retrieve(coll);
    	writeCreateProperties(img.getProperties(), user);
    	if (Status.PENDING.equals(ic.getProperties().getStatus())) img.setVisibility(Visibility.PRIVATE);
    	else img.setVisibility(Visibility.PUBLIC);
        img.setCollection(coll);
        img.setId(ObjectHelper.getURI(Image.class, Integer.toString(getUniqueId())));
        img.getMetadataSet().setProfile(ic.getProfile());
        imejiBean2RDF = new ImejiBean2RDF(ImejiJena.imageModel);
        imejiBean2RDF.create(img, user);
        ic.getImages().add(img.getId());
        cc.update(ic);
        cleanGraph(ImejiJena.imageModel);
    }

    public void create(Collection<Image> images, URI coll) throws Exception
    {
    	CollectionController cc = new CollectionController(user);
    	CollectionImeji ic = cc.retrieve(coll);
        imejiBean2RDF = new ImejiBean2RDF(ImejiJena.imageModel);
        
        for (Image img : images)
        {
        	 writeCreateProperties(img.getProperties(), user);
        	 if (Status.PENDING.equals(ic.getProperties().getStatus())) img.setVisibility(Visibility.PRIVATE);
        	 else img.setVisibility(Visibility.PUBLIC);
             img.setCollection(coll);
             img.setId(ObjectHelper.getURI(Image.class, Integer.toString(getUniqueId())));
             img.getMetadataSet().setProfile(ic.getProfile());
             imejiBean2RDF.create(img, user);
             ic.getImages().add(img.getId());
        }
        cc.update(ic);
        cleanGraph(ImejiJena.imageModel);
    }
    

    public void update(Image img) throws Exception
    {
    	imejiBean2RDF = new ImejiBean2RDF(ImejiJena.imageModel); 
    	imejiBean2RDF.saveDeep(img, user);
    	cleanGraph(ImejiJena.imageModel);
    }

    public void update(Collection<Image> images) throws Exception
    {
    	imejiBean2RDF = new ImejiBean2RDF(ImejiJena.imageModel); 
    	for (Image img : images)
        {
    		for(int i=0; i< img.getMetadataSet().getMetadata().size(); i++)
    		{
    			((List<ImageMetadata>)img.getMetadataSet().getMetadata()).set(i, MetadataFactory.newMetadata(((List<ImageMetadata>)img.getMetadataSet().getMetadata()).get(i)));
    		}
    		imejiBean2RDF.saveDeep(img, user);
        }
    	cleanGraph(ImejiJena.imageModel);
    }
    
    public Image retrieve(URI imgUri)
    {
    	imejiRDF2Bean = new ImejiRDF2Bean(ImejiJena.imageModel); 
    	return (Image)imejiRDF2Bean.load(imgUri.toString(), user);
    }

    public Image retrieve(String id)
    {
    	imejiRDF2Bean = new ImejiRDF2Bean(ImejiJena.imageModel);
    	return (Image)imejiRDF2Bean.load(ObjectHelper.getURI(Image.class, id).toString(), user);
    }
    
    public Collection<Image> retrieveAll()
    {
    	imejiRDF2Bean = new ImejiRDF2Bean(ImejiJena.imageModel);
    	return imejiRDF2Bean.load(Image.class);
    }
    
    /**
     * NOT WORKING
     * @param uri
     */
    public void getGraph(URI uri)
    {
    	additionalQuery = " . <" + uri.toString() + "> <http://imeji.mpdl.mpg.de/image/metadata> ?md . ?md <http://www.w3.org/2000/01/rdf-schema#member> ?list . ?list <http://purl.org/dc/terms/type> ?type";
    	QuerySPARQL querySPARQL = new QuerySPARQLImpl();
        String query = querySPARQL.createConstructQuery(new ArrayList<SearchCriterion>(), null,	"http://imeji.mpdl.mpg.de/image", additionalQuery , "?s=<http://imeji.mpdl.mpg.de/image/111>", 1, 0, user);
    	ImejiSPARQL.execConstruct(query).write(System.out, "RDF/XML-ABBREV");
    }
    
    /**
     * Get the number of all images
     * @return
     */
    public int allImagesSize()
    {
    	return ImejiSPARQL.execCount("SELECT ?s count(DISTINCT ?s) WHERE { ?s a <http://imeji.mpdl.mpg.de/image>}");
    }
    
    public int getNumberOfResults(List<SearchCriterion> scList) throws Exception
    {
    	additionalQuery = "";
        QuerySPARQL querySPARQL = new QuerySPARQLImpl();
        String query = querySPARQL.createCountQuery(scList, null, "http://imeji.mpdl.mpg.de/image", "", "", -1, 0, user);
    	return ImejiSPARQL.execCount(query);
    }

    public Collection<Image> search(List<SearchCriterion> scList, SortCriterion sortCri, int limit, int offset)
            throws Exception
    {
        additionalQuery = "";
        QuerySPARQL querySPARQL = new QuerySPARQLImpl();
        String query = querySPARQL.createQuery(scList, sortCri,	"http://imeji.mpdl.mpg.de/image", additionalQuery, "", limit, offset, user);
        return  ImejiSPARQL.execAndLoad(query, Image.class);
    }
    public Collection<Image> search(List<SearchCriterion> scList, SortCriterion sortCri, int limit, int offset, List<String> uris) throws Exception
	{
		additionalQuery = "";
		String additionalFilter ="";
		for (String uri : uris)
		{
			additionalFilter += " || ?s=<" + uri + ">";
		}
		QuerySPARQL querySPARQL = new QuerySPARQLImpl();
		String query = querySPARQL.createQuery(scList, sortCri,	"http://imeji.mpdl.mpg.de/image", additionalQuery, additionalFilter, limit, offset, user);
		return  ImejiSPARQL.execAndLoad(query, Image.class);
	}
    

    public int getNumberOfResultsInContainer(URI containerUri, List<SearchCriterion> scList) throws Exception
    {
    	additionalQuery = " . <" + containerUri.toString() + "> <http://imeji.mpdl.mpg.de/images> ?s";
    	QuerySPARQL querySPARQL = new QuerySPARQLImpl();
        String query = querySPARQL.createCountQuery(scList, null,	"http://imeji.mpdl.mpg.de/image", additionalQuery, "", -1, 0, user);
    	return ImejiSPARQL.execCount(query);
    }
    

    public Collection<Image> searchImageInContainer(URI containerUri, List<SearchCriterion> scList,
            SortCriterion sortCri, int limit, int offset) throws Exception
    {
        additionalQuery = " . <" + containerUri.toString() + "> <http://imeji.mpdl.mpg.de/images> ?s";
        QuerySPARQL querySPARQL = new QuerySPARQLImpl();
        String query = querySPARQL.createQuery(scList, sortCri,	"http://imeji.mpdl.mpg.de/image", additionalQuery, "", limit, offset, user);
        return ImejiSPARQL.execAndLoad(query, Image.class);
    }
    
    public void delete(Image img, User user) throws Exception
    {
    	if (img != null)
    	{
	    	String itemId = img.getEscidocId();
	    	imejiBean2RDF = new ImejiBean2RDF(ImejiJena.imageModel);
			imejiBean2RDF.delete(img, user);
    	
			try
		    {
		        ServiceLocator.getItemHandler(getEscidocUserHandle()).delete(itemId);
		    }
		    catch (Exception e)
		    {
		        logger.warn("Error deleting image in escidoc: ", e);
		    }
    	}
    }

    public void release(Image img) throws Exception
    {
    	if (Status.PENDING.equals(img.getProperties().getStatus()))
    	{
    		img.getProperties().setStatus(Status.RELEASED);
        	img.setVisibility(Visibility.PUBLIC);
        	update(img);
    	}
    }

    public void withdraw(Image img) throws Exception
    {
    	img.getProperties().setStatus(Status.WITHDRAWN);
    	img.setVisibility(Visibility.PUBLIC);
    	update(img);
    }

    public String getEscidocUserHandle() throws Exception
    {
        String userName = PropertyReader.getProperty("imeji.escidoc.user");
        String password = PropertyReader.getProperty("imeji.escidoc.password");
        return LoginHelper.login(userName, password);
    }


    @Override
    @Deprecated
    protected String getSpecificFilter() throws Exception
    {
        // Add filters for user management
        String filter = "(";
        if (user == null)
        {
            filter += "?collStatus = <http://imeji.mpdl.mpg.de/status/RELEASED> && ?visibility = <http://imeji.mpdl.mpg.de/image/visibility/PUBLIC>";
        }
        else
        {
            String userUri = "http://xmlns.com/foaf/0.1/Person/" + URLEncoder.encode(user.getEmail(), "UTF-8");
            filter += "(?collStatus = <http://imeji.mpdl.mpg.de/status/RELEASED> && ?visibility = <http://imeji.mpdl.mpg.de/image/visibility/PUBLIC>)";
            filter += " || ?collCreatedBy=<" + userUri + ">";
            for (Grant grant : user.getGrants())
            {
                switch (grant.getGrantType())
                {
                    case CONTAINER_ADMIN: // Add specifics here
                    default:
                    	if (grant.getGrantFor() != null) filter += " || ?collection=<" + grant.getGrantFor().toString() + ">";
                }
            }
        }
        filter += ")";
        return filter;
    }

    @Override
    protected String getSpecificQuery() throws Exception
    {
        return additionalQuery
                + " . ?s <http://imeji.mpdl.mpg.de/collection> ?collection . ?s <http://imeji.mpdl.mpg.de/visibility> ?visibility . ?collection <http://imeji.mpdl.mpg.de/properties> ?collprops . ?collprops <http://imeji.mpdl.mpg.de/createdBy> ?collCreatedBy . ?collprops <http://imeji.mpdl.mpg.de/status> ?collStatus ";
    }
}

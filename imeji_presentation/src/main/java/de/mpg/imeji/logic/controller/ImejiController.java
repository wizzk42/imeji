/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.logic.controller;

import java.net.URI;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import de.mpg.imeji.logic.ImejiBean2RDF;
import de.mpg.imeji.logic.ImejiJena;
import de.mpg.imeji.logic.ImejiRDF2Bean;
import de.mpg.imeji.logic.concurrency.locks.Locks;
import de.mpg.imeji.logic.util.Counter;
import de.mpg.imeji.logic.util.ObjectHelper;
import de.mpg.imeji.logic.vo.Container;
import de.mpg.imeji.logic.vo.Grant;
import de.mpg.imeji.logic.vo.Grant.GrantType;
import de.mpg.imeji.logic.vo.Properties;
import de.mpg.imeji.logic.vo.Properties.Status;
import de.mpg.imeji.logic.vo.User;
import de.mpg.j2j.exceptions.NotFoundException;
import de.mpg.j2j.helper.DateHelper;
import de.mpg.j2j.helper.J2JHelper;

public abstract class ImejiController
{
    private static Logger logger = Logger.getLogger(ImejiController.class);
    protected User user;

    // private static Model base = null;
    public ImejiController(User user2)
    {
        this.user = user2;
    }

    protected void writeCreateProperties(Properties properties, User user)
    {
        J2JHelper.setId(properties, ObjectHelper.getURI(properties.getClass(), Integer.toString(getUniqueId())));
        Calendar now = DateHelper.getCurrentDate();
        properties.setCreatedBy(ObjectHelper.getURI(User.class, user.getEmail()));
        properties.setModifiedBy(ObjectHelper.getURI(User.class, user.getEmail()));
        properties.setCreated(now);
        properties.setModified(now);
        if (properties.getStatus() == null)
            properties.setStatus(Status.PENDING);
    }

    protected void writeUpdateProperties(Properties properties, User user)
    {
        properties.setModifiedBy(ObjectHelper.getURI(User.class, user.getEmail()));
        properties.setModified(DateHelper.getCurrentDate());
    }

    protected void writeReleaseProperty(Properties properties, User user)
    {
        properties.setVersion(1);
        properties.setVersionDate(DateHelper.getCurrentDate());
        properties.setStatus(Status.RELEASED);
    }

    protected void writeWithdrawProperties(Properties properties, String comment)
    {
        if (comment != null && !"".equals(comment))
        {
            properties.setDiscardComment(comment);
        }
        if (properties.getDiscardComment() == null || "".equals(properties.getDiscardComment()))
        {
            throw new RuntimeException("Discard error: A Discard comment is needed");
        }
        properties.setStatus(Status.WITHDRAWN);
    }

    public User addCreatorGrant(URI id, User user) throws Exception
    {
        GrantController gc = new GrantController(user);
        Grant grant = new Grant(GrantType.CONTAINER_ADMIN, id);
        gc.addGrant(user, grant);
        UserController uc = new UserController(user);
        return uc.retrieve(user.getEmail());
    }

    /**
     * Remove images from a container which have been either deleted or withdrawn.
     * 
     * @param c
     * @param user
     */
    public Container loadContainerItems(Container c, User user, int limit, int offset)
    {
        ItemController ic = new ItemController(user);
        List<String> newUris = ic.searchImagesInContainer(c.getId(), null, null, limit, offset).getResults();
        c.getImages().clear();
        for (String s : newUris)
        {
            c.getImages().add(URI.create(s));
        }
        return c;
    }

    public boolean hasImageLocked(List<String> uris, User user)
    {
        for (String uri : uris)
        {
            if (Locks.isLocked(uri.toString(), user.getEmail()))
            {
                return true;
            }
        }
        return false;
    }

    public synchronized static int getUniqueId()
    {
        Counter c = new Counter();
        if (Locks.tryLockCounter())
        {
            try
            {
                ImejiRDF2Bean rdf2Bean = new ImejiRDF2Bean(ImejiJena.counterModel);
                c = (Counter)rdf2Bean.load(c.getId().toString(), ImejiJena.adminUser, c);
                int id = c.getCounter();
                logger.info("Counter : Requested id : " + id);
                incrementCounter(c);
                return id;
            }
            catch (NotFoundException e)
            {
                throw new RuntimeException("Fatal error: Counter not found. Please restart your server. ", e);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                Locks.releaseCounter();
            }
        }
        throw new RuntimeException("Counter locked, couldn't create new id");
    }

    private synchronized static void incrementCounter(Counter c)
    {
        try
        {
            c.setCounter(c.getCounter() + 1);
            ImejiBean2RDF bean2rdf = new ImejiBean2RDF(ImejiJena.counterModel);
            bean2rdf.update(bean2rdf.toList(c), ImejiJena.adminUser);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Fatal error: Counter not found. Please restart your server. ", e);
        }
    }
}

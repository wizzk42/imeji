package de.mpg.imeji.album;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import de.mpg.imeji.beans.Navigation;
import de.mpg.imeji.beans.SessionBean;
import de.mpg.imeji.image.ImageBean;
import de.mpg.imeji.user.SharingBean;
import de.mpg.imeji.util.BeanHelper;
import de.mpg.imeji.util.ImejiFactory;
import de.mpg.jena.controller.AlbumController;
import de.mpg.jena.controller.ImageController;
import de.mpg.jena.controller.UserController;
import de.mpg.jena.security.Operations.OperationsType;
import de.mpg.jena.security.Security;
import de.mpg.jena.util.ObjectHelper;
import de.mpg.jena.vo.Album;
import de.mpg.jena.vo.Grant.GrantType;
import de.mpg.jena.vo.Image;
import de.mpg.jena.vo.Organization;
import de.mpg.jena.vo.Person;
import de.mpg.jena.vo.User;

public class AlbumBean implements Serializable
{
        private SessionBean sessionBean = null;
        private Album album = null;
        private String id = null;
        private int authorPosition;
        private int organizationPosition;
        private List<SelectItem> profilesMenu = new ArrayList<SelectItem>();
        private boolean active;
        private boolean save;
        private boolean selected;

		public AlbumBean(Album album)
        {
			this.album = album;
            sessionBean = (SessionBean)BeanHelper.getSessionBean(SessionBean.class); 
            this.id = ObjectHelper.getId(album.getId());
            if(sessionBean.getActiveAlbum()!=null && sessionBean.getActiveAlbum().getAlbum().getId().equals(album.getId()))
            {
                active = true;
            }
        }

        public AlbumBean()
        {
            sessionBean = (SessionBean)BeanHelper.getSessionBean(SessionBean.class);
        }
        
        public void initView()
        {
            AlbumController ac = new AlbumController(sessionBean.getUser()); 
            try 
            {
            	setAlbum(ac.retrieve(id));
			} 
            catch (Exception e) 
			{
				BeanHelper.error("Album " + id + " not found");
			}
         
            if(sessionBean.getActiveAlbum()!=null && sessionBean.getActiveAlbum().equals(album.getId()))
            {
                active = true;
            }
            
            List<SelectItem> grantsMenu = new ArrayList<SelectItem>();
    		grantsMenu.add(new SelectItem(GrantType.PRIVILEGED_VIEWER, "Viewer", "Can view all images for this collection"));
    		grantsMenu.add(new SelectItem(GrantType.CONTAINER_EDITOR, "Album Editor", "Can edit informations about the collection"));
            ((SharingBean)BeanHelper.getRequestBean(SharingBean.class)).setGrantsMenu(grantsMenu);
        }
        
        public void initEdit()
        {
            AlbumController ac = new AlbumController(sessionBean.getUser());
            
            try 
            {
            	 setAlbum(ac.retrieve(id));
            	 save=false;
                 if(sessionBean.getActiveAlbum()!=null && sessionBean.getActiveAlbum().equals(album.getId()))
                 {
                     active = true;
                 }
			} 
            catch (Exception e) 
            {
				BeanHelper.error(e.getMessage());
			}
        }
        
        public void initCreate()
        {
            setAlbum(new Album());
            getAlbum().getMetadata().setTitle("");
            getAlbum().getMetadata().setDescription("");
            getAlbum().getMetadata().getPersons().clear();
            getAlbum().getMetadata().getPersons().add(ImejiFactory.newPerson());
            save = true;
        }

        public boolean valid()
        {
            boolean valid = true;
            boolean hasAuthor = false;
            if (getAlbum().getMetadata().getTitle() == null || "".equals(getAlbum().getMetadata().getTitle()))
            {
                BeanHelper.error(sessionBean.getMessage("collection_create_error_title"));
                valid = false;
            }
            for (Person c : getAlbum().getMetadata().getPersons())
            {
                boolean hasOrganization = false;
                if (!"".equals(c.getFamilyName()))
                {
                    hasAuthor = true;
                }
                for (Organization o : c.getOrganizations())
                {
                    if (!"".equals(o.getName()) || "".equals(c.getFamilyName()))
                    {
                        hasOrganization = true;
                    }
                    if (hasOrganization && "".equals(c.getFamilyName()))
                    {
                        BeanHelper.error(sessionBean.getMessage("collection_create_error_family_name"));
                        valid = false;
                    }
                }
                if (!hasOrganization)
                {
                    BeanHelper.error(sessionBean.getMessage("collection_create_error_organization"));
                    valid = false;
                }
            }
            if (!hasAuthor)
            {
                BeanHelper.error(sessionBean.getMessage("collection_create_error_author"));
                valid = false;
            }
            return valid;
        }

        public String addAuthor()
        {
            List<Person> list = (List<Person>) getAlbum().getMetadata().getPersons(); 
            list.add(authorPosition + 1, ImejiFactory.newPerson());
            return "";
        }

        public String removeAuthor()
        {
            List<Person> list = (List<Person>) getAlbum().getMetadata().getPersons();
            if (list.size() > 1) list.remove(authorPosition);
            else BeanHelper.error("An album needs at leat one author!");
            return "";
        }

        public String addOrganization()
        {
            Collection<Person> persons = getAlbum().getMetadata().getPersons();
            
            List<Organization> orgs = (List<Organization>) ((List<Person>) persons).get(authorPosition).getOrganizations();
            orgs.add(organizationPosition + 1, ImejiFactory.newOrganization());
            return "";
        }

        public String removeOrganization()
        {
            List<Person> persons = (List<Person>) getAlbum().getMetadata().getPersons();
            List<Organization> orgs = (List<Organization>) persons.get(authorPosition).getOrganizations();
            if (orgs.size() > 1) orgs.remove(organizationPosition);
            else BeanHelper.error("An author needs at leat one organization!");
            return "";
        }

        protected String getNavigationString()
        {
            return "pretty:";
        }

        public int getAuthorPosition()
        {
            return authorPosition;
        }

        public void setAuthorPosition(int pos)
        {
            this.authorPosition = pos;
        }

        /**
         * @return the collectionPosition
         */
        public int getOrganizationPosition()
        {
            return organizationPosition;
        }

        /**
         * @param collectionPosition the collectionPosition to set
         */
        public void setOrganizationPosition(int organizationPosition)
        {
            this.organizationPosition = organizationPosition;
        }

        /**
         * @return the id
         */
        public String getId()
        {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id)
        {
            this.id = id;
        }

        public List<SelectItem> getProfilesMenu()
        {
            return profilesMenu;
        }

        public void setProfilesMenu(List<SelectItem> profilesMenu)
        {
            this.profilesMenu = profilesMenu;
        }

        public int getSize()
        {
            return getAlbum().getImages().size();
        }

        public boolean getIsOwner()
        {
            if (sessionBean.getUser() != null)
            {
                return getAlbum().getProperties().getCreatedBy().equals(ObjectHelper.getURI(User.class, sessionBean.getUser().getEmail()));
            }
            else 
                return false;
        }

        
        public List<ImageBean> getImages() throws Exception
        {
            ImageController ic = new ImageController(sessionBean.getUser()); 
            if (getAlbum() != null)
            {
            	List<String> uris = new ArrayList<String>();
            	for (URI uri : getAlbum().getImages()) uris.add(uri.toString());
            	Collection<Image> imgList = ic.loadImages(uris, 5, 0);
            	return ImejiFactory.imageListToBeanList(imgList); 
            }
            return null;	
        }
        
        public String save() throws Exception
        {
            if(save)
            { 
                AlbumController ac = new AlbumController(sessionBean.getUser());
                if (valid())
                {
                    ac.create(getAlbum());
                    UserController uc = new UserController(sessionBean.getUser());
                    sessionBean.setUser(uc.retrieve(sessionBean.getUser().getEmail()));
                    BeanHelper.info("Album created successfully");
                    return "pretty:albums";
                }
            }
            else
            {
                update();
            }
            
            return "";
           
        }
        
        public String update() throws Exception
        {
            AlbumController ac = new AlbumController(sessionBean.getUser());
            if (valid())
            {
                ac.update(getAlbum());
                BeanHelper.info("Album updated successfully");
                Navigation navigation = (Navigation) BeanHelper.getApplicationBean(Navigation.class);
                FacesContext.getCurrentInstance().getExternalContext().redirect(navigation.getApplicationUri() + getAlbum().getId().getPath() + "/details?init=1");
            }
            return "";
        }

        public void setAlbum(Album album)
        {
            this.album = album;
        }

        public Album getAlbum()
        {
            return album; 
        }
        
        public String getPersonString()
        {
            String personString = "";
            for (Person p : album.getMetadata().getPersons())
            {
                personString += p.getFamilyName() + ", " + p.getGivenName();
            }
            return personString;
        }

        public void setActive(boolean active)
        {
            this.active = active;
        }

        public boolean getActive()
        {
            return active;
        }
        
        public String makeActive()
        {
            sessionBean.setActiveAlbum(this); 
            this.setActive(true);
            return "pretty:";
        }
        
        public String makeInactive()
        {
            sessionBean.setActiveAlbum(null);
            this.setActive(false);
            return "pretty:";
        }
        
        public String release() 
        {
        	makeInactive();
            AlbumController ac = new AlbumController(sessionBean.getUser());
            try 
            {
				ac.release(album);
			} 
            catch (Exception e) 
            {
            	BeanHelper.error("Error releasing album");
				BeanHelper.error(e.getMessage());
				e.printStackTrace();
			}
            return "pretty:";
        }
        
        public String delete()
        {
        	AlbumController c = new AlbumController(sessionBean.getUser());
        	
        	try 
        	{
        		makeInactive();
    			c.delete(album, sessionBean.getUser());
    			BeanHelper.info("Album successfully deleted.");
    		} 
        	catch (Exception e) 
        	{
        		BeanHelper.error("Error deleting Album");
    			BeanHelper.error("Details: " + e.getMessage());
    			e.printStackTrace();
    		}
        	
        	return "pretty:albums";
        }
        
        public String withdraw() throws Exception
        {
        	AlbumController c = new AlbumController(sessionBean.getUser());
        	
        	try 
        	{
        		c.withdraw(album);
            	BeanHelper.info("Album successfully withdrawn.");
    		} 
        	catch (Exception e) 
    		{
        		BeanHelper.error("Error withdrawing Album");
    			BeanHelper.error("Details: " + e.getMessage());
    		}
        	
        	return "pretty:";
        }
        
        public boolean getSelected() 
        {
        	if(sessionBean.getSelectedAlbums().contains(album.getId()))
        		selected = true;
        	else
        		selected = false;
            return selected;
		}

		public void setSelected(boolean selected) 
		{
	    	if(selected)
	    	{	
	    		if(!(sessionBean.getSelectedAlbums().contains(album.getId())))
	    			sessionBean.getSelectedAlbums().add(album.getId());
	    	}
	    	else
	    		sessionBean.getSelectedAlbums().remove(album.getId());
	        this.selected = selected;
		}
       
		public boolean isEditable() 
		{
			Security security = new Security();
			return security.check(OperationsType.UPDATE, sessionBean.getUser(), album);
		}
		
		public boolean isVisible() 
		{
			Security security = new Security();
			return security.check(OperationsType.READ, sessionBean.getUser(), album);
		}
		
		public boolean isDeletable() 
		{
			Security security = new Security();
			return security.check(OperationsType.DELETE, sessionBean.getUser(), album);
		}

}

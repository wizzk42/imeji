/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.presentation.collection;

import de.mpg.imeji.presentation.beans.Navigation;
import de.mpg.imeji.presentation.image.ImageBean;
import de.mpg.imeji.presentation.image.SingleImageBrowse;
import de.mpg.imeji.presentation.util.BeanHelper;

public class CollectionImageBean extends ImageBean
{
    private String collectionId;
    private Navigation navigation;

    public CollectionImageBean() throws Exception
    {
        super();
        this.prettyLink = "pretty:EditImageOfCollection";
        navigation = (Navigation)BeanHelper.getApplicationBean(Navigation.class);
    }

    public void initBrowsing()
    {
        setBrowse(new SingleImageBrowse((CollectionImagesBean)BeanHelper.getSessionBean(CollectionImagesBean.class),
                getImage()));
    }

    public String getCollectionId()
    {
        return collectionId;
    }

    public void setCollectionId(String collectionId)
    {
        this.collectionId = collectionId;
    }

    public String getPageUrl()
    {
        return navigation.getCollectionUrl() + collectionId + "/" + navigation.ITEM.getPath() + "/" + getId();
    }

    public String getNavigationString()
    {
        return "pretty:CollectionItem";
    }
}

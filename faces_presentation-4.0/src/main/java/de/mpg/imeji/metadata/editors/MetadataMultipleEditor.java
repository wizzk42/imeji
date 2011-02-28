package de.mpg.imeji.metadata.editors;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.mpg.jena.vo.Image;
import de.mpg.jena.vo.ImageMetadata;
import de.mpg.jena.vo.MetadataProfile;
import de.mpg.jena.vo.Statement;

public class MetadataMultipleEditor extends MetadataEditor
{
	public MetadataMultipleEditor(List<Image> images, MetadataProfile profile,Statement statement) 
	{
		super(images, profile, statement);
	}

	@Override
	public void initialize(List<Image> images) 
	{
		boolean hasStatement = (statement != null);
		for (Image im : images) 
		{
			if (hasProfile(im)) 
			{
				boolean empty = true;
				for(ImageMetadata md : im.getMetadataSet().getMetadata())
				{
					URI stURI = URI.create(profile.getId() + "/" + statement.getName());
					if (hasStatement && md.getNamespace().equals(stURI))
					{
						empty = false;
					}
				}
//				for (int i = 0; i < im.getMetadata().size(); i++) 
//				{
//					if (hasStatement && im.getMetadata().get(i).getName().equals(statement.getName()))
//					{
//						empty = false;
//					}
//				}
				if (empty && hasStatement) addMetadata(im, 0);
				this.images.add(im);
			}
		}
	}
	
	@Override
	public boolean prepareUpdate() 
	{
		if (images.size() == 0)
		{
			return false;
		}
		return true;
	}

	@Override
	public boolean validateMetadataofImages() 
	{
		// Validate only first image since all images get the same metadata
//		validator = new Validator(images.get(0).getMetadata(), profile);
//		return validator.valid();
		return true;
	}
	
	public void addMetadata(int imagePos, int metadataPos)
    {
        if (imagePos < images.size()) 
        {
			addMetadata(images.get(imagePos), metadataPos);
		}
    }
	
	public void addMetadata(Image image, int metadataPos)
	{
		if (metadataPos <= image.getMetadataSet().getMetadata().size()) 
		{
			List<ImageMetadata> newList = new ArrayList<ImageMetadata>();
			newList.add(metadataPos, newMetadata());
			image.getMetadataSet().getMetadata().addAll(newList);
			
			//image.getMetadata().add(metadataPos, newMetadata());
		}
	}
	 
	public void removeMetadata(int imagePos, int metadataPos)
	{
		if (imagePos < images.size()) 
		{
			removeMetadata(images.get(imagePos), metadataPos);
		}
	}
	
	public void removeMetadata(Image image, int metadataPos)
	{
		if (metadataPos <image.getMetadataSet().getMetadata().size()) 
		{
			image.getMetadataSet().getMetadata().remove(metadataPos);
		}
	}



}

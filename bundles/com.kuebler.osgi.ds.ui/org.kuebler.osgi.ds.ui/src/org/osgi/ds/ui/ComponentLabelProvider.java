/*
 * Copyright (c) Jens Kuebler (2015). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.ds.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.Reference;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.SatisfiedReferenceDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;

/**
 *
 */
public class ComponentLabelProvider extends StyledCellLabelProvider implements ILabelProvider, IColorProvider {

	private Color red;
	private Color yellow;
	private Color green;
	private ImageRegistry imageRegistry;
	private Color blue;
	private NumberFormat numberFormat;

	public ComponentLabelProvider() {
		red = new Color(Display.getDefault(), new RGB(255, 129, 129));
		yellow = new Color(Display.getDefault(), new RGB(250, 255, 125));
		green = new Color(Display.getDefault(), new RGB(155, 222, 141));
		blue = new Color(Display.getDefault(), new RGB(0, 0, 255));
		imageRegistry = new ImageRegistry();
//		imageRegistry.put(new Integer(Component.STATE_ACTIVATING).toString(), ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/component.png")));
		imageRegistry.put(new Integer(ComponentConfigurationDTO.ACTIVE).toString(), ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/active.png")));
		imageRegistry.put(new Integer(ComponentConfigurationDTO.SATISFIED).toString(), ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/satisfied.png")));
		imageRegistry.put(new Integer(ComponentConfigurationDTO.UNSATISFIED_REFERENCE).toString(), ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/Unsatisfied.png")));
		imageRegistry.put(new Integer(ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION).toString(), ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/configurationUnsatisfied.png")));
		imageRegistry.put("UnsatisfiedService", ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/unsatisfiedService.png")));
		imageRegistry.put("SatisfiedService", ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/satisfiedService.png")));
		imageRegistry.put("ProvdidedService", ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/providedService.png")));
		numberFormat = new DecimalFormat("000");
	}
	
  /**
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage(Object obj) {
	  if(obj instanceof ComponentConfigurationDTO) {
		ComponentConfigurationDTO componentConfigurationDTO = (ComponentConfigurationDTO) obj;
		switch(componentConfigurationDTO.state) {
		case ComponentConfigurationDTO.ACTIVE:
    		return imageRegistry.get(new Integer(ComponentConfigurationDTO.ACTIVE).toString());
		case ComponentConfigurationDTO.SATISFIED:
    		return imageRegistry.get(new Integer(ComponentConfigurationDTO.SATISFIED).toString());
		case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION:
    		return imageRegistry.get(new Integer(ComponentConfigurationDTO.UNSATISFIED_REFERENCE).toString());
		case ComponentConfigurationDTO.UNSATISFIED_REFERENCE:
    		return imageRegistry.get(new Integer(ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION).toString());

		}
	  }
	    if(obj instanceof Component) {
	    	Component component = (Component) obj;
	    	switch(component.getState()) {
	    	case Component.STATE_ACTIVATING:
	    	case Component.STATE_ACTIVE:
	    		return imageRegistry.get(new Integer(Component.STATE_ACTIVATING).toString());
	    	case Component.STATE_REGISTERED:
	    		return imageRegistry.get(new Integer(Component.STATE_ACTIVATING).toString());
	    	case Component.STATE_UNSATISFIED:
	    	case Component.STATE_DISABLED:
	    	case Component.STATE_DISABLING:
	    		return imageRegistry.get(new Integer(Component.STATE_ACTIVATING).toString());
	    	default :
	    		return imageRegistry.get(new Integer(Component.STATE_ACTIVATING).toString());
	    	}
	    }
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText(Object element) {
	    if (element instanceof ComponentConfigurationDTO) {
	    	ComponentConfigurationDTO component = (ComponentConfigurationDTO) element;
	        StringBuffer buf = new StringBuffer();
	        buf.append(component.description.name);
	        String[] services = component.description.serviceInterfaces;
	        if (services != null) {
	          buf.append("{");
	          for (String service : services) {
	            buf.append(" ");
	            buf.append(service);
	          }
	          buf.append("}");
	        }
	        return buf.toString();
	      }
	    if (element instanceof SatisfiedReferenceDTO) {
	    	SatisfiedReferenceDTO reference = (SatisfiedReferenceDTO) element;
	        StringBuffer buf = new StringBuffer();
	        buf.append(reference.name);
	        buf.append(" ");
	        buf.append(reference.target);
	        return buf.toString();
	      }
	    if (element instanceof UnsatisfiedReferenceDTO) {
	    	UnsatisfiedReferenceDTO reference = (UnsatisfiedReferenceDTO) element;
	        StringBuffer buf = new StringBuffer();
	        buf.append(reference.name);
	        buf.append(" ");
	        buf.append(reference.target);
	        return buf.toString();
	      }
    if (element instanceof Component) {
      Component component = (Component) element;
      StringBuffer buf = new StringBuffer();
      buf.append(component.getName());
      buf.append('(');
      buf.append(component.getId());
      buf.append(')');
      String[] services = component.getServices();
      if (services != null) {
        buf.append("{");
        for (String service : services) {
          buf.append(" ");
          buf.append(service);
        }
        buf.append("}");
      }
      return buf.toString();
    }
    if(element instanceof Reference) {
        StringBuffer buf = new StringBuffer();
        Reference reference = (Reference) element;
		buf.append(reference.getName());
        buf.append(reference.getServiceName());
//        buf.append();
    	
    	return buf.toString();
    }
    if(element instanceof String) {
    	return (String) element;
    }
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.StyledCellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  public void update(ViewerCell cell) {
    Object obj = cell.getElement();
    if(obj instanceof String) {
        cell.setText((String) obj);
		cell.setImage(imageRegistry.get("ProvdidedService"));
    }
    if(obj instanceof Reference) {
    	Reference reference = (Reference) obj;
    	String referenceCardinality = "";
        if (reference.isMultiple() && reference.isOptional()) {
            referenceCardinality += "(0..*)";
          } else if (reference.isMultiple() && !reference.isOptional()) {
            referenceCardinality += "(1..*)";
          } else if (!reference.isMultiple() && !reference.isOptional()) {
            referenceCardinality += "(1..1)";
          } else if (!reference.isMultiple() && reference.isOptional()) {
            referenceCardinality += "(0..1)";
          }
    	StyledString styledString = new StyledString(reference.getName() + " "  + referenceCardinality + " " + reference.getServiceName() + " " + reference.getTarget());
    	cell.setText(styledString.toString());
    	if(reference.isSatisfied()) {
    		cell.setBackground(green);
    	} else {
    		cell.setBackground(red);
    	}
    }
    if (obj instanceof SatisfiedReferenceDTO) {
    	SatisfiedReferenceDTO reference = (SatisfiedReferenceDTO) obj;
        StringBuffer buf = new StringBuffer();
        buf.append(reference.name);
        buf.append(" ");
        buf.append(reference.target);
        cell.setText(buf.toString());
		cell.setImage(imageRegistry.get(new Integer(ComponentConfigurationDTO.SATISFIED).toString()));
		cell.setImage(imageRegistry.get("SatisfiedService"));
      }
    if (obj instanceof UnsatisfiedReferenceDTO) {
    	UnsatisfiedReferenceDTO reference = (UnsatisfiedReferenceDTO) obj;
        StringBuffer buf = new StringBuffer();
        buf.append(reference.name);
        buf.append(" ");
        buf.append(reference.target);
        cell.setText(buf.toString());
		cell.setImage(imageRegistry.get("UnsatisfiedService"));
      }
    if(obj instanceof ComponentConfigurationDTO) {
    	ComponentConfigurationDTO componentDescriptionDTO = (ComponentConfigurationDTO) obj;
    	StyledString styledString = new StyledString("[" + numberFormat.format(componentDescriptionDTO.id) + "]" + componentDescriptionDTO.description.name);
		cell.setText(styledString.toString());
		switch (componentDescriptionDTO.state) {
		case ComponentConfigurationDTO.ACTIVE:
//    		cell.setBackground(green);
    		cell.setImage(imageRegistry.get(new Integer(ComponentConfigurationDTO.ACTIVE).toString()));
    		break;
		case ComponentConfigurationDTO.SATISFIED:
//    		cell.setBackground(yellow);
    		cell.setImage(imageRegistry.get(new Integer(ComponentConfigurationDTO.SATISFIED).toString()));
			break;
		case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION:
//    		cell.setBackground(blue);
    		cell.setImage(imageRegistry.get(new Integer(ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION).toString()));
			break;
		case ComponentConfigurationDTO.UNSATISFIED_REFERENCE:
//    		cell.setBackground(red);
    		cell.setImage(imageRegistry.get(new Integer(ComponentConfigurationDTO.UNSATISFIED_REFERENCE).toString()));
			break;
		default:
			break;
		}
//		if(componentDescriptionDTO.references);
    }
    if(obj instanceof Component) {
    	Component component = (Component) obj;
    	StyledString styledString = new StyledString(component.getName());
    	switch(component.getState()) {
    	case Component.STATE_ACTIVATING:
    	case Component.STATE_ACTIVE:
    		cell.setBackground(green);
    		break;
    	case Component.STATE_REGISTERED:
    		cell.setBackground(yellow);
    		break;
    	case Component.STATE_UNSATISFIED:
    	case Component.STATE_DISABLED:
    	case Component.STATE_DISABLING:
    		cell.setBackground(red);
    		break;
    	default :
    		cell.setBackground(ColorConstants.white);
    		break;
    	}
    	
    	if (obj instanceof Component) {
    		Component parent = (Component) obj;
    		//    styledString.append(" (" +
    		//    parent.getChildren().length +
    		//    ")", StyledString.COUNTER_STYLER);
    		//    }
    		cell.setText(styledString.toString());
    		cell.setStyleRanges(styledString.getStyleRanges());
    		cell.setImage(getImage(obj));
    }
    super.update(cell);
    }  }

  /**
   * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
   */
  @Override
  public Color getForeground(Object element) {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
   */
  @Override
  public Color getBackground(Object element) {
    Component component = (Component) element;
    if (component != null) {
      switch (component.getState()) {
        case Component.STATE_ACTIVATING:
        case Component.STATE_ACTIVE:
          return ColorConstants.lightGreen;
        case Component.STATE_REGISTERED:
          return ColorConstants.orange;
        case Component.STATE_UNSATISFIED:
        case Component.STATE_DISABLED:
        case Component.STATE_DISABLING:
          return ColorConstants.red;
      }
    }
    return ColorConstants.white;
  }
  
  @Override
public void dispose() {
	  red.dispose();
	  yellow.dispose();
	  green.dispose();
	  imageRegistry.dispose();
	super.dispose();
}

}

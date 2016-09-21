package org.osgi.ds.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class DynamicSCRSelection extends CompoundContributionItem {

	private ImageDescriptor icon;


	public DynamicSCRSelection() {
		icon = ImageDescriptor.createFromURL(DSUIActivator.getDefault().getBundle().getEntry("res/unsatisfiedService.png"));
	}
	
	
	@Override
	protected IContributionItem[] getContributionItems() {
		IContributionItem[] list = new IContributionItem[1];
	    Map parms = new HashMap();
	    parms.put("groupBy", "Severity");
	    
//	    .getServiceLocator();
	    CommandContributionItemParameter parmameter = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "select1", "com.kuebler.ds.select", parms, icon, null, null, "Select SCR", null, "Select SCR", CommandContributionItem.STYLE_PUSH, null, true);

	    
	    list[0] = new CommandContributionItem(parmameter);
//	            "org.eclipse.ui.views.problems.grouping",
//	            parms, null, null, null, "Severity", null,
//	            null, CommandContributionItem.STYLE_PUSH);
//	 
//	    parms = new HashMap();
//	    parms.put("groupBy", "None");
//	    list[1] = new CommandContributionItem(null,
//	            "org.eclipse.ui.views.problems.grouping",
//	            parms, null, null, null, "None", null, null,
//	            CommandContributionItem.STYLE_PUSH);
	    return list;
    }

}

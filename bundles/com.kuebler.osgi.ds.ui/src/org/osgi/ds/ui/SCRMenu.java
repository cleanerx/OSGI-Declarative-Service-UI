package org.osgi.ds.ui;

import java.util.Date;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SCRMenu extends ContributionItem {

	 public void fill(Menu menu, int index) {
		 
		 MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	        menuItem.setText("My menu item (" + new Date() + ")");
	        menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	                //what to do when menu is subsequently selected.
	                System.err.println("Dynamic menu selected");
	            }
	        });
	 }
	
}

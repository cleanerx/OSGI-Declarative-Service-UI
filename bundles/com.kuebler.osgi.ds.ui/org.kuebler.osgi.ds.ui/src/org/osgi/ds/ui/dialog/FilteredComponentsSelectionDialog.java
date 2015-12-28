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
package org.osgi.ds.ui.dialog;

import java.util.Comparator;

import org.apache.felix.scr.Component;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.osgi.ds.ui.ComponentLabelProvider;
import org.osgi.ds.ui.ServiceComponentComparator;

/**
 *
 */
public class FilteredComponentsSelectionDialog extends FilteredItemsSelectionDialog {

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Control createDialogArea = super.createDialogArea(parent);
    setListLabelProvider(new ComponentLabelProvider());
    return createDialogArea;
  }

  private Component[] _allComponents;

  /**
   * @param shell
   * @wbp.parser.constructor
   */
  public FilteredComponentsSelectionDialog(Shell shell) {
    super(shell);
  }

  /**
   * @wbp.parser.constructor 
   * @param shell
   * @param multi
   * @param allComponents
   */
  public FilteredComponentsSelectionDialog(Shell shell, boolean multi, Component[] allComponents) {
    super(shell, multi);
    _allComponents = allComponents;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createExtendedContentArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createExtendedContentArea(Composite parent) {
    return null;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDialogSettings()
   */
  @Override
  protected IDialogSettings getDialogSettings() {
    return new DialogSettings("XX");
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
   */
  @Override
  protected IStatus validateItem(Object item) {
    return Status.OK_STATUS;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter()
   */
  @Override
  protected ItemsFilter createFilter() {
    return new ComponentItemsFilter();
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
   */
  @Override
  protected Comparator getItemsComparator() {
    return new ServiceComponentComparator();
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractContentProvider, org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
      throws CoreException {
    for (Component component : _allComponents) {
      contentProvider.add(component, itemsFilter);
    }
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementName(java.lang.Object)
   */
  @Override
  public String getElementName(Object item) {
    if (item instanceof Component) {
      Component component = (Component) item;
      return component.getName();
    }
    return null;
  }
  
  public class ComponentItemsFilter extends ItemsFilter {

	    /**
	     * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#matchItem(java.lang.Object)
	     */
	    @Override
	    public boolean matchItem(Object item) {
	      if (item instanceof Component) {
	        Component component = (Component) item;
	        StringBuffer buf = new StringBuffer();
	        buf.append(component.getName());
	        String[] services = component.getServices();
	        if (services != null) {
	          for (String service : services) {
	            buf.append(" ");
	            buf.append(service);
	          }
	        }
	        if (matches(buf.toString())) {
	          return true;
	        }
	      }

	      return false;
	    }

	    @Override
	    protected boolean matches(String text) {
	      String pattern = patternMatcher.getPattern();
	      if (pattern.indexOf("*") != 0 & pattern.indexOf("?") != 0 & pattern.indexOf(".") != 0) {//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        pattern = "*" + pattern; //$NON-NLS-1$
	        patternMatcher.setPattern(pattern);
	      }
	      return patternMatcher.matches(text);
	    }

	    /**
	     * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#isConsistentItem(java.lang.Object)
	     */
	    @Override
	    public boolean isConsistentItem(Object item) {
	      return true;
	    }

  }

}

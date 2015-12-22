/*******************************************************************************
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
package org.osgi.ds.ui.handler;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.osgi.ds.ui.ComponentVisualizationView;
import org.osgi.ds.ui.DSUIActivator;
import org.osgi.ds.ui.dialog.FilteredComponentsSelectionDialog;

/**
 *
 */
public class SelectComponentHandler extends AbstractHandler {

  /**
   *
   */
  public SelectComponentHandler() {
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    Shell shell = HandlerUtil.getActiveShell(event);
    BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();
    ServiceReference<ScrService> serviceReference = bundleContext.getServiceReference(ScrService.class);
    ScrService service = bundleContext.getService(serviceReference);
    Component[] allComponents = service.getComponents();
    Arrays.sort(allComponents, new Comparator<Component>() {

      @Override
      public int compare(Component o1, Component o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    FilteredComponentsSelectionDialog filteredComponentsSelectionDialog = new FilteredComponentsSelectionDialog(shell, false, allComponents);
    //    ListSelectionDialog listSelectionDialog = new ListSelectionDialog(shell, allComponents, new ArrayContentProvider(), new ComponentLabelProvider(), "Select a component");
    if (filteredComponentsSelectionDialog.open() == Window.OK) {
      ComponentVisualizationView view = (ComponentVisualizationView) HandlerUtil.getActivePart(event);
      view.buildGraph((Component) filteredComponentsSelectionDialog.getResult()[0]);
    }
    return null;
  }

}

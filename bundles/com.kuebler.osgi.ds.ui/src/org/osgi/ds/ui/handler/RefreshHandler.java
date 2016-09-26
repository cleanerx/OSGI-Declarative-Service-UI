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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.ds.ui.ComponentViewer;
import org.osgi.ds.ui.DSUIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.dto.serial.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.serial.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.serial.ServiceComponentRuntime;

/**
 *
 */
public class RefreshHandler extends AbstractHandler {

  /**
   *
   */
  public RefreshHandler() {
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IWorkbenchPart activePart = HandlerUtil.getActivePart(event);

    BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();
    ServiceReference<ServiceComponentRuntime> serviceReference = bundleContext.getServiceReference(ServiceComponentRuntime.class);
    if(serviceReference != null) {
    	ServiceComponentRuntime serviceComponentRuntime = bundleContext.getService(serviceReference);
    	if(serviceComponentRuntime != null) {
    		if(activePart instanceof ComponentViewer) {
    			ComponentViewer componentViewer = (ComponentViewer) activePart;
    			Collection<ComponentDescriptionDTO> componentDescriptionDTOs = serviceComponentRuntime.getComponentDescriptionDTOs();
    			Collection<ComponentConfigurationDTO> componentConfigurationDTOs = new ArrayList<>();
    			for (ComponentDescriptionDTO componentDescriptionDTO : componentDescriptionDTOs) {
    				componentConfigurationDTOs.addAll(serviceComponentRuntime.getComponentConfigurationDTOs(componentDescriptionDTO));
				}
				componentViewer.setInput(componentConfigurationDTOs);
    		}
    	}
    }
    

    return null;
  }

}

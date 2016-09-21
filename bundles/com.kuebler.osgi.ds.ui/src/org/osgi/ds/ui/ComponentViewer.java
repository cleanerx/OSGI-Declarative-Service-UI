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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.ScrService;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ComponentViewer extends ViewPart  {
	private ComponentStateFilter componentFilter;
	private TreeViewer treeViewer;
	private Map<String, ComponentConfigurationDTO[]> scr2dtos;
	private ServiceTracker<ServiceComponentRuntime, ServiceComponentRuntime> componentRuntimeTracker;

	public ComponentViewer() {
		scr2dtos = new HashMap<String, ComponentConfigurationDTO[]>();
	}

	@Override
	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		treeViewer.setContentProvider(new ComponentContentProvider());
		treeViewer.setLabelProvider(new ComponentLabelProvider());
		treeViewer.setComparator(new ViewerComparator());
		ViewerFilter[] vieweFilters = new ViewerFilter[1];
		componentFilter = new ComponentStateFilter();
		vieweFilters[0] = componentFilter;
//		ResolvedComponentFilter unresolvedComponentFilter = new ResolvedComponentFilter();
		treeViewer.setFilters(vieweFilters);
		final BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();

		componentRuntimeTracker = new ServiceTracker<>(bundleContext, ServiceComponentRuntime.class, new ServiceTrackerCustomizer<ServiceComponentRuntime, ServiceComponentRuntime>() {

			@Override
			public ServiceComponentRuntime addingService(ServiceReference<ServiceComponentRuntime> reference) {
				final ServiceComponentRuntime serviceComponentRuntime = bundleContext.getService(reference);
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
//						Collection<ComponentDescriptionDTO> allComponentConfigurationDTO = serviceComponentRuntime.getComponentDescriptionDTOs();
//						treeViewer.setInput(allComponentConfigurationDTO);
						
					}
				});
				return serviceComponentRuntime;
			}

			@Override
			public void modifiedService(ServiceReference<ServiceComponentRuntime> reference,
					ServiceComponentRuntime service) {
				ServiceComponentRuntime serviceComponentRuntime = bundleContext.getService(reference);
				Collection<ComponentDescriptionDTO> allComponentConfigurationDTO = serviceComponentRuntime.getComponentDescriptionDTOs();
				treeViewer.setInput(allComponentConfigurationDTO);
			}

			@Override
			public void removedService(ServiceReference<ServiceComponentRuntime> reference,
					ServiceComponentRuntime service) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if(!treeViewer.getTree().isDisposed()) {
							treeViewer.setInput(null);
						}
						
					}
					
				});
			}
		});
		componentRuntimeTracker.open();
		ServiceComponentRuntime service = componentRuntimeTracker.getService();
		if(service != null) {
			Collection<ComponentDescriptionDTO> allComponentConfigurationDTO = service.getComponentDescriptionDTOs();
			treeViewer.setInput(allComponentConfigurationDTO);
		}
		getViewSite().setSelectionProvider(treeViewer);
	}

//	private BundleContext updateTreeViewerContents() {
//		BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();
//		ServiceReference<ScrService> serviceReference = bundleContext.getServiceReference(ScrService.class);
//		try {
//			StrippedServiceComponentRuntime service = componentRuntimeTracker.getService();
//		} finally {
//			bundleContext.ungetService(serviceReference);
//		}
//		return bundleContext;
//	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void setFilterStates(int filter) {
		componentFilter.setFilter(filter);
//		StrippedServiceComponentRuntime service = componentRuntimeTracker.getService();
//		if(service != null) {
//			Collection<ComponentConfigurationDTO> allComponentConfigurationDTO = service.getAllComponentConfigurationDTO();
//			treeViewer.setInput(allComponentConfigurationDTO);
//		}
		treeViewer.refresh();
	}

	public int getFilterState() {
		return componentFilter.getFilter();
	}

	@Override
	public void dispose() {
		if(componentRuntimeTracker != null) {
			componentRuntimeTracker.close();
		}
		super.dispose();
	}

	public void setInput(Collection<ComponentConfigurationDTO> componentDescriptionDTO) {
		treeViewer.setInput(componentDescriptionDTO);
		treeViewer.refresh(true);
	}

}

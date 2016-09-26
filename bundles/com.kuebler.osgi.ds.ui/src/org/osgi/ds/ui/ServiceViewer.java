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

import org.apache.felix.scr.ScrService;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.dto.serial.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.serial.ServiceComponentRuntime;
import org.osgi.util.tracker.ServiceTracker;

public class ServiceViewer extends ViewPart {
	private ComponentStateFilter componentFilter;
	private TreeViewer treeViewer;
	private ServiceTracker<ServiceComponentRuntime, ServiceComponentRuntime> componentRuntimeTracker;

	public ServiceViewer() {
	}

	@Override
	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		treeViewer.setContentProvider(new ComponentContentProvider());
    treeViewer.setLabelProvider(new ServiceLabelProvider());
		treeViewer.setComparator(new ViewerComparator());
		ViewerFilter[] vieweFilters = new ViewerFilter[1];
		componentFilter = new ComponentStateFilter();
		vieweFilters[0] = componentFilter;
//		ResolvedComponentFilter unresolvedComponentFilter = new ResolvedComponentFilter();
		treeViewer.setFilters(vieweFilters);
		BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();

		componentRuntimeTracker = new ServiceTracker<>(bundleContext, ServiceComponentRuntime.class, null);
		componentRuntimeTracker.open();
		ServiceComponentRuntime service = componentRuntimeTracker.getService();
		if(service != null) {
//      Collection<ServiceReferenceDTO> allServiceReferenceDTO = service.getAllServiceReferenceDTO();
//      treeViewer.setInput(allServiceReferenceDTO);
		}
		getViewSite().setSelectionProvider(treeViewer);
	}

	private BundleContext updateTreeViewerContents() {
		BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();
		ServiceReference<ScrService> serviceReference = bundleContext.getServiceReference(ScrService.class);
		try {
			ServiceComponentRuntime service = componentRuntimeTracker.getService();
		} finally {
			bundleContext.ungetService(serviceReference);
		}
		return bundleContext;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void setFilterStates(int filter) {
		componentFilter.setFilter(filter);
		ServiceComponentRuntime service = componentRuntimeTracker.getService();
		if(service != null) {
			Collection<ComponentDescriptionDTO> allComponentConfigurationDTO = service.getComponentDescriptionDTOs();
			treeViewer.setInput(allComponentConfigurationDTO);
		}
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

}

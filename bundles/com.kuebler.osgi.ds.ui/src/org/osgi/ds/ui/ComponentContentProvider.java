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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.felix.scr.Reference;
import org.eclipse.equinox.internal.ds.model.ServiceComponentProp;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.osgi.service.component.runtime.dto.serial.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.serial.SatisfiedReferenceDTO;
import org.osgi.service.component.runtime.dto.serial.UnsatisfiedReferenceDTO;

public class ComponentContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
        if (inputElement instanceof Collection) {
			return ((Collection) inputElement).toArray();
		}
        return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ComponentConfigurationDTO) {
			ComponentConfigurationDTO componentConfigurationDTO = (ComponentConfigurationDTO) parentElement;
			List<Object> list = new ArrayList<Object>();
			if(componentConfigurationDTO.description.serviceInterfaces != null) {
				for (String serviceInterface : componentConfigurationDTO.description.serviceInterfaces) {
					list.add(serviceInterface);
				}
			}
			if(componentConfigurationDTO.satisfiedReferences != null) {
				for (SatisfiedReferenceDTO dto : componentConfigurationDTO.satisfiedReferences) {
					list.add(dto);
				}
			}
			if(componentConfigurationDTO.unsatisfiedReferences != null) {
				for (UnsatisfiedReferenceDTO dto : componentConfigurationDTO.unsatisfiedReferences) {
					list.add(dto);
				}
			}
			return list.toArray(new Object[list.size()]);
		}
//		List<String> list = new ArrayList<String>();
		List<Reference> list = new ArrayList<Reference>();
		if(parentElement instanceof ServiceComponentProp) {
			ServiceComponentProp serviceComponentProp = (ServiceComponentProp) parentElement;
			Reference[] references = serviceComponentProp.getReferences();
			if(references != null) {
				return references;
			}
//			if(references != null) {
//				for (Reference reference : references) {
//					list.add(reference.getBindMethodName());
//				}
//			}
			
		}
		return new Object[0];
//		return list.toArray();
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof ComponentConfigurationDTO) {
			ComponentConfigurationDTO componentConfigurationDTO = (ComponentConfigurationDTO) element;
			if(componentConfigurationDTO.satisfiedReferences != null && componentConfigurationDTO.unsatisfiedReferences != null) {
				return true;
			}
			
		}
		return false;
	}

}

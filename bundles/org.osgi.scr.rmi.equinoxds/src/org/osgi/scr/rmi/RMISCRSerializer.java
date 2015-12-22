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
package org.osgi.scr.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.scr.rmi.api.IRMIServiceSerializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;
import org.osgi.service.component.runtime.dto.SatisfiedReferenceDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;


@Component(immediate = true)
public class RMISCRSerializer extends UnicastRemoteObject implements IRMIServiceSerializer {

	public RMISCRSerializer() throws RemoteException {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2651396002481803362L;
	
	private Registry registry;

	private ScrService scrService;

	@Activate
	public void activate() throws RemoteException {
		registry = LocateRegistry.createRegistry(1099);
		registry.rebind("Server", this);
	}
	
	@Deactivate
	public void deactivate() throws RemoteException, NotBoundException {
		registry.unbind("Server");
	}

//	@Override
	public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOs(ComponentDescriptionDTO arg0) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public ComponentDescriptionDTO getComponentDescriptionDTO(Bundle arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOs(Bundle... arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
	public void bindSCR(ScrService scrService) {
		this.scrService = scrService;
	}

	public void unbindSCR(ScrService scrService) {
		this.scrService = null;
	}

//	@Override
	public Collection<ComponentDescriptionDTO> getAllComponentDescriptionDTO() {
		List<ComponentDescriptionDTO> components = new ArrayList<ComponentDescriptionDTO>();
		org.apache.felix.scr.Component[] allComponents = scrService.getComponents();
		for (org.apache.felix.scr.Component component : allComponents) {
			ComponentDescriptionDTO componentDescriptionDTO = new ComponentDescriptionDTO();
			componentDescriptionDTO.name = component.getName();
			componentDescriptionDTO.serviceInterfaces = component.getServices();
			componentDescriptionDTO.activate = component.getActivate();
			componentDescriptionDTO.deactivate = component.getDeactivate();
			componentDescriptionDTO.immediate = component.isImmediate();
			componentDescriptionDTO.defaultEnabled = component.isDefaultEnabled();
			componentDescriptionDTO.factory = component.getFactory();
			componentDescriptionDTO.modified = component.getModified();
			
			Dictionary<String,Object> dictionary = component.getProperties();
			if (dictionary == null) {
			    return null;
			  }
			  Map<String, Object> map = new HashMap<String, Object>(dictionary.size());
			  Enumeration<String> keys = dictionary.keys();
			  while (keys.hasMoreElements()) {
			    String key = keys.nextElement();
			    map.put(key, dictionary.get(key));
			  }
			componentDescriptionDTO.properties = map;
			componentDescriptionDTO.implementationClass = component.getClassName();
			componentDescriptionDTO.configurationPolicy = component.getConfigurationPolicy();
			List<ReferenceDTO> referenceDTOs = new ArrayList<>(1);
			org.apache.felix.scr.Reference[] references = component.getReferences();
			if(references != null) {
				for (org.apache.felix.scr.Reference reference : references) {
					if(reference.isSatisfied()) {
						SatisfiedReferenceDTO satisfiedReferenceDTO = new SatisfiedReferenceDTO();
						satisfiedReferenceDTO.name = reference.getName();
						satisfiedReferenceDTO.target = reference.getTarget();
//						Vector boundServiceReferences = ((org.eclipse.equinox.internal.ds.Reference)reference).getBoundServiceReferences();
						ServiceReference[] serviceReferences = reference.getServiceReferences();
						List<ServiceReferenceDTO> serviceReferenceDTOs = new ArrayList<ServiceReferenceDTO>(1);
						if(serviceReferences != null) {
							for (ServiceReference serviceReference : serviceReferences) {
								ServiceReferenceDTO serviceReferenceDTO = new ServiceReferenceDTO();
//								serviceReferenceDTO.id = serviceReference.
								serviceReferenceDTOs.add(serviceReferenceDTO);
							}
						}
						satisfiedReferenceDTO.boundServices = serviceReferenceDTOs.toArray(new ServiceReferenceDTO[serviceReferenceDTOs.size()]);
//						referenceDTOs.add(satisfiedReferenceDTO);
					} else {
						UnsatisfiedReferenceDTO unsatisfiedReferenceDTO = new UnsatisfiedReferenceDTO();
						unsatisfiedReferenceDTO.name = reference.getName();
						unsatisfiedReferenceDTO.target = reference.getTarget();
						ServiceReference[] serviceReferences = reference.getServiceReferences();
						List<ServiceReferenceDTO> serviceReferenceDTOs = new ArrayList<ServiceReferenceDTO>(1);
						if(serviceReferences != null) {
							for (ServiceReference serviceReference : serviceReferences) {
								ServiceReferenceDTO serviceReferenceDTO = new ServiceReferenceDTO();
//								serviceReferenceDTO.id = serviceReference.
								serviceReferenceDTOs.add(serviceReferenceDTO);
							}
						}
						unsatisfiedReferenceDTO.targetServices = serviceReferenceDTOs.toArray(new ServiceReferenceDTO[serviceReferenceDTOs.size()]);
					}
					
				}
			}
			
			componentDescriptionDTO.references = referenceDTOs.toArray(new ReferenceDTO[referenceDTOs.size()]);
			
			components.add(componentDescriptionDTO);
		}
		return components;
	}

//	@Override
	public Collection<ComponentConfigurationDTO> getAllComponentConfigurationDTO() {
		List<ComponentConfigurationDTO> components = new ArrayList<ComponentConfigurationDTO>();
		org.apache.felix.scr.Component[] allComponents = scrService.getComponents();
		for (org.apache.felix.scr.Component component : allComponents) {
			ComponentConfigurationDTO componentConfigurationDTO = new ComponentConfigurationDTO();
			componentConfigurationDTO.id = component.getId();
			
			ComponentDescriptionDTO componentDescription = createDescription(component);
			componentConfigurationDTO.description = componentDescription;
			
			
			org.apache.felix.scr.Reference[] references = component.getReferences();
			if(references != null) {
				List<SatisfiedReferenceDTO> satifiedReferences = new ArrayList<>(1);
				List<UnsatisfiedReferenceDTO> unsatifiedReferences = new ArrayList<>(1);
				for (org.apache.felix.scr.Reference reference : references) {
					if(reference.isSatisfied()) {
						SatisfiedReferenceDTO satisfiedReferenceDTO = new SatisfiedReferenceDTO();
						satisfiedReferenceDTO.name = reference.getName();
						satisfiedReferenceDTO.target = reference.getTarget();
//						Vector boundServiceReferences = ((org.eclipse.equinox.internal.ds.Reference)reference).getBoundServiceReferences();
						ServiceReference[] serviceReferences = reference.getServiceReferences();
						List<ServiceReferenceDTO> serviceReferenceDTOs = new ArrayList<ServiceReferenceDTO>(1);
						if(serviceReferences != null) {
							for (ServiceReference serviceReference : serviceReferences) {
								ServiceReferenceDTO serviceReferenceDTO = new ServiceReferenceDTO();
//								serviceReferenceDTO.id = serviceReference.
//								serviceReferenceDTOs.add(serviceReferenceDTO);
							}
						}
						satisfiedReferenceDTO.boundServices = serviceReferenceDTOs.toArray(new ServiceReferenceDTO[serviceReferenceDTOs.size()]);
						satifiedReferences.add(satisfiedReferenceDTO);
					} else {
						UnsatisfiedReferenceDTO unsatisfiedReferenceDTO = new UnsatisfiedReferenceDTO();
						unsatisfiedReferenceDTO.name = reference.getName();
						unsatisfiedReferenceDTO.target = reference.getTarget();
						ServiceReference[] serviceReferences = reference.getServiceReferences();
						List<ServiceReferenceDTO> serviceReferenceDTOs = new ArrayList<ServiceReferenceDTO>(1);
						if(serviceReferences != null) {
							for (ServiceReference serviceReference : serviceReferences) {
								ServiceReferenceDTO serviceReferenceDTO = new ServiceReferenceDTO();
//								serviceReferenceDTO.id = serviceReference.
//								serviceReferenceDTOs.add(serviceReferenceDTO);
							}
						}
						unsatisfiedReferenceDTO.targetServices = serviceReferenceDTOs.toArray(new ServiceReferenceDTO[serviceReferenceDTOs.size()]);
						unsatifiedReferences.add(unsatisfiedReferenceDTO);
					}
				}
				componentConfigurationDTO.satisfiedReferences = satifiedReferences.toArray(new SatisfiedReferenceDTO[satifiedReferences.size()]);
				componentConfigurationDTO.unsatisfiedReferences = unsatifiedReferences.toArray(new UnsatisfiedReferenceDTO[unsatifiedReferences.size()]);
			}
			Dictionary<String,Object> dictionary = component.getProperties();
			if (dictionary == null) {
			    return null;
			  }
			  Map<String, Object> map = new HashMap<String, Object>(dictionary.size());
			  Enumeration<String> keys = dictionary.keys();
			  while (keys.hasMoreElements()) {
			    String key = keys.nextElement();
			    map.put(key, dictionary.get(key));
			  }
			  ;
			componentConfigurationDTO.properties = map;
			if(component.getState() == org.apache.felix.scr.Component.STATE_UNSATISFIED) {
				if(componentConfigurationDTO.unsatisfiedReferences == null || componentConfigurationDTO.unsatisfiedReferences.length == 0) {
					componentConfigurationDTO.state = ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION;
				} else {
					componentConfigurationDTO.state = ComponentConfigurationDTO.UNSATISFIED_REFERENCE;
				}
			} else if(component.getState() == org.apache.felix.scr.Component.STATE_REGISTERED) {
				componentConfigurationDTO.state = ComponentConfigurationDTO.SATISFIED;
			} else if(component.getState() == org.apache.felix.scr.Component.STATE_ACTIVE) {
				componentConfigurationDTO.state = ComponentConfigurationDTO.ACTIVE;
			}
			components.add(componentConfigurationDTO);
		}
		return components;
	}
	
	private ComponentDescriptionDTO createDescription(org.apache.felix.scr.Component component) {
		ComponentDescriptionDTO componentDescriptionDTO = new ComponentDescriptionDTO();
		componentDescriptionDTO.name = component.getName();
		componentDescriptionDTO.serviceInterfaces = component.getServices();
		componentDescriptionDTO.activate = component.getActivate();
		componentDescriptionDTO.deactivate = component.getDeactivate();
		componentDescriptionDTO.immediate = component.isImmediate();
		componentDescriptionDTO.defaultEnabled = component.isDefaultEnabled();
		componentDescriptionDTO.factory = component.getFactory();
		componentDescriptionDTO.modified = component.getModified();
		
		Dictionary<String,Object> dictionary = component.getProperties();
		if (dictionary == null) {
		    return null;
		  }
		  Map<String, Object> map = new HashMap<String, Object>(dictionary.size());
		  Enumeration<String> keys = dictionary.keys();
		  while (keys.hasMoreElements()) {
		    String key = keys.nextElement();
		    map.put(key, dictionary.get(key));
		  }
		componentDescriptionDTO.properties = map;
		componentDescriptionDTO.implementationClass = component.getClassName();
		componentDescriptionDTO.configurationPolicy = component.getConfigurationPolicy();
		List<ReferenceDTO> referenceDTOs = new ArrayList<>(1);
		org.apache.felix.scr.Reference[] references = component.getReferences();
		if(references != null) {
			for (org.apache.felix.scr.Reference reference : references) {
				
			}
		}
		
		componentDescriptionDTO.references = referenceDTOs.toArray(new ReferenceDTO[referenceDTOs.size()]);
		return componentDescriptionDTO;
	}

	@Override
	public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOsRMI(ComponentDescriptionDTO arg0)
			throws RemoteException {
		return getComponentConfigurationDTOs(arg0);
	}

	@Override
	public ComponentDescriptionDTO getComponentDescriptionDTORMI(Bundle arg0, String arg1) throws RemoteException {
		return getComponentDescriptionDTO(arg0, arg1);
	}

	@Override
	public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOsRMI(Bundle[] arg0) throws RemoteException {
		return getComponentDescriptionDTOs(arg0);
	}

	@Override
	public Collection<ComponentDescriptionDTO> getAllComponentDescriptionDTORMI() throws RemoteException {
		return getAllComponentDescriptionDTO();
	}

	@Override
	public Collection<ComponentConfigurationDTO> getAllComponentConfigurationDTORMI() throws RemoteException {
		return getAllComponentConfigurationDTO();
	}

}

/*
 * Copyright 2016 Jens Kuebler
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kuebler.osgi.ds.equinox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;
import org.osgi.service.component.runtime.dto.SatisfiedReferenceDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;
import org.osgi.util.promise.Promise;

@Component(property = {"service.exported.interfaces=*", "service.exported.configs=ecf.generic.server"})
public class EquinoxSCR implements ServiceComponentRuntime {

	@Override
	public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOs() {
		Bundle[] paramVarArgs = null;
		ScrService scrManager = _scrManager.get();
		List<ComponentDescriptionDTO> componentDescriptions = new ArrayList<>();
		
		if(paramVarArgs == null || paramVarArgs.length == 0) {
			org.apache.felix.scr.Component[] components = scrManager.getComponents();
			for (org.apache.felix.scr.Component component : components) {
				componentDescriptions.add(createComponentDTO(component));
			}
			
		} else {
			for (Bundle bundle : paramVarArgs) {
				org.apache.felix.scr.Component[] components = scrManager.getComponents(bundle);
				for (org.apache.felix.scr.Component component : components) {
					componentDescriptions.add(createComponentDTO(component));
					
				}
			}
		}
		
		return componentDescriptions;
	}

	private ComponentDescriptionDTO createComponentDTO(org.apache.felix.scr.Component component) {
		ComponentDescriptionDTO componentDescriptionDTO = new ComponentDescriptionDTO();;
		componentDescriptionDTO.name = component.getName();
		componentDescriptionDTO.configurationPolicy = component.getConfigurationPolicy();
		componentDescriptionDTO.serviceInterfaces = component.getServices();
		componentDescriptionDTO.activate = component.getActivate();
		componentDescriptionDTO.deactivate = component.getDeactivate();
		componentDescriptionDTO.immediate = component.isImmediate();
		componentDescriptionDTO.defaultEnabled = component.isDefaultEnabled();
		componentDescriptionDTO.factory = component.getFactory();
		componentDescriptionDTO.modified = component.getModified();
		componentDescriptionDTO.implementationClass = component.getClassName();
		componentDescriptionDTO.configurationPolicy = component.getConfigurationPolicy();
		org.apache.felix.scr.Reference[] references = component.getReferences();
		List<ReferenceDTO> referenceDTOs = new ArrayList<>();
		if(references != null) {
			for (org.apache.felix.scr.Reference reference : references) {
				ReferenceDTO referenceDTO = new ReferenceDTO();
				referenceDTO.name = reference.getName();
				referenceDTO.interfaceName = reference.getServiceName();
				if(reference.isMultiple() && reference.isOptional())  {
					referenceDTO.cardinality = "0..*";
				}
				if(!reference.isMultiple() && reference.isOptional())  {
					referenceDTO.cardinality = "0..1";
				}
				if(!reference.isMultiple() && !reference.isOptional())  {
					referenceDTO.cardinality = "1..1";
				}
				if(reference.isMultiple() && !reference.isOptional())  {
					referenceDTO.cardinality = "1..*";
				}

				referenceDTO.target = reference.getTarget();
//			referenceDTO.cardinality = reference.
				referenceDTOs.add(referenceDTO);
			}
		}
		componentDescriptionDTO.references = referenceDTOs.toArray(new ReferenceDTO[referenceDTOs.size()]);  
		return componentDescriptionDTO;
	}

	@Override
	public ComponentDescriptionDTO getComponentDescriptionDTO(String paramString) {
		return null;
	}

	@Override
	public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOs(
			ComponentDescriptionDTO paramComponentDescriptionDTO) {
		ScrService scrService = _scrManager.get();
		org.apache.felix.scr.Component[] components = scrService.getComponents(paramComponentDescriptionDTO.name);
		List<ComponentConfigurationDTO> componentDescriptions = new ArrayList<>();

		for (org.apache.felix.scr.Component component : components) {
			ComponentConfigurationDTO componentConfigurationDTO = new ComponentConfigurationDTO();
			componentConfigurationDTO.id = component.getId();
			switch (component.getState()) {
			case org.apache.felix.scr.Component.STATE_ACTIVE:
				componentConfigurationDTO.state = ComponentConfigurationDTO.ACTIVE;
				break;
			case org.apache.felix.scr.Component.STATE_ACTIVATING:
			case org.apache.felix.scr.Component.STATE_REGISTERED:
				componentConfigurationDTO.state = ComponentConfigurationDTO.SATISFIED;
				break;
			case org.apache.felix.scr.Component.STATE_UNSATISFIED:
				if(componentConfigurationDTO.unsatisfiedReferences == null || componentConfigurationDTO.unsatisfiedReferences.length == 0) {
					componentConfigurationDTO.state = ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION;
				} else {
					componentConfigurationDTO.state = ComponentConfigurationDTO.UNSATISFIED_REFERENCE;
				}
			default:
				break;
			}
			Dictionary dict = component.getProperties();
			Enumeration keys = dict.keys();
			Map<String,Object> properties = new HashMap<>();
			while(keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				properties.put(key, dict.get(key));
			}
			
			componentConfigurationDTO.properties = properties;
			componentConfigurationDTO.description = createComponentDTO(component);
			List<SatisfiedReferenceDTO> satisfiedReferences = new ArrayList<>(); 
			org.apache.felix.scr.Reference[] references = component.getReferences();
			if(references != null) {
				for (org.apache.felix.scr.Reference reference : references) {
					List<UnsatisfiedReferenceDTO> unboundServices = new ArrayList<>();
					if(reference.isSatisfied()) {
						SatisfiedReferenceDTO satisfiedReferenceDTO = new SatisfiedReferenceDTO();
						List<ServiceReferenceDTO> boundServices = new ArrayList<>();
						ServiceReference[] serviceReferences = reference.getServiceReferences();
						if(serviceReferences != null) {
							for (ServiceReference serviceReference : serviceReferences) {
								ServiceReferenceDTO serviceReferenceDTO = new ServiceReferenceDTO();
								serviceReferenceDTO.bundle = serviceReference.getBundle().getBundleId();
								serviceReferenceDTO.id = (long) serviceReference.getProperty(Constants.SERVICE_ID);
								Bundle[] usingBundles = serviceReference.getUsingBundles();
								long[] usingBundlesDTO = new long[usingBundles.length];
								for (int i = 0; i < usingBundles.length; i++) {
									Bundle b = usingBundles[i];
									usingBundlesDTO[i] = b.getBundleId();
								}
								serviceReferenceDTO.usingBundles = usingBundlesDTO;
								String[] propertyKeys = serviceReference.getPropertyKeys();
								Map<String,Object> serviceProperties = new HashMap<>();
								for (String key : propertyKeys) {
									Object property = serviceReference.getProperty(key);
									serviceProperties.put(key, property);
								}
								serviceReferenceDTO.properties = serviceProperties; 
								boundServices.add(serviceReferenceDTO);
							}
							satisfiedReferences.add(satisfiedReferenceDTO);
							satisfiedReferenceDTO.boundServices = boundServices.toArray(new ServiceReferenceDTO[]{});
							satisfiedReferenceDTO.name = reference.getName();
							satisfiedReferenceDTO.target = reference.getTarget();
						}
					} else {
						UnsatisfiedReferenceDTO unsatisfiedReferenceDTO = new UnsatisfiedReferenceDTO();
						unsatisfiedReferenceDTO.name = reference.getName();
						unsatisfiedReferenceDTO.target = reference.getTarget();
//						unsatisfiedReferenceDTO.targetServices = reference.getServiceName();
					}
				}
			}
			 componentConfigurationDTO.satisfiedReferences = satisfiedReferences.toArray(new SatisfiedReferenceDTO[]{});
			componentDescriptions.add(componentConfigurationDTO);
		}
		
		return componentDescriptions;
	}

	@Override
	public boolean isComponentEnabled(ComponentDescriptionDTO paramComponentDescriptionDTO) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Promise<Void> enableComponent(ComponentDescriptionDTO paramComponentDescriptionDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<Void> disableComponent(ComponentDescriptionDTO paramComponentDescriptionDTO) {
		// TODO Auto-generated method stub
		return null;
	}
	
	  private AtomicReference<ScrService> _scrManager = new AtomicReference<ScrService>();

	  /**
	   * OSGi bind method
	   * @param name
	   */
	  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
	  public void bindSCRManager(ScrService name) {
	    _scrManager.set(name);
	  }

	  /**
	   * OSGi unbind method
	   * @param name
	   */
	  public void unbindSCRManager(ScrService name) {
	    _scrManager.compareAndSet(name, null);
	  }

}

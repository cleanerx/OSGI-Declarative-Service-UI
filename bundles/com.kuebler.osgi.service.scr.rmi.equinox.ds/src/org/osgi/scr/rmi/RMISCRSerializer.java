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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.ScrService;
import org.eclipse.equinox.internal.ds.model.ComponentReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.scr.rmi.api.IRMINotifier;
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

import com.kuebler.osgi.service.dto.serial.ComponentConfigurationDTOSerializable;
import com.kuebler.osgi.service.dto.serial.ComponentDescriptionDTOSerializable;
import com.kuebler.osgi.service.dto.serial.ReferenceDTOSerializable;
import com.kuebler.osgi.service.dto.serial.SatisfiedReferenceDTOSerializable;
import com.kuebler.osgi.service.dto.serial.ServiceReferenceDTOSerializable;
import com.kuebler.osgi.service.dto.serial.UnsatisfiedReferenceDTOSerializable;


@Component(immediate = true)
public class RMISCRSerializer extends UnicastRemoteObject implements IRMIServiceSerializer {

	public RMISCRSerializer() throws RemoteException {
		super();
		registry = LocateRegistry.createRegistry(1099);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -2651396002481803362L;

	private Registry registry;

	private ScrService scrService;

	private IRMINotifier notifier;

  private BundleContext _bundleContext;

	@Activate
	public void activate(BundleContext bundleContext) throws RemoteException {
    _bundleContext = bundleContext;
    registry.rebind("Server", this);
	}

	@Deactivate
	public void deactivate() throws RemoteException, NotBoundException {
    _bundleContext = null;
		registry.unbind("Server");
	}

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
	public void bindSCR(ScrService scrService) {
		this.scrService = scrService;
	}

	public void unbindSCR(ScrService scrService) {
		this.scrService = null;
	}


//	@Override
	@SuppressWarnings("restriction")
	public Collection<ComponentConfigurationDTO> getAllComponentConfigurationDTO() {
		List<ComponentConfigurationDTO> components = new ArrayList<ComponentConfigurationDTO>();
		org.apache.felix.scr.Component[] allComponents = scrService.getComponents();
		for (org.apache.felix.scr.Component component : allComponents) {
      ComponentConfigurationDTO componentConfigurationDTO = new ComponentConfigurationDTOSerializable();
			componentConfigurationDTO.id = component.getId();

			ComponentDescriptionDTO componentDescription = createDescription(component);
			componentConfigurationDTO.description = componentDescription;


			org.apache.felix.scr.Reference[] references = component.getReferences();
			if(references != null) {
				List<SatisfiedReferenceDTO> satifiedReferences = new ArrayList<>(1);
				List<UnsatisfiedReferenceDTO> unsatifiedReferences = new ArrayList<>(1);
				List<ReferenceDTO> referenceDTOs = new ArrayList<ReferenceDTO>(1);
				for (org.apache.felix.scr.Reference reference : references) {
					if(reference.isSatisfied()) {
            SatisfiedReferenceDTO satisfiedReferenceDTO = new SatisfiedReferenceDTOSerializable();
						satisfiedReferenceDTO.name = reference.getName();
						satisfiedReferenceDTO.target = reference.getTarget();
						List<ServiceReferenceDTO> serviceReferenceDTOs = createServiceReferenceDTOs(reference);
						satisfiedReferenceDTO.boundServices = serviceReferenceDTOs.toArray(new ServiceReferenceDTO[serviceReferenceDTOs.size()]);
						satifiedReferences.add(satisfiedReferenceDTO);
					} else {
            UnsatisfiedReferenceDTO unsatisfiedReferenceDTO = new UnsatisfiedReferenceDTOSerializable();
						unsatisfiedReferenceDTO.name = reference.getName();
						unsatisfiedReferenceDTO.target = reference.getTarget();
            List<ServiceReferenceDTO> serviceReferenceDTOs = createServiceReferenceDTOs(reference);
						unsatisfiedReferenceDTO.targetServices = serviceReferenceDTOs.toArray(new ServiceReferenceDTO[serviceReferenceDTOs.size()]);
						unsatifiedReferences.add(unsatisfiedReferenceDTO);
					}
          ReferenceDTO referenceDTO = new ReferenceDTOSerializable();
					referenceDTO.name = reference.getName();
					referenceDTO.interfaceName = reference.getServiceName();
					referenceDTO.target = reference.getTarget();
					referenceDTO.bind = reference.getBindMethodName();
					referenceDTO.unbind = reference.getUnbindMethodName();
					referenceDTO.updated = reference.getUpdatedMethodName();
					 if (reference.isMultiple() && reference.isOptional()) {
					 referenceDTO.cardinality = "(0..*)";
					 } else if (reference.isMultiple() && !reference.isOptional()) {
						 referenceDTO.cardinality = "(1..*)";
					 } else if (!reference.isMultiple() && !reference.isOptional()) {
						 referenceDTO.cardinality = "(1..1)";
					 } else if (!reference.isMultiple() && reference.isOptional()) {
						 referenceDTO.cardinality= "(0..1)";
					 }

					referenceDTOs.add(referenceDTO);
				}
        componentConfigurationDTO.description.references = referenceDTOs.toArray(new ReferenceDTOSerializable[referenceDTOs.size()]);
        componentConfigurationDTO.satisfiedReferences = satifiedReferences.toArray(new SatisfiedReferenceDTOSerializable[satifiedReferences.size()]);
        componentConfigurationDTO.unsatisfiedReferences = unsatifiedReferences.toArray(new UnsatisfiedReferenceDTOSerializable[unsatifiedReferences.size()]);
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
      } else {
        System.out.println("state:" + component.getState()); //TODO (vikajkr): REMOVE!
			}
			components.add(componentConfigurationDTO);
		}
		return components;
	}

	private List<ServiceReferenceDTO> createServiceReferenceDTOs(org.apache.felix.scr.Reference reference) {
		ServiceReference[] serviceReferences = reference.getServiceReferences();
		List<ServiceReferenceDTO> serviceReferenceDTOs = new ArrayList<ServiceReferenceDTO>(1);
    if (reference instanceof org.eclipse.equinox.internal.ds.Reference) {
      org.eclipse.equinox.internal.ds.Reference dsReference = (org.eclipse.equinox.internal.ds.Reference) reference;
        ComponentReference componentReference = dsReference.reference;
      if (componentReference != null) {
        ServiceReference[] serviceReferences2 = componentReference.getServiceReferences();
        if (serviceReferences2 != null) {
          for (ServiceReference serviceReferenceX : serviceReferences2) {
            ServiceReferenceDTO serviceReferenceDTO = createServiceReferenceDTO(serviceReferenceX);
            serviceReferenceDTOs.add(serviceReferenceDTO);
          }
        }
      } else {
        System.out.println("what"); //TODO (vikajkr): REMOVE!
        }
      //      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      //        // TODO Auto-generated catch block
      //        e.printStackTrace();
      //      }
    }
		if(serviceReferences != null) {
			for (ServiceReference serviceReference : serviceReferences) {
        ServiceReferenceDTO serviceReferenceDTO = new ServiceReferenceDTOSerializable();
				String[] keys2 = serviceReference.getPropertyKeys();
				Map<String, Object> properties = new HashMap<String, Object>(keys2.length);
				for (String k : keys2) {
					Object v = serviceReference.getProperty(k);
					if (Constants.SERVICE_ID.equals(k)) {
						serviceReferenceDTO.id = ((Long) v).longValue();
					}
					properties.put(k, v);
				}
				serviceReferenceDTO.properties = properties;
				serviceReferenceDTOs.add(serviceReferenceDTO);
			}
		}
		return serviceReferenceDTOs;
	}

  private ServiceReferenceDTO createServiceReferenceDTO(ServiceReference serviceReferenceX) {
    ServiceReferenceDTO serviceReferenceDTO = new ServiceReferenceDTOSerializable();
    Map<String, Object> properties = new HashMap<String, Object>(serviceReferenceX.getPropertyKeys().length);
    for (String propertyKey : serviceReferenceX.getPropertyKeys()) {
      Object value = serviceReferenceX.getProperty(propertyKey);
      if (Constants.SERVICE_ID.equals(propertyKey)) {
        serviceReferenceDTO.id = ((Long) value).longValue();
      }
      properties.put(propertyKey, value);
    }
    serviceReferenceDTO.properties = properties;
    return serviceReferenceDTO;
  }

	private ComponentDescriptionDTO createDescription(org.apache.felix.scr.Component component) {
    ComponentDescriptionDTO componentDescriptionDTO = new ComponentDescriptionDTOSerializable();
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

    componentDescriptionDTO.references = referenceDTOs.toArray(new ReferenceDTOSerializable[referenceDTOs.size()]);
		return componentDescriptionDTO;
	}

	@Override
	public Collection<ComponentConfigurationDTO> getAllComponentConfigurationDTORMI() throws RemoteException {
		return getAllComponentConfigurationDTO();
	}

  /**
   * @see org.osgi.scr.rmi.api.IRMIServiceSerializer#getAllServiceReferenceDTORMI()
   */
  @Override
  public Collection<ServiceReferenceDTO> getAllServiceReferenceDTORMI() throws RemoteException {
    List<ServiceReferenceDTO> serviceReferences = new ArrayList<ServiceReferenceDTO>();
    try {
      ServiceReference<?>[] services = _bundleContext.getServiceReferences((String) null, (String) null);
      for (ServiceReference<?> serviceReference : services) {
        ServiceReferenceDTO referenceDTO = createServiceReferenceDTO(serviceReference);
        serviceReferences.add(referenceDTO);
      }

    } catch (InvalidSyntaxException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
    return serviceReferences;
  }

}

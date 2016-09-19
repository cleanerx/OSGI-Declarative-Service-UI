package com.kuebler.osgi.ds.equinox;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.equinox.internal.ds.SCRManager;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.promise.Promise;

@Component
public class EquinoxSCR implements ServiceComponentRuntime {

	@Override
	public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOs(Bundle... paramVarArgs) {
		SCRManager scrManager = _scrManager.get();
		
		for (Bundle bundle : paramVarArgs) {
			org.apache.felix.scr.Component[] components = scrManager.getComponents(bundle);
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComponentDescriptionDTO getComponentDescriptionDTO(Bundle paramBundle, String paramString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOs(
			ComponentDescriptionDTO paramComponentDescriptionDTO) {
		// TODO Auto-generated method stub
		return null;
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
	
	  private AtomicReference<SCRManager> _scrManager = new AtomicReference<SCRManager>();

	  /**
	   * OSGi bind method
	   * @param name
	   */
	  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
	  public void bindSCRManager(SCRManager name) {
	    _scrManager.set(name);
	  }

	  /**
	   * OSGi unbind method
	   * @param name
	   */
	  public void unbindSCRManager(SCRManager name) {
	    _scrManager.compareAndSet(name, null);
	  }

}

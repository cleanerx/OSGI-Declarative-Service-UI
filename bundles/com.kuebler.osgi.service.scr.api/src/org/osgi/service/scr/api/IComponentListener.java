package org.osgi.service.scr.api;

import org.osgi.service.component.runtime.dto.serial.ComponentConfigurationDTO;

public interface IComponentListener {
	
	void newComponentRuntime(String componentRuntimeName);

	void disposedComponentRuntime(String componentRuntimeName);

	void receive(String componentRuntimeName, ComponentConfigurationDTO[] configurations);
	
	void update(String componentRuntimeName, ComponentConfigurationDTO configurationDTO);
}

package org.osgi.scr.rmi.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import org.osgi.service.component.runtime.dto.serial.ComponentConfigurationDTO;

public interface IRMINotifier extends Remote {

	void newComponentRuntimeRMI(String componentRuntimeName) throws RemoteException;

	void disposedComponentRuntimeRMI(String componentRuntimeName) throws RemoteException;

	void receiveRMI(String componentRuntimeName, Collection<ComponentConfigurationDTO> configurations) throws RemoteException;
	
	void updateRMI(String componentRuntimeName, ComponentConfigurationDTO configurationDTO) throws RemoteException;
}

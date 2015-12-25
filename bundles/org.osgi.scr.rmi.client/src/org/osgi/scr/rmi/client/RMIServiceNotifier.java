package org.osgi.scr.rmi.client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.osgi.scr.rmi.api.IRMINotifier;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.scr.api.IComponentListener;

@Component(immediate = true)
public class RMIServiceNotifier extends UnicastRemoteObject implements IRMINotifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 988075657292870574L;
	private Registry registry;
	private List<IComponentListener> componentListener;

	public RMIServiceNotifier() throws RemoteException {
		super();
		registry = LocateRegistry.createRegistry(1100);
		registry.rebind("RMINotifier", this);
		componentListener = Collections.synchronizedList(new ArrayList<IComponentListener>());
	}
	
	@Override
	public void newComponentRuntimeRMI(String componentRuntimeName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disposedComponentRuntimeRMI(String componentRuntimeName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveRMI(String componentRuntimeName, Collection<ComponentConfigurationDTO> configurations)
			throws RemoteException {
		List<IComponentListener> list = new ArrayList<IComponentListener>(componentListener);
		for (IComponentListener componentListener : list) {
			componentListener.receive(componentRuntimeName, configurations.toArray(new ComponentConfigurationDTO[configurations.size()]));
		}
	}

	@Override
	public void updateRMI(String componentRuntimeName, ComponentConfigurationDTO configurationDTO)
			throws RemoteException {
		// TODO Auto-generated method stub
	}
	
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void bindComponentNotifier(IComponentListener componentListener) {
		this.componentListener.add(componentListener);
	}

	public void unbindComponentNotifier(IComponentListener componentListener) {
		this.componentListener.remove(componentListener);
	}

}

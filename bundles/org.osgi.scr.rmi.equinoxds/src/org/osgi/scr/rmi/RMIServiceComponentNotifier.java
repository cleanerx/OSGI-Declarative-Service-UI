package org.osgi.scr.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.scr.rmi.api.IRMINotifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;

//@Component
public class RMIServiceComponentNotifier  {

	private IRMINotifier notifier;

	@Activate
	void activate(BundleContext bundleContext) throws MalformedURLException, RemoteException, NotBoundException {
		bundleContext.addServiceListener(new ServiceListener() {
			
			@Override
			public void serviceChanged(ServiceEvent event) {
				try {
					notifier.receiveRMI(System.getProperty("application.name"), null);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}


}

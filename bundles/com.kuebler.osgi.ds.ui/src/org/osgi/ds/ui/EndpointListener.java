package org.osgi.ds.ui;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionLocator;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointEvent;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;

public class EndpointListener implements EndpointEventListener, org.osgi.service.remoteserviceadmin.EndpointListener {

	private IEndpointDescriptionLocator _locator;

	@Override
	public void endpointChanged(EndpointEvent arg0, String arg1) {
		System.out.println(arg0);

	}

	@Override
	public void endpointAdded(EndpointDescription arg0, String arg1) {
		System.out.println(arg0);
		
	}

	@Override
	public void endpointRemoved(EndpointDescription arg0, String arg1) {
		System.out.println(arg0);
		
	}
	
	  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
	  public void bindDescriptionLocator(IEndpointDescriptionLocator locator) {
	    _locator = locator;
	  }

	  /**
	   * @param metamodelManager
	   */
	  public void unbindDescriptionLocator(IEndpointDescriptionLocator locator) {
		  _locator = null;
	  }
	  
}

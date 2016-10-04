package com.kuebler.osgi.ds.equinox;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * Add additional information to augment a property for the application being debugged
 *
 */
@Component
public class EquinoxAppConfigurator {

	@Activate
	public void activate() throws IOException {
		ConfigurationAdmin configurationAdmin = _configAdmin.get();
		Configuration configuration = configurationAdmin.getConfiguration(EquinoxSCR.class.getName());
		Dictionary<String, Object> properties = configuration.getProperties();
		Properties properties2 = System.getProperties();
		configuration.update(properties);
	}

	
	private AtomicReference<ConfigurationAdmin> _configAdmin = new AtomicReference<ConfigurationAdmin>();

	  /**
	   * OSGi bind method
	   * @param name
	   */
	  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
	  public void bindConfigAdmin(ConfigurationAdmin configAdmin) {
	    _configAdmin.set(configAdmin);
	  }

	  /**
	   * OSGi unbind method
	   * @param name
	   */
	  public void unbindConfigAdmin(ConfigurationAdmin name) {
	    _configAdmin.compareAndSet(name, null);
	  }}

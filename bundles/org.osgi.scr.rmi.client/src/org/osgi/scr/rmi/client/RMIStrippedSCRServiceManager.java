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
package org.osgi.scr.rmi.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import org.osgi.framework.Bundle;
import org.osgi.scr.rmi.api.IRMIServiceSerializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.scr.api.StrippedServiceComponentRuntime;

@Component
public class RMIStrippedSCRServiceManager implements StrippedServiceComponentRuntime {

	private IRMIServiceSerializer serializer;

	@Activate
	public void activate() throws MalformedURLException, RemoteException, NotBoundException {
		serializer = (IRMIServiceSerializer) Naming.lookup("//localhost/Server");
	}
	
	@Override
	public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOs(ComponentDescriptionDTO arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComponentDescriptionDTO getComponentDescriptionDTO(Bundle arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOs(Bundle[] arg0) {
		try {
			return serializer.getAllComponentDescriptionDTORMI();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Collection<ComponentDescriptionDTO> getAllComponentDescriptionDTO() {
		try {
			return serializer.getAllComponentDescriptionDTORMI();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Collection<ComponentConfigurationDTO> getAllComponentConfigurationDTO() {
		try {
			return serializer.getAllComponentConfigurationDTORMI();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

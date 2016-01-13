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

import org.osgi.framework.BundleContext;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.scr.rmi.api.IRMIServiceSerializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.scr.api.StrippedServiceComponentRuntime;

@Component
public class RMIStrippedSCRServiceManager implements StrippedServiceComponentRuntime {

	private IRMIServiceSerializer serializer;

  private BundleContext _bundleContext;

	@Activate
  public void activate(BundleContext bundleContext) {
    _bundleContext = bundleContext;
    Thread thread = new Thread(new Runnable() {

      @Override
      public void run() {
        while (serializer == null) {
          try {
            serializer = (IRMIServiceSerializer) Naming.lookup("//localhost/Server");
          } catch (MalformedURLException e) {
            // throw
          } catch (RemoteException e) {
            e.printStackTrace();
            //
          } catch (NotBoundException e) {
            //
          }
          if (serializer == null) {
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
    thread.start();
	}

	@Override
  public Collection<ComponentConfigurationDTO> getAllComponentConfigurationDTO() {
    if (serializer != null) {
      try {
        return serializer.getAllComponentConfigurationDTORMI();
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
		return null;
	}

  /**
   * @see org.osgi.service.scr.api.StrippedServiceComponentRuntime#getAllServiceReferenceDTO()
   */
  @Override
  public Collection<ServiceReferenceDTO> getAllServiceReferenceDTO() {
    if (serializer != null) {
      try {
        return serializer.getAllServiceReferenceDTORMI();
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return null;
  }

}

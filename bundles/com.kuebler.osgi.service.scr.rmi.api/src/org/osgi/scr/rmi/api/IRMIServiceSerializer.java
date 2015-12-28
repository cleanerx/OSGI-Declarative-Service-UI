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
package org.osgi.scr.rmi.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;

/**
 * Remote abstraction
 */
public interface IRMIServiceSerializer extends Remote {

  //	Collection<ComponentConfigurationDTO> getComponentConfigurationDTOsRMI(ComponentDescriptionDTO arg0) throws RemoteException;
  //
  //	ComponentDescriptionDTO getComponentDescriptionDTORMI(Bundle arg0, String arg1)  throws RemoteException;
  //
  //	Collection<ComponentDescriptionDTO> getComponentDescriptionDTOsRMI(Bundle[] arg0) throws RemoteException;
  //
  //	Collection<ComponentDescriptionDTO> getAllComponentDescriptionDTORMI() throws RemoteException;

	Collection<ComponentConfigurationDTO> getAllComponentConfigurationDTORMI() throws RemoteException;

}

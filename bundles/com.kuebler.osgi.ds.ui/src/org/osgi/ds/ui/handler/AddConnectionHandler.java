/*******************************************************************************
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
package org.osgi.ds.ui.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.ds.ui.DSUIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 *
 */
public class AddConnectionHandler extends AbstractHandler {

  /**
   *
   */
  public AddConnectionHandler() {
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    Shell shell = HandlerUtil.getActiveShell(event);

    BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();
    //    ServiceReference<IPreferencesService> preferenceReference = bundleContext.getServiceReference(IPreferencesService.class);
    //    IPreferencesService preferencesService = bundleContext.getService(preferenceReference);
    IEclipsePreferences node = InstanceScope.INSTANCE.getNode("org.osgi.ds.ui.preferences"); //$NON-NLS-1$
    Set<URI> uris = new LinkedHashSet<URI>();
    byte[] byteArray = node.getByteArray("Connections", null); //$NON-NLS-1$
    if (byteArray != null) {
      try {
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArray));
        uris = (Set<URI>) objectInputStream.readObject();
      } catch (IOException | ClassNotFoundException e) {
        throw new ExecutionException("failed to read preferences", e);
        //      Log.logException("", e); //$NON-NLS-1$
      }
    }

    InputDialog inputDialog = new InputDialog(shell, "Add Connection URI", "Please enter the URI to track", "rmi://localhost:1099/Server", null);
    int open = inputDialog.open();
    if (open == Window.OK) {
      try {
        URI uri = new URI(inputDialog.getValue());
        uris.add(uri);
      } catch (URISyntaxException e1) {
        e1.printStackTrace();
      }

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream;
      try {
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(uris);
        node.putByteArray("Connections", byteArrayOutputStream.toByteArray());

        node.flush();
      } catch (IOException | BackingStoreException e) {
        throw new ExecutionException("failed to read preferences", e);
      }
    }

    return null;
  }

}

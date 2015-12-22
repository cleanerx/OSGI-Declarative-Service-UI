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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.ds.ui.ComponentViewer;

public abstract class AbstractComponentHandler extends AbstractHandler {
	
	private int componentState;

	public AbstractComponentHandler(int componentState) {
		this.componentState = componentState;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(event);
	    Event selEvent = (Event) event.getTrigger();
	    MenuItem item = (MenuItem) selEvent.widget;
	    boolean selection = item.getSelection();
		
		if(workbenchPart instanceof ComponentViewer) {
			ComponentViewer componentViewer = (ComponentViewer) workbenchPart;
			if(selection) {
				componentViewer.setFilterStates(componentViewer.getFilterState() | componentState);
			} else {
				int filterState = componentViewer.getFilterState();
				filterState = filterState ^ componentState;
				componentViewer.setFilterStates(filterState);
			}
		}
		return null;
	}

}

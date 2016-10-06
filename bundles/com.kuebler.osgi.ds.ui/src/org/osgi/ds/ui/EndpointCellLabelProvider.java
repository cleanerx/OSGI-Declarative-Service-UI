package org.osgi.ds.ui;

import java.util.Map;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.osgi.service.remoteserviceadmin.EndpointDescription;

final class EndpointCellLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if(element instanceof EndpointDescription) {
				EndpointDescription endpointDescription = (EndpointDescription) element;
				if(cell.getColumnIndex() == 0) {
					cell.setText(endpointDescription.getFrameworkUUID());
				} else if(cell.getColumnIndex() == 1) {
					Map<String, Object> properties = endpointDescription.getProperties();
					Object endpointID = properties.get("ecf.endpoint.id");
					if(endpointID != null) {
						cell.setText(endpointID.toString());
					}
				} else if(cell.getColumnIndex() == 2) {
					Map<String, Object> properties = endpointDescription.getProperties();
					Object application = properties.get("eclipse.application");
					if(application != null) {
						cell.setText(application.toString());
					}
				} else if(cell.getColumnIndex() == 3) {
					Map<String, Object> properties = endpointDescription.getProperties();
					Object instanceArea = properties.get("osgi.instance.area");
					if(instanceArea != null) {
						cell.setText(instanceArea.toString());
					}
				}
				
			}
			
		}
	}
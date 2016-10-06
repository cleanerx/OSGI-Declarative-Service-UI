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
					cell.setText(properties.get("ecf.endpoint.id").toString());
				} else if(cell.getColumnIndex() == 2) {
					Map<String, Object> properties = endpointDescription.getProperties();
					cell.setText(properties.get("eclipse.application").toString());
				} else if(cell.getColumnIndex() == 3) {
					Map<String, Object> properties = endpointDescription.getProperties();
					cell.setText(properties.get("osgi.instance.area").toString());
				}
				
			}
			
		}
	}
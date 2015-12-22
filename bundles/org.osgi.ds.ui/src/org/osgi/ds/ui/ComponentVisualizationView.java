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
package org.osgi.ds.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.Reference;
import org.apache.felix.scr.ScrService;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.equinox.internal.ds.model.ServiceComponent;
import org.eclipse.equinox.internal.ds.model.ServiceComponentProp;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.internal.serviceregistry.ServiceReferenceImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.osgi.ds.ui.dialog.FilteredComponentsSelectionDialog;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 *
 */
public class ComponentVisualizationView extends ViewPart implements ISelectionListener {

  private Graph graph;

  private SelectionAdapter _graphSelectionListener;

  private Component selectedComponent;

  /**
   *
   */
  public ComponentVisualizationView() {
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    graph = new Graph(parent, SWT.NONE);
    //    IStructuredSelection selection = (IStructuredSelection) getViewSite().getWorkbenchWindow().getSelectionService().getSelection();
    getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
    _graphSelectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (e.item instanceof GraphNode) {
          GraphNode graphNode = (GraphNode) e.item;
          final Component c = (Component) graphNode.getData();
          Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
              buildGraph(c);
            }
          });
        }
      }
    };

    buildGraph(null);
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
  }

  public void buildGraph(Component selectedComponent) {
    Component componentX = selectedComponent;

    BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();
    ServiceReference<ScrService> serviceReference = bundleContext.getServiceReference(ScrService.class);
    try {
      ScrService service = bundleContext.getService(serviceReference);

      Composite parent = graph.getParent();
      Graph oldGraph = graph;
      oldGraph.removeSelectionListener(_graphSelectionListener);
      oldGraph.dispose();
      graph = new Graph(parent, SWT.NONE);

      Map<Component, GraphNode> nodes = new HashMap<>();

      Component[] allComponents = service.getComponents();
      Arrays.sort(allComponents, new Comparator<Component>() {

        @Override
        public int compare(Component o1, Component o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
      if (selectedComponent == null) {
        Shell shell = getSite().getWorkbenchWindow().getShell();
        FilteredComponentsSelectionDialog filteredComponentsSelectionDialog = new FilteredComponentsSelectionDialog(shell, false, allComponents);
        //        filteredComponentsSelectionDialog.open();
        //        ListSelectionDialog listSelectionDialog = new ListSelectionDialog(shell, allComponents, new ArrayContentProvider(), new ComponentLabelProvider(), "Select a component");
        if (filteredComponentsSelectionDialog.open() == Window.OK) {
          Object[] result = filteredComponentsSelectionDialog.getResult();
          componentX = (Component) result[0];
        }
      }
      if (true) {
        String componentName = componentX.getName(); //"vi.metric.engine.MetricEngineManager";
        Component[] components2 = service.getComponents(componentName);
        Component component2 = components2[0];
        StringBuffer buf = new StringBuffer();
        String[] sourceServices = component2.getServices();
        if (sourceServices != null) {
          for (String service2 : sourceServices) {
            buf.append(service2);
            buf.append("\n");
          }
        }
        GraphNode graphNode2 = new GraphNode(graph, SWT.NONE, buf.toString() + component2.getName());
        graphNode2.setData(component2);
        graphNode2.setBackgroundColor(ColorConstants.orange);
        setGraphNodeBorder4State(component2, graphNode2);
        Label label = new Label();
        if (component2 instanceof ServiceComponent) {
          label.setText(((ServiceComponent) component2).toString());
        } else if (component2 instanceof ServiceComponentProp) {
          StringBuffer buffer = new StringBuffer();
          buffer.append(((ServiceComponentProp) component2).serviceComponent.toString());
          Dictionary props = component2.getProperties();
          buffer.append("\nConfig Properties\n");
          Enumeration keys = props.keys();
          while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = props.get(key);
            buffer.append(key);
            buffer.append(" = ");
            buffer.append(value);
            buffer.append("\n");
          }

          label.setText(buffer.toString());
        }
        graphNode2.setTooltip(label);
        nodes.put(component2, graphNode2);

        for (Component inboundComponent : allComponents) {
          Reference[] providedReferences = inboundComponent.getReferences();
          if (providedReferences != null) {
            for (Reference reference : providedReferences) {
              //              Set<Component> boundComponents = new HashSet<Component>();
              ServiceReference[] serviceReferences = reference.getServiceReferences();
              if (serviceReferences != null) {
                for (ServiceReference serviceReference2 : serviceReferences) {
                  if (component2 instanceof ServiceComponentProp) {
                    ServiceComponentProp sc = (ServiceComponentProp) component2;
                    if (((ServiceReferenceImpl) serviceReference2).getRegistration().equals(sc.registration)) {
                      createGraphNode4Component(nodes, inboundComponent);
                      GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(inboundComponent), nodes.get(component2));
                      graphConnection.setLineStyle(Graphics.LINE_SOLID);
                      setConnectionText(reference, graphConnection);
                    }
                  } else if (component2 instanceof ServiceComponent) {
                    ServiceComponent sc = (ServiceComponent) component2;
                  }
                }
              }
            }
          }
        }

        Reference[] requiredReferences = component2.getReferences();
        if (requiredReferences != null) {
          for (Reference reference : requiredReferences) {
            boolean componentFound = false;
            Set<Component> boundComponents = new HashSet<Component>();
            ServiceReference[] serviceReferences = reference.getServiceReferences();
            if (serviceReferences != null) {
              for (ServiceReference serviceReference2 : serviceReferences) {
                for (Component component1 : allComponents) {
                  if (component1 instanceof ServiceComponentProp) {
                    ServiceComponentProp sc = (ServiceComponentProp) component1;
                    if (((ServiceReferenceImpl) serviceReference2).getRegistration().equals(sc.registration)) {
                      createGraphNode4Component(nodes, component1);
                      boundComponents.add(component1);
                      GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), nodes.get(component1));
                      graphConnection.setLineStyle(Graphics.LINE_SOLID);
                      setConnectionText(reference, graphConnection);
                      componentFound = true;
                      break;
                    }
                  } else if (component1 instanceof ServiceComponent) {
                    ServiceComponent sc = (ServiceComponent) component1;
                  }
                }
                if (!componentFound) {
                  GraphNode graphNodeX = new GraphNode(graph, SWT.NONE, serviceReference2.toString());
                  graphNodeX.setBackgroundColor(ColorConstants.lightGray);
                  GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), graphNodeX);
                  graphConnection.setLineStyle(Graphics.LINE_DASH);
                  setConnectionText(reference, graphConnection);
                  componentFound = true;

                }

              }
            }
            String serviceName = reference.getServiceName();
            Set<Component> remainingUnboundComponents = new HashSet<Component>(Arrays.asList(allComponents));
            remainingUnboundComponents.removeAll(boundComponents);
            for (Component component1 : remainingUnboundComponents) {
              String[] services = component1.getServices();
              if (services != null) {
                for (String compServiceName : services) {
                  Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
                  Dictionary properties = component1.getProperties();
                  Enumeration<String> keys = properties.keys();
                  while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    hashtable.put(key, properties.get(key));
                  }
                  hashtable.put("objectClass", serviceName);
                  if (compServiceName.equals(serviceName) && FrameworkUtil.createFilter(reference.getTarget()).match(hashtable)) {
                    createGraphNode4Component(nodes, component1);
                    GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), nodes.get(component1));
                    graphConnection.setLineStyle(Graphics.LINE_DASH);
                    setConnectionText(reference, graphConnection);
                    componentFound = true;
                  }

                }

              }
            }
            if (!componentFound) {
              GraphNode graphNodeX = new GraphNode(graph, SWT.NONE, reference.getServiceName() + ":" + reference.getTarget());
              graphNodeX.setBackgroundColor(ColorConstants.white);
              GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), graphNodeX);
              graphConnection.setLineStyle(Graphics.LINE_DASH);
              setConnectionText(reference, graphConnection);
            }
          }
        }

      }
      graph.setScrollBarVisibility(FigureCanvas.ALWAYS);
      //      graph.setLayoutAlgorithm(new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] {
      //                                                                                                                         new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING),
      //                                                                                                                         new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING)}), true);
      graph.setLayoutAlgorithm(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);

      graph.addSelectionListener(_graphSelectionListener);
      parent.layout(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setConnectionText(Reference reference, GraphConnection graphConnection) {
    String text = reference.getName();
    if (reference.isMultiple() && reference.isOptional()) {
      text += "(0..*)";
    } else if (reference.isMultiple() && !reference.isOptional()) {
      text += "(1..*)";
    } else if (!reference.isMultiple() && !reference.isOptional()) {
      text += "(1..1)";
    } else if (!reference.isMultiple() && reference.isOptional()) {
      text += "(0..1)";
    }
    text += reference.getTarget();
    graphConnection.setText(text);
  }

  private void createGraphNode4Component(Map<Component, GraphNode> nodes, Component component1) {
    StringBuffer buf;
    GraphNode graphNode = nodes.get(component1);
    if (graphNode == null) {
      buf = new StringBuffer();
      String[] targetServices = component1.getServices();
      if (targetServices != null) {
        for (String service2 : targetServices) {
          buf.append(service2);
          buf.append("\n");
        }
      }
      graphNode = new GraphNode(graph, SWT.NONE, buf.toString() + component1.getName());
      Label label = new Label();
      if (component1 instanceof ServiceComponentProp) {
        label.setText(((ServiceComponentProp) component1).serviceComponent.toString());
      } else {
        label.setText(component1.toString());
      }
      graphNode.setTooltip(label);
      graphNode.setData(component1);
      setGraphNodeBorder4State(component1, graphNode);
      nodes.put(component1, graphNode);
    }
  }

  private void setGraphNodeBorder4State(Component component1, GraphNode graphNode) {
    switch (component1.getState()) {
      case Component.STATE_ACTIVATING:
      case Component.STATE_ACTIVE:
        graphNode.setBorderColor(ColorConstants.lightGreen);
        break;
      case Component.STATE_REGISTERED:
        graphNode.setBorderColor(ColorConstants.orange);
        break;
      case Component.STATE_UNSATISFIED:
      case Component.STATE_DISABLED:
      case Component.STATE_DISABLING:
        graphNode.setBorderColor(ColorConstants.red);
        break;
    }
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    //
  }

  /**
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	  return;
    //    buildGraphBySelection((IStructuredSelection) selection);
  }

}

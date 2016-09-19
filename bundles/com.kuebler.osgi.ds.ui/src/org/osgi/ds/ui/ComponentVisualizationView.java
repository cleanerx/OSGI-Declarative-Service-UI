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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.scr.Component;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;
import org.osgi.service.component.runtime.dto.SatisfiedReferenceDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;
import org.osgi.service.scr.api.StrippedServiceComponentRuntime;

/**
 *
 */
public class ComponentVisualizationView extends ViewPart implements ISelectionListener {

  private Graph graph;

  private SelectionAdapter _graphSelectionListener;

  private Component selectedComponent;

  private Color red;

  private Color yellow;

  private Color green;

  private Color blue;

  /**
	 *
	 */
  public ComponentVisualizationView() {
    red = new Color(Display.getDefault(), new RGB(255, 129, 129));
    yellow = new Color(Display.getDefault(), new RGB(250, 255, 125));
    green = new Color(Display.getDefault(), new RGB(155, 222, 141));
    blue = new Color(Display.getDefault(), new RGB(0, 0, 255));
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    graph = new Graph(parent, SWT.NONE);
    // IStructuredSelection selection = (IStructuredSelection)
    // getViewSite().getWorkbenchWindow().getSelectionService().getSelection();
    getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
    _graphSelectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (e.item instanceof GraphNode) {
          GraphNode graphNode = (GraphNode) e.item;
          if (graphNode.getData() instanceof ComponentConfigurationDTO) {
            final ComponentConfigurationDTO componentConfigurationDTO = (ComponentConfigurationDTO) graphNode.getData();
            Display.getDefault().asyncExec(new Runnable() {

              @Override
              public void run() {
                buildGraph(componentConfigurationDTO);
              }
            });
          }
          // final Component c = (Component) graphNode.getData();
        }
      }
    };

    // buildGraph(null);
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
    red.dispose();
    yellow.dispose();
    green.dispose();
    blue.dispose();
  }

  public void buildGraph(ComponentConfigurationDTO componentConfigurationDTO) {
    ComponentConfigurationDTO componentX = componentConfigurationDTO;

    BundleContext bundleContext = DSUIActivator.getDefault().getBundle().getBundleContext();
    ServiceReference<ServiceComponentRuntime> serviceReference = bundleContext.getServiceReference(ServiceComponentRuntime.class);
    try {
    	ServiceComponentRuntime serviceComponentRuntime = bundleContext.getService(serviceReference);

      Composite parent = graph.getParent();
      Graph oldGraph = graph;
      oldGraph.removeSelectionListener(_graphSelectionListener);
      oldGraph.dispose();
      graph = new Graph(parent, SWT.NONE);

      Map<ComponentConfigurationDTO, GraphNode> nodes = new HashMap<>();
		Collection<ComponentDescriptionDTO> componentDescriptionDTOs = serviceComponentRuntime.getComponentDescriptionDTOs();
		List<ComponentConfigurationDTO> componentConfigurationDTOs = new ArrayList<>();
		for (ComponentDescriptionDTO componentDescriptionDTO : componentDescriptionDTOs) {
			componentConfigurationDTOs.addAll(serviceComponentRuntime.getComponentConfigurationDTOs(componentDescriptionDTO));
		}
      
      Collections.sort(componentConfigurationDTOs, new Comparator<ComponentConfigurationDTO>() {

        @Override
        public int compare(ComponentConfigurationDTO o1, ComponentConfigurationDTO o2) {
          return o1.description.name.compareTo(o2.description.name);
        }
      });
      String componentName = componentX.description.name;
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      buf.append(componentX.id);
      buf.append("] ");
      buf.append(componentName);
      buf.append("\n");
      String[] sourceServices = componentX.description.serviceInterfaces;
      if (sourceServices != null) {
        buf.append("Provided Interfaces");
        for (String service2 : sourceServices) {
          buf.append("\n");
          buf.append(service2);
        }
      }
      GraphNode graphNode2 = new GraphNode(graph, SWT.CENTER, buf.toString());
      graphNode2.setData(componentX);
      switch (componentConfigurationDTO.state) {
        case ComponentConfigurationDTO.ACTIVE:
          graphNode2.setBackgroundColor(green);
          break;
        case ComponentConfigurationDTO.SATISFIED:
          graphNode2.setBackgroundColor(yellow);
          break;
        case ComponentConfigurationDTO.UNSATISFIED_REFERENCE:
          graphNode2.setBackgroundColor(red);
          break;
        case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION:
          graphNode2.setBackgroundColor(blue);
          break;
      }
      setGraphNodeBorder4State(componentX, graphNode2);
      Label label = new Label();
      StringBuffer buffer = new StringBuffer();
      buffer.append(componentX.description.name);
      Map<String, Object> props = componentX.properties;
      buffer.append("\nConfig Properties\n");
      Set<Entry<String, Object>> keys = props.entrySet();
      Iterator<Entry<String, Object>> iterator = keys.iterator();
      while (iterator.hasNext()) {
        Entry<String, Object> entry = iterator.next();
        Object key = entry.getKey();
        Object value = entry.getValue();
        buffer.append(key);
        buffer.append(" = ");
        buffer.append(value);
        buffer.append("\n");
      }

      label.setText(buffer.toString());
      graphNode2.setTooltip(label);
      nodes.put(componentX, graphNode2);

      for (ComponentConfigurationDTO inboundComponent : componentConfigurationDTOs) {
        SatisfiedReferenceDTO[] requiredReferences = inboundComponent.satisfiedReferences;
        if (requiredReferences != null) {
          for (SatisfiedReferenceDTO reference : requiredReferences) {
            ServiceReferenceDTO[] boundServiceReferences = reference.boundServices;
            if (boundServiceReferences != null) {
              for (ServiceReferenceDTO serviceReference2 : boundServiceReferences) {
                Map<String, Object> properties = serviceReference2.properties;
                if (properties != null && properties.get("component.id") != null) {
                  if (componentX.id == (Long) properties.get("component.id")) {
                    createGraphNode4Component(nodes, inboundComponent);
                    GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(componentX), nodes.get(inboundComponent));
                    graphConnection.setLineStyle(Graphics.LINE_SOLID);
                    for (ReferenceDTO referenceDTO : inboundComponent.description.references) {
                      if (referenceDTO.name == reference.name) {
                        setConnectionText(referenceDTO, graphConnection);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        UnsatisfiedReferenceDTO[] unsatisfiedReferences = inboundComponent.unsatisfiedReferences;
        //          if (requiredReferences != null) {
        //            for (UnsatisfiedReferenceDTO reference : unsatisfiedReferences) {
        //              ServiceReferenceDTO[] boundServiceReferences = reference.targetServices;
        //              if (boundServiceReferences != null) {
        //                for (ServiceReferenceDTO serviceReference2 : boundServiceReferences) {
        //                	Map<String, Object> properties = serviceReference2.properties;
        //                	if(properties.get("component.id") != null) {
        //	                	if(componentX.id == (Long)properties.get("component.id")) {
        //	                        createGraphNode4Component(nodes, inboundComponent);
        //	                        GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(componentX), nodes.get(inboundComponent));
        //	                        graphConnection.setLineStyle(Graphics.LINE_SOLID);
        //	                        for (ReferenceDTO referenceDTO : inboundComponent.description.references) {
        //	                        	if(referenceDTO.name == reference.name) {
        //	                        		setConnectionText(referenceDTO, graphConnection);
        //	                        	}
        //							}
        //	                	}
        //                	}
        //                }
        //              }
        //            }
        //          }
      }

              if(componentX.satisfiedReferences != null) {
              	for(SatisfiedReferenceDTO reference : componentX.satisfiedReferences) {
              		ServiceReferenceDTO[] boundServiceReferences = reference.boundServices;
              		if (boundServiceReferences != null) {
              			for (ServiceReferenceDTO serviceReference2 : boundServiceReferences) {
              				Map<String, Object> properties = serviceReference2.properties;
              				boolean foundComponent = false;
              				if(properties.get("component.id") != null) {
              					for (ComponentConfigurationDTO inboundComponent : componentConfigurationDTOs) {
              						if(inboundComponent.id == (Long)properties.get("component.id")) {
              							createGraphNode4Component(nodes, inboundComponent);
              							GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(inboundComponent),nodes.get(componentX));
              							graphConnection.setLineStyle(Graphics.LINE_SOLID);
              							foundComponent = true;
              							for (ReferenceDTO referenceDTO : inboundComponent.description.references) {
              								if(referenceDTO.name == reference.name) {
              									setConnectionText(referenceDTO, graphConnection);
              								}
              							}
              						}
              					}
              				}
              				// must be a service
              				if(!foundComponent) {
                                GraphNode graphNodeX = new GraphNode(graph, SWT.NONE, serviceReference2.toString());
                                graphNodeX.setBackgroundColor(ColorConstants.lightGray);
                                GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(componentX), graphNodeX);
                                graphConnection.setLineStyle(Graphics.LINE_DASH);
      //                          setConnectionText(reference, graphConnection);
              				}
              			}
              		}
              	}
      }
      //            if(componentX.unsatisfiedReferences != null) {
      //            	for(UnsatisfiedReferenceDTO reference : componentX.unsatisfiedReferences ) {
      //            		ServiceReferenceDTO[] boundServiceReferences = reference.targetServices;
      //            		if (boundServiceReferences != null) {
      //            			for (ServiceReferenceDTO serviceReference2 : boundServiceReferences) {
      //            				Map<String, Object> properties = serviceReference2.properties;
      //            				boolean foundComponent = false;
      //            				if(properties.get("component.id") != null) {
      //            					for (ComponentConfigurationDTO inboundComponent : allComponents) {
      //            						if(inboundComponent.id == (Long)properties.get("component.id")) {
      //            							createGraphNode4Component(nodes, inboundComponent);
      //            							GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(inboundComponent),nodes.get(componentX));
      //            							graphConnection.setLineStyle(Graphics.LINE_SOLID);
      //            							foundComponent = true;
      //            							for (ReferenceDTO referenceDTO : inboundComponent.description.references) {
      //            								if(referenceDTO.name == reference.name) {
      //            									setConnectionText(referenceDTO, graphConnection);
      //            								}
      //            							}
      //            						}
      //            					}
      //            				}
      //        				}
      //    				}
      //				}
      //			}
      //        }

      //        for (ComponentConfigurationDTO inboundComponent : allComponents) {
      //            SatisfiedReferenceDTO[] requiredReferences = inboundComponent.satisfiedReferences;
      //            if (requiredReferences != null) {
      //              for (SatisfiedReferenceDTO reference : requiredReferences) {
      //                ServiceReferenceDTO[] boundServiceReferences = reference.boundServices;
      //                if (boundServiceReferences != null) {
      //                  for (ServiceReferenceDTO serviceReference2 : boundServiceReferences) {
      //                  	Map<String, Object> properties = serviceReference2.properties;
      //                  	if(properties.get("component.id") != null) {
      //  	                	if(componentX.id == (Long)properties.get("component.id")) {
      //  	                        createGraphNode4Component(nodes, inboundComponent);
      //  	                        GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(componentX), nodes.get(inboundComponent));
      //  	                        graphConnection.setLineStyle(Graphics.LINE_SOLID);
      //  	                        for (ReferenceDTO referenceDTO : inboundComponent.description.references) {
      //  	                        	if(referenceDTO.name == reference.name) {
      //  	                        		setConnectionText(referenceDTO, graphConnection);
      //  	                        	}
      //  							}
      //  	                	}
      //                  	}
      //                  }
      //                }
      //              }
      //            }
      //            UnsatisfiedReferenceDTO[] unsatisfiedReferences = inboundComponent.unsatisfiedReferences;
      //            if (requiredReferences != null) {
      //              for (UnsatisfiedReferenceDTO reference : unsatisfiedReferences) {
      //                ServiceReferenceDTO[] boundServiceReferences = reference.targetServices;
      //                if (boundServiceReferences != null) {
      //                  for (ServiceReferenceDTO serviceReference2 : boundServiceReferences) {
      //                  	Map<String, Object> properties = serviceReference2.properties;
      //                  	if(properties.get("component.id") != null) {
      //  	                	if(componentX.id == (Long)properties.get("component.id")) {
      //  	                        createGraphNode4Component(nodes, inboundComponent);
      //  	                        GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(inboundComponent),nodes.get(componentX));
      //  	                        graphConnection.setLineStyle(Graphics.LINE_SOLID);
      //  	                        for (ReferenceDTO referenceDTO : inboundComponent.description.references) {
      //  	                        	if(referenceDTO.name == reference.name) {
      //  	                        		setConnectionText(referenceDTO, graphConnection);
      //  	                        	}
      //  							}
      //  	                	}
      //                  	}
      //                  }
      //                }
      //              }
      //            }
      //          }
      //        Reference[] requiredReferences = component2.getReferences();
      //        if (requiredReferences != null) {
      //          for (Reference reference : requiredReferences) {
      //            boolean componentFound = false;
      //            Set<Component> boundComponents = new HashSet<Component>();
      //            ServiceReference[] serviceReferences = reference.getServiceReferences();
      //            if (serviceReferences != null) {
      //              for (ServiceReference serviceReference2 : serviceReferences) {
      //                for (Component component1 : allComponents) {
      //                  if (component1 instanceof ServiceComponentProp) {
      //                    ServiceComponentProp sc = (ServiceComponentProp) component1;
      //                    if (((ServiceReferenceImpl) serviceReference2).getRegistration().equals(sc.registration)) {
      //                      createGraphNode4Component(nodes, component1);
      //                      boundComponents.add(component1);
      //                      GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), nodes.get(component1));
      //                      graphConnection.setLineStyle(Graphics.LINE_SOLID);
      //                      setConnectionText(reference, graphConnection);
      //                      componentFound = true;
      //                      break;
      //                    }
      //                  } else if (component1 instanceof ServiceComponent) {
      //                    ServiceComponent sc = (ServiceComponent) component1;
      //                  }
      //                }
      //                if (!componentFound) {
      //                  GraphNode graphNodeX = new GraphNode(graph, SWT.NONE, serviceReference2.toString());
      //                  graphNodeX.setBackgroundColor(ColorConstants.lightGray);
      //                  GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), graphNodeX);
      //                  graphConnection.setLineStyle(Graphics.LINE_DASH);
      //                  setConnectionText(reference, graphConnection);
      //                  componentFound = true;
      //
      //                }
      //
      //              }
      //            }
      //            String serviceName = reference.getServiceName();
      //            Set<ComponentConfigurationDTO> remainingUnboundComponents = new HashSet<ComponentConfigurationDTO>(Arrays.asList(allComponents));
      //            remainingUnboundComponents.removeAll(boundComponents);
      //            for (Component component1 : remainingUnboundComponents) {
      //              String[] services = component1.getServices();
      //              if (services != null) {
      //                for (String compServiceName : services) {
      //                  Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
      //                  Dictionary properties = component1.getProperties();
      //                  Enumeration<String> keys = properties.keys();
      //                  while (keys.hasMoreElements()) {
      //                    String key = keys.nextElement();
      //                    hashtable.put(key, properties.get(key));
      //                  }
      //                  hashtable.put("objectClass", serviceName);
      //                  if (compServiceName.equals(serviceName) && FrameworkUtil.createFilter(reference.getTarget()).match(hashtable)) {
      //                    createGraphNode4Component(nodes, component1);
      //                    GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), nodes.get(component1));
      //                    graphConnection.setLineStyle(Graphics.LINE_DASH);
      //                    setConnectionText(reference, graphConnection);
      //                    componentFound = true;
      //                  }
      //
      //                }
      //
      //              }
      //            }
      //            if (!componentFound) {
      //              GraphNode graphNodeX = new GraphNode(graph, SWT.NONE, reference.getServiceName() + ":" + reference.getTarget());
      //              graphNodeX.setBackgroundColor(ColorConstants.white);
      //              GraphConnection graphConnection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(component2), graphNodeX);
      //              graphConnection.setLineStyle(Graphics.LINE_DASH);
      //              setConnectionText(reference, graphConnection);
      //            }
      //          }
      //        }
      //
      //      }
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

  private void setConnectionText(ReferenceDTO reference, GraphConnection graphConnection) {
    StringBuilder builder = new StringBuilder();
    builder.append(reference.name);
    builder.append(" ");
    builder.append(reference.cardinality);
    builder.append("\n");
    builder.append(reference.target);
    graphConnection.setText(builder.toString());
  }

  private void createGraphNode4Component(Map<ComponentConfigurationDTO, GraphNode> nodes, ComponentConfigurationDTO component1) {
    StringBuffer buf = new StringBuffer();
    GraphNode graphNode = nodes.get(component1);
    if (graphNode == null) {
      String componentName = component1.description.name;
      buf.append("[");
      buf.append(component1.id);
      buf.append("] ");
      buf.append(componentName);
      buf.append("\n");
      String[] sourceServices = component1.description.serviceInterfaces;
      if (sourceServices != null) {
        buf.append("Provided Interfaces");
        for (String service2 : sourceServices) {
          buf.append("\n");
          buf.append(service2);
        }
      }
      graphNode = new GraphNode(graph, SWT.NONE, buf.toString() + component1.description.name);
      Label label = new Label();
      if (component1 instanceof ComponentConfigurationDTO) {
        label.setText((component1.properties).toString());
      }
      graphNode.setTooltip(label);
      switch (component1.state) {
        case ComponentConfigurationDTO.ACTIVE:
          graphNode.setBackgroundColor(green);
          break;
        case ComponentConfigurationDTO.SATISFIED:
          graphNode.setBackgroundColor(yellow);
          break;
        case ComponentConfigurationDTO.UNSATISFIED_REFERENCE:
          graphNode.setBackgroundColor(red);
          break;
        case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION:
          graphNode.setBackgroundColor(blue);
          break;
      }

      graphNode.setData(component1);
      setGraphNodeBorder4State(component1, graphNode);
      nodes.put(component1, graphNode);
    }
  }

  private void setGraphNodeBorder4State(ComponentConfigurationDTO componentX, GraphNode graphNode) {
    switch (componentX.state) {
      case ComponentConfigurationDTO.ACTIVE:
        graphNode.setBorderColor(ColorConstants.lightGreen);
        break;
      case ComponentConfigurationDTO.SATISFIED:
        graphNode.setBorderColor(ColorConstants.orange);
        break;
      case ComponentConfigurationDTO.UNSATISFIED_REFERENCE:
      case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION:
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
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      if (!selection.isEmpty()) {
        if (structuredSelection.getFirstElement() instanceof ComponentConfigurationDTO) {
          buildGraph((ComponentConfigurationDTO) structuredSelection.getFirstElement());
        }
      }
    }
    return;
    // buildGraphBySelection((IStructuredSelection) selection);
  }

}

package org.osgi.ds.ui;

import java.awt.Color;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionLocator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.util.tracker.ServiceTracker;

public class SCRToolContributor extends WorkbenchWindowControlContribution implements IWorkbenchContribution {
	
  /**
   *
   */
  private static final String UPDATE_ICON = "icons/LoadVersion.png"; //$NON-NLS-1$

  private static final int MIN_VERSION_BUTTON_WIDTH = 40;

  private Composite _composite;

  private ToolBar _toolBar;

  private ToolItem _toolItem;

  protected boolean _filterShowVersions;

  private IToolBarManager _toolbarManager;

private TableViewer _tableViewer;

private Text _typeaheadText;

private String _lastTypeaheadString;

private Color SYSTEM_COLOR_INACTIVE; 

private boolean _isTypeaheadDefaultText = true;


  public SCRToolContributor() {
    super();
//    SYSTEM_COLOR_INACTIVE = (new RGB(100, 100, 100));
  }

  /**
   * Computes the width of the given control which is being added to a tool bar. This is needed to determine the width of the tool bar item containing
   * the given control.
   * <p>
   * The default implementation of this framework method returns <code>control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x</code>. Subclasses may
   * override if required.
   * </p>
   *
   * @param control the control being added
   * @return the width of the control
   */
  @Override
  protected int computeWidth(Control control) {
    int newWidth = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
    return newWidth > MIN_VERSION_BUTTON_WIDTH ? newWidth : MIN_VERSION_BUTTON_WIDTH;
  }

  private Shell shell;

private ServiceTracker<IEndpointDescriptionLocator, IEndpointDescriptionLocator> serviceTracker;

  @Override
  protected Control createControl(final Composite parent) {

    _composite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.verticalSpacing = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.marginHeight = 0;
    gridLayout.marginRight = 0;
    _composite.setLayout(gridLayout);

    serviceTracker = new ServiceTracker<IEndpointDescriptionLocator,IEndpointDescriptionLocator>(DSUIActivator.getDefault().getBundle().getBundleContext(), IEndpointDescriptionLocator.class, null);
    serviceTracker.open();
    
    _toolBar = new ToolBar(_composite, SWT.FLAT);
    _toolItem = new ToolItem(_toolBar, SWT.DROP_DOWN);
//    _toolItem.setImage(ResourceManager.getImage(ModelViewPlugin.getImageDescriptor(UPDATE_ICON)));
    _toolItem.setEnabled(true);
    _toolItem.setText("Currently selected scr");
    _toolItem.setToolTipText("Choose Service Component Runtime"); //$NON-NLS-1$
    _toolItem.addSelectionListener(new SelectionAdapter() {

	/**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(SelectionEvent e) {
    	    shell = new Shell(_toolBar.getShell(),SWT.ON_TOP);
    	    GridLayout gridLayout = new GridLayout();
    	    gridLayout.numColumns = 1;
    	    gridLayout.marginHeight = 0;
    	    gridLayout.marginWidth = 0;
    	    shell.setLayout(gridLayout);

    	    Composite composite = createParent(shell);
    	    createTypeaheadField(composite);
    	    createVersionTable(composite);
    	    shell.open();

    	    Display display = shell.getDisplay();
    	    while (!shell.isDisposed() && shell.isVisible()) {
    	      if (!display.readAndDispatch()) {
    	        display.sleep();
    	      }
    	    }

      }
    });


    _toolBar.pack();
    return _composite;
  }

  private void createTypeaheadField(Composite parent) {
	    _typeaheadText = new Text(parent, SWT.BORDER);

	    GridData layoutData = new GridData();
	    layoutData.widthHint = 200;
	    _typeaheadText.setLayoutData(layoutData);
//	    _typeaheadText.setForeground(SYSTEM_COLOR_INACTIVE);
	    _typeaheadText.setText("FIXME"); //$NON-NLS-1$
	    _lastTypeaheadString = _typeaheadText.getText();
	    _typeaheadText.selectAll();
	    _typeaheadText.addFocusListener(new FocusAdapter() {

	      private boolean _isInitGained = true;

	      /**
	       * @see org.eclipse.swt.events.FocusAdapter#focusGained(org.eclipse.swt.events.FocusEvent)
	       */
	      @Override
	      public void focusGained(FocusEvent e) {
	        if (_isTypeaheadDefaultText && !_isInitGained) {
//	          _typeaheadText.setForeground(SYSTEM_COLOR_ACTIVE);
	          _typeaheadText.setText(""); //$NON-NLS-1$
	        }
	        _isInitGained = false;
	      }

	      /**
	       * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
	       */
	      @Override
	      public void focusLost(FocusEvent e) {
	        if (_isTypeaheadDefaultText) {
//	          _typeaheadText.setForeground(SYSTEM_COLOR_INACTIVE);
	          _typeaheadText.setText("FIXME"); //$NON-NLS-1$
	        }
	      }
	    });

	    _typeaheadText.addKeyListener(new KeyAdapter() {

	      /**
	       * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
	       */
	      @Override
	      public void keyReleased(KeyEvent e) {
	        String text = ((Text) e.getSource()).getText();
	        if (!_lastTypeaheadString.equals(text)) {
	          _lastTypeaheadString = text;
	          if ("".equals(text)) { //$NON-NLS-1$
	            _isTypeaheadDefaultText = true;
	            _tableViewer.removeFilter(_viewerFilter);
	          } else {
	            _isTypeaheadDefaultText = false;
	            _tableViewer.addFilter(_viewerFilter);
	          }
	        }
	      }
	    });

	  }

  private ViewerFilter _viewerFilter = new ViewerFilter() {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return true;
	}
	  
  };
  
	  private Composite createParent(Shell shell) {
	    Composite parentComposite = new Composite(shell, SWT.NONE);
	    GridLayout gridLayoutParent = new GridLayout();
	    gridLayoutParent.numColumns = 1;
	    parentComposite.setLayout(gridLayoutParent);
	    GridData gridData = new GridData(0, 0, true, true);
	    gridData.horizontalAlignment = SWT.FILL;
	    gridData.verticalAlignment = SWT.FILL;
	    parentComposite.setLayoutData(gridData);
	    return parentComposite;
	  }

	  private Composite createVersionTable(Composite parent) {
	    _tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);

	    GridData gridData = new GridData();
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.grabExcessHorizontalSpace = true;
	    _tableViewer.getControl().setLayoutData(gridData);

	    _tableViewer.getTable().setHeaderVisible(true);
	    _tableViewer.getTable().setLinesVisible(true);

	    EndpointCellLabelProvider labelProvider = new EndpointCellLabelProvider();
	    TableViewerColumn tableViewerColumnVersion = new TableViewerColumn(_tableViewer, SWT.RESIZE);
	    tableViewerColumnVersion.getColumn().setText("Framework ID"); //$NON-NLS-1$
	    tableViewerColumnVersion.getColumn().setWidth(240);
	    tableViewerColumnVersion.setLabelProvider(labelProvider);
	    TableViewerColumn tableViewerColumnVersion2 = new TableViewerColumn(_tableViewer, SWT.RESIZE);
	    tableViewerColumnVersion2.getColumn().setText("Server"); //$NON-NLS-1$
	    tableViewerColumnVersion2.getColumn().setWidth(500);
		tableViewerColumnVersion2.setLabelProvider(labelProvider);
	    TableViewerColumn tableViewerColumnVersion3 = new TableViewerColumn(_tableViewer, SWT.RESIZE);
	    tableViewerColumnVersion3.getColumn().setText("Application"); //$NON-NLS-1$
	    tableViewerColumnVersion3.getColumn().setWidth(500);
		tableViewerColumnVersion3.setLabelProvider(labelProvider);
	    TableViewerColumn tableViewerColumnVersion4 = new TableViewerColumn(_tableViewer, SWT.RESIZE);
	    tableViewerColumnVersion4.getColumn().setText("osgi.instance.area"); //$NON-NLS-1$
	    tableViewerColumnVersion4.getColumn().setWidth(500);
		tableViewerColumnVersion4.setLabelProvider(labelProvider);

	    _tableViewer.setContentProvider(new ArrayContentProvider());
//	    _tableViewer.setLabelProvider(new EndpointLabelProvider());
	    
	    _tableViewer.setInput(serviceTracker.getService().getDiscoveredEndpoints());

//	    createContextMenu();

	    _tableViewer.getTable().addKeyListener(new KeyListener() {
	      @Override
	      public void keyReleased(KeyEvent e) {

	        if (e.keyCode == SWT.ESC) {
	          close();
	        } else if (e.character == '\r') {
//	          setLastSelection(_tableViewer.getSelection());
	          close();
	        }
	      }

	      @Override
	      public void keyPressed(KeyEvent e) {
	        //
	      }
	    });

	    _tableViewer.addDoubleClickListener(new IDoubleClickListener() {

	      @Override
	      public void doubleClick(DoubleClickEvent event) {
	        ISelection selection = event.getSelection();
	        EndpointDescription ed = (EndpointDescription) ((IStructuredSelection)selection).getFirstElement();
	        String id2 = ed.getId();
	        ((ComponentViewer)getWorkbenchWindow().getActivePage().getActivePart()).setEndpointId(ed.getFrameworkUUID(), id2);
//	        _toolItem.setText(ed.getFrameworkUUID());
	        setChosenEndpoint(((IStructuredSelection)selection).getFirstElement());
	        close();
	      }

	    });

	    return parent;
	  }

//	  @Override
	  public void close() {
//	    if (_activeSiteOnOpenPopup != null) {
//	      _activeSiteOnOpenPopup.setSelectionProvider(_backupSelectionProvider);
//	    }

	    shell.close();
	    shell.dispose();
	  }

//	  public Shell getPopupTableShell() {
//	    return _shell;
//	  }

		private void setChosenEndpoint(Object firstElement) {
			// TODO Auto-generated method stub
			
		}


  private void refresh() {
    _toolbarManager.update(false);
  }

  private boolean checkIfNotDisposed(Widget widget) {
    return (widget != null && !widget.isDisposed());
  }

  private void setEnabled(boolean enabled) {
    if (checkIfNotDisposed(_toolItem)) {
      _toolItem.setEnabled(enabled);
    }
  }
  
  @Override
	public void dispose() {
		super.dispose();
		if(serviceTracker!= null) {
			serviceTracker.close();
		}
	}
  
  /**
   * @see org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator)
   */
  @Override
  public void initialize(IServiceLocator serviceLocator) {
    //nothing to do here at the moment
  }

}

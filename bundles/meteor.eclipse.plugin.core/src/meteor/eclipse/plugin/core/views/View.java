package meteor.eclipse.plugin.core.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import meteor.eclipse.plugin.core.Activator;

public class View extends ViewPart {

	private ToolItem exportItem;
	private TreeViewer treeViewer;
	private List<Item> items;

	public View() {
		this.setPartName("Refactoring Sessions");
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void clearItems() {
		items.clear();
		treeViewer.refresh();
	}

	@Override
	public void createPartControl(Composite parent) {
		// parent.setLayout(new FillLayout());
		parent.setLayout(new GridLayout(1, false)); // Uma coluna, várias linhas

		// Cria a barra de ferramentas
		ToolBar toolbar = new ToolBar(parent, SWT.HORIZONTAL | SWT.WRAP | SWT.TOP);

		// Obtém o gerenciador de barra de ferramentas
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

		// Criar os botões
		createToolbarButtons(toolbar, toolbarManager);

		// Create the TreeViewer
		treeViewer = new TreeViewer(parent, SWT.FILL | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData treeLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true); // O TreeViewer ocupa todo o espaço
																				// disponível verticalmente
		treeViewer.getControl().setLayoutData(treeLayoutData);
		treeViewer.setContentProvider(new MyContentProvider());
		treeViewer.setLabelProvider(new MyLabelProvider());

		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		// Definindo a fonte com altura maior
		Font largerFont = new Font(parent.getDisplay(), tree.getFont().getFontData()[0].getName(), 12, SWT.NORMAL);
		tree.setFont(largerFont);

		// Create the columns
		createColumns();

		// Create the initial items
		items = new ArrayList<>();

		// Set the input data for the viewer
		treeViewer.setInput(items);

		// Enable editing for the first column
		treeViewer.setColumnProperties(new String[] { "Item", "Value", "Report" });
		CellEditor[] editors = new CellEditor[1];
		editors[0] = new TextCellEditor(treeViewer.getTree());
		treeViewer.setCellEditors(editors);
		treeViewer.setCellModifier(new MyCellModifier());
		treeViewer.getTree().addListener(SWT.KeyDown, new TreeKeyListener());

		setPartName("Meteor View");

		ImageDescriptor imageDescriptor = Activator.getImageDescriptor("icons/meteorlogo16.png");
		setTitleImage(imageDescriptor.createImage());
	}

	public void createToolbarButtons(ToolBar toolbar, IToolBarManager toolbarManager) {

		// Adiciona um botão de exportação à barra de ferramentas
		exportItem = new ToolItem(toolbar, SWT.PUSH);
		exportItem.setToolTipText("Exportar");
		ImageDescriptor exportImageDescriptor = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT);
		exportItem.setImage(exportImageDescriptor.createImage());

		// Adiciona um ouvinte de seleção ao botão de exportação
		exportItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Implemente a lógica para exportar aqui
				System.out.println("Exportar botão clicado!");
			}
		});

		// Adiciona um botão de importação à barra de ferramentas
		ToolItem importItem = new ToolItem(toolbar, SWT.PUSH);
		importItem.setToolTipText("Importar");
		ImageDescriptor importImageDescriptor = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		importItem.setImage(importImageDescriptor.createImage());
		importItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Implemente a lógica para importar aqui
				System.out.println("Importar botão clicado!");
			}
		});

		// Adiciona um separador vertical à barra de ferramentas
		ToolItem separator1 = new ToolItem(toolbar, SWT.SEPARATOR);
		separator1.setWidth(5); // Define a largura da linha vertical
		separator1.setControl(new VerticalSeparator(toolbar));

		// Adiciona um botão "Set Project Entry Point For Test" à barra de ferramentas
		ToolItem setProjectEntryPointItem = new ToolItem(toolbar, SWT.PUSH);
		ImageDescriptor setProjectEntryPointItemImageDescriptor = 
				Activator.getImageDescriptor("icons/meteorpin16.png");
		setProjectEntryPointItem.setImage(setProjectEntryPointItemImageDescriptor.createImage());
		//setProjectEntryPointItem.setText("[1]");
		setProjectEntryPointItem.setToolTipText("Set Project Entry Point For Test");
		setProjectEntryPointItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.setpitentrypoint").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		// Adiciona um botão "Create Refactoring Session" à barra de ferramentas
		ToolItem createRefactoringSessionItem = new ToolItem(toolbar, SWT.PUSH);
		ImageDescriptor createRefactoringSessionItemImageDescriptor = Activator
				.getImageDescriptor("platform:/plugin/org.eclipse.ui/icons/full/etool16/editor_area.png");
		//createRefactoringSessionItem.setText("[2]");
		createRefactoringSessionItem.setImage(createRefactoringSessionItemImageDescriptor.createImage());
		createRefactoringSessionItem.setToolTipText("Create Refactoring Session");
		createRefactoringSessionItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.createrefactoringsession").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		
		// Adiciona um separador vertical à barra de ferramentas
		ToolItem separator2 = new ToolItem(toolbar, SWT.SEPARATOR);
		separator2.setWidth(5); // Define a largura da linha vertical
		separator2.setControl(new VerticalSeparator(toolbar));

		// Adiciona um botão "Run Mutation Tests" à barra de ferramentas
		ToolItem runMutationTestsItem = new ToolItem(toolbar, SWT.PUSH);
		ImageDescriptor runMutationTestsItemImageDescriptor = 
				Activator.getImageDescriptor("icons/meteortest16.png");
		//runMutationTestsItem.setText("[3]");
		runMutationTestsItem.setImage(runMutationTestsItemImageDescriptor.createImage());
		runMutationTestsItem.setToolTipText("Run Mutation Tests");
		runMutationTestsItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.runmutationtests").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		// Adiciona um botão "Set last run results as baseline" à barra de ferramentas
		ToolItem setBaselineItem = new ToolItem(toolbar, SWT.PUSH);
		ImageDescriptor setBaselineItemImageDescriptor = 
				Activator.getImageDescriptor("icons/meteorpinbl.png");
		//setBaselineItem.setText("[4]");
		setBaselineItem.setImage(setBaselineItemImageDescriptor.createImage());		
		setBaselineItem.setToolTipText("Set last run results as baseline");
		setBaselineItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.generatebaseline").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		// Adiciona um botão "Validate refactoring" à barra de ferramentas
		ToolItem validateRefactoringItem = new ToolItem(toolbar, SWT.PUSH | SWT.RIGHT);
		ImageDescriptor validateRefactoringItemImageDescriptor = 
				Activator.getImageDescriptor("icons/meteorvalidate16.png");
		//validateRefactoringItem.setText("[5]");
		validateRefactoringItem.setImage(validateRefactoringItemImageDescriptor.createImage());		
		validateRefactoringItem.setToolTipText("Validate refactoring");
		validateRefactoringItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.validaterefactoring").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		
		
		// Adiciona um separador vertical à barra de ferramentas
		ToolItem separator3 = new ToolItem(toolbar, SWT.SEPARATOR);
		separator3.setWidth(5); // Define a largura da linha vertical
		separator3.setControl(new VerticalSeparator(toolbar));

		// Adiciona um botão "Clean all Results" à barra de ferramentas
		ToolItem cleanResultsItem = new ToolItem(toolbar, SWT.PUSH | SWT.RIGHT);
		ImageDescriptor cleanResultsItemImageDescriptor = 
				Activator.getImageDescriptor("icons/meteorclear16.png");
		cleanResultsItem.setImage(cleanResultsItemImageDescriptor.createImage());				
		cleanResultsItem.setToolTipText("Clean all Results");
		cleanResultsItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.reset").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		// Adiciona um botão "Print validation results" à barra de ferramentas
		ToolItem printResultsItem = new ToolItem(toolbar, SWT.PUSH | SWT.LEFT);
		ImageDescriptor printResultsItemImageDescriptor = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT);
		printResultsItem.setImage(printResultsItemImageDescriptor.createImage());
		printResultsItem.setToolTipText("Print validation results");
		printResultsItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.printvalidation").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		// Adiciona um botão "About" à barra de ferramentas
		ToolItem aboutItem = new ToolItem(toolbar, SWT.PUSH | SWT.LEFT);
		ImageDescriptor aboutItemImageDescriptor = 
				Activator.getImageDescriptor("icons/meteorlogo16.png");
		aboutItem.setImage(aboutItemImageDescriptor.createImage());		
		aboutItem.setToolTipText("About");
		aboutItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtém o serviço de manipulador de comandos
		        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        try {
					commandService.getCommand("meteor.eclipse.plugin.core.command.about").executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotHandledException | NotDefinedException | NotEnabledException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		// Atualiza a barra de ferramentas
		toolbarManager.update(true);
	}

	private void createColumns() {
		// First column
		TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		nameColumn.getColumn().setWidth(200);
		nameColumn.getColumn().setText("Item");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Item) {
					Item item = (Item) element;
					return item.getKey();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof Item) {
					Item item = (Item) element;
					// Use o plugin Activator para obter o caminho do ícone
					if (!item.getIconPath().isEmpty()) {
						ImageDescriptor imageDescriptor = Activator.getImageDescriptor(item.getIconPath());
						return imageDescriptor.createImage();
					} else {
						return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
					}
				}
				return super.getImage(element);
			}
		});

		// Second column
		TreeViewerColumn descriptionColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		descriptionColumn.getColumn().setWidth(200);
		descriptionColumn.getColumn().setText("Value");
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Item) {
					Item item = (Item) element;
					return item.getValue();
				}
				return super.getText(element);
			}
		});

		// Third column
		TreeViewerColumn reportColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		reportColumn.getColumn().setWidth(400);
		reportColumn.getColumn().setText("Report");
		reportColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Item) {
					Item item = (Item) element;
					return item.getReport();
				}
				return super.getText(element);
			}
		});

	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	public void addItem(Item item) {
		items.add(item);
		treeViewer.refresh();
	}

	private class MyContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List<?>) {
				return ((List<?>) inputElement).toArray();
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Item) {
				Item item = (Item) parentElement;
				return item.getChildren().toArray();
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof Item) {
				Item item = (Item) element;
				return item.getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof Item) {
				Item item = (Item) element;
				return !item.getChildren().isEmpty();
			}
			return false;
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}

	private class MyLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof Item) {
				Item item = (Item) element;
				return item.getKey();
			}
			return super.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof Item) {
				Item item = (Item) element;
				// Use o plugin Activator para obter o caminho do ícone
				if (!item.getIconPath().isEmpty()) {
					ImageDescriptor imageDescriptor = Activator.getImageDescriptor(item.getIconPath());
					return imageDescriptor.createImage();
				} else {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
				}
			}
			return super.getImage(element);
		}

	}

	public static class Item {
		private String key;
		private String value;
		private String iconPath;
		private Item parent;
		private String report;
		private List<Item> children;

		public Item(String key, String value, String iconPath) {
			this.key = key;
			this.value = value;
			this.iconPath = iconPath;
			this.parent = null;
			this.children = new ArrayList<>();
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Item getParent() {
			return parent;
		}

		public String getReport() {
			return report;
		}

		public void setReport(String report) {
			this.report = report;
		}

		public List<Item> getChildren() {
			return children;
		}

		public void addChild(Item child) {
			child.parent = this;
			children.add(child);
		}

		public String getIconPath() {
			return iconPath;
		}

		public void setIconPath(String iconPath) {
			this.iconPath = iconPath;
		}
	}

	private class MyCellModifier implements ICellModifier {

		@Override
		public boolean canModify(Object element, String property) {
			return property.equals("Name") || property.equals("Description");
		}

		@Override
		public Object getValue(Object element, String property) {
			if (element instanceof Item) {
				Item item = (Item) element;
				if (property.equals("Name")) {
					return item.getKey();
				} else if (property.equals("Description")) {
					return item.getValue();
				}
			}
			return null;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				Item item = (Item) element;
				if (property.equals("Name")) {
					if (value instanceof String) {
						item.setKey((String) value);
						treeViewer.update(item, new String[] { "Name" });
					}
				} else if (property.equals("Description")) {
					if (value instanceof String) {
						item.setValue((String) value);
						treeViewer.update(item, new String[] { "Description" });
					}
				}
			}
		}
	}

	private class TreeKeyListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				if (!selection.isEmpty()) {
					Object selected = selection.getFirstElement();
					if (selected instanceof Item) {
						Item item = (Item) selected;
						if (item.getParent() != null) {
							item.getParent().getChildren().remove(item);
							treeViewer.refresh(item.getParent());
						} else {
							items.remove(item);
							treeViewer.refresh();
						}
					}
				}
			}
		}
	}
	
	public class VerticalSeparator extends Canvas {

	    public VerticalSeparator(Composite parent) {
	        super(parent, SWT.NONE);
	        addPaintListener(new PaintListener() {
	            public void paintControl(PaintEvent e) {
	                drawSeparator(e.gc);
	            }
	        });
	    }

	    private void drawSeparator(GC gc) {
	        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
	        Point size = getSize();
	        gc.drawLine(1, 0, 1, size.y);
	    }

	    @Override
	    public Point computeSize(int wHint, int hHint, boolean changed) {
	        return new Point(2, hHint);
	    }
	}
}
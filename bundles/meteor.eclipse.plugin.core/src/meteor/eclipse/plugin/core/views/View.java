package meteor.eclipse.plugin.core.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import meteor.eclipse.plugin.core.Activator;

public class View extends ViewPart {

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
        parent.setLayout(new FillLayout());

        // Create the TreeViewer
        treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        treeViewer.setContentProvider(new MyContentProvider());
        treeViewer.setLabelProvider(new MyLabelProvider());
        
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

        // Create the columns
        createColumns();

        // Create the initial items
        items = new ArrayList<>();
        
        // Set the input data for the viewer
        treeViewer.setInput(items);

        // Enable editing for the first column
        treeViewer.setColumnProperties(new String[] { "Item", "Value" });
        CellEditor[] editors = new CellEditor[1];
        editors[0] = new TextCellEditor(treeViewer.getTree());
        treeViewer.setCellEditors(editors);
        treeViewer.setCellModifier(new MyCellModifier());
        treeViewer.getTree().addListener(SWT.KeyDown, new TreeKeyListener());
        
        setPartName("Meteor View");
        
        ImageDescriptor imageDescriptor = Activator.getImageDescriptor("icons/meteorlogo16.png");
        setTitleImage(imageDescriptor.createImage());
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
                    if(!item.getIconPath().isEmpty()) {
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
                if(!item.getIconPath().isEmpty()) {
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
}
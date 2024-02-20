package meteor.eclipse.plugin.core.components.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import meteor.eclipse.plugin.core.functional.FunctionAfterLoadJSONFile;
import meteor.eclipse.plugin.core.tuples.Tuple5;
import meteor.eclipse.plugin.core.views.View;
import meteor.eclipse.plugin.core.views.View.Item;

import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.error;
import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.info;

public class ViewUtils {

	private static View view;
	private static int counter;

	private static String REFACTORING_SESSION_LABEL = "Refactoring Session";
	private static String BASELINE_TEST_MUTATION_SCORE_LABEL = "Baseline -> Test Mutation Score";
	private static String LAST_RESULT_TEST_MUTATION_SCORE_LABEL = "Last Result -> Test Mutation Score";
	private static String REFACTORING_RESULT_LABEL = "Refactoring Result";

	public static void clear() {
		view.clearItems();
		counter = 0;
	}
	
	public static void showViewMainPanel() throws PartInitException {
		view = (View) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView("meteor.eclipse.plugin.core.view");
	}

	public static int addNewSession() {

		@SuppressWarnings("unchecked")
		List<View.Item> input = (List<Item>) view.getTreeViewer().getInput();

		View.Item parentItem = new View.Item(REFACTORING_SESSION_LABEL, "#" + String.valueOf(++counter), "icons/meteortest16.png"),
				childItem1 = new View.Item(BASELINE_TEST_MUTATION_SCORE_LABEL, "", "icons/meteorpinbl.png"),
				childItem2 = new View.Item(LAST_RESULT_TEST_MUTATION_SCORE_LABEL, "", "icons/meteortest16.png"),
				childItem3 = new View.Item(REFACTORING_RESULT_LABEL, "", "icons/meteorres.png");

		parentItem.addChild(childItem1);
		parentItem.addChild(childItem2);
		parentItem.addChild(childItem3);

		input.add(parentItem);
		view.getTreeViewer().refresh(true);

		return counter;
	}

	public static void changeBaselineTestMutationScore(int refactoringSession, Double baselineTestMutationScore) {

		View.Item parentItem = getParentItem(refactoringSession);
		parentItem.getChildren().forEach(e -> {
			if (e.getKey().equals(BASELINE_TEST_MUTATION_SCORE_LABEL)) {
				e.setValue(String.valueOf(baselineTestMutationScore == null ? "" : baselineTestMutationScore));
				view.getTreeViewer().update(e, new String[] { "Key", "Value" });
				view.getTreeViewer().refresh(true);
			}
		});

	}

	public static void changeResult(int refactoringSession, String result, String report) {

		View.Item parentItem = getParentItem(refactoringSession);
	    parentItem.getChildren().forEach(e -> {
	        if (e.getKey().equals(REFACTORING_RESULT_LABEL)) {
	            if (result != null) {
					if (result.contains("Refactoring unsuccessfull"))
	                	e.setIconPath("icons/meteorrefnok.png") ;
	                else if (result.contains("Refactoring successfull"))	    	                    
	                	e.setIconPath("icons/meteorrefok.png");           
					
	                e.setValue(String.valueOf(result));
	            }
	            if (report != null)
	                e.setReport(report);
	            
	            view.getTreeViewer().update(e, new String[] { "Key", "Value", "Item" });
	            view.getTreeViewer().refresh(true);
	        }
	    });

	}
	
	public static void changeLastResultTestMutationScore(int refactoringSession, String lastResultTestMutationScore) {

		View.Item parentItem = getParentItem(refactoringSession);
		parentItem.getChildren().forEach(e -> {
			if (e.getKey().equals(LAST_RESULT_TEST_MUTATION_SCORE_LABEL)) {
				e.setValue(lastResultTestMutationScore == null ? "" : lastResultTestMutationScore);
				view.getTreeViewer().update(e, new String[] { "Key", "Value" });
				view.getTreeViewer().refresh(true);
			}
		});

	}

	public static View.Item getParentItem(int refactoringSession) {

		// Traverse through the tree items to find the desired item
		TreeItem[] treeItems = view.getTreeViewer().getTree().getItems();
		for (TreeItem parentItem : treeItems) {

			View.Item item = (Item) parentItem.getData();

			// Check if the parent item meets the desired criterion
			if (item.getKey().equals(REFACTORING_SESSION_LABEL)
					&& item.getValue().equals("#" + String.valueOf(refactoringSession))) {
				return item;
			}
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void exportRefactoringSessions() {
		
		if (view == null || view.getTreeViewer() == null || view.getTreeViewer().getInput() == null
			|| ((List<Item>) view.getTreeViewer().getInput()).size() ==0 ) {
			error("You must run at least one refactoring session!");
			return;
		}
			
		Display.getDefault().syncExec(new Runnable() {
		    @Override
		    public void run() {
		        Shell shell = new Shell(Display.getDefault());
		        File file = showSaveDialog(shell);
		    	if (file != null) {
					List<View.Item> input = (List<Item>) view.getTreeViewer().getInput();
			
					if (input != null) {
						exportToJson(input, file.getAbsolutePath());
					}			
				}
		    }
		});
	}
	
	public static void importRefactoringSessions(FunctionAfterLoadJSONFile afterLoadJsonFunction) {
		
		if (view == null || view.getTreeViewer() == null) {
			error("You must run show the view!");
			return;
		}
		
		Display.getDefault().syncExec(new Runnable() {
		    @Override
		    public void run() {
		        Shell shell = new Shell(Display.getDefault());
		        File file = showOpenDialog(shell);			       			        
		    	if (file != null) {			    				    		
					Tuple5<List<View.Item>, Integer, Double, Double, String> result = importFromJson(file.getAbsolutePath());
					if (result != null) {
						afterLoadJsonFunction.execute(result.second, result.third, result.fourth, result.fifth);
						view.setTreeViewItems(result.first);
						view.getTreeViewer().refresh();
						counter = result.first.size();
					}			
				}
		    }
		});
		
	}
	
	public static File showSaveDialog(Shell shell) {
	    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
	    dialog.setText("Export to ...");
	    dialog.setFilterExtensions(new String[] {"*.json"});
	    String filePath = dialog.open();
	    if (filePath != null) {
	        return new File(filePath);
	    } else {
	        return null;
	    }
	}
	
	public static File showOpenDialog(Shell shell) {
	    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
	    dialog.setText("Import from ...");
	    dialog.setFilterExtensions(new String[] {"*.json"});
	    String filePath = dialog.open();
	    if (filePath != null) {
	        return new File(filePath);
	    } else {
	        return null;
	    }
	}
    
	// Exporta a lista para JSON
    public static void exportToJson(List<Item> itemList, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<Item> listCopied = new ArrayList<View.Item>(itemList);

        try {
        	
            if(listCopied.size() > 0) {
            	Item lastItem = itemList.get(listCopied.size() -1);
            	if (lastItem.getKey().equalsIgnoreCase(REFACTORING_SESSION_LABEL)) {            		
            		AtomicReference<String> sessionResult = new AtomicReference<String>();
            		
            		lastItem.getChildren().forEach(e -> {
            			if(e.getKey().equalsIgnoreCase(REFACTORING_RESULT_LABEL))            				
            				sessionResult.set(e.getValue());
            		});
            		
            		if (sessionResult.get() != null && sessionResult.get().equals("")){
            			info("Your last refactoring session was not validate, so it can not be exported because it is not concluded. The other sessions will be exported normally to your json.");
            			listCopied.remove(listCopied.size() -1);
            		}
            	}
            }
            	
            objectMapper.writeValue(new File(filePath), listCopied);
            
            
            info("Items exported to JSON successfully!");
        } catch (Exception e) {
        	error("Error on export to JSON!");
        }
    }

    // Importa a lista de JSON
    public static Tuple5<List<Item>, Integer, Double, Double, String> importFromJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Item> itemList = new ArrayList<>();
        Tuple5<List<Item>, Integer, Double, Double, String> result = null;
        
        try {
            itemList = objectMapper.readValue(new File(filePath), objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class));
            
            if(itemList.size() > 0) {
            	Item lastItem = itemList.get(itemList.size() -1);
            	if (lastItem.getKey().equalsIgnoreCase(REFACTORING_SESSION_LABEL)) {
            		AtomicReference<Double> baselineMutationScore = new AtomicReference<Double>(), 
            					  			lastResultMutationScore = new AtomicReference<Double>();
            		AtomicReference<String> sessionResult = new AtomicReference<String>();
            		
            		lastItem.getChildren().forEach(e -> {
            			if (e.getKey().equalsIgnoreCase(BASELINE_TEST_MUTATION_SCORE_LABEL)) 
            				baselineMutationScore.set(Double.valueOf(e.getValue()));
            			else if(e.getKey().equalsIgnoreCase(LAST_RESULT_TEST_MUTATION_SCORE_LABEL)) 
                			lastResultMutationScore.set(Double.valueOf(e.getValue()));     
            			else if(e.getKey().equalsIgnoreCase(REFACTORING_RESULT_LABEL))            				
            				sessionResult.set(e.getValue());
            		});
            		            		
            		result = new Tuple5<List<Item>, Integer, Double, Double, String>
            												(itemList, 
            												 Integer.valueOf(lastItem.getValue().replaceAll("#", "")), 
            												 baselineMutationScore.get(), 
            												 lastResultMutationScore.get(),
            												 sessionResult.get());
            	}
            }            	
            
            info("Items imported from JSON successfully!");
        } catch (Exception e) {
        	error("Error on import from JSON!");
        }

        return result;
    }

}

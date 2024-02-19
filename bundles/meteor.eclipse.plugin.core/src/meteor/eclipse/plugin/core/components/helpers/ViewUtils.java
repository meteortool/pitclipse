package meteor.eclipse.plugin.core.components.helpers;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;

import meteor.eclipse.plugin.core.views.View;
import meteor.eclipse.plugin.core.views.View.Item;

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
	
	/*public static void exportRefactoringSessions() {
		
		File file = showSaveDialog();
		if (file != null) {
			@SuppressWarnings("unchecked")
			List<View.Item> input = (List<Item>) view.getTreeViewer().getInput();
	
			if (input != null) {
				exportToJson(input, file.getAbsolutePath());
			}			
		}
		
	}
	
	public static void importRefactoringSessions() {
		
		
		
	}*/
	
	// Mostra um seletor de arquivo para salvar
    public static File showSaveDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar como...");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }
	
    /*
	// Exporta a lista para JSON
    public static void exportToJson(List<Item> itemList, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(filePath), itemList);
            System.out.println("Items exported to JSON successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Importa a lista de JSON
    public static List<Item> importFromJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Item> itemList = new ArrayList<>();

        try {
            itemList = objectMapper.readValue(new File(filePath), objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class));
            System.out.println("Items imported from JSON successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemList;
    }*/

}

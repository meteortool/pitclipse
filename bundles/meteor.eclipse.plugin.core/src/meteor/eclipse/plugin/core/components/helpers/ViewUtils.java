package meteor.eclipse.plugin.core.components.helpers;

import java.util.List;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

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
	}
	
	public static void showViewMainPanel() throws PartInitException {
		view = (View) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView("meteor.eclipse.plugin.core.view");
	}

	public static int addNewSession() {

		@SuppressWarnings("unchecked")
		List<View.Item> input = (List<Item>) view.getTreeViewer().getInput();

		View.Item parentItem = new View.Item(REFACTORING_SESSION_LABEL, "#" + String.valueOf(++counter), "icons/meteortest16.png"),
				childItem1 = new View.Item(BASELINE_TEST_MUTATION_SCORE_LABEL, "", "icons/meteortest16.png"),
				childItem2 = new View.Item(LAST_RESULT_TEST_MUTATION_SCORE_LABEL, "", "icons/meteortest16.png"),
				childItem3 = new View.Item(REFACTORING_RESULT_LABEL, "", "icons/meteortest16.png");

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

	public static void changeResult(int refactoringSession, String result) {

		View.Item parentItem = getParentItem(refactoringSession);
		parentItem.getChildren().forEach(e -> {
			if (e.getKey().equals(REFACTORING_RESULT_LABEL)) {
				e.setValue(String.valueOf(result));
				view.getTreeViewer().update(e, new String[] { "Key", "Value" });
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

}

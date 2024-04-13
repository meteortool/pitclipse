package meteor.eclipse.plugin.core.components.helpers;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import meteor.eclipse.plugin.core.tuples.Tuple7;
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

		View.Item parentItem = new View.Item(REFACTORING_SESSION_LABEL, "#" + String.valueOf(++counter),
				"icons/meteortest16.png"),
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
	

	public static void changeLastResultTo(int refactoringSession, String text) {

		View.Item parentItem = getParentItem(refactoringSession);
		parentItem.getChildren().forEach(e -> {
			if (e.getKey().equals(LAST_RESULT_TEST_MUTATION_SCORE_LABEL)) {
				e.setValue(text);
				view.getTreeViewer().update(e, new String[] { "Key", "Value" });
				view.getTreeViewer().refresh(true);
			}
		});

	}

	
	public static void changeLastResultTestMutationScore(int refactoringSession, Integer lastResultTestMutationCoverage, Double lastResultTestMutationScore) {

		View.Item parentItem = getParentItem(refactoringSession);
		parentItem.getChildren().forEach(e -> {
			if (e.getKey().equals(LAST_RESULT_TEST_MUTATION_SCORE_LABEL)) {
	            String scoreText = "";
	            if (lastResultTestMutationCoverage != null && lastResultTestMutationScore != null) {
	                DecimalFormat df = new DecimalFormat("#.####");
	                scoreText = lastResultTestMutationCoverage + "% (" + df.format(lastResultTestMutationScore) + ")";
	            }
	            e.setValue(scoreText);
	            view.getTreeViewer().update(e, new String[] { "Key", "Value" });
	            view.getTreeViewer().refresh(true);
	        }
		});

	}

	public static void changeBaselineTestMutationScore(int refactoringSession, Integer baselineTestMutationCoverage, Double baselineTestMutationScore) {

		View.Item parentItem = getParentItem(refactoringSession);
		parentItem.getChildren().forEach(e -> {
			if (e.getKey().equals(BASELINE_TEST_MUTATION_SCORE_LABEL)) {
	            String scoreText = "";
	            if (baselineTestMutationCoverage != null && baselineTestMutationScore != null) {
	                DecimalFormat df = new DecimalFormat("#.####");
	                scoreText = baselineTestMutationCoverage + "% (" + df.format(baselineTestMutationScore) + ")";
	            }
	            e.setValue(scoreText);
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
						e.setIconPath("icons/meteorrefnok.png");
					else if (result.contains("Refactoring successfull"))
						e.setIconPath("icons/meteorrefok.png");
					else if (result.contains("inconclusive"))
						e.setIconPath("icons/meteorrefinc.png");

					e.setValue(String.valueOf(result));
				}
				if (report != null)
					e.setReport(report);

				view.getTreeViewer().update(e, new String[] { "Key", "Value", "Item" });
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
				|| ((List<Item>) view.getTreeViewer().getInput()).size() == 0) {
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
					Tuple7<List<View.Item>, Integer, Integer, Double, Integer, Double, String> result = importFromJson(
							file.getAbsolutePath());
					if (result != null) {
						afterLoadJsonFunction.execute(
								result.second, 
								result.third, 
								result.fourth, 
								result.fifth,
								result.sixth,
								result.seventh);
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
		dialog.setFilterExtensions(new String[] { "*.json" });
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
		dialog.setFilterExtensions(new String[] { "*.json" });
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

			if (listCopied.size() > 0) {
				Item lastItem = itemList.get(listCopied.size() - 1);
				if (lastItem.getKey().equalsIgnoreCase(REFACTORING_SESSION_LABEL)) {
					AtomicReference<String> sessionResult = new AtomicReference<String>();

					lastItem.getChildren().forEach(e -> {
						if (e.getKey().equalsIgnoreCase(REFACTORING_RESULT_LABEL))
							sessionResult.set(e.getValue());
					});

					if (sessionResult.get() != null && sessionResult.get().equals("")) {
						info("Your last refactoring session was not validate, so it can not be exported because it is not concluded. The other sessions will be exported normally to your json.");
						listCopied.remove(listCopied.size() - 1);
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
	public static Tuple7<List<Item>, Integer, Integer, Double, Integer, Double, String> importFromJson(String filePath) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<Item> itemList = new ArrayList<>();
		Tuple7<List<Item>, Integer, Integer, Double, Integer, Double, String> result = null;

		try {
			itemList = objectMapper.readValue(new File(filePath),
					objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class));

			if (itemList.size() > 0) {
				Item lastItem = itemList.get(itemList.size() - 1);
				if (lastItem.getKey().equalsIgnoreCase(REFACTORING_SESSION_LABEL)) {
					AtomicReference<Integer> baselineMutationCoverage = new AtomicReference<Integer>(),
											lastResultMutationCoverage = new AtomicReference<Integer>();
					AtomicReference<Double> baselineMutationScore = new AtomicReference<Double>(),
											lastResultMutationScore = new AtomicReference<Double>();
					AtomicReference<String> sessionResult = new AtomicReference<String>();

					// Dentro do seu código
					lastItem.getChildren().forEach(e -> {
					    if (e.getKey().equalsIgnoreCase(BASELINE_TEST_MUTATION_SCORE_LABEL)) {
					        String value = e.getValue();
					        if (value.contains("%")) {
					            Pattern pattern = Pattern.compile("(\\d+)% \\((\\d+\\,\\d+)\\)");
					            Matcher matcher = pattern.matcher(value);
					            if (matcher.find()) {
					                int coverage = Integer.parseInt(matcher.group(1));
					                double score = Double.parseDouble(matcher.group(2).replace(',', '.'));
					                baselineMutationCoverage.set(coverage);
					                baselineMutationScore.set(score);
					            }
					        } else {
					            baselineMutationCoverage.set(null);
					            baselineMutationScore.set(Double.valueOf(value));
					        }
					    } else if (e.getKey().equalsIgnoreCase(LAST_RESULT_TEST_MUTATION_SCORE_LABEL)) {
					        String value = e.getValue();
					        if (value.contains("%")) {
					            Pattern pattern = Pattern.compile("(\\d+)% \\((\\d+\\,\\d+)\\)");
					            Matcher matcher = pattern.matcher(value);
					            if (matcher.find()) {
					                int coverage = Integer.parseInt(matcher.group(1));
					                double score = Double.parseDouble(matcher.group(2).replace(',','.'));
					                lastResultMutationCoverage.set(coverage);
					                lastResultMutationScore.set(score);
					            }
					        } else {
					            lastResultMutationCoverage.set(null);
					            lastResultMutationScore.set(Double.valueOf(value));
					        }
					    } else if (e.getKey().equalsIgnoreCase(REFACTORING_RESULT_LABEL)) {
					        sessionResult.set(e.getValue());
					    }
					});

					result = new Tuple7<List<Item>, Integer, Integer, Double, Integer, Double, String>(
							itemList,
							Integer.valueOf(lastItem.getValue().replaceAll("#", "")), 
							baselineMutationCoverage.get(),
							baselineMutationScore.get(),
							lastResultMutationCoverage.get(),
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

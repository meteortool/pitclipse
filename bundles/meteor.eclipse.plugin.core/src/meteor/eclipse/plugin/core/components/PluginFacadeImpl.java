package meteor.eclipse.plugin.core.components;

import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.ask;
import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.error;
import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.pitest.pitclipse.runner.PitResults;
import org.pitest.pitclipse.ui.swtbot.ResultsParser;
import org.pitest.util.Log;

import meteor.eclipse.plugin.core.Activator;
import meteor.eclipse.plugin.core.components.helpers.ValidatorUtils;
import meteor.eclipse.plugin.core.components.helpers.ValidatorUtils.ValidationResult;
import meteor.eclipse.plugin.core.components.helpers.ViewUtils;
import meteor.eclipse.plugin.core.components.mutation.tests.TestMutationAgent;
import meteor.eclipse.plugin.core.threading.ResultListenerNotifier;

public class PluginFacadeImpl implements PluginFacade, ResultListenerNotifier {

	private int refactoringSession = -1;
	private Double lastResultTestMutationScore, baselineResultTestMutationScore;
	private TestMutationAgent mutationAgent;
	private ISelection selection;
	private boolean isLocked;
	private boolean isValidationDone;
	private List<ValidationResult> validationResults, behaviourChangedMutants;

	public PluginFacadeImpl(TestMutationAgent mutationAgent) {
		this.mutationAgent = mutationAgent;
		Activator.getDefault().addNotifier(this);
	}

	public void lock() {
		this.isLocked = true;
	}

	public void unlock() {
		this.isLocked = true;
	}

	@Override
	public void viewMainPanel() throws Exception {
		ViewUtils.showViewMainPanel();
	}
	
	public void generatePdfAnalysisReport() throws Exception {
		if (ask("Do you want to generate analysis report?")) {
			ViewUtils.showViewMainPanel();

			if (baselineResultTestMutationScore == null) {
				info("You must fix a baseline to proceed!");
			} else {
				if (lastResultTestMutationScore == null) {
					info("You must run mutation tests after refactoring to generate report!");
				} else {
					if (!isValidationDone) {
						info("You must validate refactoring before generate report");
					}	else {
						ValidatorUtils validatorUtils = new ValidatorUtils();
						Path tempDir = Files.createTempDirectory("pdf_reports");
						
						validatorUtils.generatePdfReport(validationResults, 
														 tempDir.resolve("mutation_test_report.pdf")
														 	.toString());
					}
				}
			}
		}
	}

	@Override
	public void validateRefactoring() throws Exception {
		if (ask("Do you want to validate refactoring?")) {
			ViewUtils.showViewMainPanel();

			if (baselineResultTestMutationScore == null) {
				info("You must fix a baseline to proceed!");
			} else {
				if (lastResultTestMutationScore == null) {
					info("You must run mutation tests after refactoring to validate!");
				} else {
					ValidatorUtils validatorUtils = new ValidatorUtils();
					validationResults = validatorUtils
							.validateMutations(mutationAgent.getBaselineResults(), mutationAgent.getLastResults());

					behaviourChangedMutants = validationResults.stream()
							.filter(r -> r.isChangedBehaviour())
							.collect(Collectors.toList());

					if (behaviourChangedMutants.size() > 0) {
						ViewUtils.changeResult(refactoringSession,
								"Refactoring unsuccessfull (" + behaviourChangedMutants.size() + ") changes.");

						Log.getLogger().info("________________________BEHAVIOUR CHANGES____________________________");
						behaviourChangedMutants.forEach(i -> {
							Log.getLogger().info(i.toString());
						});

					} else {
						ViewUtils.changeResult(refactoringSession, "Refactoring successfull.");
					}
					
					isValidationDone = true;
				}

			}
		}

	}

	@Override
	public void runMutationTests() throws Exception {
		if (ask("Do you want to run mutation tests? This process can take some minutes.")) {
			isValidationDone = false;
			ViewUtils.showViewMainPanel();

			if (getSelectedResource() == null) {
				error("You must select a valid test package or a test class before running your mutation testing for this refactoring session!");
			} else {
				if (refactoringSession == -1 && ask("Do you want to create a new refactoring session?")) {
					refactoringSession = ViewUtils.addNewSession();
				}
				ViewUtils.changeLastResultTestMutationScore(refactoringSession, "Wait...");
				mutationAgent.run(this, this, refactoringSession);
			}
		}
	}

	@Override
	public void generateBaseline() throws Exception {
		if (ask("Do you want to fix actual results as baseline?")) {
			isValidationDone = false;
			ViewUtils.showViewMainPanel();
			ViewUtils.changeLastResultTestMutationScore(refactoringSession, null);
			ViewUtils.changeBaselineTestMutationScore(refactoringSession, lastResultTestMutationScore);
			baselineResultTestMutationScore = lastResultTestMutationScore;
			lastResultTestMutationScore = null;
			mutationAgent.generateBaseline();
		}

	}

	@Override
	public void notifyOnComplete(PitResults results) {

		try {
			ResultsParser parser = new ResultsParser(results);
			mutationAgent.setLastResults(results);

			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				lastResultTestMutationScore = parser.getSummary().getMutationCoverage();
				ViewUtils.changeLastResultTestMutationScore(refactoringSession,
						String.valueOf(lastResultTestMutationScore));
			});

			Log.getLogger().info("________________________RESULT____________________________");
			mutationAgent.getLastResults().forEach(i -> {
				Log.getLogger().info(i.toString());
			});

		} catch (IOException e) {
			throw new RuntimeException("Error on parsing results.", e);
		}

	}

	public void setSelectedResource(ISelection selection) {
		this.selection = selection;
	}

	public ISelection getSelectedResource() {
		return this.selection;
	}

	@Override
	public boolean IsLocked() {
		return isLocked;
	}

	@Override
	public void reset() throws Exception {
		if (ask("Do you want to delete all the previous refactoring results?")) {
			ViewUtils.showViewMainPanel();
			ViewUtils.clear();

			mutationAgent.clearBaseline();
			mutationAgent.setLastResults(null);

			baselineResultTestMutationScore = null;
			lastResultTestMutationScore = null;
			refactoringSession = -1;
		}
	}

}

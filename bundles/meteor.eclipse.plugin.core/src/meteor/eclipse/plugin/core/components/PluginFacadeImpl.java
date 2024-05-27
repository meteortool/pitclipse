package meteor.eclipse.plugin.core.components;

import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.ask;
import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.error;
import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.pitest.pitclipse.runner.PitResults;
import org.pitest.pitclipse.ui.swtbot.ResultsParser;
import org.pitest.util.Log;

import meteor.eclipse.plugin.core.Activator;
import meteor.eclipse.plugin.core.components.helpers.EclipseUtils;
import meteor.eclipse.plugin.core.components.helpers.FileUtils;
import meteor.eclipse.plugin.core.components.helpers.ValidatorUtils;
import meteor.eclipse.plugin.core.components.helpers.ValidatorUtils.ValidationResult;
import meteor.eclipse.plugin.core.components.helpers.ViewUtils;
import meteor.eclipse.plugin.core.components.mutation.tests.TestMutationAgent;
import meteor.eclipse.plugin.core.threading.ResultListenerNotifier;
import meteor.eclipse.plugin.core.tuples.Tuple4;
import meteor.eclipse.plugin.core.tuples.Tuple5;

public class PluginFacadeImpl implements PluginFacade, ResultListenerNotifier {

	private int refactoringSession = -1;
	private Double lastResultTestMutationScore, baselineTestMutationScore, lastResultStatementCoverage,
			baselineStatementCoverage;
	private Integer lastResultTestMutationCoverage, baselineTestMutationCoverage;
	private TestMutationAgent mutationAgent;
	private ISelection selection;
	private IProject project;
	private boolean isLocked;
	private boolean isValidationDone;
	private Tuple5<List<ValidationResult>, Boolean, Boolean, Boolean, Integer> validationResults;
	private List<ValidationResult> behaviourChangedMutants;
	private Path tempDir;

	public PluginFacadeImpl(TestMutationAgent mutationAgent) {
		this.mutationAgent = mutationAgent;
		Activator.getDefault().addNotifier(this);
	}

	public void lock() {
		this.isLocked = true;
	}

	public void unlock() {
		this.isLocked = false;
	}

	@Override
	public void viewMainPanel() throws Exception {
		ViewUtils.showViewMainPanel();
	}

	public void generateCsvAnalysisReport() throws Exception {
		if (ask("Do you want to generate analysis report?")) {
			ViewUtils.showViewMainPanel();
			if (baselineTestMutationScore == null) {
				info("You must fix a baseline to proceed!");
			} else {
				if (lastResultTestMutationScore == null) {
					info("You must run mutation tests after refactoring to generate report!");
				} else {
					if (!isValidationDone) {
						info("You must validate refactoring before generate report");
					} else {
						String filePath = "";

						try {
							ValidatorUtils validatorUtils = new ValidatorUtils();

							if (tempDir == null)
								tempDir = Files.createTempDirectory("");

							LocalDateTime now = LocalDateTime.now();
							String reportName = String.format("mutation_test_report_%d%02d%02d%02d%02d%02d%03d.csv",
									now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(),
									now.getMinute(), now.getSecond(), now.get(ChronoField.MILLI_OF_SECOND));

							filePath = tempDir.resolve(reportName).toString();
							validatorUtils.generateCSV(validationResults.first, filePath);

							if (ask("Analysis report generated successfully in path: \n\n" + filePath
									+ "\n\n Do you want to open the report?")) {
								FileUtils.openFileInEclipse(filePath);
							}

							ViewUtils.changeResult(refactoringSession, null, filePath);

						} catch (Exception e) {
							if (filePath != "")
								error("Error on analysis report generation in path " + filePath + "\n" + e.getMessage()
										+ "\n" + e.getStackTrace());
							else
								error("Error on analysis report generation\n" + e.getMessage() + "\n"
										+ e.getStackTrace());
						}
					}
				}
			}
		}
	}

	@Override
	public void validateRefactoring() throws Exception {
		if (ask("Do you want to validate refactoring?")) {
			ViewUtils.showViewMainPanel();

			if (baselineTestMutationScore == null) {
				info("You must fix a baseline to proceed!");
			} else {
				if (lastResultTestMutationScore == null) {
					info("You must run mutation tests after refactoring to validate!");
				} else {
					ValidatorUtils validatorUtils = new ValidatorUtils();
					validationResults = validatorUtils.validateMutations(mutationAgent.getBaselineResults(),
							mutationAgent.getLastResults());

					behaviourChangedMutants = validationResults.first.stream().filter(r -> r.isChangedBehaviour())
							.collect(Collectors.toList());

					if (behaviourChangedMutants.size() > 0 || (behaviourChangedMutants.size() == 0
							&& validationResults.fourth
							&& ask("It was detected that there were some changes in killing tests before and after refactoring. "
									+ "Would you like to take this into consideration during the analysis? "
									+ "If you only changed the name of a test, or if you aggregated or disaggregated tests, it is suggested to confirm this dialog with 'no.' "
									+ "Otherwise, indeed, confirm as 'yes' and you may need to evaluate the tests to prevent your validation from being contaminated by the masking effect."
									+ "\n\nIf you confirm 'yes', your refactoring will be considered unsuccessful even if the mutant states have not changed. "
									+ "\n\nAll results marked with (*) will indicate differences in testing."))) {

						Log.getLogger().info("________________________BEHAVIOUR CHANGES____________________________");
						behaviourChangedMutants.forEach(i -> {
							Log.getLogger().info(i.toString());
						});

						if (validationResults.third) {
							ViewUtils.changeResult(refactoringSession,
									"Refactoring was inconclusive due to the presence of TIMED_OUT or other not expected mutante states changes."
											+ "\n\nTotal (" + behaviourChangedMutants.size() + ") changes.",
									null);

							info("There are some behavior changes related to non default mutant detection. "
									+ "The expected detection are SURVIVED, KILLED or NO_COVERAGE. "
									+ "You can retry your refactoring session running again the test execution."
									+ "Please review carefully your results.");
						} else {
							if (behaviourChangedMutants.size() == 0 && validationResults.fifth > 0) {
								ViewUtils.changeResult(refactoringSession, "Refactoring unsuccessfull ("
										+ validationResults.fifth + ") changes in killing tests.", null);
							} else {
								ViewUtils.changeResult(refactoringSession,
										"Refactoring unsuccessfull (" + behaviourChangedMutants.size() + ") changes.",
										null);
							}
						}

					} else {
						if (!baselineStatementCoverage.equals(lastResultStatementCoverage)) {
							DecimalFormat df = new DecimalFormat("#.####");
							ViewUtils.changeResult(refactoringSession,
									"Refactoring unsuccessfull due to statement coverage divergence. (Stmt. cvrg. before: "
											+ df.format(baselineStatementCoverage) + " - Stmt. cvrg. after: "
											+ df.format(lastResultStatementCoverage) + ")",
									null);
						} else {
							ViewUtils.changeResult(refactoringSession, "Refactoring successfull"
									+ (behaviourChangedMutants.size() == 0 && validationResults.fourth ? "*" : "")
									+ ".", null);
						}
					}

					isValidationDone = true;

					generateCsvAnalysisReport();
				}
			}
		}
	}

	@Override
	public void runMutationTests() throws Exception {
		if (ask("Do you want to run mutation tests? This process can take some minutes.")) {
			ViewUtils.showViewMainPanel();
			if (isValidationDone) {
				if (validationResults == null || validationResults.first.size() == 0) {
					error("You have imported a JSON file and you need to create a new refactoring session before run mutation testing!");
					return;
				}

				info("This refactory session was validated before. So the last validation results will be clear, you must validate again after run mutation test.");
				mutationAgent.setLastResults(null);
				lastResultTestMutationScore = null;
				validationResults = null;
				ViewUtils.changeResult(refactoringSession, "", null);
			}
			isValidationDone = false;

			if (getSelectedResource() == null || getSelectedProject() == null) {
				error("You must select a valid test package or a test class before running your mutation testing for this refactoring session!");
			} else {
				if (EclipseUtils.hasCompilationErrors(getSelectedProject())) {
					error("This project has build errors. Please review your code before run mutation tests.");
					return;
				}

				if (refactoringSession == -1 && ask("Do you want to create a new refactoring session?")) {
					refactoringSession = ViewUtils.addNewSession();
				}
				ViewUtils.changeLastResultTo(refactoringSession, "Wait ...");
				mutationAgent.run(this, this, refactoringSession);
			}
		}
	}

	@Override
	public void createSessionRefactoring() throws Exception {
		if (refactoringSession != -1 && !isValidationDone) {
			error("You must finish the previous session to evolve to a new refactoring session.");
		} else {
			if (ask("Do you want to create a session refactoring?")) {
				ViewUtils.showViewMainPanel();
				refactoringSession = ViewUtils.addNewSession();

				if (refactoringSession > 1 && lastResultTestMutationScore != null && validationResults != null
						&& ask("Do you want to reuse last run result as baseline for this new refactoring session?")) {

					baselineTestMutationScore = lastResultTestMutationScore;
					baselineTestMutationCoverage = lastResultTestMutationCoverage;
					baselineStatementCoverage = lastResultStatementCoverage;

					ViewUtils.changeBaselineTestMutationScore(refactoringSession, baselineTestMutationCoverage,
							baselineTestMutationScore);
					mutationAgent.generateBaseline();
				} else {
					baselineTestMutationScore = null;
					baselineTestMutationCoverage = null;
					baselineStatementCoverage = null;
					mutationAgent.clearBaseline();
				}

				isValidationDone = false;
				validationResults = null;
				mutationAgent.setLastResults(null);
				lastResultTestMutationScore = null;
				lastResultTestMutationCoverage = null;
				lastResultStatementCoverage = null;
			}
		}
	}

	@Override
	public void generateBaseline() throws Exception {
		if (ask("Do you want to fix actual results as baseline?")) {
			isValidationDone = false;
			ViewUtils.showViewMainPanel();
			ViewUtils.changeLastResultTo(refactoringSession, "");
			ViewUtils.changeBaselineTestMutationScore(refactoringSession, lastResultTestMutationCoverage,
					lastResultTestMutationScore);
			baselineTestMutationScore = lastResultTestMutationScore;
			baselineStatementCoverage = lastResultStatementCoverage;
			lastResultTestMutationScore = null;
			mutationAgent.generateBaseline();
		}

	}

	@Override
	public void notifyOnComplete(PitResults results) {

		try {
			unlock();
			ResultsParser parser = new ResultsParser(results);
			mutationAgent.setLastResults(results);

			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				lastResultTestMutationCoverage = parser.getSummary().getMutationCoverage();
				lastResultTestMutationScore = parser.getSummary().getMutationScore();
				lastResultStatementCoverage = (double) (parser.getSummary().getLinesCovered())
						/ parser.getSummary().getLinesTotal();
				ViewUtils.changeLastResultTestMutationScore(refactoringSession, lastResultTestMutationCoverage,
						lastResultTestMutationScore);
			});

			Log.getLogger().info("________________________RESULT____________________________");
			mutationAgent.getLastResults().forEach(i -> {
				Log.getLogger().info(i.toString());
			});

		} catch (IOException e) {
			throw new RuntimeException("Error on parsing results.", e);
		} catch (Exception e) {
			throw new RuntimeException("Error on notification.", e);
		}

	}

	@Override
	public void notifyOnClose() {
		if (isLocked) {
			unlock();
			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				ViewUtils.changeLastResultTo(refactoringSession, null);
			});
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
	public void reset(Boolean skipConfirmation) throws Exception {
		if (skipConfirmation
				|| (!skipConfirmation && ask("Do you want to delete all the previous refactoring results?"))) {
			ViewUtils.showViewMainPanel();
			ViewUtils.clear();

			mutationAgent.clearBaseline();
			mutationAgent.setLastResults(null);

			baselineTestMutationScore = null;
			lastResultTestMutationScore = null;
			refactoringSession = -1;
		}
	}

	@Override
	public void exportData() throws Exception {
		if (ask("Confirm export refactoring sessions?")) {
			ViewUtils.showViewMainPanel();
			ViewUtils.exportRefactoringSessions();
		}
	}

	@Override
	public void importData() throws Exception {
		ViewUtils.showViewMainPanel();
		if (ask("All your not saved data will be lost! Confirm loading refactoring sessions?")) {
			ViewUtils.importRefactoringSessions((refactoringSession, baselineMutationCoverage, baselineMutationScore,
					lastResultMutationCoverage, lastResultMutationScore, result) -> {
				try {
					reset(true);
					if (refactoringSession != null)
						this.refactoringSession = refactoringSession;
					if (baselineMutationCoverage != null)
						this.baselineTestMutationCoverage = baselineMutationCoverage;
					if (baselineMutationScore != null)
						this.baselineTestMutationScore = baselineMutationScore;
					if (lastResultMutationCoverage != null)
						this.lastResultTestMutationCoverage = lastResultMutationCoverage;
					if (lastResultMutationScore != null)
						this.lastResultTestMutationScore = lastResultMutationScore;
					if (result != null && result != "")
						this.isValidationDone = true;
				} catch (Exception e) {
					error("Error on reset current view!");
				}
			});
		}
	}

	@Override
	public void setSelectedProject(IProject project) throws Exception {
		this.project = project;
	}

	@Override
	public IProject getSelectedProject() {
		return project;
	}

}

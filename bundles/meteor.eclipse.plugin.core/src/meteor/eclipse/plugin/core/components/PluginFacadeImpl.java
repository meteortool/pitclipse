package meteor.eclipse.plugin.core.components;

import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.ask;
import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.error;
import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.pitest.pitclipse.runner.PitResults;
import org.pitest.pitclipse.ui.swtbot.ResultsParser;
import org.pitest.util.Log;

import meteor.eclipse.plugin.core.Activator;
import meteor.eclipse.plugin.core.components.helpers.FileUtils;
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
						String filePath = "";
						
						try {
							ValidatorUtils validatorUtils = new ValidatorUtils();
							
							if(tempDir == null)
								tempDir = Files.createTempDirectory("");// ("meteor_reports");
												
							LocalDateTime now = LocalDateTime.now();
				            String reportName = String.format("mutation_test_report_%d%02d%02d%02d%02d%02d%03d.csv",
				                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
				                now.getHour(), now.getMinute(), now.getSecond(), now.get(ChronoField.MILLI_OF_SECOND));
				            
				            filePath = tempDir.resolve(reportName).toString();		
							validatorUtils.generateCSV(validationResults, filePath);
							
							if(ask("Analysis report generated successfully in path: \n\n" + filePath + "\n\n Do you want to open the report?")) {
								FileUtils.openFileInEclipse(filePath);
							}
							
							ViewUtils.changeResult(refactoringSession, null, filePath);
							
						} catch (Exception e) {
							if (filePath != "")								
								error("Error on analysis report generation in path " +  filePath + "\n" + e.getMessage() + "\n" + e.getStackTrace());
							else
								error("Error on analysis report generation\n" + e.getMessage() + "\n" + e.getStackTrace());
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
								"Refactoring unsuccessfull (" + behaviourChangedMutants.size() + ") changes.", null);

						Log.getLogger().info("________________________BEHAVIOUR CHANGES____________________________");
						behaviourChangedMutants.forEach(i -> {
							Log.getLogger().info(i.toString());
						});

					} else {
						ViewUtils.changeResult(refactoringSession, "Refactoring successfull.", null);
					}
					
					isValidationDone = true;
					
					generatePdfAnalysisReport();
				}

			}
		}

	}

	@Override
	public void runMutationTests() throws Exception {
		if (ask("Do you want to run mutation tests? This process can take some minutes.")) {
			ViewUtils.showViewMainPanel();
			if (isValidationDone) {			
				if (validationResults == null  || validationResults.size() == 0) {
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
	public void createSessionRefactoring() throws Exception {
		if(refactoringSession != -1 && !isValidationDone) { 
			error("You must finish the previous session to evolve to a new refactoring session.");
		} else { 
			if (ask("Do you want to create a session refactoring?")) {
				ViewUtils.showViewMainPanel();
				refactoringSession = ViewUtils.addNewSession();
				
				if(refactoringSession > 1 
						&& lastResultTestMutationScore != null 
						&& validationResults != null
						&& ask("Do you want to reuse last run result as baseline for this new refactoring session?")){
					baselineResultTestMutationScore = lastResultTestMutationScore;			
					ViewUtils.changeBaselineTestMutationScore(refactoringSession, baselineResultTestMutationScore);
					mutationAgent.generateBaseline();
				} else {
					baselineResultTestMutationScore = null;
					mutationAgent.clearBaseline();
				}
				
				isValidationDone = false;
				validationResults = null;			
				mutationAgent.setLastResults(null);
				lastResultTestMutationScore = null;						
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
			unlock();
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
	
	@Override
	public void notifyOnClose() {		
		if (isLocked) {
			unlock();
			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				ViewUtils.changeLastResultTestMutationScore(refactoringSession, "Stopped or failure");			
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
		if (skipConfirmation || (!skipConfirmation && ask("Do you want to delete all the previous refactoring results?"))) {
			ViewUtils.showViewMainPanel();
			ViewUtils.clear();

			mutationAgent.clearBaseline();
			mutationAgent.setLastResults(null);

			baselineResultTestMutationScore = null;
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
			ViewUtils.importRefactoringSessions((refactoringSession, baselineMutationScore, lastResultMutationScore, result) -> {
				try {
					reset(true);
					if (refactoringSession != null)
						this.refactoringSession = refactoringSession;
					if (baselineMutationScore != null)
						this.baselineResultTestMutationScore = baselineMutationScore;
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

}

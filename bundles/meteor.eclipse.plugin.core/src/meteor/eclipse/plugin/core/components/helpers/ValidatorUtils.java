package meteor.eclipse.plugin.core.components.helpers;

import static org.pitest.pitclipse.runner.results.DetectionStatus.KILLED;
import static org.pitest.pitclipse.runner.results.DetectionStatus.NO_COVERAGE;
import static org.pitest.pitclipse.runner.results.DetectionStatus.SURVIVED;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pitest.pitclipse.runner.results.DetectionStatus;
import org.pitest.util.Log;

import meteor.eclipse.plugin.core.components.mutation.tests.ResultEntry;
import meteor.eclipse.plugin.core.tuples.Tuple2;
import meteor.eclipse.plugin.core.tuples.Tuple3;
import meteor.eclipse.plugin.core.tuples.Tuple4;
import meteor.eclipse.plugin.core.tuples.Tuple5;

public class ValidatorUtils {

	public class ValidationResult {

		private int lineOfCode;
		private String description;
		private List<String> previousKillingTests;
		private List<String> afterKillingTests;
		private String className;
		private String methodName;
		private String mutator;
		private String previousDetectionStatus;
		private String afterDetectionStatus;
		private String sourceFile;
		private boolean changedBehaviour;
		private boolean changedKillingTests;

		public ValidationResult(int lineOfCode, String description, List<String> previousKillingTests,
				List<String> afterKillingTests, String className, String methodName, String mutator,
				String previousDetectionStatus, String afterDetectionStatus, String sourceFile,
				boolean changedBehaviour, boolean changedKillingTests) {
			this.lineOfCode = lineOfCode;
			this.description = description;
			this.previousKillingTests = previousKillingTests;
			this.afterKillingTests = afterKillingTests;
			this.className = className;
			this.methodName = methodName;
			this.mutator = mutator;
			this.previousDetectionStatus = previousDetectionStatus;
			this.afterDetectionStatus = afterDetectionStatus;
			this.sourceFile = sourceFile;
			this.changedBehaviour = changedBehaviour;
			this.changedKillingTests = changedKillingTests;
		}

		public int getLineOfCode() {
			return lineOfCode;
		}

		public void setLineOfCode(int lineOfCode) {
			this.lineOfCode = lineOfCode;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public List<String> getPreviousKillingTests() {
			return previousKillingTests;
		}

		public void setPreviousKillingTests(List<String> previousKillingTests) {
			this.previousKillingTests = previousKillingTests;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public String getMutator() {
			return mutator;
		}

		public void setMutator(String mutator) {
			this.mutator = mutator;
		}

		public String getPreviousDetectionStatus() {
			return previousDetectionStatus;
		}

		public void setPreviousDetectionStatus(String previousDetectionStatus) {
			this.previousDetectionStatus = previousDetectionStatus;
		}

		public String getAfterDetectionStatus() {
			return afterDetectionStatus;
		}

		public void setAfterDetectionStatus(String afterDetectionStatus) {
			this.afterDetectionStatus = afterDetectionStatus;
		}

		public String getSourceFile() {
			return sourceFile;
		}

		public void setSourceFile(String sourceFile) {
			this.sourceFile = sourceFile;
		}

		public boolean isChangedBehaviour() {
			return changedBehaviour;
		}

		public void setChangedBehaviour(boolean changedBehaviour) {
			this.changedBehaviour = changedBehaviour;
		}

		@Override
		public String toString() {
			return "ValidationResult [lineOfCode=" + lineOfCode + ", description=" + description + ", killingTest="
					+ afterKillingTests + ", className=" + className + ", methodName=" + methodName + ", mutator="
					+ mutator + ", previousDetectionStatus=" + previousDetectionStatus + ", afterDetectionStatus="
					+ afterDetectionStatus + ", sourceFile=" + sourceFile + ", changedBehaviour=" + changedBehaviour
					+ "]";
		}

		public List<String> getAfterKillingTests() {
			return afterKillingTests;
		}

		public void setAfterKillingTests(List<String> afterKillingTests) {
			this.afterKillingTests = afterKillingTests;
		}

		public boolean isChangedKillingTests() {
			return changedKillingTests;
		}

		public void setChangedKillingTests(boolean changedKillingTests) {
			this.changedKillingTests = changedKillingTests;
		}

	}

	public Tuple5<List<ValidationResult>, Boolean, Boolean, Boolean, Integer> validateMutations(
			List<ResultEntry> baselineResults, List<ResultEntry> lastRunResults) {

		Log.getLogger().info("________________BASELINE______________");
		baselineResults.forEach(i -> {
			Log.getLogger().info(i.toString());
		});

		Log.getLogger().info("________________LASTRUNRESULTS______________");
		lastRunResults.forEach(i -> {
			Log.getLogger().info(i.toString());
		});

		return compareResults(baselineResults, lastRunResults);

	}

	public void generateCSV(List<ValidationResult> validationResults, String filePath) {
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.append("Line of Code, " + "Class Name, " + "Method Name, " + "Mutator, " + "Description, "
					+ "Previous Killing Tests, " + "After Killing Tests, " + "Previous Detection State, "
					+ "After Detection State, " + "Changed Behaviour, " + "Source File\n");
			for (ValidationResult result : validationResults) {
				writer.append(result.getLineOfCode() + ",");
				writer.append(result.getClassName() + ",");
				writer.append(result.getMethodName() + ",");
				writer.append(result.getMutator() + ",");
				writer.append("\"" + result.getDescription() + "\",");
				writer.append("\"" + result.getPreviousKillingTests() + "\",");
				writer.append("\"" + result.getAfterKillingTests() + "\",");
				writer.append(result.getPreviousDetectionStatus() + ",");
				writer.append(result.getAfterDetectionStatus() + ",");
				writer.append(result.isChangedBehaviour() + (maskingEffect(result) ? "*" : "") + ",");
				writer.append(result.getSourceFile() + "\n");
			}

			Log.getLogger().info("CSV report generated successfully: " + filePath);
		} catch (IOException e) {
			Log.getLogger().severe(e.getMessage());
			Log.getLogger().severe(e.getStackTrace().toString());
			throw new RuntimeException(e);
		}
	}
	
	public boolean maskingEffect(ValidationResult result) {
		if (!result.isChangedBehaviour() &&
		    result.isChangedKillingTests() &&
			result.getAfterKillingTests().size() > 0 && 
			result.getPreviousKillingTests().size() > 0 /*&&
			(result.getAfterDetectionStatus().equals("SURVIVED") || result.getAfterDetectionStatus().equals("NO_COVERAGE"))*/){
			return true;			
		} 
		
		return false;		
	}

	public boolean checkIsNonDefaultResultValidation(ResultEntry entry1, ResultEntry entry2) {
		return (!DetectionStatus.fromValue(entry1.getDetectionStatus()).equals(KILLED)
				&& !DetectionStatus.fromValue(entry1.getDetectionStatus()).equals(SURVIVED)
				&& !DetectionStatus.fromValue(entry1.getDetectionStatus()).equals(NO_COVERAGE)
				&& (DetectionStatus.fromValue(entry2.getDetectionStatus()).equals(KILLED)
						|| DetectionStatus.fromValue(entry2.getDetectionStatus()).equals(SURVIVED)
						|| DetectionStatus.fromValue(entry2.getDetectionStatus()).equals(NO_COVERAGE)));
	}

	public boolean checkIsNonDefaultResult(ResultEntry entry1, ResultEntry entry2) {
		return checkIsNonDefaultResultValidation(entry1, entry2) || checkIsNonDefaultResultValidation(entry2, entry1);
	}

	public Tuple5<List<ValidationResult>, Boolean, Boolean, Boolean, Integer> compareResults(List<ResultEntry> baselineResults,
			List<ResultEntry> lastRunResults) {
		Tuple5<List<ValidationResult>, Boolean, Boolean, Boolean, Integer> result;
		List<ValidationResult> validationResults = new ArrayList<>();
		boolean hasChangedBehaviour = false, hasNonDefaultResult = false, hasKillingTestsDiff = false;
		Integer killingTestsChanges = 0;

		// Iterate over the baselineResults list
		for (ResultEntry baselineEntry : baselineResults) {

			ValidationResult validationResult = null;
			ResultEntry matchingEntry = findMatchingEntry(baselineEntry, lastRunResults);

			if (matchingEntry != null) {
				Tuple2<Boolean, Boolean> comparisonEntriesResult = compareEntries(baselineEntry, matchingEntry);
				validationResult = new ValidationResult(baselineEntry.getLineNumber(), baselineEntry.getDescription(),
						baselineEntry.getKillingTests(), matchingEntry.getKillingTests(),
						baselineEntry.getMutatedClass(), baselineEntry.getMutatedMethod(), baselineEntry.getMutator(),
						baselineEntry.getDetectionStatus(), matchingEntry.getDetectionStatus(),
						matchingEntry.getSourceFile(), !comparisonEntriesResult.first, !comparisonEntriesResult.second);

				if (!comparisonEntriesResult.second) {
					hasKillingTestsDiff = true;
					killingTestsChanges += 1;
				}
				if (validationResult.isChangedBehaviour()) {
					hasNonDefaultResult = checkIsNonDefaultResult(baselineEntry, matchingEntry);
				}

			} else {
				validationResult = new ValidationResult(baselineEntry.getLineNumber(), baselineEntry.getDescription(),
						baselineEntry.getKillingTests(), null, baselineEntry.getMutatedClass(),
						baselineEntry.getMutatedMethod(), baselineEntry.getMutator(),
						baselineEntry.getDetectionStatus(), "NOT PRESENT IN LAST RUN", baselineEntry.getSourceFile(),
						true, false);
			}

			if (validationResult.isChangedBehaviour()) {
				System.out.println("Change behaviour detected");
			}

			validationResults.add(validationResult);

		}

		// Iterate over the lastRunResults list
		for (ResultEntry lastRunResultsEntry : lastRunResults) {

			ValidationResult validationResult = null;
			ResultEntry matchingEntry = findMatchingEntry(lastRunResultsEntry, lastRunResults);

			if (matchingEntry != null) {
				Tuple2<Boolean, Boolean> comparisonEntriesResult = compareEntries(lastRunResultsEntry, matchingEntry);
				validationResult = new ValidationResult(lastRunResultsEntry.getLineNumber(),
						lastRunResultsEntry.getDescription(), lastRunResultsEntry.getKillingTests(),
						matchingEntry.getKillingTests(), lastRunResultsEntry.getMutatedClass(),
						lastRunResultsEntry.getMutatedMethod(), lastRunResultsEntry.getMutator(),
						lastRunResultsEntry.getDetectionStatus(), matchingEntry.getDetectionStatus(),
						lastRunResultsEntry.getSourceFile(), !comparisonEntriesResult.first, !comparisonEntriesResult.second);

				if (!comparisonEntriesResult.second) {
					hasKillingTestsDiff = true;
				}
				if (validationResult.isChangedBehaviour()) {
					hasNonDefaultResult = checkIsNonDefaultResult(lastRunResultsEntry, matchingEntry);
				}

			} else {
				validationResult = new ValidationResult(lastRunResultsEntry.getLineNumber(),
						lastRunResultsEntry.getDescription(), null, lastRunResultsEntry.getKillingTests(),
						lastRunResultsEntry.getMutatedClass(), lastRunResultsEntry.getMutatedMethod(),
						lastRunResultsEntry.getMutator(), lastRunResultsEntry.getDetectionStatus(),
						"NOT PRESENT IN BASELINE", lastRunResultsEntry.getSourceFile(), true, false);
			}

			if (validationResult.isChangedBehaviour()) {
				System.out.println("Change behaviour detected");
			}

			validationResults.add(validationResult);
		}

		result = new Tuple5<List<ValidationResult>, Boolean, Boolean, Boolean, Integer>(validationResults, hasChangedBehaviour,
				hasNonDefaultResult, hasKillingTestsDiff, killingTestsChanges);

		return result;
	}

	private ResultEntry findMatchingEntry(ResultEntry targetEntry, List<ResultEntry> resultList) {
		for (ResultEntry entry : resultList) {
			// Compare the necessary attributes to determine a match
			if (entriesMatch(targetEntry, entry)) {
				return entry;
			}
		}
		return null; // No matching entry found
	}

	private boolean entriesMatch(ResultEntry entry1, ResultEntry entry2) {

		return entry1.getMutatedClass().equals(entry2.getMutatedClass())
				&& entry1.getMutatedMethod().equals(entry2.getMutatedMethod())
				&& entry1.getMutator().equals(entry2.getMutator())
				&& entry1.getSourceFile().equals(entry2.getSourceFile())
				&& entry1.getLineNumber() == entry2.getLineNumber() && entry1.getIndex().equals(entry2.getIndex())
				&& entry1.getDescription().equals(entry2.getDescription());
	}

	private Tuple2<Boolean, Boolean> compareEntries(ResultEntry entry1, ResultEntry entry2) {

		if (entry1.getMutatedClass().equals(entry2.getMutatedClass())
				&& entry1.getMutatedMethod().equals(entry2.getMutatedMethod())
				&& entry1.getMutator().equals(entry2.getMutator())
				&& entry1.getSourceFile().equals(entry2.getSourceFile())
				&& entry1.getLineNumber() == entry2.getLineNumber() && entry1.getIndex().equals(entry2.getIndex())
				&& entry1.getDescription().equals(entry2.getDescription())
				&& entry1.getDetectionStatus().equals(entry2.getDetectionStatus())) {

			return new Tuple2<Boolean, Boolean>(true,
					checkKillingTests(entry1.getKillingTests(), entry2.getKillingTests()));
		}

		return new Tuple2<Boolean, Boolean>(false, false);
	}

	private boolean checkKillingTests(List<String> previousKillingTests, List<String> afterKillingTests) {
		// Verifica se os tamanhos das listas são iguais
		if (previousKillingTests.size() != afterKillingTests.size()) {
			return false;
		}

		// Cria cópias das listas para não modificar as originais
		List<String> previousCopy = new ArrayList<>(previousKillingTests);
		List<String> afterCopy = new ArrayList<>(afterKillingTests);

		// Ordena as cópias das listas
		Collections.sort(previousCopy);
		Collections.sort(afterCopy);

		// Verifica se as listas ordenadas são iguais
		return previousCopy.equals(afterCopy);
	}

	public static void saveResultEntries(List<ResultEntry> resultEntries, String filePath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			for (ResultEntry entry : resultEntries) {
				String line = formatResultEntry(entry);
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String formatResultEntry(ResultEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("Index: ").append(entry.getIndex()).append("\n");
		sb.append("Description: ").append(entry.getDescription()).append("\n");
		sb.append("Detection Status: ").append(entry.getDetectionStatus()).append("\n");
		sb.append("Killing Test: ").append(entry.getKillingTest()).append("\n");
		sb.append("Killing Tests: ").append(entry.getKillingTests()).append("\n");
		sb.append("Line Number: ").append(entry.getLineNumber()).append("\n");
		sb.append("Mutated Class: ").append(entry.getMutatedClass()).append("\n");
		sb.append("Mutated Method: ").append(entry.getMutatedMethod()).append("\n");
		sb.append("Mutator: ").append(entry.getMutator()).append("\n");
		sb.append("Source File: ").append(entry.getSourceFile()).append("\n");
		sb.append("Is Detected: ").append(entry.isDetected()).append("\n");
		sb.append("\n");
		return sb.toString();
	}

}

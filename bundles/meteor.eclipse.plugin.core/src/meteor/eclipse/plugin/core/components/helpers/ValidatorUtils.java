package meteor.eclipse.plugin.core.components.helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.pitest.util.Log;

import meteor.eclipse.plugin.core.components.mutation.tests.ResultEntry;

public class ValidatorUtils {


	public class ValidationResult {
	    private int lineOfCode;
	    private String description;
	    private String previousDetectionStatus;
	    private String afterDetectionStatus;
	    private boolean changedBehaviour;

	    public ValidationResult(int lineOfCode, String description, String previousDetectionStatus, String afterDetectionStatus, boolean changedBehaviour) {
	        this.lineOfCode = lineOfCode;
	        this.description = description;
	        this.previousDetectionStatus = previousDetectionStatus;
	        this.afterDetectionStatus = afterDetectionStatus;
	        this.changedBehaviour = changedBehaviour;
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

	    public boolean isChangedBehaviour() {
	        return changedBehaviour;
	    }

	    public void setChangedBehaviour(boolean changedBehaviour) {
	        this.changedBehaviour = changedBehaviour;
	    }

	    @Override
	    public String toString() {
	        return "ValidationResult{" +
	                "lineOfCode=" + lineOfCode +
	                ", description='" + description + '\'' +
	                ", previousDetectionStatus='" + previousDetectionStatus + '\'' +
	                ", afterDetectionStatus='" + afterDetectionStatus + '\'' +
	                ", changedBehaviour=" + changedBehaviour +
	                '}';
	    }
	}

	public List<ValidationResult> validateMutations(List<ResultEntry> baselineResults,
			List<ResultEntry> lastRunResults) {

		Log.getLogger().info("________________BASELINE______________");
		baselineResults.forEach(i -> {
			Log.getLogger().info(i.toString());
		});

		Log.getLogger().info("________________LASTRUNRESULTS______________");
		lastRunResults.forEach(i -> {
			Log.getLogger().info(i.toString());
		});

		// saveResultEntries(baselineResults, "C:\\Projects\\TMP\\brr.txt");
		// saveResultEntries(lastRunResults, "C:\\Projects\\TMP\\llr.txt");

		return compareResults(baselineResults, lastRunResults);

	}
	
	public void generatePdfReport(List<ValidationResult> validationResults, String pdfFilePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(new PDType1Font(FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(20, 750);
                contentStream.showText("Mutation Test Results Analysis");
                contentStream.endText();

                contentStream.setFont(new PDType1Font(FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(20, 730);

                for (ValidationResult result : validationResults) {
                    contentStream.showText(result.toString());
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            document.save(pdfFilePath);
            System.out.println("PDF report generated successfully: " + pdfFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	/*
	 * public static String getMutantIdentification(ResultEntry entry) { return
	 * entry.getMutatedClass() + "::" + entry.getMutatedMethod() + "::" +
	 * entry.getMutator(); }
	 */

	public List<ValidationResult> compareResults(List<ResultEntry> baselineResults, List<ResultEntry> lastRunResults) {
		List<ValidationResult> validationResults = new ArrayList<>();

		// Iterate over the baselineResults list
		for (ResultEntry baselineEntry : baselineResults) {

			ValidationResult validationResult = null;
			ResultEntry matchingEntry = findMatchingEntry(baselineEntry, lastRunResults);

			if (matchingEntry != null) {
				validationResult = new ValidationResult(baselineEntry.getLineNumber(), baselineEntry.getDescription(),
						baselineEntry.getDetectionStatus(), matchingEntry.getDetectionStatus(),
						!compareEntries(baselineEntry, matchingEntry));

			} else {
				validationResult = new ValidationResult(baselineEntry.getLineNumber(), baselineEntry.getDescription(),
						baselineEntry.getDetectionStatus(), "NOT PRESENT IN LR", false);
			}

			if (validationResult.isChangedBehaviour()) {
				System.out.println("Teste");
			}

			validationResults.add(validationResult);

		}

		// Iterate over the lastRunResults list
		for (ResultEntry lastRunResultsEntry : lastRunResults) {

			ValidationResult validationResult = null;
			ResultEntry matchingEntry = findMatchingEntry(lastRunResultsEntry, lastRunResults);

			if (matchingEntry != null) {
				validationResult = new ValidationResult(lastRunResultsEntry.getLineNumber(),
						lastRunResultsEntry.getDescription(), lastRunResultsEntry.getDetectionStatus(),
						matchingEntry.getDetectionStatus(), !compareEntries(lastRunResultsEntry, matchingEntry));

			} else {
				validationResult = new ValidationResult(lastRunResultsEntry.getLineNumber(),
						lastRunResultsEntry.getDescription(), lastRunResultsEntry.getDetectionStatus(),
						"NOT PRESENT IN BLs", false);
			}

			if (validationResult.isChangedBehaviour()) {
				System.out.println("Teste");
			}

			validationResults.add(validationResult);

		}

		return validationResults;
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

		// Compare the necessary attributes to determine a match
		// You can modify this logic based on the specific attributes that should match

		return entry1.getDescription().equals(entry2.getDescription()) &&
				entry1.getIndex().equals(entry2.getIndex())
				&& entry1.getLineNumber() == entry2.getLineNumber();
		/*
		 * return entry1.getMutatedClass().equals(entry2.getMutatedClass()) &&
		 * entry1.getMutatedMethod().equals(entry2.getMutatedMethod()) &&
		 * entry1.getMutator().equals(entry2.getMutator()) && entry1.isDetected() ==
		 * entry2.isDetected() &&
		 * entry1.getKillingTest().equals(entry2.getKillingTest()) &&
		 * entry1.getSourceFile().equals(entry2.getSourceFile()) &&
		 * entry1.getLineNumber() == entry2.getLineNumber() && entry1.getIndex() ==
		 * entry2.getIndex() && entry1.getDescription().equals(entry2.getDescription());
		 */

	}

	private boolean compareEntries(ResultEntry entry1, ResultEntry entry2) {

		return entry1.getLineNumber() == entry2.getLineNumber()
				&& entry1.getDescription().equals(entry2.getDescription())
				&& entry1.getDetectionStatus().equals(entry2.getDetectionStatus());

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

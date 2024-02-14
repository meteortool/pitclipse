package meteor.eclipse.plugin.core.components.helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

		return compareResults(baselineResults, lastRunResults);

	}
	
	/*public void generatePdfReport(List<ValidationResult> validationResults, String pdfFilePath) {
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
	            contentStream.setLeading(14.5f); // Define o espaçamento entre linhas
	            contentStream.beginText();
	            contentStream.newLineAtOffset(20, 730);

	            for (ValidationResult result : validationResults) {
	                contentStream.showText(result.toString());
	                contentStream.newLine();
	            }

	            contentStream.endText();	    
	        } 
	        
	        document.save(pdfFilePath);
            Log.getLogger().info("PDF report generated successfully: " + pdfFilePath);

	    } catch (IOException e) {
	    	Log.getLogger().severe(e.getMessage());
	    	Log.getLogger().severe(e.getStackTrace().toString());
	    	throw new RuntimeException(e);
	    }
	}*/
	
   public void generateCSV(List<ValidationResult> validationResults, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Line of Code,Description,Previous Detection Status,After Detection Status,Changed Behaviour\n");
            for (ValidationResult result : validationResults) {
                writer.append(result.getLineOfCode() + ",");
                writer.append("\"" + result.getDescription() + "\",");
                writer.append(result.getPreviousDetectionStatus() + ",");
                writer.append(result.getAfterDetectionStatus() + ",");
                writer.append(result.isChangedBehaviour() + "\n");
            }
            
            Log.getLogger().info("PDF report generated successfully: " + filePath);
        } catch (IOException e) {
        	Log.getLogger().severe(e.getMessage());
	        Log.getLogger().severe(e.getStackTrace().toString());
	        throw new RuntimeException(e);
        }
    }
   
   
	/*public void generatePdfReport(List<ValidationResult> validationResults, String pdfFilePath) {
	    try (PDDocument document = new PDDocument()) {
	        PDPage page = new PDPage();
	        document.addPage(page);

	        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
	            contentStream.setFont(new PDType1Font(FontName.HELVETICA), 12);
	            contentStream.beginText();
	            contentStream.newLineAtOffset(20, 750);
	            contentStream.showText("Mutation Test Results Analysis");
	            contentStream.endText();

	            float margin = 50;
	            float yStart = 700;
	            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
	            int rows = validationResults.size();
	            float rowHeight = 20;
	            float tableHeight = rowHeight * rows;

	            drawTable(document, contentStream, page, yStart, tableWidth, margin, yStart, rowHeight, tableHeight, validationResults);
	        }

	        document.save(pdfFilePath);
	        Log.getLogger().info("PDF report generated successfully: " + pdfFilePath);

	    } catch (IOException e) {
	        Log.getLogger().severe(e.getMessage());
	        Log.getLogger().severe(e.getStackTrace().toString());
	        throw new RuntimeException(e);
	    }
	}

	private void drawTable(PDDocument document, PDPageContentStream contentStream, PDPage page, float yStart, float tableWidth, float margin, float yPosition, float rowHeight, float tableHeight, List<ValidationResult> validationResults) throws IOException {
	    float fontSize = 10;
	    PDType1Font font = new PDType1Font(FontName.HELVETICA);

	    float xStart = margin;
	    float columnWidth = (tableWidth - 2 * margin) / 5; // Dividindo em 5 colunas

	    drawRow(contentStream, yPosition, xStart, tableWidth, rowHeight, fontSize, font, "Line of Code", "Description", "Previous Detection Status", "After Detection Status", "Changed Behaviour");
	    yPosition -= rowHeight;

	    for (ValidationResult result : validationResults) {
	        float cellHeight = calculateCellHeight(result.getDescription(), fontSize, font, columnWidth);
	        if (yPosition - cellHeight < 50) { // Verifica se há espaço suficiente na página atual
	            contentStream.close();
	            document.addPage(page);
	            contentStream = new PDPageContentStream(document, page);
	            drawRow(contentStream, 750, xStart, tableWidth, rowHeight, fontSize, font, "Line of Code", "Description", "Previous Detection Status", "After Detection Status", "Changed Behaviour");
	            yPosition = 700 - rowHeight; // Reinicia a posição para a próxima página
	        }
	        drawRow(contentStream, yPosition, xStart, tableWidth, cellHeight, fontSize, font, String.valueOf(result.getLineOfCode()), result.getDescription(), result.getPreviousDetectionStatus(), result.getAfterDetectionStatus(), String.valueOf(result.isChangedBehaviour()));
	        yPosition -= cellHeight;
	    }

	    contentStream.close();
	}

	private void drawRow(PDPageContentStream contentStream, float y, float xStart, float tableWidth, float rowHeight, float fontSize, PDType1Font font, String... content) throws IOException {
	    float x = xStart;
	    boolean isHeader = (y == 700); // Verifica se é a linha do cabeçalho
	    boolean isRed = false;
	    if (!isHeader) {
	        isRed = content[4].equals("false"); // Verifica se "Changed Behaviour" é false
	    }
	    float cellHeight = calculateCellHeight(content[1], fontSize, font, tableWidth / 5); // Calcula a altura da célula "Description"
	    for (String text : content) {
	        drawCell(contentStream, x, y, tableWidth / 5, cellHeight, fontSize, font, text, isHeader, isRed);
	        x += tableWidth / 5;
	    }
	}
	
	private void drawCell(PDPageContentStream contentStream, float x, float y, float width, float height, float fontSize, PDType1Font font, String text, boolean bold, boolean red) throws IOException {
	    contentStream.setFont(font, fontSize);
	    if (bold) {
	        contentStream.setFont(new PDType1Font(FontName.HELVETICA), fontSize); // Fonte em negrito para o cabeçalho
	    }
	    if (red) {
	        contentStream.setNonStrokingColor(Color.RED); // Texto em vermelho se "Changed Behaviour" for false
	    }
	    contentStream.beginText();
	    contentStream.newLineAtOffset(x, y - height);
	    contentStream.showText(text);
	    contentStream.endText();
	    contentStream.setNonStrokingColor(Color.BLACK); // Restaura a cor padrão
	    contentStream.addRect(x, y - height, width, height);
	    contentStream.stroke();
	}

	private float calculateCellHeight(String text, float fontSize, PDType1Font font, float cellWidth) throws IOException {
	    float textWidth = font.getStringWidth(text) / 1000 * fontSize;
	    float cellHeight = (textWidth / cellWidth) * fontSize; // Calcula a altura com base no tamanho do texto e largura da célula
	    return cellHeight;
	}*/

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

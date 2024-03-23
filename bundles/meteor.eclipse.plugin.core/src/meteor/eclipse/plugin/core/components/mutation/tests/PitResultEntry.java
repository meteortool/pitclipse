package meteor.eclipse.plugin.core.components.mutation.tests;

import java.math.BigInteger;
import java.util.List;

public class PitResultEntry implements ResultEntry {

	private String mutatedClass;
	private String mutatedMethod;
	private String mutator;
	private String sourceFile;
	private boolean isDetected;
	private int lineNumber;
	private String detectionStatus;
	private BigInteger index;
	private String description;
	private String killingTest;
	private List<String> killingTests;
	
	public String getKillingTest() {
		return killingTest;
	}
	
	public void setKillingTest(String killingTest) {
		this.killingTest = killingTest;
	}
	
	public String getMutatedClass() {
		return mutatedClass;
	}
	
	public void setMutatedClass(String mutatedClass) {
		this.mutatedClass = mutatedClass;
	}
	
	public String getMutatedMethod() {
		return mutatedMethod;
	}
	
	public void setMutatedMethod(String mutatedMethod) {
		this.mutatedMethod = mutatedMethod;
	}
	
	public String getMutator() {
		return mutator;
	}
	
	public void setMutator(String mutator) {
		this.mutator = mutator;
	}
	
	public String getSourceFile() {
		return sourceFile;
	}
	
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public boolean isDetected() {
		return isDetected;
	}
	
	public void setDetected(boolean isDetected) {
		this.isDetected = isDetected;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public String getDetectionStatus() {
		return detectionStatus;
	}
	
	public void setDetectionStatus(String detectionStatus) {
		this.detectionStatus = detectionStatus;
	}
	
	public BigInteger getIndex() {
		return index;
	}
	
	public void setIndex(BigInteger index) {
		this.index = index;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getKillingTests() {
		return killingTests;
	}

	public void setKillingTests(List<String> killingTests) {
		this.killingTests = killingTests;
	}
	
	
	@Override
	public String toString() {
		return "PitResultEntry [mutatedClass=" + mutatedClass + ", mutatedMethod=" + mutatedMethod + ", mutator="
				+ mutator + ", sourceFile=" + sourceFile + ", isDetected=" + isDetected + ", lineNumber=" + lineNumber
				+ ", detectionStatus=" + detectionStatus + ", index=" + index + ", description=" + description
				+ ", killingTest=" + killingTest + ", killingTests=" + killingTests.toString() + "]";
	}
}

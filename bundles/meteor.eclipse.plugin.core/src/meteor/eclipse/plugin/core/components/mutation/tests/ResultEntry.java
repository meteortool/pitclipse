package meteor.eclipse.plugin.core.components.mutation.tests;

import java.math.BigInteger;
import java.util.List;

public interface ResultEntry {

	String getDetectionStatus();

	String getKillingTest();
	
	List<String> getKillingTests();

	int getLineNumber();
	
	BigInteger getIndex();
	
	String getDescription();

	String getMutatedClass();

	String getMutatedMethod();

	String getMutator();

	String getSourceFile();

	boolean isDetected();

	void setDetected(boolean isDetected);

	void setDetectionStatus(String detectionStatus);

	void setKillingTest(String killingTest);

	void setLineNumber(int lineNumber);
	
	void setIndex(BigInteger index);
	
	void setDescription(String description);

	void setMutatedClass(String mutatedClass);

	void setMutatedMethod(String mutatedMethod);

	void setMutator(String mutator);

	void setSourceFile(String sourceFile);
	
	void setKillingTests(List<String> killingTests);

}
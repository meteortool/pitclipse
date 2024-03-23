package meteor.eclipse.plugin.core.components.mutation.tests;

import java.util.ArrayList;
import java.util.List;

import org.pitest.pitclipse.runner.PitResults;

public class PitResultConverter {

	public static List<ResultEntry> getResults(PitResults results) {

		if (results == null)
			throw new RuntimeException("Results argument of getResults function is null");

		List<ResultEntry> resultsConverted = new ArrayList<ResultEntry>();

		results.getMutations().getMutation().forEach(r -> {
			
			PitResultEntry pitResultEntry = new PitResultEntry();
			pitResultEntry.setKillingTests(r.getKillingTests());
			pitResultEntry.setDetected(r.isDetected());
			pitResultEntry.setIndex(r.getIndex());
			pitResultEntry.setDescription(r.getDescription());
			pitResultEntry.setKillingTest(r.getKillingTest() == null ? "" : r.getKillingTest());
			pitResultEntry.setLineNumber(r.getLineNumber().intValue());
			pitResultEntry.setMutatedClass(r.getMutatedClass() == null ? "" : r.getMutatedClass());
			pitResultEntry.setMutatedMethod(r.getMutatedMethod() == null ? "" : r.getMutatedMethod());
			pitResultEntry.setMutator(r.getMutator() == null ? "" : r.getMutator());
			pitResultEntry.setSourceFile(r.getSourceFile() == null ? "" : r.getSourceFile());
			pitResultEntry.setDetectionStatus(r.getStatus() == null ? "" : r.getStatus().toString());
			
			resultsConverted.add(pitResultEntry);
		});

		return resultsConverted;

	}

}

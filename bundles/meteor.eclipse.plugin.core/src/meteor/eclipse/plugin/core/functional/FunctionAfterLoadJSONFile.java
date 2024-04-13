package meteor.eclipse.plugin.core.functional;

public interface FunctionAfterLoadJSONFile {
	
	void execute(Integer refactoringSession, Integer baselineMutationCoverage, Double baselineMutationScore, Integer lastResultMutationCoverage, Double lastResultMutationScore, String result);

}

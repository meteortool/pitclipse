package meteor.eclipse.plugin.core.functional;

public interface FunctionAfterLoadJSONFile {
	
	void execute(Integer refactoringSession, Double baselineMutationScore, Double lastResultMutationScore, String result);

}

package meteor.eclipse.plugin.core.components.mutation.tests;

import java.util.List;

import org.pitest.pitclipse.runner.PitResults;

import meteor.eclipse.plugin.core.components.PluginFacade;
import meteor.eclipse.plugin.core.threading.ResultListenerNotifier;

public interface TestMutationAgent {
	
	void run(PluginFacade pluginFacade, ResultListenerNotifier listener, int refactoringSession) throws Exception;
	void generateBaseline();
	void clearBaseline();
	void setLastResults(PitResults results);
	
	List<ResultEntry> getBaselineResults();
	List<ResultEntry> getLastResults();

}

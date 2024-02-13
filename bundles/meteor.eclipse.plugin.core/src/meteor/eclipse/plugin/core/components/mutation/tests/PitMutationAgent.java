package meteor.eclipse.plugin.core.components.mutation.tests;

import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.error;

import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.pitest.pitclipse.launch.ui.PitLaunchShortcut;
import org.pitest.pitclipse.runner.PitResults;

import meteor.eclipse.plugin.core.components.PluginFacade;
import meteor.eclipse.plugin.core.threading.ResultListenerNotifier;

public class PitMutationAgent implements TestMutationAgent {

	private PitResults results = null, baselineResults = null;

	@Override
	public void run(PluginFacade pluginFacade, ResultListenerNotifier listener, int refactoringSession)
			throws Exception {

		/*if (pluginFacade.IsLocked() || !pluginFacade.IsLocked())
			error("Meteor is running a mutation testing. Wait for finish.");
		else {*/
			ISelection selectedResource = pluginFacade.getSelectedResource();

			if (selectedResource == null) {
				error("You must select a valid test package or a test class before running your mutation testing for this refactoring session!");
			} else {
				pluginFacade.lock();
				new PitLaunchShortcut().launch(selectedResource, "run");
			}
		//}

	}
	
	public void clearBaseline() {
		baselineResults = null;
	}

	@Override
	public void generateBaseline() {
		if (results == null)
			throw new RuntimeException(
					"You must run at least one time the mutation testing to be able to fix a baseline.");
		baselineResults = results;
		results = null;
	}

	@Override
	public List<ResultEntry> getBaselineResults() {
		return PitResultConverter.getResults(baselineResults);
	}

	@Override
	public List<ResultEntry> getLastResults() {
		return PitResultConverter.getResults(results);
	}
	
	public void setLastResults(PitResults results) {
		this.results = results;
	}

}

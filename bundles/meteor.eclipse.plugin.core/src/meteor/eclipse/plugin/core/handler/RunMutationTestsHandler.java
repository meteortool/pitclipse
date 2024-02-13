package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class RunMutationTestsHandler extends PluginHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			pluginFacade().runMutationTests();
		} catch (Exception e) {
			throw new ExecutionException("Error on execution of command to run mutation testing", e);
		}
		return null;
	}

}

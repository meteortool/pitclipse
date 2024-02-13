package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ResetResultsHandler extends PluginHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			pluginFacade().reset();
		} catch (Exception e) {
			throw new ExecutionException("Error on execution of command to reset results", e);
		}
		return null;
	}

}

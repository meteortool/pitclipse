package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class GenerateBaselineHandler extends PluginHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			pluginFacade().generateBaseline();
		} catch (Exception e) {
			throw new ExecutionException("Error on execution of command generate baseline.", e);
		}
		return null;
	}

}

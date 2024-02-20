package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ImportDataHandler extends PluginHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			pluginFacade().importData();
		} catch (Exception e) {
			throw new ExecutionException("Error on execution of command generate baseline.", e);
		}
		return null;
	}

}

package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ViewMainPanelHandler extends PluginHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			pluginFacade().viewMainPanel();
		} catch (Exception e) {
			throw new ExecutionException("Error on execution of command view Panel", e);
		}
		return null;
	}

}

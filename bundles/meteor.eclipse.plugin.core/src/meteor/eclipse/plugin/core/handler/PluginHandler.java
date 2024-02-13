package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.AbstractHandler;

import meteor.eclipse.plugin.core.components.PluginFacade;
import meteor.eclipse.plugin.core.components.PluginFacadeImpl;
import meteor.eclipse.plugin.core.components.mutation.tests.PitMutationAgent;

public abstract class PluginHandler extends AbstractHandler {
	
	static {
		pluginFacade = new PluginFacadeImpl(new PitMutationAgent());
	}
	
	private static PluginFacade pluginFacade;
	
	
	public PluginFacade pluginFacade() {
		return pluginFacade;		
	}

}

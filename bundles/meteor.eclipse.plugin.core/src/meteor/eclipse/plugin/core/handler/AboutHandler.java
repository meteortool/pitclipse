package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import meteor.eclipse.plugin.core.window.AboutWindow;

public class AboutHandler extends PluginHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
        AboutWindow aboutWindow = new AboutWindow(HandlerUtil.getActiveShell(arg0));
        aboutWindow.setBlockOnOpen(true);
        aboutWindow.open();
		return null;
	}

}

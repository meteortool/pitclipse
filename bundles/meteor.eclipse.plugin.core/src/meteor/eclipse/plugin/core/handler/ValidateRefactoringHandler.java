package meteor.eclipse.plugin.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ValidateRefactoringHandler extends PluginHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			pluginFacade().validateRefactoring();
		} catch (Exception e) {
			throw new ExecutionException("Error on execution of command for validate refactoring.", e);
		}
		return null;
	}

}

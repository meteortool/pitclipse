package meteor.eclipse.plugin.core.handler;

import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.error;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class SetMutationEntrypointHandler extends PluginHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ISelection selectedResource = (ISelection) HandlerUtil.getCurrentSelection(event);
			IStructuredSelection structuredSelection = (IStructuredSelection) selectedResource;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				IProject project = ((IAdaptable) firstElement).getAdapter(IProject.class);
				pluginFacade().setSelectedProject(project);
			}
			pluginFacade().setSelectedResource((ISelection) HandlerUtil.getCurrentSelection(event));
		} catch (Exception e) {
			error("Error on setting the entry mutation point. Please select a valid test package or test class!");
			e.printStackTrace();
		}
		return null;
	}

}

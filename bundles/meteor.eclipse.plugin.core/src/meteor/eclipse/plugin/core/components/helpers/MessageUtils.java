package meteor.eclipse.plugin.core.components.helpers;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class MessageUtils {
	
	public static void error(String message) {
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Meteor Error", message);
	}
	
	public static boolean ask(String message) {
		return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Meteor Question", message);
	}
	
	public static void info(String message) {
		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Meteor Question", message);
	}

}

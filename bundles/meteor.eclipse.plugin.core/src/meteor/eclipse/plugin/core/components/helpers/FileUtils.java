package meteor.eclipse.plugin.core.components.helpers;

import static meteor.eclipse.plugin.core.components.helpers.MessageUtils.error;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.pitest.util.Log;

public class FileUtils {
	
	public static void openFile(String filePath) {
	    try {
	        File file = new File(filePath);
	        if (file.exists()) {
	            Desktop.getDesktop().open(file);
	        } else {
	            error("The PDF não foi encontrado.");
	        }
	    } catch (IOException e) {
	    	Log.getLogger().severe(e.getMessage());
	    	Log.getLogger().severe(e.getStackTrace().toString());
	    	throw new RuntimeException(e);
	    }
	}
	
	public static void openFileInEclipse(String filePath) {		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    try {
	        IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(filePath));
	        IDE.openEditorOnFileStore(page, fileStore);
	    } catch (PartInitException e) {
	        e.printStackTrace();
	    }
				
	}
	
}

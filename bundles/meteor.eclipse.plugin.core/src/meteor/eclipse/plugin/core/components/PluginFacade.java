package meteor.eclipse.plugin.core.components;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;

public interface PluginFacade {
	
	void viewMainPanel() throws Exception;

	void validateRefactoring() throws Exception;

	void runMutationTests() throws Exception;

	void generateBaseline() throws Exception;
	
	void generateCsvAnalysisReport() throws Exception;
	
	void createSessionRefactoring() throws Exception;

	void lock();
	
	void unlock();
	
	void reset(Boolean skipConfirmation) throws Exception;
	
	void setSelectedResource(ISelection resource) throws Exception;
	
	void setSelectedProject(IProject project) throws Exception;
	
	void exportData() throws Exception;
	
	void importData() throws Exception;
	
	ISelection getSelectedResource();
	
	IProject getSelectedProject();
	
	boolean IsLocked();

}
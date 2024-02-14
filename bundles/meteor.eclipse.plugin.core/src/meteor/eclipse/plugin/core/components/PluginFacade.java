package meteor.eclipse.plugin.core.components;

import org.eclipse.jface.viewers.ISelection;

public interface PluginFacade {
	
	void viewMainPanel() throws Exception;

	void validateRefactoring() throws Exception;

	void runMutationTests() throws Exception;

	void generateBaseline() throws Exception;
	
	void generatePdfAnalysisReport() throws Exception;
	
	void createSessionRefactoring() throws Exception;

	void lock();
	
	void unlock();
	
	void reset() throws Exception;
	
	void setSelectedResource(ISelection resource) throws Exception;
	
	ISelection getSelectedResource();
	
	boolean IsLocked();

}
package meteor.eclipse.plugin.core.components.helpers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;

public class EclipseUtils {

	public static boolean hasCompilationErrors(IProject project) throws CoreException {
		
		IMarker[] markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);
		for (int a = 0; a < markers.length; a++) {
			IMarker marker = markers[a];
			Object severity = marker.getAttribute(IMarker.SEVERITY);
			if (((Integer) severity).intValue() == IMarker.SEVERITY_ERROR) {
				return true;
			}
		}
		
		return false;
	}

}
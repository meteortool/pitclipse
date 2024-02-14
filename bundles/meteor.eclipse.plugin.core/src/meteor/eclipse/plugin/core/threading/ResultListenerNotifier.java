package meteor.eclipse.plugin.core.threading;

import org.pitest.pitclipse.runner.PitResults;

public interface ResultListenerNotifier {
	
	void notifyOnComplete(PitResults results);
	void notifyOnClose();

}

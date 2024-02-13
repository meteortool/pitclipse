package meteor.eclipse.plugin.core.pitest.handlers;

import org.pitest.pitclipse.core.extension.point.ResultNotifier;
import org.pitest.pitclipse.runner.model.MutationsModel;

public class MeteorNativePITMutationsResultsListener implements ResultNotifier<MutationsModel>  {
	
    @Override
    public void handleResults(MutationsModel mutations) {
        // process detected mutations...
    }

}

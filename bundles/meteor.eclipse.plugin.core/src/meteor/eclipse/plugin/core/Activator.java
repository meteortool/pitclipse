package meteor.eclipse.plugin.core;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.Event;
import org.pitest.pitclipse.runner.PitResults;

import meteor.eclipse.plugin.core.threading.ResultListenerNotifier;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "meteor.eclipse.plugin.core"; //$NON-NLS-1$

	// Topic to handle
	private final String TOPIC = "onresults";

	// The shared instance
	private static Activator plugin;

	// Results handler
	private static ResultsHandler resultsHandler;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {		
		super.start(context);
		
		plugin = this;
		resultsHandler = new ResultsHandler();
		
		configureHandler(context);
	}
	
	private void configureHandler(BundleContext context) {
		
		// Get the EventAdmin service reference
		ServiceReference<EventAdmin> serviceRef = context.getServiceReference(EventAdmin.class);

		// Define the event properties
		String[] topics = new String[] { TOPIC };

		// Register the event handler with the Event Admin service
		context.registerService(EventHandler.class.getName(), (EventHandler) resultsHandler,
				createEventHandlerProperties(topics));

		// Release the EventAdmin service reference
		context.ungetService(serviceRef);
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public void addNotifier(ResultListenerNotifier notifier) {
		if (Activator.resultsHandler != null)
			Activator.resultsHandler.addNotifier(notifier);
	}

	public class ResultsHandler implements EventHandler {

		private List<ResultListenerNotifier> notifiers;

		private ResultsHandler() {
			notifiers = new ArrayList<>();
		}

		public void addNotifier(ResultListenerNotifier notifier) {
			if (!this.notifiers.contains(notifier))
				this.notifiers.add(notifier);
		}

		@Override
		public void handleEvent(Event event) {
			// Handle the event here
			// You can access the event properties using event.getProperty("propertyName")
			System.out.println("Received event: " + event.getTopic());
			if (event.getTopic().equals(TOPIC)) {
				this.notifiers.forEach(n -> n.notifyOnComplete((PitResults) event.getProperty("results")));
			}
		}
	}

	private Dictionary<String, Object> createEventHandlerProperties(String[] topics) {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, new String[] { TOPIC });
		return properties;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

}
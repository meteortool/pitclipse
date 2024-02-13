package meteor.eclipse.plugin.core;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		try {
			System.out.println("Tentando ativar o plugin");
			//Activator.getDefault().start(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}

/*******************************************************************************
 * Copyright 2012-2019 Phil Glover and contributors
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package org.pitest.pitclipse.launch.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class PitLaunchUiActivator extends AbstractUIPlugin {
    
    public static String PLUGIN_ID = "org.pitest.pitclipse.launch.ui";
    
	public static final String METEOR_PLUGIN_ID = "meteor.eclipse.plugin.core"; //$NON-NLS-1$
    
    private static PitLaunchUiActivator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        startMeteor();
    }
    
    public static PitLaunchUiActivator getInstance() {
        return plugin;
    }
    
    private void startMeteor() {
    	Bundle bundle = Platform.getBundle(METEOR_PLUGIN_ID);
        if (bundle != null) {
            try {
                bundle.start(Bundle.START_TRANSIENT);
            } catch (BundleException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Plugin com ID " + METEOR_PLUGIN_ID + " não encontrado.");
        }
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow workBenchWindow = getActiveWorkbenchWindow();
        if (workBenchWindow == null) {
            return null;
        }
        return workBenchWindow.getShell();
    }

    /**
     * Returns the active workbench window
     * 
     * @return the active workbench window
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        if (plugin == null) {
            return null;
        }
        IWorkbench workBench = PlatformUI.getWorkbench();
        if (workBench == null) {
            return null;
        }
        return workBench.getActiveWorkbenchWindow();
    }

}

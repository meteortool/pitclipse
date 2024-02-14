package meteor.eclipse.plugin.core.window;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import meteor.eclipse.plugin.core.Activator;

public class AboutWindow extends ApplicationWindow {

    public AboutWindow(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createContents(Composite parent) {
        getShell().setText("About Meteor Plugin");
        parent.setLayout(new FillLayout(SWT.APPLICATION_MODAL));

        Image logo = createImage("icons/meteorlogo.png");
        if (logo != null) {
        	getShell().setImage(logo);
        }

        Text text = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        text.setText("Meteor is a tool resulting from a master's degree research work for the applied computing course at IPT (São Paulo Technological Research Institute - https://ipt.br/ensino).\n"
        		+ "It works as a plugin for the Eclipse IDE designed to support developers in validating the success of a test code refactoring. To install Meteor, see the instructions below.\n"
        		+ "To make the project possible, a fork of the Pitclipse project was made to make the necessary changes for the tool's functionality."
        		+ "\n\n"
        		+ "Version: 1.0"
        		+ "\nAuthor: Tiago Samuel Rodrigues Teixeira" 
        		+ "\nWebsite: https://github.com/meteortool");

        return parent;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(900, 300);
        centerShell(newShell);
    }

    private void centerShell(Shell shell) {
        Shell parent = getParentShell();
        if (parent != null) {
            int x = parent.getLocation().x + (parent.getSize().x - shell.getSize().x) / 2;
            int y = parent.getLocation().y + (parent.getSize().y - shell.getSize().y) / 2;
            shell.setLocation(x, y);
        }
    }

    private Image createImage(String path) {
        ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
        ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(Activator.getDefault().getBundle().getEntry(path));
        imageRegistry.put(path, imageDescriptor);
        return imageRegistry.get(path);
    }
}
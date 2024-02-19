package meteor.eclipse.plugin.core.views;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author mengqp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HyperlinkCellEditor extends CellEditor {
    private Hyperlink link;
    //private Composite parent;
    public HyperlinkCellEditor(Composite parent){
        super(parent,0);
        //link.setFocus();
    }
    
    public HyperlinkCellEditor(Composite parent, int style){
        super(parent, style);
        //link.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#createControl(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControl(Composite parent) {
        // TODO Auto-generated method stub
        link = new Hyperlink(parent, SWT.NONE);
        link.setForeground(new Color(null,0,0,255));
        link.setFocus();
        
        link.addHyperlinkListener(new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent event)
            {
                System.out.println("click hyper link = "+link.getText());
                
            }
            public void linkEntered(HyperlinkEvent event)
            {
                
            }
            public void linkExited(HyperlinkEvent event)
            {
                
            }
        });
        return link;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
     */
    protected Object doGetValue() {
        // TODO Auto-generated method stub
        
        return link;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
     */
    protected void doSetFocus() {
        // TODO Auto-generated method stub
        //link.setFocus();
        System.out.println("dddddfsdgdfg'");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
     */
    protected void doSetValue(Object value) {
        // TODO Auto-generated method stub
        link.setText(((Hyperlink)value).getText());
        
        //link = (Hyperlink)value; // is wrong
    }
    
    
}
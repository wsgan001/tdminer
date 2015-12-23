/*
 * HelpPanel.java
 *
 * Created on April 22, 2006, 6:51 PM
 */

package edu.iisc.tdminer.help;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 *
 * @author  Deb
 */
public class HelpPanel extends javax.swing.JPanel
{
    
    /** Creates new form HelpPanel */
    public HelpPanel()
    {
	initComponents();
	jEditorPane.setContentType("text/html");
	try
	{
	    jEditorPane.setPage(getClass().getResource("/docs/index.html"));
	}
	catch (IOException ex)
	{
	    ex.printStackTrace();
	}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jEditorPane.setEditable(false);
        jEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener()
        {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt)
            {
                jEditorPaneHyperlinkUpdate(evt);
            }
        });

        jScrollPane1.setViewportView(jEditorPane);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("Status:");
        add(jLabel1, java.awt.BorderLayout.SOUTH);

    }// </editor-fold>//GEN-END:initComponents
    
    private void jEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt)//GEN-FIRST:event_jEditorPaneHyperlinkUpdate
    {//GEN-HEADEREND:event_jEditorPaneHyperlinkUpdate
// TODO add your handling code here:
	if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	{
	    JEditorPane pane = (JEditorPane) evt.getSource();
	    if (evt instanceof HTMLFrameHyperlinkEvent)
	    {
		HTMLFrameHyperlinkEvent  evtFrame = (HTMLFrameHyperlinkEvent)evt;
		HTMLDocument doc = (HTMLDocument)pane.getDocument();
		doc.processHTMLFrameHyperlinkEvent(evtFrame);
	    }
	    else
	    {
		try
		{
		    pane.setPage(evt.getURL());
		}
		catch (Throwable t)
		{
		    t.printStackTrace();
		}
	    }
	}
    }//GEN-LAST:event_jEditorPaneHyperlinkUpdate
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}
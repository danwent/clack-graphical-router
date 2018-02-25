/*
 * Created on Nov 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.propertyview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.clackrouter.component.extension.Classifier;



/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ClassifierPopup extends DefaultPropertiesView {


    private DefaultListModel mPatternModel = null;
    private JList mPatternList = null; 

    public ClassifierPopup(Classifier classy) {
    	super(classy.getName(), classy);
    	
        mPatternModel = new DefaultListModel();
        mPatternList  = new JList(mPatternModel);

        JPanel main = addMainPanel("Classifier");
        main.add(createPatternListPanel());
        addHTMLDescription("classifier.html");
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        fillPatternList();
        
        updatePropertiesFrame();
    } // -- init
    
    
    protected JPanel createPatternListPanel()
    {
        JPanel patternPanel = new JPanel(new BorderLayout());
        addBorderToPanel(patternPanel, "Patterns");
        mPatternList.setPreferredSize(new Dimension(150, 70));
        mPatternList.setVisibleRowCount(8);
        mPatternList.setAlignmentX(LEFT_ALIGNMENT);
        JScrollPane scrollableList = new JScrollPane(mPatternList);
        patternPanel.add(scrollableList, BorderLayout.CENTER);

        JPanel addPanel = new JPanel();
        patternPanel.add(addPanel, BorderLayout.SOUTH);
        JTextField addField = new JTextField("", 15);
        addPanel.add(addField);
        JButton addButton = new JButton("add");
        addButton.addActionListener(new PatternAdder(addField));
        addPanel.add(addButton);

        mPatternList.addKeyListener(new ListKeyListener());
        
        return patternPanel;
    } // -- createPatternListPanel



    private void fillPatternList()
    {
        Classifier model = (Classifier)m_model;
        mPatternModel.clear();

        Object[] patterns = model.getPatterns();
        for ( int i = 0 ; i < patterns.length ; ++i ) {
            if (patterns[i] != null ) {
              mPatternModel.addElement((Classifier.PatternMatcher)patterns[i]);
            }
        }

    } // -- fillPatternList

   
    private class PatternAdder implements ActionListener
    {
        private JTextField mTextField = null;

        public PatternAdder(JTextField field) {
            mTextField = field;
        }

        public void actionPerformed(ActionEvent event)
        {
            Classifier model = (Classifier)m_model;
            model.addPattern(mTextField.getText());
            mTextField.setText("");
            fillPatternList();
            repaint();
        }
    }
    
    private class ListKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent evt) 
        {

            // Check for key codes.
            if (evt.getKeyCode() == KeyEvent.VK_DELETE) 
            { 
                int index  =
                    mPatternList.getSelectedIndex();

                if ( index == -1 ) { 
                    System.out.println("No entry selected");
                    return;
                }
                Classifier classy = (Classifier)m_model;
                classy.delPattern(index);

                fillPatternList();
                repaint();
                
            } // -- VK_DELETE
        
        } // -- ListPressed
    } // -- private class ListKeyListener
}

package net.clackrouter.tutorial;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.clackrouter.gui.ClackFramework;

public class ClackAssignment extends JFrame {

	//JPanel mMainPanel;
	public static int DEFAULT_HEIGHT = 300;
	public static int DEFAULT_WIDTH = 300;
	
	public ClackAssignment(File f){
		super("Clack Assignment");
		setIconImage(ClackFramework.applicationIcon.getImage());
	//	mMainPanel = new JPanel();

		loadFromXMLFile(f);
		
	//	JScrollPane editorScrollPane = new JScrollPane(mMainPanel);
	//	editorScrollPane.setVerticalScrollBarPolicy(
	//	                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
	//	editorScrollPane.setMinimumSize(new Dimension(10, 10));

	//	getContentPane().add(editorScrollPane);
		setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		show();		
	}
	
	private void loadFromXMLFile(File f){
		try {
			InputStream is = new FileInputStream(f);
		
			DocumentBuilderFactory dbf =
    		DocumentBuilderFactory.newInstance();

			dbf.setValidating(false);
    
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc  = db.parse(is);
			Element assignment_elem = (Element)doc.getDocumentElement();
			
		//	mMainPanel.setPreferredSize(new Dimension(280,400));
		//	mMainPanel.setLayout(new BoxLayout(mMainPanel, BoxLayout.PAGE_AXIS));
			
			JTabbedPane tabbed_pane = new JTabbedPane();
			
			getContentPane().add(tabbed_pane);
			//mMainPanel.add(tabbed_pane);
			
			
			NodeList list = assignment_elem.getElementsByTagName("problem");
			for(int i = 0; i < list.getLength(); i++){
				Element prob = (Element)list.item(i);
				String type = prob.getAttribute("type");
				String question = prob.getAttribute("question");
				String answer = prob.getAttribute("answer");
				
		        JPanel mini_panel     = new JPanel();
		    //    mini_panel.setLayout(new BoxLayout(mini_panel, BoxLayout.PAGE_AXIS));
		     
		  //      mini_panel.setPreferredSize(new Dimension(350,400));
				
				JLabel question_label = new JLabel(question);
		        mini_panel.add(question_label);
		        
				if(type.equals("mc")){
					String options_str = prob.getAttribute("options");
					String[] all_options = options_str.split(":");

					JComboBox option_list = new JComboBox(all_options);
					try {
						int index = Integer.parseInt(answer);
						option_list.setSelectedIndex(index);
					}catch (Exception e) { /*ignore*/ }
					mini_panel.add(option_list);
				}else if(type.equals("long")){
				
					JTextField answer_area = new JTextField(30);
					answer_area.setText(answer);
	
					JScrollPane scroll = new JScrollPane(answer_area);
				//	scroll.setPreferredSize(new Dimension(280, 65));
					mini_panel.add(scroll);
				}
				tabbed_pane.addTab("Problem #" + i, mini_panel);
			//	mMainPanel.add(mini_panel);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
}

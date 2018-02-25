package net.clackrouter.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.topology.core.TopologyModel;

public class ConnectivityTestWindow extends JFrame implements WindowListener, ActionListener {

	public static int DEFAULT_HEIGHT = 300;
	public static int DEFAULT_WIDTH = 400;
	
	ConnectivityTestThread my_test;
	JButton stop, restart;
	JTextArea log;
	TopologyModel model;
	
	public ConnectivityTestWindow(TopologyModel m){
		super("Connectivity Test");
		setIconImage(ClackFramework.applicationIcon.getImage());

		model = m;
        log = new JTextArea();
        log.setEditable(false);
        JScrollPane scrollpane = new JScrollPane(log);
        scrollpane.setPreferredSize(new Dimension(DEFAULT_WIDTH - 30, DEFAULT_HEIGHT - 50));

       
		Container pane = getContentPane();
     //   main.setLayout(new BorderLayout()); 
		pane.setLayout(new BorderLayout());
        pane.add(scrollpane, BorderLayout.NORTH);
        
        JPanel button_panel = new JPanel();
		restart = new JButton("Restart Test");
		restart.addActionListener(this);
		stop = new JButton("Stop Test");
		stop.addActionListener(this);
		button_panel.add(restart);
		button_panel.add(stop);
		button_panel.setPreferredSize(new Dimension(DEFAULT_WIDTH - 20, 30));
		pane.add(button_panel, BorderLayout.SOUTH);
		addWindowListener(this);

        pack();
        setVisible(true);
		
		my_test = new ConnectivityTestThread(model.getHosts(), log);
		my_test.start();
	}
	
    public void windowClosed(WindowEvent e) {
    	System.err.println("killing connectivity test on window closing");
    	my_test.killTest();
    }
    
    public void windowDeactivated(WindowEvent e){
    	
    }
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == stop){
			my_test.killTest();
		}else if(e.getSource() == restart){
			my_test.killTest();
			try{
				Thread.sleep(200);
			}catch (Exception ex) {}
			log.setText("");
			my_test = new ConnectivityTestThread(model.getHosts(), log);
			my_test.start();
		}
	}

	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosing(WindowEvent arg0) {
		
		
	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}

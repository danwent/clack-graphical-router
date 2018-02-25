
package net.clackrouter.application;

/* 
 * 
 * TclShell: a simple Tcl shell for Jacl
*
* Copyright (c) 1998 The Regents of the University of California.
*       All Rights Reserved.  See the COPYRIGHT file for details.
* 
* Heavily modified by Dan Wendlandt to be used in Clack.
* 
* Major changes include asynchronous command processing, removal of anything Tcl,
* and router specific commands, as well as a generic application running capability
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.ImageLoader;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.router.core.Router;

/**
 *
 * Simple Command Line Interface for a Clack Router.  
 * 
 * <p> Provides simple commands to access clack applications
 * and inspect router state.  Currently, history is not functional. </p>
 */

public class ClackShell extends JFrame implements Runnable {

 // The command input
 private StringBuffer commandBuffer = new StringBuffer();
 private ArrayList keyevent_buffer = new ArrayList();
 private boolean run_shell = true;  // set false on window close
 
 private boolean app_is_running, kill_app;
 
 // The TextArea widget for displaying commands and results
 public TextArea textArea;

 // Prompts
 public String prompt;

 // Cursors
 private int promptCursor = 0;
 private int commandCursor = 0;

 // History
 public int historyLength = 10;
 private int historyCursor = 0;
 private Vector historyCommands = new Vector();
 public static String PROMPT_END = "#";
 
 public static int CONSOLE_WIDTH = 85;
 
 private Router mRouter;
 private ApplicationManager appManager;
 
 public ClackShell (Router r, ClackFramework framework) {
 	super("");
 	changeHost(r);
 	
 	appManager = framework.getApplicationManager();
	String iconName = Translator.getString("Icon");
	ImageIcon applicationIcon = ImageLoader.getImageIcon(iconName);
	setIconImage(applicationIcon.getImage());
 }
 
 
 private void changeHost(Router newRouter){
	 mRouter = newRouter;
	 setTitle("Clack Shell: " + mRouter.getHost());
	 prompt = mRouter.getHost() + " " + PROMPT_END + " ";
 }
 
 public void run() {
 	
 	  // Graphics part
    JPanel panel = new JPanel(new BorderLayout());
    this.setSize(new Dimension(300, 400));
    textArea = new TextArea("", 10, CONSOLE_WIDTH, TextArea.SCROLLBARS_BOTH);
    textArea.setBackground(Color.WHITE);

    textArea.addMouseListener(new ShellMouseListener());
    panel.add("Center", textArea);

    // Event handling part
    textArea.addKeyListener(new ShellKeyListener());
    this.getContentPane().add(panel);
    setSize(500, 400);

    show();
    putText("Welcome to host '" + mRouter.getHost() + "'.\n");
    putText("Type \"?\" for help.\n");
    putText("Use Up/Down arrows for cmd history, Control-C kills current app\n");
    putText(prompt);
    while(run_shell){
    	while(keyevent_buffer.size() > 0){
    		KeyEvent keyEvent = (KeyEvent) keyevent_buffer.remove(0);
    		switch (keyEvent.getKeyCode()) {
    			case KeyEvent.VK_ENTER:
    				boolean isDone = evalCommand();
    				if(isDone) {
    					this.hide();
    					return;
    				}
    				break;

    			// NOTE: we currently have history disabled, 
    			// so these cases never happen
    			case KeyEvent.VK_UP:
    				previousCommand();
    				break;
    			case KeyEvent.VK_DOWN:
    				nextCommand();
    				break;
    		}
        } // end inner while
    	try {
    		Thread.sleep(100);
    	} catch (Exception e) { /* ignore */ }
    }

 }

 // Override this to initialize the prompt. We need to do
 // because we can't write to the TextArea until the peer
 // has been created
 public void addNotify () {
   super.addNotify();
 //  putText(prompt);
 }

 // Print output to the console, update the prompt cursor
 public void putText (String s) {
	 String next_line = null;

	 // stupid hack to keep all text on visual console
	 if(s.length() > CONSOLE_WIDTH){
		 String tmp = s;
		 int nl_index = tmp.indexOf("\n");
		 if(nl_index > CONSOLE_WIDTH){
			 s = tmp.substring(0,CONSOLE_WIDTH) + "\n";
			 next_line = tmp.substring(CONSOLE_WIDTH );
		 }else {
			 s = tmp.substring(0, nl_index + 1);
			 next_line = tmp.substring(nl_index + 1);
		 }

	 }
	 
 	textArea.append(s);
 	promptCursor += s.length();
 	textArea.setCaretPosition(promptCursor);
 	
 	if(next_line != null)
 		putText(next_line);

 }

 // Evaluate the command so far, if possible, printing
 // a continuation prompt if not.
 protected boolean evalCommand () {
   String newtext = textArea.getText().substring(promptCursor);
   
   if(newtext.length() == 0){
	   putText("\n" + prompt);
	   return false;
   }
   
   promptCursor += newtext.length();
   if (commandBuffer.length() > 0) {
     commandBuffer.append("\n");
   }
   putText("\n");
   commandBuffer.append(newtext);
   String command = commandBuffer.toString();
   String[] command_strings = command.split(" ");
   if(command_strings.length > 0){
   	Cursor oldCursor = textArea.getCursor();
   	textArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    
    //echo command
	if(command_strings[0].equals("?") || command_strings[0].equals("help")){
		printHelp();
	}else if(command_strings[0].equals("exit") || command_strings[0].equals("quit")){
		return true;	
	}else if(command_strings[0].indexOf("!") == 0){
		String hostname = command_strings[0].substring(1);
		Router newRouter = mRouter.getDocument().getFramework().getTopologyManager().getRouterByName(hostname);
		if(newRouter == null){
			putText("Can't find router named '" + hostname + "'");
		}else {
			putText("Switching to new Host = '" + hostname + "'");
			changeHost(newRouter);
		}
	}else {
		
		try {
			ClackApplication app = appManager.runApplication(mRouter, command_strings, this);

			app_is_running = true;
			while(app_is_running){
				if(kill_app){
					app.stop();
					app_is_running = false;
					kill_app = false; // clear flag
					putText("Application Recived Kill Signal");
				}
				Thread.sleep(1000);
			}
			//app.join();
		}catch (Exception e) {
			putText("Error running application: " + e.getMessage()); 
		}
		
	}
	
    commandBuffer.setLength(0);
    commandCursor = promptCursor;
    textArea.setCursor(oldCursor);
    updateHistory(command);
   }
   putText("\n" + prompt);
   return false;
 }
 
 public void killApp() { kill_app = true; }
 public void appIsFinished() { app_is_running = false; }
 
 private void printHelp(){
	putText("\nThe following Shell Commands are available:\n\n");
	putText("? \t: print all available shell commands and applications\n");
	putText("!hostname\t: switch to the command-shell on another host\n");

	Properties all_apps = appManager.getMap();
	for(Enumeration e = all_apps.keys(); e.hasMoreElements();) {
		try {
			String cmd = (String)e.nextElement();	
			String class_name = all_apps.getProperty(cmd);
			Class class_obj = Class.forName(class_name);
			Constructor constructor = class_obj.getConstructor(new Class[] { });
			ClackApplication app = (ClackApplication) constructor.newInstance(new Object[] { });
			putText(cmd + "\t: " + app.getDescription() + "\n");
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
 }
 


 // Update the command history
 void updateHistory (String command) {
     historyCursor = 0;
     if (historyCommands.size() == historyLength) {
	historyCommands.removeElementAt(0);
     }
     historyCommands.addElement(command);
 }

 // Replace the command with an entry from the history
 void nextCommand () {
   String text;
   if (historyCursor == 0) {
     text = "";
   } else {
     historyCursor --;
     text = (String)historyCommands.elementAt(
          historyCommands.size() - historyCursor - 1);
   }
   commandCursor = textArea.getText().lastIndexOf(PROMPT_END) + 2;
   String contents = textArea.getText().substring(0, commandCursor);
   textArea.setText(contents);

   putText(text);
   promptCursor = commandCursor; // must be after putText();
 }

 void previousCommand () {
   String text;
   if (historyCursor == historyCommands.size()) {
     return;
   } else {
     historyCursor ++;
     text = (String)historyCommands.elementAt(
           historyCommands.size() - historyCursor);
   }

   commandCursor = textArea.getText().lastIndexOf(PROMPT_END) + 2;
   String contents = textArea.getText().substring(0, commandCursor);
   textArea.setText(contents);

   putText(text);
   promptCursor = commandCursor; // needs to be after putText()
 }
 
 // this is an attempt to not let the user use the mouse
 // to move within the shell, as it messes things up
 class ShellMouseListener extends MouseAdapter {
	 
	 public void mouseClicked(MouseEvent e) {	 e.consume();  }
	 public void mouseEntered(MouseEvent e){	 e.consume(); }
	 public void mouseExited(MouseEvent e){	 e.consume(); } 
	 public void mouseReleased(MouseEvent e){ e.consume(); } 
	 public void mousePressed(MouseEvent e){	 e.consume(); }
 }

 // The key listener
 class ShellKeyListener extends KeyAdapter {
   public void keyPressed (KeyEvent keyEvent) {
     // Process keys
     switch (keyEvent.getKeyCode()) {
     	case KeyEvent.VK_ENTER:
     		keyEvent.consume();
     		keyevent_buffer.add(keyEvent);
     		break;
     	case KeyEvent.VK_BACK_SPACE:
     		if (textArea.getCaretPosition() == promptCursor) {
     			keyEvent.consume(); // don't backspace over prompt!
     		}
     		break;
     	case KeyEvent.VK_LEFT:
     		if (textArea.getCaretPosition() == promptCursor) {
     			keyEvent.consume();
     		}
     		break;
     	case KeyEvent.VK_UP:
     		keyevent_buffer.add(keyEvent);
     		keyEvent.consume();
     		break;
       	case KeyEvent.VK_DOWN:
  			keyevent_buffer.add(keyEvent);
     		keyEvent.consume();
     		break;
       	case KeyEvent.VK_CONTROL:  // no C required right now :P
       		killApp();
       		keyEvent.consume();
       		break;
   		
     	default:
     				// Otherwise we got a regular character. Don't consume it,
     				// and TextArea will take care of displaying it.
     	}
   	}
 }
 

}
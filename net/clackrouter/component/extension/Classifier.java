/*
 *  File: Classifier.java
 *  Date: Wed Jul 21 17:57:50 PDT 2004 
 *  Author: Martin Casado
 *
 */

package net.clackrouter.component.extension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.ClassifierPopup;
import net.clackrouter.router.core.Router;

/**
 * <p>Classifies packets and sends them out distict ports if they match user-specified
 * byte-string/offset pairs called patterns.</p>
 * 
 * <p> This is a very powerful but very general component that can be used to demultiplex packets
 * based on different header information.  The number of output ports is dynamic, depending on the
 * total number of possible pattern matches. </p>
 *   
 */

public class Classifier extends ClackComponent implements Cloneable, Serializable 
{
    public static int sFROM_IFACE = 0; 

    protected ArrayList mPatternMatchers;

    
    public Classifier(Router router, String name) {
    	super(router, name);
        mPatternMatchers = new ArrayList(); 
    	setupPorts(1);
    	addPattern("-"); 
    }
    

    protected void setupPorts(int numports)
    {
        super.setupPorts(numports);
        String sFROM_IFACE_desc = "input for packets to be declassified";
        m_ports[sFROM_IFACE] = new ClackPort(this, sFROM_IFACE, sFROM_IFACE_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; }
    
    // replaces m_ports with a new array containing one more additional port
    protected void addNewPortToArray(String descr){

    	ClackPort[] newArr = new ClackPort[m_ports.length + 1];
    	System.arraycopy(m_ports, 0, newArr, 0, m_ports.length);
    	newArr[m_ports.length] = new ClackPort(this, m_ports.length, descr, 
    				ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
    	System.out.println("Creating new Classifier port # " + m_ports.length + " for " + descr);
    	m_ports = newArr;
    	m_num_ports = m_ports.length;
    }
    
    protected void removePortFromArray(int portNum) {
    	ClackPort[] newArr = new ClackPort[m_ports.length - 1];
    	int newArrIndex = 0;
    	for(int i = 0; i < m_ports.length; i++){
    		if(i != portNum){
    			newArr[newArrIndex] = m_ports[i];
    			newArrIndex++;
    		}
    	}
    	m_ports = newArr; 	
    	m_num_ports = m_ports.length;
    }

    public synchronized boolean delPattern(int index)
    {
    	index++; // we need to add one b/c of the inital incoming port is actually port 0
    	if(index >= m_ports.length || index < 0)
    		return false;
    	removePortFromArray(index);
        mPatternMatchers.remove(index - 1); // TODO: fix all this
        return true;
    }

    public boolean addPattern(String pattern) 
    {
        PatternMatcher tmpMatcher =  null;

        if (pattern.trim().equals("")){
            return false;
        }

	    try {
	    	tmpMatcher = new PatternMatcher(pattern);
	    }catch(Exception e){
	         System.err.println(e);
	         return false;
	    }
	    addNewPortToArray(" outgoing packets which match pattern: " + pattern);
	    mPatternMatchers.add(tmpMatcher);

	
	    return true;
   
    } // -- addPattern

    public void acceptPacket(VNSPacket packet, int port_number) 
    {
        if ( port_number == sFROM_IFACE){
            int output_port;
            for ( int i = 0; i < mPatternMatchers.size(); ++i){
            	PatternMatcher matcher = (PatternMatcher) mPatternMatchers.get(i);
                
                if (matcher.matches(packet)) {
                	System.out.println("Classifier found match at " + (i + 1) + " for pattern " + matcher.mPatternString);
                    m_ports[i + 1].pushOut(packet);
                    return;
                }
            }
        }
    } // -- acceptPacket


    public PatternMatcher getPattern(int index){
        return (PatternMatcher)mPatternMatchers.get(index);
    }

    public Object[] getPatterns(){
        return mPatternMatchers.toArray();
    }
    
    public JPanel getPropertiesView() {
    	return new ClassifierPopup(this);
    }
    
    public Properties getSerializableProperties(boolean isTransient){
    	Properties p = super.getSerializableProperties(isTransient);
    	for(int i = 0; i < mPatternMatchers.size(); i++){
    		PatternMatcher pm = (PatternMatcher) mPatternMatchers.get(i);
    		p.setProperty("pattern"+i, pm.mPatternString);
    	}
    	return p;
    }
    
    public void initializeProperties(Properties props) {
    	int i = 0;
    	while(true){
    		String pattern = props.getProperty("pattern"+i);
    		if(pattern == null) break;
    		addPattern(pattern);
    		i++;
    	}
    }

    public class PatternMatcher 
	{
	    protected String mPatternString  = null;

	    ArrayList mMatchers = null;

	    public PatternMatcher(String pattern) throws Exception
	    {
	        mPatternString = pattern;


	        mMatchers = new ArrayList();

	        // --
	        // Special case, '-' matches everything
	        // --
	        if (pattern.trim().equals("-")){
	            return;
	        }

	        StringTokenizer tok = new StringTokenizer(pattern, " ");
	        while ( tok.hasMoreElements()){
	            mMatchers.add(createQM(tok.nextToken()));
	        }

	    } // -- PatternMatcher

	    public QuickMatch createQM(String pattern) throws Exception
	    {
	        if (pattern.indexOf("/") == -1 ){
	            throw new Exception("Invalid pattern format!");
	        }
	        StringTokenizer tok = new StringTokenizer(pattern, "/");

	        String prefix  = tok.nextToken().trim();
	        boolean negate = false;
	        if (prefix.startsWith("!") ){
	            prefix = prefix.substring(1);
	            negate = true;
	        }

	        String suffix = tok.nextToken().trim();
	        int mask  = 0xffffffff;

	        if (suffix.indexOf('%') != -1 ) {
	            mask = Integer.parseInt(suffix.substring(
	                        suffix.indexOf('%') + 1), 16);
	            suffix = suffix.substring(0, suffix.indexOf('%'));
	        }

	        int   index = Integer.parseInt(prefix);
	        short value = Short.parseShort(suffix, 16);

	        QuickMatch qm = new QuickMatch(index, value, mask);
	        qm.setNegate(negate);

	        return qm; 
	    } // -- createQM

	    public boolean matches(VNSPacket in){
	        for (int i = 0; i < mMatchers.size() ; ++i){
	            if (! ((QuickMatch)mMatchers.get(i)).match(in)){
	                return false;
	            }
	        }
	        return true;
	    } // -- matches


	    public String toString(){
	        return mPatternString;
	    }



	} // -- 
    
    protected class QuickMatch implements Serializable
    {
        int   mOffset;
        short mValue;
        int   mMask;
        boolean mNegate = false;

        public QuickMatch(int offset, short value, int mask)
        {
            mOffset = offset;
            mValue  = value;
            mMask   = mask;
        }

        public void setNegate(boolean val){
            mNegate = val;
        }

        public boolean match(VNSPacket packet){
            // System.out.println("Match: " + 
            //         packet.getByteBuffer().getShort(mOffset) + ":" + mValue);
            // System.out.println("Match/Mask: " + 
            //         ((short)packet.getByteBuffer().getShort(mOffset) &
            //          (short)mMask) + ":" +
            //         ((short)mValue & (short)mMask));
            // System.out.println("mask" + mMask); 

            return 
                (mNegate ^
                ((mMask & packet.getByteBuffer().getShort(mOffset)) == 
                 (mMask & mValue)));
        }
    }
} // --  

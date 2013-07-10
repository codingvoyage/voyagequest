package scripting;

import java.util.HashMap;

/**
 *
 * @author Edmund
 */
public class ScriptableClass implements Scriptable {
    
    protected Thread mainThread;
    
    //Basically, a catch all variable usable by the current method 
    private Parameter progressTemp;
    
    //Holds the variables declared by the Scriptable in the scripting engine
    private HashMap<String, Parameter> memoryBox;
    
    /** Script ID for Entity */
    private String scriptID;
    
    protected boolean isMarkedForDeletion = false;
    
    public void markForDeletion() {
        isMarkedForDeletion = true;
    }
    
    public boolean isMarkedForDeletion()
    {
        return isMarkedForDeletion;
    }
    
    public ScriptableClass(String newScriptID)
    {
        mainThread.setScriptID(newScriptID);
        mainThread.setLineNumber(0);
        mainThread.setRunningState(false);
        
        memoryBox = new HashMap<String, Parameter>();
    }
    
    public ScriptableClass() 
    {
        memoryBox = new HashMap<String, Parameter>();
    }
    
    public void setMainThread(Thread t)
    {
        mainThread = t;
    }
    
    public Thread getMainThread()
    {
        return mainThread;
    }
    
    /**
     * Set the main script ID
     * @param scriptID entity script ID
     */
    public void setMainScriptID(String scriptID) {
        this.scriptID = scriptID;
    }
    
    /**
     * Get the main script ID
     * @return the main script ID
     */
    public String getMainScriptID() {
        return scriptID;
    }
    
}

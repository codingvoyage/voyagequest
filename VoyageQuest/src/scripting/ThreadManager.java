package scripting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import voyagequest.GameState;
import voyagequest.Global;
import voyagequest.VoyageQuest;

/**
 *
 * @author Edmund
 */
public class ThreadManager implements Serializable {

    private static final long serialVersionUID = 100L;

    ArrayList<Thread> threadCollection;
    
    ScriptReader scriptReader;
    
    public ThreadManager(ScriptReader scriptReaderInstance) 
    {
        threadCollection = new ArrayList<Thread>();
        scriptReader = scriptReaderInstance;
    }
    
    public void addThread(Thread newThread)
    {
        threadCollection.add(newThread);
    }
    
    public Thread getThreadAtName(String targetThread)
    {
        for (Thread t: threadCollection)
        {
            String threadName = t.getName();
            if (threadName != null && threadName.equals(targetThread))
            {
                return t;
            }
        }
        
        //couldn't find...
        return null;
    }
    
    public boolean isThreadAlive(String targetThread)
    {
        if (getThreadAtName(targetThread) == null)
            return false;
        else
            return true;
    }
    
    public void clear()
    {
        threadCollection.clear();
    }
    
    public void markForDeletion(String targetThread)
    {
        Thread markedThread = getThreadAtName(targetThread);
        if (markedThread != null)
        {
            System.out.println("Marking the thread for deletion!");
            //Mark for deletion.
            markedThread.markForDeletion();
        }
        else
        {
            System.out.println(targetThread + " does not exist; hence it could " +
                    "not be marked for deletion!");
        }
    }

    //Unfortunately a clunky while-based loop is necessary to avoid problems we'd otherwise
    //encounter is we used a ListIterator.
    public void act(double delta)
    {
        boolean partOfBattle = VoyageQuest.state == GameState.COMBAT;

        boolean continueStepping = !threadCollection.isEmpty();
        int index = 0;
        Thread currentThread;
        while (continueStepping)
        {
            //Get current thread...
            currentThread = threadCollection.get(index);
            //Should any threads be deleted right now?
            if (currentThread.isMarkedForDeletion())
            {
                System.out.println("Removing " + currentThread.getName() + "!");
                threadCollection.remove(index);
                //index is unchanged, since everything shifts back by one
            }
            else
            //Otherwise, just act on it.
            {
                if (!Global.isFrozen)
                {
                    scriptReader.act(currentThread, delta);

                    //See if we left a battle, then abort execution of threads
                    //immediately.
                    if (partOfBattle && (VoyageQuest.state != GameState.COMBAT))
                        return;
                }
                else
                {
                    //Else if frozen, run only the one that's permitted to run
                    if (currentThread.equals(Global.unfrozenThread))
                        scriptReader.act(currentThread, delta);
                }

                index++;
            }

            //Stop when we've reached the last thread.
            if (index >= getThreadCount()) {
                continueStepping = false;
            }
        }
    }

    public ArrayList<Thread> getCollection() {
        return threadCollection;
    }

    public int getThreadCount()
    {
        return threadCollection.size();
    }
}

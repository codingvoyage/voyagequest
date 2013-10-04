package scripting;

import battle.*;
import map.Entity;
import map.Map;
import voyagequest.GameState;
import voyagequest.Global;
import voyagequest.VoyageQuest;

import java.util.ArrayList;
import java.util.HashMap;

import static voyagequest.GameState.*;


/**
 *
 * @author Edmund
 */

public class ScriptReader
{
    private ScriptManager scr;
    private ThreadManager threadManager;
    
    //These will get changed every time act(Scriptable s, double deltaTime)
    //is called. It makes it convenient since now the ScriptReader methods
    //do not have to be passed this every time.
    private Scriptable currentScriptable;
    private Thread currentThread;
    private double currentDeltaTime;
    
    private boolean justJumped;
    
    public ScriptReader(ScriptManager scriptManagerHandle) 
    {
        //Initialize ScriptManager! rather, change the constructor such that
        //it can receive the object reference to ScriptManager, which probably
        //has been initialized sometime during the initialization stage of the
        //program
        scr = scriptManagerHandle;
    }
    
    public void setThreadHandle(ThreadManager threadManagerHandle)
    {
        threadManager = threadManagerHandle;
    }
    
    public void act(Thread t, double deltaTime)
    {
        currentDeltaTime = deltaTime;
        currentThread = t;
        
        //Get Scriptable Object
        currentScriptable = currentThread.getScriptable();
        
        
        //We need to know what line of what script the Thread is on
        int currentLineNumber = currentThread.getCurrentLine();
        String currentScriptID = currentThread.getScriptID();
        
        //Now we call ScriptManager to have it return a reference to the
        //Script class which currentScriptID refers to
        Script currentScript = scr.getScriptAtID(currentScriptID);
        
        //Now, we get the current line
        Line currentLine = currentScript.getLine(currentLineNumber);
        
        //Now, our response depends on whether there is a command already in 
        //execution
        if (currentThread.isRunning()) 
        {
            //Will we continue running the same command?
            boolean doesContinue = continuesRunning(currentLine);
            
            //This only gets called when we JUST finished the current command
            if (!doesContinue) 
            {
                //So since we're not staying on the same command, we now move on
                //to the next line!
                currentThread.setLineNumber(
                        currentThread.getCurrentLine() + 1);
            }
        }
        
        //Now, if by now there is no command already in execution, then...
        if (!currentThread.isRunning()) 
        {
            
            //We will go from line to line
            boolean doWeContinue = true;
            while (doWeContinue) 
            {
                justJumped = false;
                        
                //First, get the current Line of the current Script
                //This is kind of a mouthful, so first it gets the ScriptID
                //from the class implementing Scriptable, and then grabs from
                //the ScriptManager, using that scriptID, the Script object
                //itself, and then finally, from that Script object the program
                //retrieves the line which is located at the line number
                //specified in the class implementing Scriptable.
                Line thisLine = scr.getScriptAtID(currentThread.getScriptID()).
                        getLine(currentThread.getCurrentLine());
                
                //Now that we have the line, we execute it.
                doWeContinue = executeCommand(thisLine);
                
                
               // if  (!currentThread.functionStack.isEmpty())
               //     System.out.println(currentThread.getCurrentLine() + " is the current line"
               //             + "of thread " + currentThread.getName());
                    
                //Now, if we aren't halting, then after the line is over, 
                //move on to the next line! UNLESS it was a goto statement!
                //Of course, if it is a goto statement, it will remain a
                //"Yes we continue!" but without switching lines
                if (doWeContinue && (justJumped == false)) 
                {
                    //System.out.println("Next line!");
                    currentThread.setLineNumber(
                        currentThread.getCurrentLine() + 1);
                
                
                }
                else 
                {
                    //Well, we DID stop then, and so we DON'T want to move on
                    //to the next line...
                }
                
            } //End of the while loop which basically continues to execute the
            //next command until it reaches a command which takes time to do
          
        } //This is the end of the if-statement that executes commands. It will
        //only get called if there isn't a command in progress already for
        //the given script
        
    } //This is the end of the entire act(Scriptable s, double deltaTime) method
    
    
    //continuesRunning basically asks currentScriptable whether it has completed
    //its task. If it still has not, then continuesRunning returns a TRUE, as in
    //"yes, since the task isn't complete, DO CONTINUE TO RUN". If the class
    //instead indicates that it has completed its task, then continuesRunning
    //will return false.
    private boolean continuesRunning(Line currentLine)
    {
        boolean result = true;
        
        switch (currentLine.getCommandID())
        {
            case 0: //Waiting
                result = currentThread.continueWait(currentDeltaTime);
                break;
            case 103:
            case 104:
                result = ((Entity)currentScriptable).continueMove(currentDeltaTime, currentThread);
                break;
                
            //Dialogbox continue-speaks
            case 150:
            case 151:
                result = currentThread.continueSpeak();
                break;

            //For the prompt boxes
            case 152:
            case 153:
                result = currentThread.continuePrompt();
                break;
                
            //fade in
            case 161:
                //Relies on Global.camera.fade
                if (Global.camera.fade < 255)
                {
                    Global.camera.fade += 8;
                    result = true;
                }
                else
                {
                    //Fade is already less than 0, we're done
                    currentThread.setRunningState(false);
                    result = false;
                }
                
                
                
                break;
                
            //fade out
            case 162:
                //Relies on Global.camera.fade
                if (Global.camera.fade > 50)
                {
                    Global.camera.fade -= 4;
                    result = true;
                }
                else
                {
                    //Fade is already less than 0, we're done
                    currentThread.setRunningState(false);
                    result = false;
                }
                
                break;


        }
        
        return result;
    }
    
    //Now, there are two types of commands - ones which occupy time
    //or ones which are executed immediately. When executeCommand goes
    //through and discovers what the command does, it will return a boolean
    //which indicates whether to continue to the next line. TRUE means 
    //"YES, we do continue" and FALSE means "FALSE, this is the last command!" 
    public boolean executeCommand(Line currentLine)
    {
        //By default it's true, unless the command is the type that makes it
        //false
        boolean continueExecuting = true;
        
        switch (currentLine.getCommandID())
        {
            /***********************************************************
                                BASIC COMMAND FLOW
             ***********************************************************/

            case 0: //wait
                double waitDuration = identifierCheck(currentLine, 0).getDoubleValue();
                currentThread.beginWait(waitDuration);
                continueExecuting = false;
                break;
            case 1: //GOTO, huh?
                Script currentScript = scr.getScriptAtID(currentThread.getScriptID());
                String theLabel = currentLine.getStringParameter(0);
                int newLineIndex = currentScript.getLabelIndexOnLineList(theLabel);
                currentThread.setLineNumber(newLineIndex);
                justJumped = true;
                break;
            case 2:
                //System.out.println(currentThread.getCurrentLine() + " is the current line "
                //            + "of the current thread we're on... which is " + currentThread.getName());
                //We'll leave this out for now...
                break;

            //The return statement. Returns the thread
            //to its previous layer.
            case 4:
                returnFromFunction(currentLine);
                break;

            //if statement
            case 5:
                //Evaluate line
                Parameter result = evaluateExpression(currentLine,
                        0, currentLine.getParameterCount() - 1);
                
                //if it evaluates to true, then program logic continues
                //that's why we check if it's false, and then if it's false,
                //we look for where we skip to.
                if (result.getBooleanValue() == false)
                {
                    //findEndLimiter starts on the indexed line, so that's why we 
                    //compensate by adding 1.
                    int newLine = findEndLimiter(scr.getScriptAtID(currentThread.getScriptID()),
                            "if", "endif", currentThread.getCurrentLine() + 1, 1);
                    
                    currentThread.setLineNumber(newLine + 1);
                    justJumped = true;
                }
                break;
                
            case 6:
                //Evaluate line
                Parameter whileResult = evaluateExpression(currentLine,
                        0, currentLine.getParameterCount() - 1);
                
                //if it evaluates to true, then program logic continues
                //that's why we check if it's false, and then if it's false,
                //we look for where we skip to.
                if (whileResult.getBooleanValue() == false)
                {
                    //findEndLimiter starts on the indexed line, so that's why we 
                    //compensate by adding 1.
                    int newLine = findEndLimiter(scr.getScriptAtID(currentThread.getScriptID()),
                            "while", "wend", currentThread.getCurrentLine() + 1, 1);
                    
                    currentThread.setLineNumber(newLine + 1);
                    justJumped = true;
                }
                break;
                
            case 7:
                //wend
                int newLine = findEndLimiter(scr.getScriptAtID(currentThread.getScriptID()),
                            "wend", "while", currentThread.getCurrentLine() - 1, -1);
                currentThread.setLineNumber(newLine);
                justJumped = true;
                
                break;
                
            case 8:
                //for ___ = ___ and BLAHBLAH
                
                //First, do the declaration and initialization
                String varName = currentLine.getStringParameter(0);
                
                int equalsLoc = findCorrespondingBracket(currentLine, "=", 0, 1);
                int andLoc = findCorrespondingBracket(currentLine, "and", 0, 1);
                
                if (andLoc - 2 == equalsLoc)
                {
                    //Then it's a simple initialization at index 2
                    Parameter varValue = identifierCheck(currentLine, 2);
                    currentThread.setVariable(varName, varValue);
                }
                else
                {
                    //Alright, Goddamn. The scripter decided to actually have
                    //an expression in the for loop initialization. Seriously?
                    Parameter varValue = evaluateExpression(currentLine, equalsLoc + 1, andLoc - 1);
                    currentThread.setVariable(varName, varValue);
                    
                }
                
                //Next, check if we have to run the for loop at all.
                Parameter checkResult = evaluateExpression(currentLine, andLoc + 1, currentLine.getParameterCount() - 1);
                if (checkResult.getBooleanValue() == false)
                {
                    //We skip past the next...
                    int skippingNext = findEndLimiter(scr.getScriptAtID(currentThread.getScriptID()),
                            "for", "next", currentThread.getCurrentLine() + 1, 1);
                    
                    currentThread.setLineNumber(skippingNext + 1);
                    
                    justJumped = true;
                }
                //Otherwise, we're really just fine. We'll go along with the for loop
                
                break;
                
            case 9:
                //next blah blah blah --> blah
                
                //That's actually it. evaluate it.
                evaluate(currentLine);
                
                //Used a lot in next calculations
                Script currentScriptObj = scr.getScriptAtID(currentThread.getScriptID());
                
                //Get location of the last for tag
                int forLocation = findEndLimiter(currentScriptObj,
                        "next", "for", currentThread.getCurrentLine() -1, -1);
                
                //We want to evaluate that expression again to see if the condition
                //is still satisfied
                
                int andLocation = findCorrespondingBracket(currentScriptObj.getLine(forLocation),
                        "and", 0, 1);
                
                Parameter doWeKeepGoing = evaluateExpression(
                        currentScriptObj.getLine(forLocation),
                        andLocation + 1, 
                        currentScriptObj.getLine(forLocation).getParameterCount() - 1);
                
                if (doWeKeepGoing.getBooleanValue() == true)
                {
                    //Then we will set the line to the one right in front of the for
                    //tag
                    currentThread.setLineNumber(forLocation + 1);
                    justJumped = true;
                }
                else
                {
                    //Alright, we're done. We do nothing, and we let the act()
                    //carry us to the line after the "next"
                }
                
                break;
                
            case 10:
                callFunction(currentLine);
                break;

            /***********************************************************
                                 VARIABLE MANIPULATION
             ***********************************************************/

            //createVariable identifier (Optional value)
            case 11:
                createVariable(currentLine);
                break;

            //setVariable identifier newValue
            case 12:
                setVariable(currentLine);
                break;

            //setThreadVariable
            case 13:
                currentThread.modifyVariable(currentLine.getStringParameter(0),
                        identifierCheck(currentLine, 1));
                break;

            /***********************************************************
                                ARRAY MANIPULATION
             ***********************************************************/

            //newArray arrayName size
            case 15:
                String newArrayName = currentLine.getStringParameter(0);
                int newArraySize = (int)identifierCheck(currentLine, 1).getDoubleValue();
                Parameter newArray = new Parameter(new Object[newArraySize]);

                currentThread.setVariable(newArrayName, newArray);
                break;

            //getArraySize arrayName --> variable
            case 16:
                int arraySize = currentThread.getVariable(currentLine.getStringParameter(0)).
                        getObjectArrayValue().length;

                currentThread.setVariable(
                        currentLine.getStringParameter(2),
                        new Parameter(arraySize));
                break;

            //getArray arrayName index --> variable
            case 17:
                //Gets the Parameter with the name arrayName, and grabs the Object[] from that.
                Object[] objArray = currentThread.getVariable(currentLine.getStringParameter(0)).getObjectArrayValue();

                //Extract variable Index, get parameter at that.
                int variableIndex = (int)identifierCheck(currentLine, 1).getDoubleValue();
                Parameter indexedVariable = (Parameter)objArray[variableIndex];

                //Put in variable.
                currentThread.setVariable(
                        currentLine.getStringParameter(3),
                        indexedVariable);
                break;

            //putArray arrayName index <-- variable
            case 18:
                Parameter newVariable = identifierCheck(currentLine, 3);
                int newVariableIndex = (int)identifierCheck(currentLine, 1).getDoubleValue();
                Object[] array = currentThread.getVariable(currentLine.getStringParameter(0)).getObjectArrayValue();
                array[newVariableIndex] = newVariable;
                break;

            /***********************************************************
                                    MATH FUNCTIONS
             ***********************************************************/

            //Evaluating
            case 20:
                evaluate(currentLine);
                break;

            //sin (variable) --> resultVar
            //sin (expression) --> resultVar
            case 21:
                double trigresult;

                int arrowIndex = findCorrespondingBracket(currentLine, "-->", 1, 1);
                if (arrowIndex == 1)
                {
                    trigresult = Math.sin(identifierCheck(currentLine, 0).getDoubleValue());
                }
                else
                {
                    //Evaluate, then take the sine...
                    Parameter ourParameter = evaluateExpression(currentLine, 0, arrowIndex - 1);
                    trigresult = Math.sin(ourParameter.getDoubleValue());
                }

                currentThread.setVariable(currentLine.getStringParameter(arrowIndex + 1),
                        new Parameter(trigresult));

                break;

            //cos (variable) --> resultVar
            //cos (expression) --> resultVar
            case 22:
                double triganswer;

                int indexOfArrow = findCorrespondingBracket(currentLine, "-->", 1, 1);
                if (indexOfArrow == 1)
                {
                    triganswer = Math.cos(identifierCheck(currentLine, 0).getDoubleValue());
                }
                else
                {
                    //Evaluate, then take the sine...
                    Parameter ourParameter = evaluateExpression(currentLine, 0, indexOfArrow - 1);
                    triganswer = Math.cos(ourParameter.getDoubleValue());
                }

                currentThread.setVariable(currentLine.getStringParameter(indexOfArrow + 1),
                        new Parameter(triganswer));

                break;

            //tan (variable) --> resultVar
            //tan (expression) --> resultVar
            case 23:
                break;

            //sqrt (variable) --> var
            case 24:
                Parameter sqrt = new Parameter(
                        Math.sqrt(identifierCheck(currentLine, 0).getDoubleValue()));
                currentThread.setVariable(currentLine.getStringParameter(2),
                        sqrt);
                break;
            case 25:
                //toradian degree --> identifier
                currentThread.setVariable(currentLine.getStringParameter(2),
                        new Parameter(Math.toRadians(identifierCheck(currentLine, 0).getDoubleValue())));

                break;
            case 26:
                //rand min max --> number
                double min = identifierCheck(currentLine, 0).getDoubleValue();
                double max = identifierCheck(currentLine, 1).getDoubleValue();
                double randomNumber = min + (int)(Math.random() * ((max - min) + 1));
                String identifier = currentLine.getStringParameter(3);
                currentThread.setVariable(identifier,
                        new Parameter(randomNumber));
                break;

            case 27:
                double needFloor = identifierCheck(currentLine, 0).getDoubleValue();
                double floored = Math.floor(needFloor);
                String flooredName = currentLine.getStringParameter(2);
                currentThread.setVariable(flooredName, new Parameter(floored));
                break;

            case 30: //new Thread
                //newThread scriptID
                createNewThread(currentLine);
                break;


            /***********************************************************
                              THREAD-RELATED FUNCTIONS
             ***********************************************************/

            //This thread is done
            case 31:
                currentThread.markForDeletion();
                //Ending the thread obviously means that you DON'T go to the next line
                continueExecuting = false;
                break;

            //killThread by marking the target thread for deletion.
            case 32:
                String targetThread = identifierCheck(currentLine, 0).getStringValue();
                threadManager.markForDeletion(targetThread);
                break;

            //This is like calling a static function, except from a script
            case 33:
                callScriptFunction(currentLine);
                break;

            // Librarycall is like callScriptFunction, but from a certain Script
            case 34:
                break;
                
            //Case 21 works with calling the function
            //located in another thread.
            case 35:
                callThreadFunction(currentLine);
                break;
                
            case 36:
                String idOfThread = currentThread.getName();
                currentThread.setVariable(currentLine.getStringParameter(0), 
                        new Parameter(idOfThread));
                break;

            /***********************************************************
                                SYSTEM-RELATED FUNCTIONS
             ***********************************************************/
            //Print a variable, for debugging
            case 45:
                print(currentLine);
                break;
            case 46:
                getSystemMilliTime(currentLine);
                break;
            case 47:
                getSystemNanoTime(currentLine);
                break;


            /***********************************************************
             ***********************************************************
                  ---      VoyageQuest Specific Functions    ---
             ***********************************************************
             ***********************************************************/

            /***********************************************************
                       Control of Entities, Both World and Battle
             ***********************************************************/
            //setVelocity 101
            case 101:
                double vx = identifierCheck(currentLine, 0).getDoubleValue();
                double vy = identifierCheck(currentLine, 1).getDoubleValue();
                ((Entity)currentScriptable).setVelocity(vx, vy);
                break;

            //movePixelAmount 104
            //moveByPixels distance_in_pixels
            case 104:
                double pixel_distance = (int)identifierCheck(currentLine, 0).getDoubleValue();
                ((Entity)currentScriptable).beginMove(pixel_distance, currentThread);
                continueExecuting = false;

                break;

            //entitySetLocation x y
            case 107:

                double newX = identifierCheck(currentLine, 0).getDoubleValue();
                double newY = identifierCheck(currentLine, 1).getDoubleValue();
                ((Entity)currentScriptable).place(newX, newY);
                break;

            /***********************************************************
                        Control of Entities, World Only
             ***********************************************************/
            //setVelocityStandard 102
            //setVelocityStandard vx/vy 0,1,2,3 will do setAnimationDirection too.
            case 102:
                double velocity = identifierCheck(currentLine, 0).getDoubleValue();
                int direction = (int)identifierCheck(currentLine, 1).getDoubleValue();

                double velocity_x = 0.0d;
                double velocity_y = 0.0d;

                switch (direction)
                {
                    //NORTH/UP
                    case 0:
                        velocity_y = -velocity;
                        break;
                    //SOUTH/DOWN
                    case 1:
                        velocity_y = velocity;
                        break;
                    //EAST/RIGHT
                    case 2:
                        velocity_x = velocity;
                        break;
                    //WEST/LEFT
                    case 3:
                        velocity_x = -velocity;
                        break;
                }
                ((Entity)currentScriptable).setVelocity(velocity_x, velocity_y);

                break;

            //moveTileAmount 103
            //moveByTiles tile_distance
            case 103:
                int tile_distance = (int)identifierCheck(currentLine, 0).getDoubleValue();
                ((Entity)currentScriptable).beginMove(tile_distance, currentThread);
                continueExecuting = false;

                break;

            //setAnimationDirection NORTH SOUTH EAST WEST
            case 60:
                String dir = identifierCheck(currentLine, 0).getStringValue();
                if (dir.equalsIgnoreCase("NORTH"))
                    ((Entity)currentScriptable).setAnimation(0);
                if (dir.equalsIgnoreCase("SOUTH"))
                    ((Entity)currentScriptable).setAnimation(1);
                if (dir.equalsIgnoreCase("WEST"))
                    ((Entity)currentScriptable).setAnimation(2);
                if (dir.equalsIgnoreCase("EAST"))
                    ((Entity)currentScriptable).setAnimation(3);
                break;

            /***********************************************************
                            GLOBAL VARIABLE STATES
             ***********************************************************/

            //existsGlobal "StringName" --> boolVar
            case 120:
                String variableName = identifierCheck(currentLine, 0).getStringValue();
                boolean exists = Global.globalMemory.containsKey(variableName);
                currentThread.setVariable(
                        currentLine.getParameter(2).getStringValue(),
                        new Parameter(exists));
                break;

            //writeGlobal NEW_VALUE --> "VariableName"
            case 121:
                Parameter newValue = currentLine.getParameter(0);
                Global.globalMemory.put(
                        currentLine.getParameter(2).getStringValue(),
                        newValue);
                break;

            //getGlobal "GlobalVariableName" --> localvariablename
            case 122:
                currentThread.setVariable(
                        currentLine.getStringParameter(2),
                        Global.globalMemory.get(currentLine.getStringParameter(0)));
                break;


            /***********************************************************

             ***********************************************************/


                
            //freezeThreads 130
            case 130:
                Global.isFrozen = true;
                Global.unfrozenThread = currentThread;
                break;
                
            //freezeInputs 131
            case 131:
                Global.isInputFrozen = true;
                break;
                
            //unfreezeThreads 133
            case 133:
                Global.isFrozen = false;
                Global.unfrozenThread = null;
                break;
                
            //unfreezeInputs 134
            case 134:
                Global.isInputFrozen = false;
                break;
                
            //assumeControlOfPlayer
            //sets the scriptable of this thread to that of the player
            //recently updated to make it work in both RPG and Battle modes
            case 136:
                switch (VoyageQuest.state)
                {
                    case RPG:
                        currentThread.setScriptable(VoyageQuest.player);
                        VoyageQuest.player.setMainThread(currentThread);
                        break;
                    case COMBAT:
                        currentThread.setScriptable(BattleField.player);
                        break;
                }

                currentScriptable = currentThread.getScriptable();
                break;

                
            case 150:
            //genericMessageBox text
                currentThread.speak(
                        identifierCheck(currentLine, 0).getStringValue());
                continueExecuting = false;
                break;
                
                
            case 151:
            //dialogbox animationname text
                currentThread.speak(
                        identifierCheck(currentLine, 1).getStringValue(),
                        identifierCheck(currentLine, 0).getStringValue());
                
                continueExecuting = false;
                break;

            case 152:
            // dialogprompt animationname text options --> result
                Object[] objectArray = identifierCheck(currentLine, 2).getObjectArrayValue();
                String[] options = new String[objectArray.length];
                for (int i = 0; i < objectArray.length; i++) {
                    options[i] = objectArray[i].toString();
                }

                currentThread.speak(
                        identifierCheck(currentLine, 1).getStringValue(),
                        identifierCheck(currentLine, 0).getStringValue(),
                        options,
                        currentLine.getParameter(4));

                continueExecuting = false;
                break;

            case 153:
            // genericprompt text options --> result
                Object[] objectArray2 = identifierCheck(currentLine, 1).getObjectArrayValue();
                String[] options2 = new String[objectArray2.length];
                for (int i = 0; i < objectArray2.length; i++) {
                    options2[i] = objectArray2[i].toString();
                }

                currentThread.speak(
                        identifierCheck(currentLine, 0).getStringValue(),
                        options2,
                        currentLine.getParameter(3));

                continueExecuting = false;
                break;


            //mapChange NameOfNewMap playernewLocX playernewLocY
            case 160:
                //Clear the threads of the current map
                VoyageQuest.threadManager.clear();

                // Play teleport music
                voyagequest.Res.playEffect("Teleport");

                //Load map with name
                String newMapName = identifierCheck(currentLine, 0).getStringValue();
                try {
                    Global.currentMap =
                            new Map(newMapName);
                } catch (Exception e) {
                    System.out.println("Error: failed to load the map called " + newMapName);
                }

                //Change background music if needed
                System.out.println(Global.currentMap.getMusic() + " is " + voyagequest.Res.getAudio(Global.currentMap.getMusic()).isPlaying());
                voyagequest.Res.playMusic(Global.currentMap.getMusic());

                //Now put the player where the player is supposed to be
                Entity player = VoyageQuest.player;
                player.r.x = identifierCheck(currentLine, 1).getDoubleValue();
                player.r.y = identifierCheck(currentLine, 2).getDoubleValue();

                Global.currentMap.entities.add(player);
                Global.currentMap.collisions.addEntity(player);

                //Fade in.
                Thread fadeIn = new Thread("FADEIN");
                fadeIn.setLineNumber(0);
                fadeIn.setRunningState(false);
                fadeIn.setName("FADEIN");
                VoyageQuest.threadManager.addThread(fadeIn);

                break;

            //fade in
            case 161:
                currentThread.setRunningState(true);
                
                continueExecuting = false;
                break;
                
            //fade out
            case 162:
                currentThread.setRunningState(true);
                continueExecuting = false;
                break;

            //setlight 0-255
            case 163:
                int newLightValue = (int)identifierCheck(currentLine, 0).getDoubleValue();
                Global.camera.fade = newLightValue;
                break;

            //cavelighton 164
            case 164:
                Global.camera.lightSourceOn = true;
                break;

            //cavelightoff 165
            case 165:
                Global.camera.lightSourceOn = false;
                break;

            //freezeCamera ULX ULY
            case 170:
                Global.camera.freezeAt(
                        (int)identifierCheck(currentLine, 0).getDoubleValue(),
                        (int)identifierCheck(currentLine, 1).getDoubleValue());
                break;

            //unfreezeCamera
            case 171:
                Global.camera.unfreeze();
                break;

            // sound effect
            case 175:
                voyagequest.Res.playEffect(identifierCheck(currentLine, 0).getStringValue());
                break;

            // play background music
            case 176:
                voyagequest.Res.playMusic(identifierCheck(currentLine, 0).getStringValue());
                break;

            // startBattle battleID
            case 200:
                String battleID = identifierCheck(currentLine, 0).getStringValue();
                BattleManager.initBattle(battleID);
                break;

            // endbattle
            case 201:
                BattleManager.endBattle();
                break;

            // inBattleMode --> variable
            case 202:
                currentThread.setVariable(
                        currentLine.getStringParameter(1),
                        new Parameter(VoyageQuest.state == GameState.COMBAT));
                break;

            //spawnEnemy enemyID x y instanceName threadName
            case 210:
                String enemyID = identifierCheck(currentLine, 0).getStringValue();
                int xLoc = (int)identifierCheck(currentLine, 1).getDoubleValue();
                int yLoc = (int)identifierCheck(currentLine, 2).getDoubleValue();
                String enemyInstanceName = identifierCheck(currentLine, 3).getStringValue();
                String enemyThreadName = identifierCheck(currentLine, 4).getStringValue();

                //Spawn the enemy based on its name and location, and set instanceID
                Enemy newEnemy = EntityManager.spawnEnemy(enemyID, xLoc, yLoc);
                newEnemy.instanceID = enemyInstanceName;

                //Now we have to create the main thread of this enemy
                Thread enemyThread = new Thread(newEnemy.mainScriptID);
                enemyThread.setLineNumber(0);
                enemyThread.setName(enemyThreadName);
                enemyThread.setScriptable(newEnemy);
                newEnemy.setMainThread(enemyThread);
                newEnemy.associatedThreadInstances.add(enemyThreadName);

                //Add new thread to the battleThreadManager
                VoyageQuest.battleThreadManager.addThread(enemyThread);

                //Finally, add the generated Enemy instance to spawns
                BattleField.addBattleEntity(newEnemy, enemyInstanceName);

                break;

            //spawnProjectile projectileID x y vx vy rotation allegiance
            case 211:
                String projectileID = identifierCheck(currentLine, 0).getStringValue();
                int projectileX = (int)identifierCheck(currentLine, 1).getDoubleValue();
                int projectileY = (int)identifierCheck(currentLine, 2).getDoubleValue();
                double projectileVx = identifierCheck(currentLine, 3).getDoubleValue();
                double projectileVy = identifierCheck(currentLine, 4).getDoubleValue();
                int rotation = (int)identifierCheck(currentLine, 5).getDoubleValue();

                //Allegiance is unfriendly by default.
                String allegiance = currentLine.getStringParameter(6);
                Allegiance projectileAllegiance = Allegiance.UNFRIENDLY;
                if (allegiance.equalsIgnoreCase("FRIENDLY")) projectileAllegiance = Allegiance.FRIENDLY;
                if (allegiance.equalsIgnoreCase("DANGER")) projectileAllegiance = Allegiance.DANGER;

                Projectile newProjectile = EntityManager.spawnProjectile(
                        projectileID,projectileX, projectileY, projectileAllegiance);

                newProjectile.velocityX = projectileVx;
                newProjectile.velocityY = projectileVy;
                newProjectile.rotationInDegrees = rotation;

                BattleField.addBattleEntity(newProjectile);

                break;

            //setAnimation animationID
            case 230:
                ((BattleEntity)currentScriptable).setAnimation(
                        identifierCheck(currentLine, 0).getStringValue());
                break;

            //changeAnimationDirection 1/-1
            case 231:
                ((Entity)currentScriptable).changeAnimationDirection(
                        (int)identifierCheck(currentLine, 0).getDoubleValue());
                break;

            // getLocation instanceID --> x y or getLocation --> x y
            // I'll implement the second one first
            case 190:
                String xVariable = currentLine.getStringParameter(1);
                String yVariable = currentLine.getStringParameter(2);
                double scriptableXLoc = ((Entity)currentScriptable).r.getX();
                double scriptableYLoc = ((Entity)currentScriptable).r.getY();

                currentThread.setVariable(xVariable, new Parameter(scriptableXLoc));
                currentThread.setVariable(yVariable, new Parameter(scriptableYLoc));

                break;

            //setLocation instanceID x y
            //setLocation x y... I'll do the second one first, too
            case 191:
                double newXLoc = identifierCheck(currentLine, 0).getDoubleValue();
                double newYLoc = identifierCheck(currentLine, 1).getDoubleValue();
                ((Entity)currentScriptable).place(newXLoc, newYLoc);
                break;

            //fireWeapon weaponID
            case 195:
                String weaponID = identifierCheck(currentLine, 0).getStringValue();
                System.out.println("weapon is " + weaponID);
                WeaponManager.fireWeapon(weaponID, currentScriptable);
                break;

            //The manipulation of the locations of Displayables goes here
            case 50:
                //Depends on whether it's THIS Scriptable or another one
                int determinant = currentLine.getParameterCount();
                if (determinant == 1)
                {
                    //This scriptable
                    int setRotation = (int)(identifierCheck(currentLine, 0).getDoubleValue());
                    ((Entity)currentScriptable).setRotation(setRotation);
                }
                else if (determinant == 2)
                {
                    //InstanceID determines which to set
                    String instanceID = identifierCheck(currentLine, 0).getStringValue();
                    int newRotation = (int)identifierCheck(currentLine, 1).getDoubleValue();

                    BattleEntity rotatedEntity = BattleField.entityInstances.get(instanceID);
                    rotatedEntity.setRotation(newRotation);
                }

                break;


            //Given a threadID and the ID of an entity, binds the
            //entity as the Scriptable of the thread.
            //setThreadScriptable threadID ENTITYID
            case 233:
                String threadName = identifierCheck(currentLine, 0).getStringValue();
                String entityName = identifierCheck(currentLine, 1).getStringValue();

                System.out.println("THREAD IS" + threadName);
                //Give the thread at threadName
                Thread t;
                switch (VoyageQuest.state)
                {
                    case RPG:
                        t = VoyageQuest.threadManager.getThreadAtName(threadName);
                        t.setScriptable(Global.currentMap.entityLookup.get(entityName));
                        break;

                    case COMBAT:
                        t = VoyageQuest.battleThreadManager.getThreadAtName(threadName);
                        t.setScriptable(BattleField.entityInstances.get(entityName));
                        break;
                }
                break;

            //setThisThreadScriptable ENTITYID
            case 234:
                String entityID = identifierCheck(currentLine, 0).getStringValue();
                switch (VoyageQuest.state)
                {
                    case RPG:
                        currentThread.setScriptable(Global.currentMap.entityLookup.get(entityID));
                        break;

                    case COMBAT:
                        Scriptable test = BattleField.entityInstances.get(entityID);
                        currentThread.setScriptable(test);
                        break;
                }
                //We need to refresh the currentScriptable, since we just modified it!
                currentScriptable = currentThread.getScriptable();
                break;

            //getThreadScriptable threadID --> variable
            case 235:
                String threadID = identifierCheck(currentLine, 0).getStringValue();
                String var = identifierCheck(currentLine, 2).getStringValue();

                Thread selectedThread;
                switch (VoyageQuest.state)
                {
                    case RPG:
                        selectedThread = VoyageQuest.threadManager.getThreadAtName(threadID);
                        String RPGinstanceID = ((Entity)selectedThread.getScriptable()).name;
                        currentThread.setVariable(var, new Parameter(RPGinstanceID));
                        break;

                    case COMBAT:
                        selectedThread = VoyageQuest.battleThreadManager.getThreadAtName(threadID);
                        String instanceID = ((BattleEntity)selectedThread.getScriptable()).instanceID;
                        currentThread.setVariable(var, new Parameter(instanceID));
                        break;
                }
                break;

            //getThisThreadScriptable --> variable
            case 236:
                String scriptableVarName = identifierCheck(currentLine, 1).getStringValue();
                switch (VoyageQuest.state)
                {
                    case RPG:
                        String RPGinstanceID = ((Entity)currentThread.getScriptable()).name;
                        currentThread.setVariable(scriptableVarName, new Parameter(RPGinstanceID));
                        break;

                    case COMBAT:
                        String instanceID = ((BattleEntity)currentThread.getScriptable()).instanceID;
                        currentThread.setVariable(scriptableVarName, new Parameter(instanceID));
                        break;
                }
                currentScriptable = currentThread.getScriptable();
                break;


        }
        
        //Returns whether to continue loading more commands
        return continueExecuting;
    }
    
    
    public void getSystemMilliTime(Line currentLine)
    {
        String variableIdentifier = currentLine.getStringParameter(0);
        Parameter referencedParam = new Parameter(System.currentTimeMillis());
        
        
        currentThread.setVariable(variableIdentifier,
                        referencedParam);
    }
    
    public void getSystemNanoTime(Line currentLine)
    {
        String variableIdentifier = currentLine.getStringParameter(0);
        Parameter referencedParam = new Parameter(System.nanoTime());
        
        currentThread.setVariable(variableIdentifier,
                        referencedParam);
    }
    
    /**************************************************************************
    ***************************************************************************
    ********************ALL THE INDIVIDUAL METHODS GO HERE*********************
    ***************************************************************************
    **************************************************************************/
    
     
    private Parameter identifierCheck(Parameter iDunno)
    {
        if (iDunno.isIdentifier())
        {
            return currentThread.getVariable(iDunno.getStringValue());
        }
        return iDunno;
    }
    
    private Parameter identifierCheck(Line currentLine, int indexOnLine)
    {
        Parameter iDunno = currentLine.getParameter(indexOnLine);
        return identifierCheck(iDunno);
    }
    
    private void createVariable(Line currentLine)
    {

        //The name of the variable.
        String variableIdentifier = currentLine.getStringParameter(0);

        //We're trying to check if we're initializing the variable
        //as well as declaring it
        if (currentLine.getParameterCount() >= 2)
        {
            //So they decided to declare and initialize.
            Parameter initParameter = currentLine.getParameter(1);
            
            //Now set variableIdentifier to either the literal or to the
            //value pointed to by the identifier
            currentThread.setVariable(variableIdentifier,
                    identifierCheck(currentLine, 1));
            
        }
        else 
        {
            //They decided to declare a variable without initialization
            currentThread.newVariable(variableIdentifier);
        }
    }
    
    private void setVariable(Line currentLine)
    {
        String variableIdentifier = currentLine.getStringParameter(0);
        
        currentThread.setVariable(variableIdentifier,
                    identifierCheck(currentLine, 1));
        
    }
    
    private void print(Line currentLine)
    {
        Parameter toBePrinted = currentLine.getParameter(0);
            
        //We might evaluate something before printing the result
        if (toBePrinted.getStoredType() == Parameter.STRING
                && toBePrinted.getStringValue().equals("<--"))
        {
            Parameter result = evaluateExpression(currentLine, 1, currentLine.getParameterCount() - 1);
            System.out.println(result.toString());
        }
        else
        {
            Parameter message = identifierCheck(toBePrinted);
            System.out.println(message.toString());
        }
        
    }               
    
    private void createNewThread(Line currentLine) 
    {
        //Extract from currentLine...
        String scriptID = currentLine.getStringParameter(0);
        String threadName = identifierCheck(currentLine, 1).getStringValue();

        //Create a new thread with that scriptID, giving it scriptName
        Thread newThread = createNewThread(scriptID, threadName);

        //Now add it to the global Thread list.
        threadManager.addThread(newThread);
    }
    
    private Thread createNewThread(String scriptID, String threadName)
    {
        //Create a new thread with that scriptID, giving it scriptName
        Thread newThread = new Thread(scriptID);
            newThread.setName(threadName);
            newThread.setLineNumber(0);
            newThread.setRunningState(false);
            newThread.setScriptable(currentScriptable);

        //for the convenience of certain functions
        return newThread;
    }
    
    private Object[] formatFunctionLine(Line currentLine, Line functionLine, int searchIndexOnLine)
    {
        //We will act differently if searchIndexOnLine is a 1, since that means
        //we're doing callfunction and we need to adjust our indices
        boolean doingCallFunction = (searchIndexOnLine == 1);
        
        ArrayList<String> returnKeys = new ArrayList<String>();
        HashMap<String, Parameter> newMemoryBox = new HashMap<String, Parameter>();
       
        if (currentLine.getParameterCount() <= 2)
        {
            //In this case, don't do anything 
        }
        else 
        {
            boolean isArrowReached = false;
            int searchIndex = searchIndexOnLine;
            while (searchIndex < currentLine.getParameterCount())
            {
                //Our current Parameter at searchIndex
                Parameter currentParameter = currentLine.getParameter(searchIndex);
                
                //See if the currently indexed thing is a -->
                if ( (currentParameter.getStoredType() == 1) &&
                    (currentParameter.getStringValue().equals("-->")))
                {
                    //If so, then now we have reached the arrow
                    isArrowReached = true;
                    
                    //We go on to next parameter
                }
                else
                {
                    //So we have reached something meaningful. Now, our
                    //response depends on whether the arrow has been reached yet
                    
                    if (isArrowReached)
                    {
                        //So it's a return, so add the Parameter's name to the
                        //return thing
                        String nameOfReturn = currentLine.getParameter(searchIndex).getStringValue();
                        returnKeys.add(nameOfReturn);
                    }
                    else
                    {
                        //We are adding to the memorybox
                        String ourIdentifier;
                                
                        //Get the name that the variable will be referred as
                        if (doingCallFunction)
                        {
                            
                            ourIdentifier = functionLine.getStringParameter(searchIndex);
                        }
                        else 
                        {
                            ourIdentifier = functionLine.getStringParameter(searchIndex - 1);
                        }
                        
                        //But hold on a second. currentParameter could be a literal, or it
                        //could be an identifier to something else. Use identifierCheck
                        Parameter checkedParam = identifierCheck(currentParameter);
                        newMemoryBox.put(ourIdentifier, checkedParam);
                        
                    }
                }
                //Increment searchIndex
                searchIndex++;
            }
        }
        
        Object[] returnArray = new Object[2];
        returnArray[0] = returnKeys;
        returnArray[1] = newMemoryBox;
        return returnArray;
    }

    //callFunction [act] param1 param2 param3 --> returned1 returned2
    private void callScriptFunction(Line currentLine)
    {
        //callFunction 5 [act] param1 param2 param3 --> returned1 returned2
        
        //Get the Script object that 5 refers to
        String scriptID = currentLine.getStringParameter(0);
        Script jumpedScript = scr.getScriptAtID(scriptID);
        
        //Basically, extracting the label [labelname]
        String labelName = currentLine.getStringParameter(1);
        
        //Now find the line number that [labelname] is located on
        int lineNumber = jumpedScript.getLabelIndexOnLineList(labelName);
        
        //But BEFORE setting the currentplace to that line, first store the
        //old script ID and old line number for returning purposes
        currentThread.makeReturnPoint();
        
        //Set the current place to that line, that script
        currentThread.setLineNumber(lineNumber);
        currentThread.setScriptID(scriptID);
        
        //But here's the thing. We need to know what to make its identifier.
        //In order to do that, we need to go all the way to the line we're jumping
        //to and getting a copy of their line object.
        Line functionLine = jumpedScript.getLineAtLabel(labelName);
        //function [labelname] param1identifier param2identifier ...
        
        Object[] formattedLineData = formatFunctionLine(currentLine, functionLine, 2);
        
        ArrayList<String> returnKeys = (ArrayList<String>)formattedLineData[0];
        HashMap<String, Parameter> newMemoryBox = (HashMap<String, Parameter>)formattedLineData[1];
        
        
        //Now convert returnKeys to an array
        String[] returnKeyArray = new String[returnKeys.size()];
        returnKeys.toArray(returnKeyArray);
        
        currentThread.setFunctionReturns(returnKeyArray);
        currentThread.setMemoryBox(new HashMap<String, Parameter>());
        currentThread.setLocalMemoryBox(newMemoryBox);
        currentThread.increaseFunctionLayer();
        
        
    }
    
    private void callThreadFunction(Line currentLine)
    {
        //callFunction "threadname" [act] param1 param2 ... --> returned1 returned2 ...
       
        //Get the Thread object which threadname refers to
        //String threadName = currentLine.getStringParameter(0);
        String threadName = identifierCheck(currentLine, 0).getStringValue();

        Thread jumpedThread = threadManager.getThreadAtName(threadName);

        HashMap<String, Parameter> jumpedBox = jumpedThread.getMemoryBox();


        //Basically, extracting the label [act]
        String labelName = currentLine.getStringParameter(1);
        
        //Alright, this gets complicated.
        //Thread --> what is its first script ID number?
        //Go find that Script object at the scriptID
        //Get the label index
        String threadScriptID = jumpedThread.baseScriptID;
        //System.out.println(threadScriptID + " is the ID of the thread I'm jumping TO");
        
        Script jumpedScript = scr.getScriptAtID(threadScriptID);
        int newLine = jumpedScript.getLabelIndexOnLineList(labelName);
        //System.out.println("I am jumping onto line " + newLine);
        
        //But BEFORE setting the currentplace to that line, first store the
        //old script ID and old line number for returning purposes
        currentThread.makeReturnPoint();
        
        //Set the current place to that line, that script
        currentThread.setLineNumber(newLine);
        currentThread.setScriptID(threadScriptID);
        
        //But here's the thing. We need to know what to make its identifier.
        //In order to do that, we need to go all the way to the line we're jumping
        //to and getting a copy of their line object.
        Line functionLine = jumpedScript.getLineAtLabel(labelName);
        //function [nameblah] param1identifier param2identifier ...
        
        Object[] formattedLineData = formatFunctionLine(currentLine, functionLine, 2);
        
        ArrayList<String> returnKeys = (ArrayList<String>)formattedLineData[0];
        HashMap<String, Parameter> newMemoryBox = (HashMap<String, Parameter>)formattedLineData[1];
        
        
        //System.out.println("Local Memorybox is size" + newMemoryBox.size());
        
        //Now convert returnKeys to an array
        String[] returnKeyArray = new String[returnKeys.size()];
        returnKeys.toArray(returnKeyArray);
        
        currentThread.setFunctionReturns(returnKeyArray);
        currentThread.setMemoryBox(jumpedBox);
        currentThread.setLocalMemoryBox(newMemoryBox);
        currentThread.increaseFunctionLayer();
        
    }
    
    //The difference is that this callFunction is for the SAME script file
    //I basically just took the more advanced callThreadFunction and
    //modified it for this
    private void callFunction(Line currentLine)
    {
        //callFunction [act] param1 param2 ... --> returned1 returned2 ...
        
        //The script object we are working with
        Script thisScript = scr.getScriptAtID(currentThread.getScriptID());
        
        
        //Basically, extracting the label [act] and finding out where it is
        String labelName = currentLine.getStringParameter(0);
        int newLine = thisScript.getLabelIndexOnLineList(labelName);
        
        //But BEFORE setting the currentplace to that line, first store the
        //old script ID and old line number for returning purposes
        currentThread.makeReturnPoint();
        
        //Set the current place to that line, that script
        currentThread.setLineNumber(newLine);
        currentThread.setScriptID(currentThread.getScriptID());
        
        //But here's the thing. We need to know what to make its identifier.
        //In order to do that, we need to go all the way to the line we're jumping
        //to and getting a copy of their line object.
        Line functionLine = thisScript.getLineAtLabel(labelName);
        
        Object[] formattedLineData = formatFunctionLine(currentLine, functionLine, 1);
        
        ArrayList<String> returnKeys = (ArrayList<String>)formattedLineData[0];
        HashMap<String, Parameter> newMemoryBox = (HashMap<String, Parameter>)formattedLineData[1];
        
        
        //Now convert returnKeys to an array
        String[] returnKeyArray = new String[returnKeys.size()];
        returnKeys.toArray(returnKeyArray);
        
        //Make all settings
        currentThread.setFunctionReturns(returnKeyArray);
        currentThread.setMemoryBox(currentThread.getMemoryBox());
        currentThread.setLocalMemoryBox(newMemoryBox);
        currentThread.increaseFunctionLayer();
        
    }
    
    //return val1 param2 val3
    private void returnFromFunction(Line currentLine)
    {
        //Remember how we passed the returned variables' names?
        //Now we retrieve them
        String[] returnKeys = currentThread.getFunctionReturns();

        //Before restoreLastReturnPoint(), retain the values which
        //need to be retained
        Parameter[] parameters = new Parameter[returnKeys.length];
        for (int i = 0; i < returnKeys.length; i++)
        {
            Parameter currentParam = currentLine.getParameter(i);
            
            //A literal, or a variable?
            parameters[i] = identifierCheck(currentLine, i);
        }
        
        //Return, and also decrease the function layer
        currentThread.restoreLastReturnPoint();
        currentThread.decreaseFunctionLayer();
        
        //Now add those retained variables to the current thread
        //layer's memory.
        for (int i = 0; i < returnKeys.length; i++)
        {
            currentThread.setVariable(returnKeys[i], parameters[i]);
        }
        
    }
    
    public void evaluate(Line currentLine)
    {
        Parameter result = evaluateExpression(currentLine, 0, currentLine.getParameterCount() - 3);
        
        currentThread.setVariable(
                currentLine.getStringParameter(currentLine.getParameterCount() - 1),
                result);
    }
    
    public Parameter evaluateExpression(Line l, int front, int back) 
    {
        //We are at our base case if _ _ _ front is two less than back
        if (front == back - 2)
        {
            return simpleEvaluate(l, front, back);
        }
        
        //Alright, NOT at our base case. Continue splitting expression
        //up into separate expressions
        
        //Get the thing at the first indexed Parameter
        Parameter firstParam = l.getParameter(front);
        
        Parameter resultOnLeft;
        Parameter resultOnRight;
        
        //If it is a "["...
        if (firstParam.getStoredType() == Parameter.STRING
                && firstParam.getStringValue().equals("["))
        {
            
            //Find the corresponding end bracket
            int paramEnd = findCorrespondingBracket(l, "]", "[", front + 1, 1);
            
            //Evaluate that expression
            resultOnLeft = evaluateExpression(l, front + 1, paramEnd - 1);
            
            //Alright, get the opCode...
            Parameter opCode = l.getParameter(paramEnd + 1);
            
            //Now perform the same "is it a [" check for the one after that...
            if (l.getParameter(paramEnd + 2).getStoredType() == Parameter.STRING 
                && l.getParameter(paramEnd + 2).getStringValue().equals("["))
            {
                int secondParamEnd = findCorrespondingBracket(l, "]", "[", paramEnd + 3, 1);
                
                resultOnRight = evaluateExpression(l, paramEnd + 3, secondParamEnd - 1);
            }
            else
            {
                //It's simply that.
                resultOnRight = l.getParameter(paramEnd + 2);
            }
            
            return simpleEvaluate(resultOnLeft, opCode, resultOnRight);
            
        }
        else
        {
            //Alright, this is not a "["...
            //So on the left side of the equation is in fact the first parameter
            resultOnLeft = firstParam;
            
            //A opcode comes right after.
            Parameter opCode = l.getParameter(front + 1);
            
            //There must be an open bracket after, so find the close-bracket
            //which corresponds to it, then do recursion
            int secondParamEnd = findCorrespondingBracket(l, "]", "[", front + 3, 1);
            resultOnRight = evaluateExpression(l, front + 3, secondParamEnd - 1);
            
            //Now that we have all components, evaluate normally.
            return simpleEvaluate(resultOnLeft, opCode, resultOnRight);
            
        }
        
    }
    
    public int findCorrespondingBracket(Line l, String targetBracket, int currentSearchLoc, int stepDirection)
    {
        boolean found = false;
        int index = currentSearchLoc;
        int totalParameters = l.getParameterCount();
        
        //Keep going if we haven't found it, and we haven't reached the last one 
        //already OR overshot the beginning
        while (!found && (index < totalParameters || index >= 0))
        {
            //Ignore if it isn't a string.
            if (l.getParameterType(index) == Parameter.STRING)
            {
                if (l.getStringParameter(index).equals(targetBracket))
                {
                    return index;
                }
            }
            
            //Don't forget
            index += stepDirection;
        }

        return -1;
    }
    
    public int findCorrespondingBracket(Line l, String targetBracket, String ignoredBracket, int currentSearchLoc, int stepDirection)
    {
        boolean found = false;
        
        int additionalLayers = 0;
        int index = currentSearchLoc;
        int totalParameters = l.getParameterCount();
        
        //Keep going if we haven't found it, and we haven't reached the last one 
        //already OR overshot the beginning
        while (!found && (index < totalParameters || index >= 0))
        {
            //Ignore if it isn't a string.
            if (l.getParameterType(index) == Parameter.STRING)
            {
                if (l.getStringParameter(index).equals(ignoredBracket))
                {
                    //Alright, one more to go then
                    additionalLayers++;
                } 
                else if (l.getStringParameter(index).equals(targetBracket))
                {
                    //If we found a "]" then we have to see
                    //if we have more to go first.
                    if (additionalLayers > 0)
                    {
                        additionalLayers--;
                    }
                    else
                    {
                        //Alright, now we found it for good.
                        return index;
                    }
                }
            }

            //Don't forget
            index += stepDirection;
        }
        
        return -1;
    }
    
    public int findEndLimiter(Script currentScript, String openingLimiter, String closingLimiter, int start, int stepDirection)
    {
        boolean found = false;
        int additionalLayers = 0;
        int index = start;
        
        int totalParameters = currentScript.getLineCount();
        int openingCommandID = currentScript.findCommandID(openingLimiter);
        int closingCommandID = currentScript.findCommandID(closingLimiter);
        
        //Keep going if we haven't found it, and we haven't reached the last one 
        //already OR overshot the beginning
        while (!found && (index < totalParameters || index >= 0))
        {
            int currentCommandID = currentScript.getLine(index).getCommandID();
            
            if (currentCommandID == openingCommandID)
            {
                //Alright, one more to go then
                additionalLayers++;
            } 
            else if (currentCommandID == closingCommandID)
            {
                //If we found one of them, then we have to see
                //if we have more to go first.
                if (additionalLayers > 0)
                {
                    additionalLayers--;
                }
                else
                {
                    //Alright, now we found it for good.
                    return index;
                }
            }
            
            //Don't forget
            index += stepDirection;
        }
        
        return -1;
    }
    
    public Parameter simpleEvaluate(Parameter p1, Parameter opCode, Parameter p2)
    {
        //If either of the Parameters are identifiers, then load their
        //identified value and replace them.
        p1 = identifierCheck(p1);
        p2 = identifierCheck(p2);
        
        String opCodeName = opCode.getStringValue();
        
        //What we will return
        Parameter result;
        
        if (p1.getStoredType() == Parameter.BOOLEAN)
        {
            if (opCodeName.equals("&&"))
            {
                result = new Parameter(p1.getBooleanValue() && p2.getBooleanValue());
                return result;
            }
            else if (opCodeName.equals("||"))
            {
                result = new Parameter(p1.getBooleanValue() || p2.getBooleanValue());
                return result;
            }
        }
        
        if (opCodeName.equals("+"))
        {
            //Hold on, this may be a string...
            if (p1.getStoredType() == Parameter.STRING || p2.getStoredType() == Parameter.STRING)
            {
                result = new Parameter(p1.toString() + p2.toString());
            }
            else 
            {
                //Oh, so it's just a normal double we're adding
                result = new Parameter(p1.getDoubleValue() + p2.getDoubleValue());
            }
            
            return result;
        }   
        else if (opCodeName.equals("-"))
        {
            result = new Parameter(p1.getDoubleValue() - p2.getDoubleValue());
            return result;
        }
        else if (opCodeName.equals("*"))
        {
            result = new Parameter(p1.getDoubleValue() * p2.getDoubleValue());
            return result;
        }
        else if (opCodeName.equals("/"))
        {
            result = new Parameter(p1.getDoubleValue() / p2.getDoubleValue());
            return result;
        }
        else if (opCodeName.equals("%"))
        {
            result = new Parameter(p1.getDoubleValue() % p2.getDoubleValue());
            return result;
        }
        
        //Alright, now that we've eliminated the option of 
        //doing addition, ect, we're doing comparisons.
        if (opCodeName.equals("=="))
        {
            if (p1.getStoredType() == 1)
            {
                //Then we are comparing Strings
                result = new Parameter(p1.getStringValue().equals(p2.getStringValue()));
                return result;
            }
            if (p1.getStoredType() == 2)
            {
                //Then we are comparing booleans
                result = new Parameter(p1.getBooleanValue() == p2.getBooleanValue());
                return result;
            }
            if (p1.getStoredType() == 3)
            {
                //Then we are comparing doubles
                result = new Parameter(p1.getDoubleValue() == p2.getDoubleValue());
                return result;
            }
        }
        if (opCodeName.equals("!="))
        {
            if (p1.getStoredType() == 1)
            {
                //Then we are comparing Strings
                result = new Parameter(!p1.getStringValue().equals(p2.getStringValue()));
                return result;
            }
            if (p1.getStoredType() == 2)
            {
                //Then we are comparing booleans
                result = new Parameter(p1.getBooleanValue() != p2.getBooleanValue());
                return result;
            }
            if (p1.getStoredType() == 3)
            {
                //Then we are comparing doubles
                result = new Parameter(p1.getDoubleValue() != p2.getDoubleValue());
                return result;
            }
        }
        else if (opCodeName.equals("<"))
        {
            //We can only be comparing doubles
            result = new Parameter(p1.getDoubleValue() < p2.getDoubleValue());
            return result;
        }
        else if (opCodeName.equals("<="))
        {
            //We can only be comparing doubles
            result = new Parameter(p1.getDoubleValue() <= p2.getDoubleValue());
            return result;
        }
        else if (opCodeName.equals(">"))
        {
            result = new Parameter(p1.getDoubleValue() > p2.getDoubleValue());
            return result;
        }
        else if (opCodeName.equals(">="))
        {
            result = new Parameter(p1.getDoubleValue() >= p2.getDoubleValue());
            return result;
        }

        //Halt the program and alert user of the scripting error
        System.out.println("Fatal Error: Unrecognized numerical evaluation");
        int droppingTheBomb = 1337/0;
        return new Parameter(droppingTheBomb);
        
    }
    
    public Parameter simpleEvaluate(Line l, int front, int back)
    {
        //We expect it to be in format __ __ __
        Parameter p1 = l.getParameter(front);
        Parameter p2 = l.getParameter(back);
        
        //Get the name of the operation
        Parameter opCode = l.getParameter(front + 1);
        
        return simpleEvaluate(p1, opCode, p2);
        
    }
    
}

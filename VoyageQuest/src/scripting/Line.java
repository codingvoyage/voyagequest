package scripting;

import java.util.ArrayList;

/**
 *
 * @author Edmund
 */
public class Line {
    private int commandID;
    //String commandName;
    private ArrayList<Parameter> parameterList;
    
    //Constructors
    
    public Line(int commandID)
    {
        this.commandID = commandID;
        parameterList = new ArrayList<Parameter>();
    }
    
    public Line()
    {
        this.commandID = -1;
        parameterList = new ArrayList<>();
    }
    
    //Setters and getters for commandID
    public void setCommandID(int newCommandID) 
    {
        commandID = newCommandID;
    }
    
    public int getCommandID()
    {
        return commandID;
    }
    
    
    //Adds a new parameter which contains the indicated value
    public void addParameter(int newIntegerParameter) 
    {
        //It just gets cast into a double when stored
        addParameter(new Parameter((double)newIntegerParameter));
    }
    
    public void addParameter(String newStringParameter) 
    {
        addParameter(new Parameter(newStringParameter));
    }
    
    public void addIdentifierParameter(String newStringParameter)
    {
        Parameter identifierParameter = new Parameter(newStringParameter);
        identifierParameter.setIdentifier(true);
        addParameter(identifierParameter);
    }
    
    public void addParameter(boolean newBooleanParameter) 
    {
        addParameter(new Parameter(newBooleanParameter));
    }
    
    public void addParameter(double newDoubleParameter) 
    {
        addParameter(new Parameter(newDoubleParameter));
    }
    
    
    //Returns the value of the parameter at the indicated index
    public String getStringParameter(int index)
    {
        return getParameter(index).getStringValue();
    }
    
    public int getIntegerParameter(int index)
    {
        //You use this when you are certain that it's an int
        return (int)getParameter(index).getDoubleValue();
    }
    
    public boolean getBooleanParameter(int index)
    {
        return getParameter(index).getBooleanValue();
    }
    
    public double getDoubleParameter(int index)
    {
        return getParameter(index).getDoubleValue();
    }
    
    public int getParameterType(int index)
    {
        return getParameter(index).getStoredType();
    }
    
    //Of course, the user should be able to tell how many Parameters there are
    public int getParameterCount()
    {
        return parameterList.size();
    }
    
    //Adds the Parameter object to the end of the ArrayList
    private void addParameter(Parameter newParameter) {
        parameterList.add(newParameter);
    }
    
    //Returns the Parameter object at the selected index
    //This is kept separate so that the user never has to deal with
    //the Parameter objects; interactions should only happen through Line
    //CHANGE OF PLANS, having public access is better for the Scripting engine
    public Parameter getParameter(int index) {
        return parameterList.get(index);
    }
    
    //For debugging, changing the whole Line into a String.
    public String toString()
    {
        //First, it's the command ID
        String returnThis = Integer.toString(commandID);
        
        //Now add all of the parameters, if applicable
        for (int i = 0; i < parameterList.size(); i++)
        {
            //Add a space..
            returnThis += " ";
            
            //Add the parameter
            //Now this depends on the type it is...
            Parameter currentParameter = getParameter(i);
            returnThis += currentParameter.toString();
        }
        
        return returnThis;
    }
}

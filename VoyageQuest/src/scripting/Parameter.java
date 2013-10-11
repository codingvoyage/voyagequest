package scripting;

/**
 * Parameter<br/>
 * Stores temporary parameters for use in other classes<br/>
 * Supports the following:
 * <ul>
 * <li>String</li>
 * <li>boolean</li>
 * <li>double</li>
 * <li>double[]</li>
 * <li>Object[]</li>
 * </ul>
 * 
 * @author Edmund Qiu
 * @author Brian Yang
 */
public class Parameter {
    private int whichType;
    
    public static final int STRING = 1;
    public static final int BOOLEAN = 2;
    public static final int DOUBLE = 3;
    public static final int DOUBLE_ARRAY = 4;
    public static final int OBJECT_ARRAY = 5;
    
    //Stores the actual value
    private String s;
    private boolean b;
    private double d;
    private double[] da;
    private Object[] oa;
    
    //Is this Parameter used as an identifier for a variable under Scripting?
    private boolean isIdentifier;
  
    //Here are all the constructors of the different supported types...
    public Parameter(String newString)
    {
        setStringValue(newString);
        whichType = STRING;
        setIdentifier(false);
    }
    
    public Parameter(boolean newBoolean)
    {
        setBooleanValue(newBoolean);
        whichType = BOOLEAN;
        setIdentifier(false);
        
    }
    
    public Parameter(double newDouble)
    {
        setDoubleValue(newDouble);
        whichType = DOUBLE;
        setIdentifier(false);
    }
    
    public Parameter(double[] newDoubleArray)
    {
        setDoubleArrayValue(newDoubleArray);
        whichType = DOUBLE_ARRAY;
        setIdentifier(false);
    }
    
    public Parameter(Object[] objectArray) {
        setObjectArrayValue(objectArray);
        whichType = OBJECT_ARRAY;
        setIdentifier(false);
    }
    
    //Returns the type stored
    public int getStoredType() {
        return whichType;
    }
    
    //Returns the corresponding value
    //All the getters come below
    public String getStringValue() 
    {
        return s;
    }
    
    public boolean getBooleanValue()
    {
        return b;
    }
    
    public double getDoubleValue()
    {
        return d;
    }

    //For when you want it to be converted into an int first
    public int getIntValue()
    {
        return (int)d;
    }

    public double[] getDoubleArrayValue()
    {
        return da;
    }
    
    public Object[] getObjectArrayValue() {
        return oa;
    }

    //Sets the Parameter's variable to the provided value
    //All the setters come below 
    public void setStringValue(String newString) 
    {
        s = newString;
    }
    
    public void setBooleanValue(boolean newBoolean)
    {
        b = newBoolean;
    }
    
    public void setDoubleValue(double newDouble)
    {
        d = newDouble;
    }
    
    public void setDoubleArrayValue(double[] newDoubleArray)
    {
        da = newDoubleArray;
    }
    
    public void setObjectArrayValue(Object[] newObjectArray) {
        oa = newObjectArray;
    }
    
    public String toString() 
    {
        String returnedString = null;
        switch (getStoredType())
        {
            case STRING: // String
                return getStringValue();
            case BOOLEAN: // boolean
                return Boolean.toString(getBooleanValue());
            case DOUBLE: // double
                return Double.toString(getDoubleValue());
            case DOUBLE_ARRAY: // double[]
                String array = "{";
                for(double val : da ) {
                    array += val + ", ";
                }
                // Oh no, we'll have a fence-post error. Let's remove the trailing comma and space.
                array = array.substring(0, array.length() - 2);
                array += "}";
                
                return array;
            case OBJECT_ARRAY: // Object[]
                String arr = "{";
                for(Object val : oa ) {
                    arr += val.toString() + ", ";
                }
                // Oh no, we'll have a fence-post error. Let's remove the trailing comma and space.
                arr = arr.substring(0, arr.length() - 2);
                arr += "}";
        }
        
        //null....
        return returnedString; 
    }
    
    public boolean isIdentifier() 
    { 
        return isIdentifier;
    }

    public void setIdentifier(boolean isIdentifier)
    {
        this.isIdentifier = isIdentifier;
    }
}

/*
 * If I ever support doubles, then let this be what is used to check...
 *
 * private static Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d*)?");
 * public boolean isDouble(String string) {
 *      return doublePattern.matcher(string).matches();
 * }
 */ 

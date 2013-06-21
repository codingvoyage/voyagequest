package scripting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the Script class, which holds individual scripts. It loads a given
 * script file and splits it into structures which enable easy access to the
 * parameters of the script.
 *
 * Some things which could use some work: It's currently quite strongly typed
 * so if two spaces are used instead of one, the whole thing fails. I am
 * considering later using the function Java provides for breaking String
 * objects into tokens. I wrote the current code keeping in mind that String
 * Parameters should not be separated by whitespace, but in hindsight it would
 * be simple to merge the tokens surrounded by quotation marks. I might do this
 * later but for now it works quite well, so I have no problems with the class
 * yet.
 *
 * @author Edmund
 */
public class Script {
    private ArrayList<Line> lineList;
    private HashMap<String, Integer> labelMap;
    private HashMap<String, Integer> commandDictionary;
    
    /** path to scripts folder */
    public final String SCRIPT_FOLDER = "scripting/scripts/";

    public Script(String filename, HashMap<String, Integer> commandDictionary)
    {
        lineList = new ArrayList<Line>();
        labelMap = new HashMap<String, Integer>();
        this.commandDictionary = commandDictionary;

        loadFile(filename);

    }

    public Line getLine(int index)
    {
        return lineList.get(index);
    }

    public int getLineCount()
    {
        return lineList.size();
    }
    /*
     * This is the part that loads the Script file, and then processes it.
     */
    private void loadFile(String filename)
    {
        //So like script.txt would end up being scripts/script.txt
        //which is where it should be...
        InputStream is = getClass().getClassLoader().getResourceAsStream(SCRIPT_FOLDER + filename);
        
        if (is == null)
            System.out.println("Script don't exist");
        else {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try (Scanner in = new Scanner(reader)) {

                while (in.hasNextLine())
                {
                    String myNextLine = in.nextLine();

                    System.out.println(myNextLine);

                    //Of course, if the next line contains nothing, then ignore
                    //Everything else that happens will happen if the line isn't ""
                    if (!myNextLine.equals("")) {

                        //Alright, first trim any whitespace at the ends
                        myNextLine = myNextLine.trim();

                        //The formatLine class will work its magic and take the
                        //line as a String and make a Line object with properly
                        //formatted Parameters
                        Line formattedLineObject = formatLine(myNextLine);

                        //If it's null, then that means it hit a comment and
                        //decided to ignore the line. Otherwise, maybe it screwed
                        //up. Then just ignore the line.
                        if (formattedLineObject != null)
                            //Add this to the master list of Lines
                            lineList.add(formattedLineObject);

                    }
                }


                //If we're done loading the lines, now we find the labels
                for (int i = 0; i < getLineCount(); i++)
                {
                    Line currentLine = lineList.get(i);

                    //If it's either a goto branch or a function branch...
                    if (currentLine.getCommandID() == 2 ||
                            currentLine.getCommandID() == 3)
                    {
                        //The key will be the name of the branch
                        String labelKey = currentLine.getStringParameter(0);

                        //The value is the line number
                        labelMap.put(labelKey, i);
                    }

                }
            }
        }

    }

    //This is the method which will work its magic and format the
    //line from String to proper Line object
    private Line formatLine(String unformattedLine)
    {
        //This is the actual Line object that we're going to construct, modify
        //and return. I'm not initializing it, because that should be done
        //in the following piece of code. If I messed up, then let the program
        //crash so that I will know.
        Line formattedLine = null;

        //currentIndex refers to the index of the currently examined
        //character on the unformattedLine String
        int currentIndex = 0;

        //These are temporary values which hold how far the Scripting Parameter
        //reaches, from the currentIndex, to whatever delimits the Parameter.
        int spaceIndex;
        int doubleQuoteIndex;

        //currentToken is the temporary variable for the short pieces which we
        //have extracted.
        String currentToken = "";

        //So basically, the first token on the line will be the command.
        //This should be promptly converted to true after that happens.
        boolean doneWithCommandID = false;

        //Once areWeDone evaluates to true, we're done with the line.
        //This will happen differently depending on the final Parameter.
        boolean areWeDone = false;
        while (!areWeDone)
        {

            //First of all, find the character at currentIndex
            char currentChar = unformattedLine.charAt(currentIndex);

            //Now, we ask ourselves whether this is a quotation mark...
            if (currentChar == '"')
            {
                //Basically, if it's starting with a quotation mark, then we're
                //dealing with a String parameter. First of all, we'll find the
                //boundaries of the String variable in the script by using
                //indexOf to find the next time a '"' appears in the line,
                //starting from currentIndex + 1. A good 15 minute debugging
                //session revealed to me that when it looks for the '"' starting
                //from currentIndex, if there is a '"' at currentIndex, that
                //ends up counting. Which is why it failed. But now if I make it
                //currentIndex + 1, then it's all good. Even if it were a
                //empty String "", that still makes sense.
                doubleQuoteIndex = unformattedLine.indexOf("\"",
                        currentIndex + 1);

                //Now, the token which we take is a substring from the current
                //Index plus one, to the quotationIndex
                currentToken = unformattedLine.substring(currentIndex + 1,
                        doubleQuoteIndex);

                //So now we must add currentToken into the Line object
                //No shenanigans necessary because we know for a fact that it's
                //a String - it was delimited by double quotes...
                formattedLine.addParameter(currentToken);

                //At this point, we would move currentIndex. However, we can't.
                //See, unlike the following code for non-String Parameters, we
                //don't have to check if there's a space coming up because a
                //String variable must be bounded by another double quote. But
                //nonetheless now we must see whether we're at the end of the
                //Line, or else we might have a nasty crash.
                if (doubleQuoteIndex == unformattedLine.length() - 1)
                {
                    //We're done. Don't bother moving the currentIndex
                    areWeDone = true;
                }
                else
                {
                    //Now, we move the currentIndex to the doubleQuoteIndex, and
                    //then increment it by two. It's first at the double quote.
                    //Moving it once moves it to the space. Moving it a second
                    //time arrives at the next parameter.
                    currentIndex = doubleQuoteIndex + 2;
                }

            }
            else
            //Alright, so it's NOT a quotation mark. It's a normal character,
            //such as 'a' or ']' or 'd'.
            {
                //So we want to find out where the next instance of a space is
                //starting from the currentIndex mark
                spaceIndex = unformattedLine.indexOf(" ", currentIndex);

                //So we know that if indexOf returns a -1, that means that it
                //couldn't find that next instance of a space. In that case,
                //that means this is the last parameter on the line.
                if (spaceIndex == -1)
                {
                    //Just take the substring from the currentIndex to the
                    //length of the entire String.
                    currentToken = unformattedLine.substring(currentIndex,
                            unformattedLine.length());

                    //So, we're done.
                    areWeDone = true;
                }
                else
                //In this case, spaceIndex was not -1, meaning that there was in
                //fact another space coming up.
                {
                    //Hence, take the substring from currentIndex to spaceIndex
                    currentToken = unformattedLine.substring(currentIndex,
                            spaceIndex);

                    //Since we're still going on, we need to adjust currentIndex
                    //accordingly. Must move currentIndex to spaceIndex + 1
                    currentIndex = spaceIndex + 1;
                }

                //BUT WAIT. LET'S NOT FORGET ABOUT THE currentToken.
                //Unfortunately, the process will be slightly more complicated.

                //First off, if the doneWithCommandID is false, then this
                //MUST BE the commandID UNLESS IT IS '--' WHICH DENOTES THAT
                //IT IS A COMMENT, for which we move on to the next line
                //immediately.
                if (!doneWithCommandID)
                {
                    //IS IT NOT A COMMENT??
                    if (!currentToken.equals("--"))
                    {
                        formattedLine = new Line();
                        formattedLine.setCommandID(findCommandID(currentToken));
                        doneWithCommandID = true;
                    }
                    else //So it was a comment!
                    {
                        areWeDone = true;
                        //What happens now is that the formatLine method will
                        //return a null, and the code will see that if it's a
                        //null, it just moves on. Hence, comments are ignored.
                    }
                }
                else
                //Otherwise, yeah now we have to determine whether this is a
                //boolean, an int, or a double. Simple. We see whether it's
                //"true" or "false" at first. If not, fails the boolean test.
                //Then we try to convert it to a double. If that works, then
                //great. Otherwise, try an int. If it finally fails again, then
                //it must be a String (!!)
                {
                    //Going to create a separate method to return what kind
                    //this is, or else this gets ugly fast
                    int whatKind = getCurrentType(currentToken);

                    //Then we respond based on whatKind
                    switch (whatKind)
                    {
                        case 0: //false
                            formattedLine.addParameter(false);
                            break;
                        case 1: //true
                            formattedLine.addParameter(true);
                            break;
                        case 2: //double
                            formattedLine.addParameter(
                                    Double.parseDouble(currentToken));
                            break;
                        case 3: //An identifier
                            formattedLine.addIdentifierParameter(currentToken);
                            break;
                    }

                //This is the end bracket for the part which determined
                //the type of currentToken. That lets us pass it to Line
                //in an appropriate fashion.
                }

            //This is the end bracket for the ENTIRE part which acts whenever
            //the currently indexed character is or is NOT a quotation mark
            }

        //This is the end bracket for the entire while loop. Basically, it
        //hits the end of the while loop with each token that gets extracted
        }

        //If it's still null, something's really wrong
        //assert (formattedLine != null);
        //Actually if it's null, then it's a comment!
        return formattedLine;
    }

    public int findCommandID(String commandIDName)
    {
        commandIDName = commandIDName.toLowerCase();
        Integer commandID = commandDictionary.get(commandIDName);
        if (commandID != null)
        {
            return commandID.intValue();
        }
        else
        {
            //A commandID number of 1337. My way of communicating that we've failed
            //to find a match...
            return 1337;
        }

    }

    //I realized that it would be super-unwieldy to place the code which
    //checks what type the token is in the loop, so I made it a private
    //function. Another neat thing is that the fact it's a function means
    //we have a sort of an early-out mechanism when determining what kind of
    //token it is. For example if it's "true", a 1 gets returned immediately.
    private int getCurrentType(String token)
    {
        //Alright, first we attempt to see if it's "true" or "false"
        if (token.equals("true"))
            return 1;
        else if (token.equals("false"))
            return 0;

        //If we're still here, let's try to convert it to a double...
        try
        {
            //Then it worked. It's a double!
            double dummyVal = Double.parseDouble(token);
            return 2;
        }
        catch(NumberFormatException e) {}

        //It's an identifier instead
        return 3;

    }

    public int getLabelIndexOnLineList(String labelName)
    {
        return labelMap.get(labelName);
    }

    public Line getLineAtLabel(String labelName)
    {
        return lineList.get(getLabelIndexOnLineList(labelName));
    }


}
package scripting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Edmund
 */
public class ScriptManager {
    //An array of all the Script objects
    private Script[] scriptCollection;
    public final int SCRIPT_CAPACITY = 100;

    //A hashmap of all the Script objects
    private HashMap<String, Script> scriptHash;
    
    //A HashMap which acts as a dictionary which ties command names with
    //their corresponding integer IDs
    public final HashMap<String, Integer> commandIDDictionary;

    /** path to scripts folder */
    public final String SCRIPT_FOLDER = "scripting/scripts/";
    
    public ScriptManager() 
    {
        //Let's make space for scripts
        scriptCollection = new Script[SCRIPT_CAPACITY];
        scriptHash = new HashMap<>();
        
        //Let's set each object to null
        for (int i = 0; i < SCRIPT_CAPACITY; i++) 
        {
            scriptCollection[i] = null;
        }
        
        //Now based on our definition file, load the IDDictionary
        commandIDDictionary = createIDDictionary("DICTIONARY.txt");

        loadAllScripts();
    }
    
    private HashMap<String, Integer> createIDDictionary(String dictionaryFilename) 
    {
        //Create a HashMap which holds function and commandID pairs
        HashMap<String, Integer> newDictionary = new HashMap<String, Integer>();

        InputStream is = getClass().getClassLoader().getResourceAsStream(SCRIPT_FOLDER + dictionaryFilename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try (Scanner in = new Scanner(reader)) {
            while (in.hasNextLine())
            {
                String myNextLine = in.nextLine();

                //Ignore if the next Line is a comment (--) or if it is blank
                if (!(myNextLine.equals("")||(myNextLine.substring(0,2).equals("--"))))
                {
                    //Get the name of the command
                    int indexOfSpace = myNextLine.indexOf(" ");
                    String commandNameKey = myNextLine.substring(0, indexOfSpace);

                    //Except we need to be not-case-specific, so let's make it
                    //lowercase!
                    commandNameKey = commandNameKey.toLowerCase();

                    //Find out the number, basically from ahead of the space 
                    //one step until the length of the line should be the number
                    String commandIDNumber = myNextLine.substring(indexOfSpace + 1,
                            myNextLine.length());

                    //Now I need the number in integer form
                    int commandIDInteger = Integer.parseInt(commandIDNumber);

                    //Now I can stick this in the HashMap!
                    newDictionary.put(commandNameKey, commandIDInteger);
                }
            }
        }
        
        return newDictionary;
    }
    
    public HashMap<String, Integer> getCommandIDDictionary()
    {
        return commandIDDictionary;
    }


    /**
     * Loads all the scripts by combing through the SCRIPT_FOLDER directory.
     * Works for both programming in an IDE environment, as well as when the JAR file
     * is executed because there is code for unzipping Jars, and code for exploring
     * file directories normally. Someday, this code will likely be redone to make it
     * more efficient/clean, but for now, this works.
     */
    public void loadAllScripts()
    {
        //The final list of Scripts, we need to find this by going through the SCRIPT_FOLDER
        ArrayList<String> finalScriptPathList = new ArrayList<>();

        //This is the location of our program, whether JAR or in the IDE
        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        String programPath = src.getLocation().getPath();


        try
        {
            //IF WE'RE RUNNING FROM A JAR FILE...
            if (src.getLocation().getPath().endsWith(".jar") && src != null ) {

                //Creates a ZipInputStream of this JAR file
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream( jar.openStream());

                //Goes through every file. This is probably not the most efficient. Sorry.
                ZipEntry ze = null;
                while( ( ze = zip.getNextEntry() ) != null ) {

                    //Ignoring directories, see if any files are in the script directory
                    if (!ze.isDirectory())
                    {
                        String entryName = ze.getName();
                        if (entryName.contains(SCRIPT_FOLDER))
                            finalScriptPathList.add(entryName.substring(SCRIPT_FOLDER.length()));
                    }
                }
            }
            else
            {
                //NOT RUNNING FROM JAR FILE - BUT FROM AN IDE ENVIRONMENT
                //Use the exploreDirectory method I wrote to get all the files in the script folder
                File scriptDirectory = new File(programPath + SCRIPT_FOLDER);
                ArrayList<File> scriptFiles = this.exploreDirectory(scriptDirectory);

                for (File file: scriptFiles)
                {
                    //You have something long
                    //Something like C:\Users\Edmund\...VoyageQuest\scripting\scripts\SCENE1SCRIPTS\SCENE1
                    //Trim so that only SCENE1SCRIPTS\SCENE1 remains
                    String resultingPath = file.getPath().substring(scriptDirectory.getPath().length() + 1);

                    //If the operating system decides to use \ symbols instead of / for whatever
                    //reason, fix that.
                    resultingPath.replace("\\", "/");
                    finalScriptPathList.add(resultingPath);
                }

            }
        }
        catch (Exception e) {}

        //At this point, all of the script paths should have been put in finalScriptPathList.
        //Now, load them all.
        for (String scriptPath: finalScriptPathList)
            loadScript(scriptPath);


    }


    /**
     * I got tired of looking for code to explore a directory, so I wrote my own which works
     * recursively. It returns all the Files in the directory
     * @param origin The path of the directory to explore
     * @return an ArrayList of File objects within the directory. No subdirectories are included.
     */
    public ArrayList<File> exploreDirectory(File origin)
    {
        //Base case: does this directory no longer contain directories?
        ArrayList<File> foundContents = new ArrayList<>();

        File[] thisDirectoryContents = origin.listFiles();
        for (File f : thisDirectoryContents)
        {
            //For each directory, expand recursively
            if (f.isDirectory())
                for (File eachItem : exploreDirectory(f)) foundContents.add(eachItem);
            else
                //If not a directory, just add the file
                foundContents.add(f);
        }

        return foundContents;
    }


    public boolean loadScript(String filename)
    {
        int lastDashIndex = -1;
        String scriptID = "";
        //What we do depends on whether it's \\ or /
        if (filename.contains("\\")  )
        {
            //The ID is the name of the script file.
            lastDashIndex = filename.lastIndexOf("\\");

        }
        else if (filename.contains("/"))
        {
            lastDashIndex = filename.lastIndexOf("/");
        }

        //Take a substring starting from the dash index, plus one.
        //If dash index was not found, then it should be -1 by default.
        //But notice how -1 + 1 here is 0, so substring(0) returns itself anyway.
        scriptID = filename.substring(lastDashIndex + 1);

        //Debug info
        System.out.println(filename + " has been loaded as " + scriptID);

        //Return false if it already exists or the ID somehow turned out to be blank
        if (scriptHash.containsKey(scriptID) || scriptID.equals("")) return false;

        //From this point on, we're loading the script...
        Script loadedScript = new Script(filename, commandIDDictionary);
        scriptHash.put(scriptID, loadedScript);

        return true;
    }

    public boolean loadScript(String filename, int indexID) 
    {
        //We only want this function to work when there was no existing
        //script on the provided indexID. It should be null, in that case.
        if (scriptCollection[indexID] == null)
        {
            scriptCollection[indexID] = new Script(filename, commandIDDictionary);
            return true;
        }
        
        //If there was one already, the user should have used the changeScript
        //function, so this one fails.
        return false;
    }
    
    
    
    public Script getScriptAtID(int scriptIDNumber) 
    {
        //Refers to the array, find the one with scriptIDnumber
        return scriptCollection[scriptIDNumber];
    }

    public Script getScriptAtID(String scriptID)
    {
        return scriptHash.get(scriptID);
    }
    
}

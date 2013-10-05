package voyagequest;

import org.newdawn.slick.UnicodeFont;

import java.awt.*;
import java.io.InputStream;

/**
 * A utility class of random useful functions and variables
 * 
 * @author Edmund Qiu
 * @author Brian Yang
 */
public final class Util {
    
    /** Font to be used throughout */
    public static final UnicodeFont WHITE_FONT;
    static {
        UnicodeFont newFont;
        try {
            InputStream is = Util.class.getClassLoader().getResourceAsStream("voyagequest/Roboto-Regular.ttf");
            Font droid = Font.createFont(Font.TRUETYPE_FONT, is);
            newFont = new UnicodeFont(droid, 24, false, false);
            newFont.addAsciiGlyphs();
            newFont.getEffects().add(new org.newdawn.slick.font.effects.ColorEffect(new java.awt.Color(218, 249, 248)));
            newFont.loadGlyphs(); 
        } catch (Exception e) {
            newFont = new UnicodeFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 24));
        }
        WHITE_FONT = newFont;
    }

    /** Font to be used throughout */
    public static final UnicodeFont FONT;
    static {
        UnicodeFont newFont;
        try {
            InputStream is = Util.class.getClassLoader().getResourceAsStream("voyagequest/Roboto-Regular.ttf");
            Font droid = Font.createFont(Font.TRUETYPE_FONT, is);
            newFont = new UnicodeFont(droid, 24, false, false);
            newFont.addAsciiGlyphs();
            newFont.getEffects().add(new org.newdawn.slick.font.effects.ColorEffect(new java.awt.Color(89, 89, 89)));
            newFont.loadGlyphs();
        } catch (Exception e) {
            newFont = new UnicodeFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 24));
        }
        FONT = newFont;
    }
    
    /**
     * Given an integer bound, returns a random integer from
     * [min, max], so it's inclusive of min and max.
     * 
     * @param min minimum bound for random number
     * @param max maximum bound for random number
     * @return the random number
     */
    public static int rand(int min, int max)
    {
        int randomNumber = min + (int)(Math.random() * ((max - min + 1)));
        return randomNumber;
    }
    
    public static float rand(float min, float max)
    {
        float randomNumber = min + (float)(Math.random() * ((max - min + 1)));
        return randomNumber;
    }
    
    
    public static double rand(double min, double max)
    {
        double randomNumber = min + (double)(Math.random() * ((max - min + 1)));
        return randomNumber;
    }
    
    /**
     * 
     * @param xCoord 
     * @param yCoord
     * @return a DoubleRect in which x and y are the tile coordinates.
     */
    public static DoubleRect coordinateToTile(double xCoord, double yCoord)
    {
        int xMapCoord = (int)(xCoord / Global.currentMap.TILE_LENGTH);
        int yMapCoord = (int)(yCoord/ Global.currentMap.TILE_LENGTH);
        
        return new DoubleRect(xMapCoord, yMapCoord, 0, 0);
    }
    
    
    /**
     * Nobody likes typing out System.out.println();
     * @param s the String to print 
     */
    public static void p(String s)
    {
        System.out.println(s);
    }
    
    /**
     * Nobody likes typing out System.out.println();
     * @param i the integer to print
     */
    public static void p(int i)
    {
        System.out.println(i);
    }
    
    /**
     * Nobody likes typing out System.out.println();
     * @param f the float to print
     */
    public static void p(float f)
    {
        System.out.println(f);
    }
    
    
    /**
     * Nobody likes typing out System.out.println();
     * @param b the boolean value to print 
     */
    public static void p(boolean b)
    {
        System.out.println(b);
    }
}

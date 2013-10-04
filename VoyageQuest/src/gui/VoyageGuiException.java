package gui;

/**
 * Something went wrong with the Gui
 *
 * @author Brian Yang
 * @version 2.0
 */
public class VoyageGuiException extends Exception {

    /**
     * Throw a new exception
     */
    public VoyageGuiException() {}

    /**
     * Throw a new exception
     * @param msg the message to print
     */
    public VoyageGuiException(String msg) {
        super("**VoyageGuiException**: " + msg);
    }

}


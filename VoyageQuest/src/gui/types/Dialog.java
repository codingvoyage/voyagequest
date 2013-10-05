package gui.types;

import gui.Displayable;
import gui.Gui;
import gui.GuiManager;
import gui.VoyageGuiException;
import map.Entity;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.UnicodeFont;
import voyagequest.Res;
import voyagequest.Util;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Brian Yang
 * @version 1.0
 */
public class Dialog implements Displayable {

    /** The String to be printed */
    private String text;
    /** x coordinate */
    private float x;
    /** y coordinate */
    private float y;
    /** Is the dialog being printed? */
    private boolean isPrinting;
    /** width */
    private int width;
    /** height */
    private int height;
    /** the dialog parser */
    private DialogParser parser;
    /** the current Gui window */
    private Gui<Dialog> window;
    /** speaker */
    private Entity speaker;
    /** options */
    private String[] options;

    /** Profile animation (optional) */
    public String animationId;

    /** Font */
    public static final UnicodeFont FONT = Util.FONT;


    /** dialog box offset */
    public static final float DIALOG_PADDING = 25.0f;

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param text
     */
    public Dialog(float x, float y, String text, int width, int height) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.width = width;
        this.height = height;
        parser = new DialogParser(text);
    }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param text
     */
    public Dialog(float x, float y, String text, int width, int height, String animationId) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.width = width;
        this.height = height;
        parser = new DialogParser(text, animationId);
        this.animationId = animationId;
    }

    /**
     *
     * @param x
     * @param y
     * @param text
     * @param width
     * @param height
     * @param options
     */
    public Dialog(float x, float y, String text, int width, int height, String[] options) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.width = width;
        this.height = height;
        parser = new DialogParser(text, options);
        this.options = options;
    }

    /**
     *
     * @param x
     * @param y
     * @param text
     * @param width
     * @param height
     * @param animationId
     * @param options
     */
    public Dialog(float x, float y, String text, int width, int height, String animationId, String[] options) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.width = width;
        this.height = height;
        parser = new DialogParser(text, animationId, options);
        this.animationId = animationId;
        this.options = options;
    }

    /**
     * Start the dialog box
     */
    public void start() {
        window.start();
    }

    /**
     * Draw the window
     */
    @Override
    public void draw() throws VoyageGuiException {
        parser.drawNext();
    }

    /**
     *
     * @param gc
     * @param delta
     */
    public void next(GameContainer gc, int delta) {
        parser.update(gc, delta);
    }

    /**
     * Get Gui element
     * @return the gui window
     */
    public Gui<Dialog> getGui() {
        return window;
    }

    /**
     * Get the dialog object
     * @return the dialog object
     */
    public Dialog getDialog() {
        return window.getObject();
    }

    /**
     * Get the text
     * @return the dialog text
     */
    public String getText() {
        return window.getObject().getText();
    }

    public DialogParser getParser() {
        return parser;
    }

    /**
     * Set the window this dialog is contained in
     * Used only for closing the dialog box from the parser
     * @param window the super window
     */
    public void setWindow(Gui<Dialog> window) {
        this.window = window;
    }

    public int getParserOption() {
        return parser.getOption();
    }

    public boolean getParserStatus() {
        return parser.getStatus();
    }

    /**
     * Parses the dialog text and displays them in a
     * typewriter-like style. Also processes dialog option prompts
     */
    private class DialogParser {

        /** Elapsed time */
        private int time;

        /** Linked list of characters to be printed */
        private LinkedList<LinkedList<String>> chars;
        /** Iterator for outer list to be printed */
        private ListIterator<LinkedList<String>> wordIterator;
        /** Iterator for inner list to be printed */
        private ListIterator<String> charIterator;

        /** Linked list of printed characters mapped to their coordinates to avoid repetitive calculations */
        private LinkedList<Coordinate> printedChars;

        /** Begin a new word */
        private boolean newWord;

        /** x coordinate of current character */
        private float x;
        /** y coordinate of current character */
        private float y;

        /** left margin */
        private float xStart;
        /** top margin */
        private float yStart;
        /** width of the text area */
        private int totalWidth;
        /** height of the text area */
        private int totalHeight;

        /** speed of text printing in milliseconds */
        public static final int PRINT_SPEED = 20;

        /** waiting for user */
        private boolean waiting = false;

        /** blink continue cursor */
        private boolean blink = true;
        /** blink speed */
        public int blinkTimer = 0;

        /** status */
        private boolean continuePrinting = true;

        /** profile animation */
        private Animation profile;
        private boolean profileLeft;
        private String name;

        /** options */
        private String[] options;
        /** options coordinates */
        private LinkedList<Coordinate> optionCoordinates = new LinkedList<>();
        /** option box rectangle */
        private Gui<Menu> menu;
        /** option box object */
        private Menu menuObject;
        /** selected option index */
        private int selected = -1;
        /** are we printing? */
        private boolean optionsDisplaying;

        /**
         * Create a dialog parser instance
         * @param text the text to be printed
         */
        public DialogParser(String text) {
            this.x = Dialog.this.x;
            this.y = Dialog.this.y;

            // Split the text up into different words
            String[] words = text.split(" ");

            xStart = x + DIALOG_PADDING;
            yStart = y + DIALOG_PADDING;

            this.x += DIALOG_PADDING;
            this.y += DIALOG_PADDING;
            totalWidth = Dialog.this.width + (int)xStart - (int)DIALOG_PADDING * 3;
            totalHeight = Dialog.this.height + (int)yStart - (int)DIALOG_PADDING * 3;

            chars = new LinkedList<>();

            newWord = true;

            boolean first = true;
            // For each word, split into characters
            for (String s : words) {
                char[] tempChars = s.toCharArray();

                // For each character array, make a new linked list
                LinkedList<String> charList = new LinkedList<>();
                for (char c : tempChars) {
                    charList.add(Character.toString(c));
                }
                // Add a space after each word
                charList.add(" ");
                // Add this character list to the outer list of words
                chars.add(charList);
                // Start the character iterator with the first word
                if (first) {
                    charIterator = charList.listIterator();
                    first = false;
                }
            }

            wordIterator = chars.listIterator();
            if (wordIterator.hasNext())
                wordIterator.next();
            printedChars = new LinkedList<>();
        }


        /**
         * Print a new dialog message
         * @param text the text to print
         * @param animationId name of profile animation
         */
        public DialogParser(String text, String animationId) {
            this(text);

            profile = Res.animations.get(animationId);
            // profileLeft = box.getSpeaker().profLeft;
            int namePart = animationId.indexOf(" Profile");
            name = animationId.substring(0, namePart);
        }

        /**
         * Print a new dialog message
         * @param text the text to print
         * @param animationId name of profile animation
         * @param options dialog options
         */
        public DialogParser(String text, String animationId, String[] options) {
            this(text, animationId);
            initOptions(options);
        }

        /**
         * Print a new dialog message
         * @param text the text to print
         * @param options dialog options
         */
        public DialogParser(String text, String[] options) {
            this(text);
            initOptions(options);
        }

        /**
         * Constructor helper method for setting up the options menu
         * @param options An array of options (String)
         */
        private void initOptions(String[] options) {
            this.options = options;
            float optionsX = xStart;
            float optionsY = y - (FONT.getLineHeight() * options.length) - 50;
            float optionsYStart = optionsY;
            int maxWidth = 0;
            Util.p("Printing options at " + optionsX + ", " + optionsY);
            for (String s : this.options) {
                optionCoordinates.add(new Coordinate(s, optionsX, optionsY));
                if (FONT.getWidth(s) > maxWidth)
                    maxWidth = FONT.getWidth(s);
                optionsY += FONT.getLineHeight();
            }
            maxWidth += DIALOG_PADDING * 2;
            optionsY += DIALOG_PADDING;
            menu = new Gui<>(optionsX, optionsYStart, maxWidth, (int)(optionsY - optionsYStart),
                    new Menu(optionsX, optionsYStart, (int)maxWidth, (int)optionsY, this.options));
            menu.setColor(gui.special.DialogBox.DEFAULT_COLOR_START, gui.special.DialogBox.DEFAULT_COLOR_END);
            menuObject = menu.getObject();
        }

        /**
         * Update the time
         * @param delta the time interval
         */
        public void update(GameContainer gc, int delta) {
            time += delta;

            if (waiting) {
                Input input = gc.getInput();
                if (!hasNext()) {
                    if (options != null) {
                        optionsDisplaying = true;
                        menu.next(gc, delta);
                    }
                }
                if (input.isKeyDown(Input.KEY_E)) {

                    if (hasNext()) {

                        waiting = false;
                        printedChars.clear();
                        x = xStart;
                        y = yStart;

                    } else {
                        // dialog is done printing, but are there options to display?
                        if ((options != null && menu.getObject().isDone()) || options == null) {
                            // the box has no more to print. close it.
                            GuiManager.close(Dialog.this.window);
                            continuePrinting = false;
                            return;
                        }
                    }
                }
            }

            blinkTimer += delta;

            if (blinkTimer >= 400) {
                blink = !blink;
                blinkTimer = 0;
            }
        }

        /**
         * Check if there is more to print
         * @return boolean indicating whether or not there is more to print
         */
        public boolean hasNext() {
            return charIterator.hasNext() || wordIterator.hasNext();
        }

        /**
         * Get the next character
         * @return the next character
         */
        public String next() {
            if (charIterator.hasNext()) {
                return charIterator.next();
            } else if (wordIterator.hasNext()) {
                charIterator = wordIterator.next().listIterator();
                newWord = true;
                return next();
            } else {
                waiting = true;
                return "";
            }
        }

        /**
         * Processes and prints the dialog box
         */
        public void drawNext() throws VoyageGuiException {

            if (profile != null)
                drawProfile();
            if (optionsDisplaying) {
                menu.draw();
            }
            printPrevious();
            if (!waiting) {
                if (time >= PRINT_SPEED) {
                    String next = next();
                    time = 0;

                    if (newWord) {
                        String currentWord = "";

                        wordIterator.previous();
                        ListIterator<String> tempWord = wordIterator.next().listIterator();
                        while (tempWord.hasNext()) {
                            currentWord += tempWord.next();
                        }

                        int width = FONT.getWidth(currentWord);

                        if (x + width > totalWidth) {
                            x = xStart;
                            y += FONT.getLineHeight();
                            if (y > totalHeight) {
                                waiting = true;
                                charIterator.previous();
                                return;
                            }
                        }

                        newWord = false;

                    }

                    FONT.drawString(x, y, next);
                    printedChars.add(new Coordinate<>(next, x, y));
                    x += FONT.getWidth(next);

                }
            } else {
                if (blink)
                    FONT.drawString(xStart + Dialog.this.width, yStart + Dialog.this.height, "Press E");
            }

        }

        /**
         * Get status
         * @return whether or not it is continuing to print
         */
        public boolean continuePrinting() {
            return Dialog.this.getParser().getStatus();
        }

        /**
         * Render what is already printed
         */
        private void printPrevious() {

            ListIterator<Coordinate> print = printedChars.listIterator();
            while (print.hasNext()) {
                Coordinate<String> next = print.next();
                FONT.drawString(next.getPosition()[0], next.getPosition()[1], next.getObject());
            }

        }

        /**
         * Draw profile
         */
        private void drawProfile() {
            profile.setSpeed((float)Math.random() * 4);
            if (profileLeft) {
                profile.draw(xStart, yStart - 530);
                Util.WHITE_FONT.drawString(xStart, yStart - 256, name);
            } else {
                profile.draw(totalWidth - 290, yStart - 525);
                Util.WHITE_FONT.drawString(totalWidth - 275, yStart - 256, name);
            }

        }

        /**
         * Get the status
         * @return whether or not it should continue printing
         */
        public boolean getStatus() {
            return continuePrinting;
        }

        /**
         * Get selected option index
         * @return the index of the selected option or -1 if none has been selected yet
         */
        public int getOption() {
            return menuObject.getOption();
        }

    }
}

package voyagequest.special;

/**
 * A special class used simply so Json can load animation data
 * This data is then processed and passed on to Slick2D's animation class
 * @author Brian Yang
 */
public class LoadAudio {

    private String name;
    private String path;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}

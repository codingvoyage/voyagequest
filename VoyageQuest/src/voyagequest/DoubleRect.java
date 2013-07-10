package voyagequest;

public class DoubleRect
{
    public double x;
    public double y;
    public double width;
    public double height;
    
    public DoubleRect() {
        this(0, 0, 0, 0);
    }
    
    public DoubleRect(DoubleRect r) {
        this(r.x, r.y, r.width, r.height);
    }

    public DoubleRect(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public DoubleRect(int width, int height) {
        this(0, 0, width, height);
    }

    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
    
    public boolean contains(double x, double y) {
        return contains(x, y, 0, 0);
    }

    public boolean contains(int x, int y) {
        return contains(x, y, 0, 0);
    }

    public boolean contains(java.awt.Rectangle r) {
        return contains(r.x, r.y, r.width, r.height);
    }
    
    public boolean contains(voyagequest.DoubleRect r) {
        return contains(r.x, r.y, r.width, r.height);
    }

    //I think I'll write my own version of contains...
    public boolean contains(double x, double y, double w, double h) {
        if (( this.x <= x ) && (x + w <= this.x + this.width ) &&
            ( this.y <= y ) && (y + h <= this.y + this.height ))
            return true;
        else
            return false;
    }

    //Courtesy of Java.awt.Rectangle
    public boolean intersects(DoubleRect r) {
        double otherX = r.x;
        double otherY = r.y;
        double otherW = r.width;
        double otherH = r.height;
        
        if (this.x + this.width <= otherX || 
            this.y + this.height <= otherY || 
            this.x >= otherX + otherW || 
            this.y >= otherY + otherH) 
            return false;
        else
            return true;
    }

    public boolean intersects(double x, double y, double width, double height) {
        double otherX = x;
        double otherY = y;
        double otherW = width;
        double otherH = height;

        if (this.x + this.width <= otherX ||
                this.y + this.height <= otherY ||
                this.x >= otherX + otherW ||
                this.y >= otherY + otherH)
            return false;
        else
            return true;
    }

    //Courtesy of Java.awt.Rectangle
    public boolean equals(Object obj) {
        if (obj instanceof java.awt.Rectangle) {
            java.awt.Rectangle r = (java.awt.Rectangle)obj;
            return ((x == r.x) &&
                    (y == r.y) &&
                    (width == r.width) &&
                    (height == r.height));
        }
        return super.equals(obj);
    }

    //Courtesy of Java.awt.Rectangle
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }
}

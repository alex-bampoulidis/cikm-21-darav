package datatypes;

public class Textual {

    private String x[];
    private String y[];
    private double z[][];

    public Textual(String[] x, String[] y, double[][] z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String[] getX() {
        return x;
    }

    public void setX(String[] x) {
        this.x = x;
    }

    public String[] getY() {
        return y;
    }

    public void setY(String[] y) {
        this.y = y;
    }

    public double[][] getZ() {
        return z;
    }

    public void setZ(double[][] z) {
        this.z = z;
    }

}

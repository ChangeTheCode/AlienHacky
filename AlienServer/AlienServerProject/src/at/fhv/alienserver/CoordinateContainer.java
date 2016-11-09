package at.fhv.alienserver;

public class CoordinateContainer {
    public double x;
    public double y;
    public double z;

    public CoordinateContainer(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public CoordinateContainer(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public CoordinateContainer(CoordinateContainer source){
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }
}

package at.fhv.alienserver.Common;

public class AccelerationContainer {
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    private double x;
    private double y;
    //public double z;

    public AccelerationContainer(){
        this.x = 0;
        this.y = 0;
        //this.z = 0;
    }

    public AccelerationContainer(double x, double y){
        this.x = x;
        this.y = y;
    }

    public AccelerationContainer(AccelerationContainer source){
        this.x = source.x;
        this.y = source.y;
        //this.z = source.z;
    }

    @Override
    public String toString(){
        return String.valueOf(this.x) + '|' + String.valueOf(this.y);
    }
}

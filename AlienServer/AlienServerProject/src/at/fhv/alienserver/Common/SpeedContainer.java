package at.fhv.alienserver.common;

public class SpeedContainer{
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

    //public double z;
    private double x;
    private double y;

    public SpeedContainer(){
        this.x = 0;
        this.y = 0;
        //this.z =0 ;
    }

    public SpeedContainer(double x, double y){
        this.x = x;
        this.y = y;
    }

    public SpeedContainer(SpeedContainer source){
        this.x = source.x;
        this.y = source.y;
        //this.z = source.z;
    }

    @Override
    public String toString(){
        return String.valueOf(this.x) + '|' + String.valueOf(this.y);
    }
}

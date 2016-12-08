package at.fhv.alienserver.Common;

public class SpeedContainer{
    public double x;
    public double y;
    //public double z;

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

package at.fhv.alienserver;

public class SpeedContainer{
    public double x;
    public double y;
    //public double z;

    public SpeedContainer(){
        this.x = 0;
        this.y = 0;
        //this.z =0 ;
    }

    public SpeedContainer(SpeedContainer source){
        this.x = source.x;
        this.y = source.y;
        //this.z = source.z;
    }
}

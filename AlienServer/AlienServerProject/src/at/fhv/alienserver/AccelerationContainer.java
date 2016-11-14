package at.fhv.alienserver;

public class AccelerationContainer {
    public double x;
    public double y;
    //public double z;

    public AccelerationContainer(){
        this.x = 0;
        this.y = 0;
        //this.z = 0;
    }

    public AccelerationContainer(AccelerationContainer source){
        this.x = source.x;
        this.y = source.y;
        //this.z = source.z;
    }
}

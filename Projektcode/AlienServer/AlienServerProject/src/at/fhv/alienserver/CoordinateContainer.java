package at.fhv.alienserver;

public class CoordinateContainer {
    public double x;
    public double y;
    //public double z;

    public CoordinateContainer(){
        this.x = 0;
        this.y = 0;
        //this.z = 0;
    }

    public CoordinateContainer(double x, double y){
        this.x = x;
        this.y = y;
        //this.z = z;
    }

    public CoordinateContainer(CoordinateContainer source){
        this.x = source.x;
        this.y = source.y;
        //this.z = source.z;
    }

    @Override
    public String toString(){
        return String.valueOf(this.x) + '|' + String.valueOf(this.y);
    }

    public CoordinateContainer fromString(String input){
        int pipeIndex = input.indexOf('|');
        double x = Double.parseDouble(input.substring(0, pipeIndex - 1));
        double y = Double.parseDouble(input.substring(pipeIndex + 1, input.length() - 1));
        return new CoordinateContainer(x, y);
    }
}

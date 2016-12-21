package at.fhv.alienserver.common;



public class CoordinateContainer {
    public double getX() {
        return this.x;
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

    public CoordinateName get_name() {
        return _name;
    }

    private CoordinateName _name ;

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

    public CoordinateContainer(double x, double y, CoordinateName name){
        this.x = x;
        this.y = y;
        //this.z = z;
        this._name = name;
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

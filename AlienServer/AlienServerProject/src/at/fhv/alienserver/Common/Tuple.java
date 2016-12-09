package at.fhv.alienserver.Common;

/**
 * Created by thomas on 14.11.16.
 */
public class Tuple<TypeA, TypeB> {
    public TypeA getA() {
        return a;
    }

    public void setA(TypeA a) {
        this.a = a;
    }

    public TypeB getB() {
        return b;
    }

    public void setB(TypeB b) {
        this.b = b;
    }

    private TypeA a;
    private TypeB b;
    public Tuple(TypeA a, TypeB b){
        this.a = a;
        this.b = b;
    }
}

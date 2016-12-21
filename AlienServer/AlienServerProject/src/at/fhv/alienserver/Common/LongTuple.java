package at.fhv.alienserver.common;

/**
 * Created by thomas on 05.12.16.
 */
public class LongTuple<TypeA, TypeB, TypeC, TypeD> {
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

    public TypeC getC() {
        return c;
    }

    public void setC(TypeC c) {
        this.c = c;
    }

    public TypeD getD() {
        return d;
    }

    public void setD(TypeD d) {
        this.d = d;
    }

    private TypeA a;
    private TypeB b;
    private TypeC c;
    private TypeD d;

    public LongTuple(TypeA a, TypeB b, TypeC c, TypeD d){
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
}


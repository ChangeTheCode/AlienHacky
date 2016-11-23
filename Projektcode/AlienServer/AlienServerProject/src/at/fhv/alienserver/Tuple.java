package at.fhv.alienserver;

/**
 * Created by thomas on 14.11.16.
 */
public class Tuple<TypeA, TypeB> {
    public TypeA a;
    public TypeB b;
    public Tuple(TypeA a, TypeB b){
        this.a = a;
        this.b = b;
    }
}

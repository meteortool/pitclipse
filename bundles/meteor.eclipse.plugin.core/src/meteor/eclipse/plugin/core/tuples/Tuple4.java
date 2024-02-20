package meteor.eclipse.plugin.core.tuples;

public class Tuple4<A, B, C, D> {
	
    public final A first;
    public final B second;
    public final C third;
    public final D fourth;

    public Tuple4(A first, B second, C third, D fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }
}
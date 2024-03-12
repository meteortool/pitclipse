package meteor.eclipse.plugin.core.tuples;

public class Tuple3<A, B, C> {
	
    public final A first;
    public final B second;
    public final C third;

    public Tuple3(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
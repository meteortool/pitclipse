package meteor.eclipse.plugin.core.tuples;

public class Tuple2<A, B> {
	
    public final A first;
    public final B second;

    public Tuple2(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
import java.util.Objects;

public class Tuple<T, S> {
	
	private T first;
	private S second;
	
	public Tuple(T first, S second) {
		this.first = first;
		this.second = second;
	}
	
	public T getFirst() {
		return first;
	}
	
	public S getSecond() {
		return second;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tuple) {
			Tuple<T, S> other = (Tuple<T, S>)obj;
			if ((this.first == other.first)&&(this.second == other.second))
				return true;
		}
		return false;
	}
	
	//Take care of the hash because its going to be a key in a map
	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}

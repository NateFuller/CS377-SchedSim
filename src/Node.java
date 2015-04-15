public class Node<T> {

	public Node<T> next;
	public T data;

	public Node() {
		next = null;
		data = null;
	}

	public Node(T data) {
		next = null;
		this.data = data;
	}

}

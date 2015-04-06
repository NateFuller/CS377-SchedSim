public class Queue<T> {

	private Node<T> front;
	private Node<T> back;
	private int size;

	public Queue() {
		front = null;
		back = null;
		size = 0;
	}

	public boolean enqueue(T data) {
		Node<T> add = new Node<T>(data);

		if (isEmpty()) { // empty
			front = add;
			back = front;
			size++;
			return true;
		}

		back.next = add;
		back = add;
		size++;

		return true;
	}

	public T dequeue() {
		if (isEmpty()) {
			return null;
		}

		Node<T> tmp = front;
		T retVal = tmp.data;
		front = tmp.next;
		tmp = null;
		size--;

		return retVal;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public String toString() {
		Node<T> curr = new Node<T>();
		curr = front;
		String ret = "";

		while (curr != null) {
			ret += curr.data + " ";
			curr = curr.next;
		}

		return ret;
	}

}
package model;

public interface DataTable extends Table {
	public int capacity(); 

	//returns true if table is full
	public default boolean isFull() {
		return size() == capacity();
	}

	//ratio of size to capacity using floating point division avoiding integer division error
	public default double loadFactor() {
		return (double) size() / (double) capacity();
	}
}

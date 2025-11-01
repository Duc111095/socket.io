package com.ducnh.socket.io.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompositeIterable<T> implements Iterable<T> {

	private List<Iterable<T>> iterablesList;
	private Iterable<T>[] iterables;
	
	public CompositeIterable(List<Iterable<T>> iterables) {
		this.iterablesList = iterables;
	}
	
	public CompositeIterable(Iterable<T>... iterables) {
		this.iterables = iterables;
	}
	
	public CompositeIterable(CompositeIterable<T> iterable) {
		this.iterables = iterable.iterables;
		this.iterablesList = iterable.iterablesList;
	}
	
	@Override
	public Iterator<T> iterator() {
		List<Iterator<T>> iterators = new ArrayList<Iterator<T>>();
	
		if (iterables != null) {
			for (Iterable<T> iterable : iterables) {
				iterators.add(iterable.iterator());
			}
		} else {
			for (Iterable<T> iterable : iterablesList) {
				iterators.add(iterable.iterator());
			}
		}
		return new CompositeIterator<T>(iterators.iterator());
	}
}

package com.opal;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class OpalIterator<U extends UserFacing, O extends Opal<U>> implements Iterator<U> {
	final Iterator<O> myIterator;
	
	public OpalIterator(Iterator<O> argIterator) {
		super();
		myIterator = argIterator;
	}
	
	/**
	 * Returns <tt>true</tt> if the iteration has more elements. (In other
	 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
	 * rather than throwing an exception.)
	 *
	 * @return <tt>true</tt> if the iterator has more elements.
	 */
	@Override
	public boolean hasNext() {
		return myIterator.hasNext();
	}
	
	/**
	 * Returns the next element in the iteration.
	 *
	 * @return the next element in the iteration.
	 * @exception NoSuchElementException iteration has no more elements.
	 */
	@Override
	public U next() {
		return myIterator.next().getUserFacing();
	}
	
	/**
	 * 
	 * Removes from the underlying collection the last element returned by the
	 * iterator (optional operation).  This method can be called only once per
	 * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
	 * the underlying collection is modified while the iteration is in
	 * progress in any way other than by calling this method.
	 *
	 * @exception UnsupportedOperationException if the <tt>remove</tt>
	 *		  operation is not supported by this Iterator.
	
	 * @exception IllegalStateException if the <tt>next</tt> method has not
	 *		  yet been called, or the <tt>remove</tt> method has already
	 *		  been called after the last call to the <tt>next</tt>
	 *		  method.
	 */
	@Override
	public void remove() {
		myIterator.remove();
	}
}

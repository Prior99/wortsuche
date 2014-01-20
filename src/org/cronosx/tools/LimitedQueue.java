package org.cronosx.tools;

import java.util.concurrent.LinkedBlockingQueue;

public class LimitedQueue<T> extends LinkedBlockingQueue<T>
{
	private static final long	serialVersionUID	= -4577987576499278072L;
	private int size;
	public LimitedQueue(int size)
	{
		this.size = size;
	}
	
	@Override
	public boolean add(T elem)
	{
		super.add(elem);
		while (size() > size) 
		{
			super.remove();
		}
		return false;
	}
}

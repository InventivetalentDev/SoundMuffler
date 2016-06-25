package org.inventivetalent.soundmuffler;

public abstract class RunnableFutureResult<V> extends FutureResult<V> implements Runnable {

	@Override
	public void run() {
		set(evaluate());
	}

	public abstract V evaluate();

}

package org.inventivetalent.soundmuffler;

import java.util.concurrent.CountDownLatch;

// Based on http://stackoverflow.com/questions/3379787/make-asynchronous-queries-synchronous
public class FutureResult<V> {

	private volatile V result;
	private final CountDownLatch countDownLatch = new CountDownLatch(1);

	public V get() throws InterruptedException {
		countDownLatch.await();
		return result;
	}

	public void set(V result) {
		this.result = result;
		countDownLatch.countDown();
	}

}

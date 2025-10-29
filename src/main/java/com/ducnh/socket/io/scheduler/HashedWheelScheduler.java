package com.ducnh.socket.io.scheduler;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.internal.PlatformDependent;

public class HashedWheelScheduler implements CancelableScheduler{
	
	private final Map<SchedulerKey, Timeout> scheduledFutures = PlatformDependent.newConcurrentHashMap();
	private final HashedWheelTimer executorService;
	
	public HashedWheelScheduler() {
		this.executorService = new HashedWheelTimer();
	}	
	
	public HashedWheelScheduler(ThreadFactory threadFactory) {
		this.executorService = new HashedWheelTimer(threadFactory);
	}
	
	private ChannelHandlerContext ctx;
	
	@Override
	public void update(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void cancel(SchedulerKey key) {
		Timeout timeout = scheduledFutures.get(key);
		if (timeout != null) {
			timeout.cancel();
		}
	}

	@Override
	public void scheduleCallback(SchedulerKey key, Runnable runnable, long delay, TimeUnit unit) {
		Timeout timeout = executorService.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				ctx.executor().execute(new Runnable() {
					@Override
					public void run() {
						try {
							runnable.run();
						} finally {
							scheduledFutures.remove(key);
						}
					}
				});
			}
		}, delay, unit);
		if (!timeout.isExpired()) {
			scheduledFutures.put(key, timeout);
		}
	}

	@Override
	public void schedule(Runnable runnable, long delay, TimeUnit unit) {
		executorService.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				runnable.run();
			}
		}, delay, unit);
	}

	@Override
	public void schedule(SchedulerKey key, Runnable runnable, long delay, TimeUnit unit) {
		Timeout timeout = executorService.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				try {
					runnable.run();
				} finally {
					scheduledFutures.remove(key);
				}
			}
		}, delay, unit);
		
		if (!timeout.isExpired()) {
			scheduledFutures.put(key, timeout);
		}
	}

	@Override
	public void shutdown() {
		executorService.stop();
	}

}

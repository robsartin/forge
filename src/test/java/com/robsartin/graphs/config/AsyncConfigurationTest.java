package com.robsartin.graphs.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class AsyncConfigurationTest {

    private static final int MAX_POOL_PLUS_QUEUE = 4 + 50;

    @Test
    @DisplayName("should run overflow task on the caller instead of rejecting it when the metrics executor is saturated")
    void shouldRunTaskOnCallerWhenExecutorSaturated() throws Exception {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) new AsyncConfiguration().metricsTaskExecutor();
        CountDownLatch release = new CountDownLatch(1);
        try {
            for (int i = 0; i < MAX_POOL_PLUS_QUEUE; i++) {
                executor.execute(() -> awaitQuietly(release));
            }

            AtomicReference<Thread> ranOn = new AtomicReference<>();
            assertDoesNotThrow(() -> executor.execute(() -> ranOn.set(Thread.currentThread())));
            assertSame(Thread.currentThread(), ranOn.get());
        } finally {
            release.countDown();
            executor.shutdown();
        }
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

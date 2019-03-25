package org.symphonyoss.integration.healthcheck.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SchedulerConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class SchedulerConfigurationTest {

    @Autowired
    private Executor schedulerExecutor;

    @Test
    public void executerIsCreated() throws InterruptedException {
        final ThreadNameHolder threadName = new ThreadNameHolder();
        final Semaphore s = new Semaphore(1);
        s.acquireUninterruptibly();
        schedulerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                threadName.putName(Thread.currentThread().getName());
                s.release();
            }
        });

        s.tryAcquire(1, TimeUnit.SECONDS);
        s.release();

        assertEquals("scheduler-0", threadName.getName().get(0));
    }

    @Test
    public void executorAlwaysExecuteInTheSameThread() throws InterruptedException {
        final ThreadNameHolder threadNameHolder = new ThreadNameHolder();
        int taskCount = 3;
        String expectedThreadName = "scheduler-0";
        final CountDownLatch cdl = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            schedulerExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    threadNameHolder.putName(Thread.currentThread().getName());
                }
            });
        }


        cdl.await(1, TimeUnit.SECONDS);

        for (int i = 0; i < taskCount; i++) {
            assertEquals("All thread must run in the same thread", expectedThreadName, threadNameHolder.getName().get(i));
        }
    }

    private static class ThreadNameHolder {
        private List<String> name = Collections.synchronizedList(new ArrayList<String>());

        public void putName(String name) {
            this.name.add(name);
        }

        public List<String> getName() {
            return name;
        }
    }

}
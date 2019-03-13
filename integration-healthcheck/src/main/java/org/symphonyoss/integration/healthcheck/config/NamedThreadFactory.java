package org.symphonyoss.integration.healthcheck.config;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private boolean daemon;
    private String name;
    private AtomicInteger id = new AtomicInteger(0);

    public NamedThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, this.name + "-" + this.id.getAndIncrement());
        t.setDaemon(this.daemon);
        return t;
    }
}

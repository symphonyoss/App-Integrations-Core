package org.symphonyoss.integration.core.bootstrap;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Thread Factory that allows to assign a name to the threads. Useful to monitor threads using tools like JVisualVM.
 */
public class NamedThreadFactory implements ThreadFactory {
    private boolean daemon;
    private String name;
    private AtomicInteger id = new AtomicInteger(0);

    public NamedThreadFactory(String name, boolean daemon) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Thread's name cannot be blank");
        }

        this.name = name;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, this.name + "-" + this.id.getAndIncrement());
        t.setDaemon(this.daemon);
        return t;
    }
}

package org.symphonyoss.integration.core.bootstrap;

import org.junit.Test;

import static org.junit.Assert.*;

public class NamedThreadFactoryTest {

    private static final Runnable DEFAULT_RUNNABLE = createDummyRunnable();

    @Test(expected = IllegalArgumentException.class)
    public void threadNameCannotBeNull() {
        new NamedThreadFactory(null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void threadNameCannotBeEmptyString() {
        new NamedThreadFactory("", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void threadNameCannotBeBlankString() {
        new NamedThreadFactory("  ", false);
    }

    @Test
    public void createNewThreadShouldAssignAnNameToIt() {
        NamedThreadFactory factory = new NamedThreadFactory("custom-name", false);

        Thread createdThread = factory.newThread(DEFAULT_RUNNABLE);

        assertEquals("custom-name-0", createdThread.getName());
    }

    @Test
    public void createSubsequentThreadsShouldIncrementNameCounter() {
        NamedThreadFactory factory = new NamedThreadFactory("custom-name", false);

        factory.newThread(DEFAULT_RUNNABLE); // Just to start the counter
        Thread subsequentCreateThread = factory.newThread(DEFAULT_RUNNABLE);

        assertEquals("custom-name-1", subsequentCreateThread.getName());
    }

    private static Runnable createDummyRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // Do nothing
            }
        };
    }
}
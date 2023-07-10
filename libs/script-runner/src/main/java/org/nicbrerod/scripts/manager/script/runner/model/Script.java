package org.nicbrerod.scripts.manager.script.runner.model;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Model to represent an script to run in a system
 */
public record Script(
    /**
     * ID related to script
     */
    UUID id,
    /**
     * Script content as string
     */
    String script,
    /**
     * Array with dependencies to be installed
     */
    String[] dependencies,
    /**
     * Timeout to wait before killing the script when its executes
     */
    long timeout,
    /**
     * Time unit related to {@link #timeout} field
     */
    TimeUnit timeunit
) implements Serializable {
    /**
     * Additional constructor with 'timeunit' field as MILLISECONDS by default
     * @param id ID related to script
     * @param script Script content as string
     * @param dependencies Array with dependencies to be installed
     * @param timeout Timeout, in milliseconds, to wait before killing the script when its executes
     */
    public Script(UUID id, String script, String[] dependencies, long timeout) {
        this(id, script, dependencies, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Additional constructor with a timeout of 30 minutes by default
     * @param id ID related to script
     * @param script Script content as string
     * @param dependencies Array with dependencies to be installed
     */
    public Script(UUID id, String script, String[] dependencies) {
        this(id, script, dependencies, 30, TimeUnit.MINUTES);
    }

    /**
     * Additional constructor without dependencies or timeout
     * @param id ID related to script
     * @param script Script content as string
     */
    public Script(UUID id, String script) {
        this(id, script, new String[]{});
    }

    /**
     * Additional constructor without dependencies
     * @param id ID related to script
     * @param script Script content as string
     * @param timeout Timeout, in milliseconds, to wait before killing the script when its executes
     * @param timeUnit Time unit related to {@link #timeout} field
     */
    public Script(UUID id, String script, long timeout, TimeUnit timeUnit) {
        this(id, script, new String[]{}, timeout, timeUnit);
    }

    /**
     * Additional constructor with timeUnit as milliseconds by default
     * @param id ID related to script
     * @param script Script content as string
     * @param timeout Timeout, in milliseconds, to wait before killing the script when its executes
     */
    public Script(UUID id, String script, long timeout) {
        this(id, script, new String[]{}, timeout);
    }
}

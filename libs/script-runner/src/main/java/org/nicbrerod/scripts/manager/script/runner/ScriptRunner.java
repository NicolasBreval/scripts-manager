package org.nicbrerod.scripts.manager.script.runner;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.nicbrerod.scripts.manager.script.runner.exception.ScriptNotExistException;
import org.nicbrerod.scripts.manager.script.runner.exception.ScriptTimeoutException;
import org.nicbrerod.scripts.manager.script.runner.model.Script;

/**
 * Abstract class to define any scripts runner. An script runner is an object with the ability of 
 * run an script of an specific language and control its execution
 */
public abstract class ScriptRunner {
    /**
     * Logger object used to show some information about script lifetime
     */
    protected final Logger log = Logger.getLogger(getClass());

    /**
     * Map with all scripts registered by the script runner. If an script is present in this map, the 
     * script is ready to be executed
     */
    protected final Map<UUID, Script> registeredScripts = new ConcurrentHashMap<>();

    /**
     * This is the previous phase to script execution. With this method, script runner make all operations needed to
     * ensure the script will be successfully executed, like create an environment space, install script dependencies,...
     * 
     * @param script Script to prepare
     * @return True, if script was successfully prepared, else false
     * @throws IOException Thrown if any I/O operation (create folders, files, create a proces,...) fails
     * @throws InterruptedException Thrown if system kills a created process
     * @throws ScriptTimeoutException
     */
    protected abstract boolean prepareScript(Script script) throws IOException, InterruptedException, ScriptTimeoutException;

    /**
     * Method used to run a previously registered script by their id
     * @param scriptId ID related to script to execute
     * @param parameters Parameters to pass to script in this execution
     * @return True if script returns a zero code, else false
     * @throws IOException Thrown if any I/O operation (create folders, files, create a proces,...) fails
     * @throws ScriptNotExistException Thrown if not exists any script related to passed scriptId
     * @throws ScriptTimeoutException Throw if script exceeds its timeout
     */
    public abstract boolean runScript(UUID scriptId, Object[] parameters) throws IOException, ScriptNotExistException, ScriptTimeoutException;
    
    /**
     * Used to run a previously registered script by their id, without parameters
     * @param scriptId ID related to script to execute
     * @return True if script returns a zero code, else false
     * @throws IOException Thrown if any I/O operation (create folders, files, create a proces,...) fails
     * @throws ScriptNotExistException Thrown if not exists any script related to passed scriptId
     * @throws ScriptTimeoutException Throw if script exceeds its timeout
     * @see {@link #runScript(UUID, Object[])}
     */
    public boolean runScript(UUID scriptId) throws IOException, ScriptNotExistException, ScriptTimeoutException {
        return runScript(scriptId, new Object[0]);
    }

    /**
     * Used to register an script in this script runner. It's important to register an script, because, in this
     * phase, the script runner must create the script environment and install their dependencies
     * @param script Script to be registered
     * @return True if script was successfully registered, else false
     */
    public boolean registerScript(Script script) {
        boolean scriptPrepared = false;

        try {
            scriptPrepared = prepareScript(script);

            if (scriptPrepared) {
                registeredScripts.put(script.id(), script);
            }
        } catch (Exception e) {
            log.error(String.format("Error preparing script %s", script.id()));
        }

        return scriptPrepared;
    }

    public boolean unregisterScript(UUID id) {
        return registeredScripts.remove(id) != null;
    }
}

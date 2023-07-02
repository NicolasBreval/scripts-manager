package org.nicbrerod.scripts.manager.script.runner.exception;

import java.util.UUID;

/**
 * Exception thrown when an user tries to run an script that is not registered on system
 */
public class ScriptNotExistException extends Exception {
    public ScriptNotExistException(UUID scriptId) {
        super(String.format("Not exists any script related to id %s", scriptId));
    }
}

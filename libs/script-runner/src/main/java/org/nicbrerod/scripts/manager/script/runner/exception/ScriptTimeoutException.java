package org.nicbrerod.scripts.manager.script.runner.exception;

import java.util.UUID;

import lombok.NoArgsConstructor;

/**
 * Exception to be thrown when script execution exceeds the configured timeout
 */
@NoArgsConstructor
public class ScriptTimeoutException extends Exception {    
    public ScriptTimeoutException(UUID scriptId) {
        super(String.format("Script %s exceeds the timeout for their execution, it has been killed", scriptId));
    }
}

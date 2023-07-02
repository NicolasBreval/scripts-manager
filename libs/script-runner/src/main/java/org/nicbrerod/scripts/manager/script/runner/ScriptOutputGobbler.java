package org.nicbrerod.scripts.manager.script.runner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;

/**
 * Used to redirect a process output to another way
 */
@AllArgsConstructor
public class ScriptOutputGobbler implements Runnable {
    /**
     * InputStream obtained from process to redirect their output
     */
    private InputStream iStream;

    /**
     * Consumer object with logic to redirect messages
     */
    private Consumer<String> consumer;

    /**
     * Method that redirects all process output lines to consumer
     */
    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(iStream)).lines()
            .forEach(consumer);
    }
    
}

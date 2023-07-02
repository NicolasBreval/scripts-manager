package org.nicbrerod.scripts.manager.script.runner.py;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.nicbrerod.scripts.manager.script.runner.ScriptOutputGobbler;
import org.nicbrerod.scripts.manager.script.runner.ScriptRunner;
import org.nicbrerod.scripts.manager.script.runner.exception.ScriptNotExistException;
import org.nicbrerod.scripts.manager.script.runner.exception.ScriptTimeoutException;
import org.nicbrerod.scripts.manager.script.runner.model.Script;

/**
 * ScriptRunner child used to process Python scripts
 */
public class PythonScriptRunner extends ScriptRunner {
    /**
     * Python's interpreter path
     */
    private final String pythonPath;

    /**
     * VirtualEnv's path
     */
    private final String venvPath;

    /**
     * Options to pass to install command when script runner tries to install script's dependencies
     */
    private String venvInstallOpts;

    /**
     * Options to pass to execution command when script runner tries to execute an script
     */
    private String venvExecOpts;

    /**
     * Working directory where to run all commands
     */
    private final String homeDir;

    public PythonScriptRunner(String pythonPath, String venvPath, String homeDir) {
        this.pythonPath = pythonPath;
        this.venvPath = venvPath;
        this.venvInstallOpts = "";
        this.venvExecOpts = "";
        this.homeDir = homeDir;
    }

    public PythonScriptRunner(String pythonPath, String venvPath, String homeDir, String venvInstallOpts) {
        this(pythonPath, venvPath, homeDir);
        this.venvInstallOpts = venvInstallOpts;
    }

    public PythonScriptRunner(String pythonPath, String venvPath, String homeDir, String venvInstallOpts, String venvExecOpts) {
        this(pythonPath, venvPath, homeDir, venvInstallOpts);
        this.venvExecOpts = venvExecOpts;
    }

    /**
     * Runs a command in a new process and redirects its outpur to {@link #log}
     * @param command Command to be executed
     * @param homeDir Working directory
     * @param timeout Amount of time to wait for command to run
     * @param timeUnit Time unit related to timeout
     * @return The exit code of the process to run
     * @throws Exception
     */
    private int runCommand(String command, String homeDir, long timeout, TimeUnit timeUnit) throws ScriptTimeoutException {
        int exitCode = 0;
        Future<?> gobblerTask = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));

        try {
            var processBuilder = new ProcessBuilder(command.split("\\s+")).directory(new File(homeDir));
            var process = processBuilder.start();
            var outputGobbler = new ScriptOutputGobbler(process.getInputStream(), x -> log.info(x));
            gobblerTask = executor.submit(outputGobbler);
            if (timeout <= 0) {
                exitCode = process.waitFor();
            } else {
                boolean terminated = process.waitFor(timeout, timeUnit);
                
                if (!terminated) {
                    process.destroy();
                    throw new ScriptTimeoutException();
                } else {
                    exitCode = process.exitValue();
                }
            }
        } catch (Exception e) {
            if (gobblerTask != null) {
                gobblerTask.cancel(true);
            }

            if (e instanceof ScriptTimeoutException) {
                throw new ScriptTimeoutException();
            }
        }
        
        return exitCode;
    }

    /**
     * Equivalent to {@link #runCommand(String, String, long, TimeUnit)}, with timeout as zero by default, to produce a non-wait execution
     * @param command Command to be executed
     * @param homeDir Working directory
     * @return The exit code of the process to run
     * @throws IOException Thrown if there are any problem with I/O operations (create file, take process input stream,...)
     * @throws ScriptTimeoutException
     */
    private int runCommand(String command, String homeDir) throws IOException, ScriptTimeoutException {
        return runCommand(command, homeDir, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    protected boolean prepareScript(Script script) throws IOException, InterruptedException, ScriptTimeoutException {
        int exitCode = 0;

        log.info(String.format("Preparing script %s", script.id()));

        String venvCommand = String.format("%s -m virtualenv %s", pythonPath, script.id());
        String depsInstallCommand = String.format("%s %s ./%s/bin/pip3 install %s", venvPath, venvInstallOpts, script.id(), Arrays.stream(script.dependencies()).collect(Collectors.joining(" ")));
        
        log.info("Creating Python virtual environment");
        exitCode = runCommand(venvCommand, homeDir);

        if (exitCode == 0) {
            log.info("Copying file");
            Files.writeString(Paths.get(homeDir, script.id().toString(), "script.py"), script.script());
        }

        if (exitCode == 0 && script.dependencies().length > 0) {
            log.info("Installing dependencies");
            exitCode = runCommand(depsInstallCommand, homeDir);
        }

        return exitCode == 0;
    }

    @Override
    public boolean runScript(UUID scriptId, Object[] parameters) throws IOException, ScriptNotExistException, ScriptTimeoutException {
        var script = registeredScripts.get(scriptId);

        if (script == null)
            throw new ScriptNotExistException(scriptId);
        
        int exitCode = 0;
        log.info(String.format("Running script %s", scriptId));
        
        String runScriptCommand = String.format("%1$s %2$s ./bin/python3 ./script.py %4$s", venvPath, venvExecOpts.replace("{scriptDir}", scriptId.toString()), scriptId, Arrays.stream(parameters).map(o -> o.toString()).collect(Collectors.joining(" ")));

        try {
            exitCode = runCommand(runScriptCommand, homeDir, script.timeout(), script.timeunit());
            return exitCode == 0;
        } catch (ScriptTimeoutException e) {
            throw new ScriptTimeoutException(scriptId);
        }
        
    }
}

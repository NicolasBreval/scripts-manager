package org.nicbrerod.scripts.manager.script.runner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.nicbrerod.scripts.manager.script.runner.exception.ScriptNotExistException;
import org.nicbrerod.scripts.manager.script.runner.exception.ScriptTimeoutException;
import org.nicbrerod.scripts.manager.script.runner.model.Script;
import org.nicbrerod.scripts.manager.script.runner.py.PythonScriptRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

/**
 * All tests used to check the correct operation of {@link PythonScriptRunner} class. To don't force developers 
 * to install a Python interpreter, and also ensure all developers uses the same Python version to run the tests, 
 * the Python interpreter used is based on TestContainers library, so really the developer is running python test 
 * scripts in a Docker container
 */
public class PythonScriptRunnerTest {

    /**
     * Local path where python container mounts their volume
     */
    private static final String HOST_PATH = "./python-test";

    /**
     * Container used to perform tests
     */
    @ClassRule
    public static GenericContainer<?> container = new GenericContainer<>(new ImageFromDockerfile().withFileFromClasspath("Dockerfile", "python/Dockerfile"))
        .withWorkingDirectory("/test")
        .withFileSystemBind(HOST_PATH, "/test", BindMode.READ_WRITE);

    /**
     * Script with a basic test that exports a csv using pandas library
     */
    private static final Script script = new Script(UUID.randomUUID(), 
        new BufferedReader(new InputStreamReader(PythonScriptRunnerTest.class.getClassLoader().getResourceAsStream("python/python-test.py"), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")), 
        new String[]{ "pandas==2.0.3" });

    /**
     * Script with a ethernal loop inside to test timeout exception
     */
    private static final Script timeoutScript = new Script(UUID.randomUUID(), 
        new BufferedReader(new InputStreamReader(PythonScriptRunnerTest.class.getClassLoader().getResourceAsStream("python/python-timeout-test.py"), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")), 
        30, TimeUnit.SECONDS);

    /**
     * Script that receives parameters and write them in a text file
     */
    private static final Script parametersScript = new Script(UUID.randomUUID(), 
        new BufferedReader(new InputStreamReader(PythonScriptRunnerTest.class.getClassLoader().getResourceAsStream("python/python-param-test.py"), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")));

    /**
     * Cleans all files created locally from script
     * @throws IOException Thrown if there are any problem during I/O operations, like check if file exists or deleting folder
     */
    private static void cleanHostPath() throws IOException {
        var hostPath = new File(HOST_PATH);

        if (hostPath.exists()) {
            Files.walk(Paths.get(HOST_PATH))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }

        Files.createDirectory(Paths.get(HOST_PATH));
    }

    /**
     * Generates a command to run Python interpreter
     * @param containerName Name of container used as Python interpreter
     * @return Command to run as Python interpreter
     */
    private static String pyExecutable(String containerName) {
        return String.format("docker exec -i %s python", containerName);
    }

    /**
     * Command to run virtualenv inside Docker container
     */
    private static String venvExecutable = "docker exec";

    /**
     * Options to pass to virtualenv command to install script's dependencies
     * @param containerName Name of container used as Python interpreter
     * @return Options to pass to virtualenv command in install phase
     */
    private static String venvInstallOpts(String containerName) {
        return String.format(" -i %s", containerName);
    }

    /**
     * Options to pass to virtualenv command to run an script
     * @param containerName Name of container used as Python interpreter
     * @return Options to pass to virtualenv command in execution phase
     */
    private static String venvExecOpts(String containerName) {
        return String.format("-w /test/{scriptDir} -i %s", containerName);
    }

    /**
     * Checks that a well-writen Python script executes correctly
     * @throws InterruptedException Thrown if system kills any waiting process
     * @throws IOException Throws if any I/O operation fails, like file-based operations, or process streams management operations
     * @throws CsvException Thrown if there are any problem opening CSV file exported by Python script
     * @throws ScriptNotExistException Thrown if script to run doesn't exists
     * @throws ScriptTimeoutException Thrown if script to run exceeds its timeout
     */
    @Test
    public void completeFlowTest() throws InterruptedException, IOException, CsvException, ScriptNotExistException, ScriptTimeoutException {
        var containerName = container.getContainerName();
        var scriptRunner = new PythonScriptRunner(pyExecutable(containerName), venvExecutable, "./python-test", venvInstallOpts(containerName), venvExecOpts(containerName));
        var prepared = scriptRunner.registerScript(script);
        var envFolder = Paths.get(HOST_PATH, script.id().toString());

        // Check if command returns exit code 0
        assertTrue("Command returns a non-zero exit code", prepared);
        // Check if exists the env folder in container's volume folder
        assertTrue("Not exists any folder created from virtualenv", Files.exists(envFolder));
        // Check if exists folder of script dependency
        assertTrue("Script dependencies have't been installed", Files.exists(Paths.get(HOST_PATH, script.id().toString(), "lib", "python3.11", "site-packages", "pandas")));

        var executed = scriptRunner.runScript(script.id());

        // Check if execution command returns exit code 0
        assertTrue("Command returns a non-zero exit code", executed);
        // Check if python script has generated the expected file
        assertTrue("Script has not generated the expected file", Files.exists(Paths.get(HOST_PATH, script.id().toString(), "ninja-turtles.csv")));
        
        List<String[]> csvLines = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(HOST_PATH, script.id().toString(), "ninja-turtles.csv"))) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                csvLines = csvReader.readAll();
            }
        }

        // Check if file is not empty
        assertNotEquals("Expected file is empty", csvLines.size(), 0);

        // Check if file content is the expected one
        assertArrayEquals("First line of file is not the expected", csvLines.get(0), new String[]{ "name", "mask", "weapon" });
        assertArrayEquals("Second line of file is not the expected", csvLines.get(1), new String[]{ "Raphael", "red", "sai" });
        assertArrayEquals("Third line of file is not the expected", csvLines.get(2), new String[]{ "Donatello", "purple", "bo staff" });
    }

    /**
     * Checks that the script runner throws an ScriptNotExistsException if tries to run an script that doesn't exists
     * @throws IOException Throws if any I/O operation fails, like file-based operations, or process streams management operations
     * @throws ScriptNotExistException Thrown if script to run doesn't exists
     * @throws ScriptTimeoutException Thrown if script to run exceeds its timeout
     */
    @Test(expected = ScriptNotExistException.class)
    public void scriptNotExistsTest() throws IOException, ScriptNotExistException, ScriptTimeoutException {
        var containerName = container.getContainerName();
        var scriptRunner = new PythonScriptRunner(pyExecutable(containerName), venvExecutable, "./python-test", venvInstallOpts(containerName), venvExecOpts(containerName));
        scriptRunner.runScript(UUID.randomUUID());
    }

    /**
     * Checks that if script runner runs an script that exceeds its timeout, an ScriptTimeoutExcepetion is thrown
     * @throws IOException Throws if any I/O operation fails, like file-based operations, or process streams management operations
     * @throws ScriptNotExistException Thrown if script to run doesn't exists
     * @throws ScriptTimeoutException Thrown if script to run exceeds its timeout
     */
    @Test(expected = ScriptTimeoutException.class)
    public void scriptExceedsTimeoutTest() throws IOException, ScriptNotExistException, ScriptTimeoutException {
        var containerName = container.getContainerName();
        var scriptRunner = new PythonScriptRunner(pyExecutable(containerName), venvExecutable, "./python-test", venvInstallOpts(containerName), venvExecOpts(containerName));
        var prepared = scriptRunner.registerScript(timeoutScript);

        assertTrue("Error preparing script", prepared);

        scriptRunner.runScript(timeoutScript.id());
    }

    /**
     * Checks that an script receives the passed parameters correctly
     * @throws IOException Throws if any I/O operation fails, like file-based operations, or process streams management operations
     * @throws ScriptNotExistException Thrown if script to run doesn't exists
     * @throws ScriptTimeoutException Thrown if script to run exceeds its timeout
     */
    @Test
    public void scriptWithParametersTest() throws IOException, ScriptNotExistException, ScriptTimeoutException {
        var containerName = container.getContainerName();
        var scriptRunner = new PythonScriptRunner(pyExecutable(containerName), venvExecutable, "./python-test", venvInstallOpts(containerName), venvExecOpts(containerName));
        var prepared = scriptRunner.registerScript(parametersScript);

        assertTrue("Error preparing script", prepared);

        var result = scriptRunner.runScript(parametersScript.id(), new Object[] { "param1", 34, 123.5 });
        
        assertTrue("Error running script", result);
        assertTrue("Script not executed correctly, expected file doesn't exists", Files.exists(Paths.get(HOST_PATH, parametersScript.id().toString(), "params-test.txt")));
    
        var content = Files.readAllLines(Paths.get(HOST_PATH, parametersScript.id().toString(), "params-test.txt"));

        assertEquals("Script not executed correctly, file content is not the expected", "./script.py,param1,34,123.5", content.get(0));
    }
    
    /**
     * Used to clear local-generated files by a test execution before another test
     * @throws IOException Throws if any I/O operation fails, like file-based operations, or process streams management operations
     */
    @Before
    public void beforeTest() throws IOException {
        cleanHostPath();
    }

    /**
     * Used to clear local-generated files by a test execution after another test
     * @throws IOException Throws if any I/O operation fails, like file-based operations, or process streams management operations
     */
    @After
    public void afterTest() throws IOException {
        cleanHostPath();
    }

}

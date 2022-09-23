package util.Tasks;

import gui.Dialogs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This class is used to run a PowershellScript and handle the in and outputs of such.
 * In Powershellscript include: <p> $Log = "@LOG"<br>  $Timer = "@TIMER"<br>$Error = "@ERROR"<br>  $Special = "@SPECIAL" </p>
 * Use these Tags at the beginning of each Write-Output to use the correct handler. The Timer should be used as this: $Timer@PROGRESS@.
 * If there is no tag the line will just be logged, but not handled. You may work with these lines in the {@link ShellRunner#stdout}
 * The ErrorStream and the StandardOutputStream should be handled by a {@link ConsoleStreamHandler}.
 * This Handler and the Script should work with the {@link ShellRunner#ERROR} and {@link ShellRunner#LOG} String as Information of the Current State.
 */
public class ShellRunner implements Task {
    private final String command;
    private final int timeout;
    private final List<ConsoleStreamHandler> errorHandlers;
    private final List<ConsoleStreamHandler> logHandlers;
    private final List<ConsoleStreamHandler> timerHandlers;
    private final List<ConsoleStreamHandler> specialHandlers;
    private final List<Runnable> successExecution;
    private final List<Runnable> failureExecution;
    private final StringBuilder stdout = new StringBuilder();
    private final StringBuilder errout = new StringBuilder();
    private boolean error;

    private float progress;
    private int maxProgress;
    public static final String ERROR = "@ERROR";
    public static final String LOG = "@LOG";
    public static final String SPECIAL = "@SPECIAL";
    public static final String TIMER = "@TIMER";
    static final Logger logger = Logger.getLogger("ShellRunner");

    public ShellRunner(String command, int timeout, int maxProgress) {
        logger.info("Creating new ShellRunner");
        this.command = command;
        this.timeout = timeout;
        this.maxProgress = maxProgress;
        this.errorHandlers = new LinkedList();
        this.logHandlers = new LinkedList();
        this.specialHandlers = new LinkedList();
        this.timerHandlers = new LinkedList();
        this.successExecution = new LinkedList<>();
        this.failureExecution = new LinkedList<>();
    }

    /**
     * Returns the standard standartLogger: Logs all lines with the class-logger.
     *
     * @return the Handler
     */
    public static ConsoleStreamHandler getStandardStandardLogger() {
        return (line, progress) -> logger.info(line);
    }

    /**
     * Returns the standard errorLogger: Logs all errors with the class-logger.
     *
     * @return the Handler
     */
    public static ConsoleStreamHandler getStandardErrorLogger() {
        return (line, progress) -> logger.warning(line);
    }

@Override
    public void addSuccessRunnable(Runnable runnable) {
        successExecution.add(runnable);
    }

    @Override
    public void addFailureRunnable(Runnable runnable) {
        failureExecution.add(runnable);
    }

    @Override
    public String getName() {
        return "Sample";
    }

    public void addErrorHandler(ConsoleStreamHandler erroutHandler) {
        errorHandlers.add(erroutHandler);
    }

    public void addLogHandler(ConsoleStreamHandler logHandler) {
        logHandlers.add(logHandler);
    }

    public void addTimerHandler(ConsoleStreamHandler timerHandler) {
        timerHandlers.add(timerHandler);
    }

    public void addSpecialHandler(ConsoleStreamHandler specialHandler) {
        specialHandlers.add(specialHandler);
    }

    /**
     * @return stout as given by the handler
     */
    public String getStdout() {
        return stdout.toString();
    }

    /**
     * @return stout and errout as given by the handlers
     */
    public String getErrout() {
        return stdout.toString() + "\n" + errout.toString();
    }

    public boolean hadError() {
        return error;
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(this.getExecution());

        try {
            System.out.println("Started..");
            System.out.println((String) future.get((long) this.timeout, TimeUnit.MINUTES));
            System.out.println("Finished!");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            logger.severe("ShellRunner not finished in " + this.timeout + " minutes. Please check logs. Force Termination!");
            Dialogs.errorBox("ShellRunner not finished in " + this.timeout + " minutes. Please check logs. Force Termination!", "Error in Execution");
            logger.warning("Errors in execution");
            this.error = true;
            this.stdout.append(e.getMessage());
        }

        executorService.shutdownNow();
        if (this.error) {
            logger.warning("Execution in Execution. Executing " + this.successExecution.size() + " success Runners.");
            for (Runnable failure : failureExecution)
                failure.run();
        } else {
            logger.info("Execution successful. Executing " + this.successExecution.size() + " success Runners.");
            for (Runnable success : successExecution)
                success.run();
        }
        logger.info("Finished Running the Script");
    }

    private Runnable getExecution() {
        return () -> {
            if (this.logHandlers.size() == 0) {
                logger.warning("There is no log handler set. The standard log handler will be used");
                this.logHandlers.add(getStandardStandardLogger());
            }

            if (this.errorHandlers.size() == 0) {
                logger.warning("There is no error handler set. The standard error handler will be used");
                this.logHandlers.add(getStandardErrorLogger());
            }

            logger.info("Running command" + command);
            try {
                Process powerShellProcess = Runtime.getRuntime().exec(command);
                powerShellProcess.getOutputStream().close();
                BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = stdoutReader.readLine()) != null) {
                    System.out.println(line);
                    //HANDEL @LOG
                    if (line.startsWith(LOG)) {
                        stdout.append(line.substring(LOG.length()));
                        for (ConsoleStreamHandler logHandler : logHandlers) {
                            logHandler.handle(line.substring(LOG.length()));
                        }

                    }
                    //HANDLE @TIMER
                    else if (line.startsWith(TIMER)) {
                        String[] temp = line.split("@");
                        if (temp.length != 4) {
                            logger.severe("Wrong @TIMER line format! This line will be skipped: \t");
                            getStandardStandardLogger().handle(line);
                        } else {
                            try {
                                Integer.parseInt(temp[2]);
                            } catch (NumberFormatException e) {
                                logger.severe("Wrong @TIMER line format! This line will be skipped: \t");
                                getStandardStandardLogger().handle(line);
                            }
                            stdout.append(temp[3]);
                            progress = Integer.parseInt(temp[2]) / (float) maxProgress;
                            for (ConsoleStreamHandler timerHandler : timerHandlers) {
                                timerHandler.handle(temp[3], Integer.parseInt(temp[2]));
                            }
                        }
                    }
                    //HANDLE @SPECIAL
                    else if (line.startsWith(SPECIAL)) {
                        stdout.append(line.substring(SPECIAL.length()));
                        for (ConsoleStreamHandler specialHandler : specialHandlers) {
                            specialHandler.handle(line.substring(SPECIAL.length()));
                        }
                    }
                    //HANDLE @ERROR
                    else if (line.startsWith(ERROR)) {
                        errout.append(line.substring(ERROR.length())).append(1);
                        for (ConsoleStreamHandler errorHandler : errorHandlers) {
                            errorHandler.handle(line.substring(ERROR.length()));
                        }
                    }
                    //HANDEL THE REST
                    else {
                        stdout.append(line);
                    }
                    stdout.append('\n');
                }
                BufferedReader erroutReader = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream(), StandardCharsets.UTF_8));
                while ((line = erroutReader.readLine()) != null) {
                    errout.append(line).append('\n');
                    for (ConsoleStreamHandler errorHandler : errorHandlers) {
                        errorHandler.handle(line);
                    }
                }
                if (errout.length() > 0)
                    error = true;
            } catch (Exception e) {
                Dialogs.errorBox("Error while executing Thread: " + command + ":\n" + e.getMessage(), "Execution Error");
                logger.severe("Error while executing Thread: " + command + ":\n" + e.getMessage() + Arrays.toString(e.getStackTrace()));
                error = true;
            }
            logger.info("Finished Running the Script");
        };
    }
}



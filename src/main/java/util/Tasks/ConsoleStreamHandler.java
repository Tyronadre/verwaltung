package util.Tasks;

import java.io.BufferedReader;
import java.io.IOException;


public interface ConsoleStreamHandler {

    /**
     * Handels lines of a Stream.
     *
     * @param line The Line of the stream to handel
     * @param progress The Progress if this line is a {@link ShellRunner#TIMER} line
     * @throws InterruptedException error while handling
     */
    void handle(String line, int progress) throws InterruptedException;

    default void handle(String line) throws InterruptedException {
        handle(line, -1);
    }
}

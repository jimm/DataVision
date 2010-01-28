package jimm.util;
import java.io.PrintWriter;

/**
 * Prints time durations; used for development purposes only.
 * <p>
 * No threads or system resources are harmed in the making of a stop watch. A
 * stop watch simply remembers the start time (and elapsed time when paused)
 * and prints the total &quot;running&quot; time when either {@link #mark} or
 * {@link #stop} is called.
 * <p>
 * {@link #stop} doesn't really stop anything. You can call stop as many times
 * as you like and it will print the time elapsed since start. It would be
 * easy to change this behavior, but I haven't needed to.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class StopWatch {

protected String name;
protected long t0;
protected long elapsedTime;
protected PrintWriter out;

public StopWatch() {
    this(null, null);
}

public StopWatch(String name) {
    this(name, null);
}

public StopWatch(PrintWriter out) {
    this(null, out);
}

public StopWatch(String name, PrintWriter out) {
    this.name = name;
    if (out == null)
	this.out = new PrintWriter(System.err);
    else
	this.out = out;
    elapsedTime = -1L;		// So we can tell if we are ever started
}

/**
 * Remembers the current time and prints a message.
 */
public void start() {
    start(true);
}

/**
 * Remembers the current time and prints a message if requested.
 *
 * @param printStarting if <code>true</code> and this stop watch has a
 * name, print a message
 */
public void start(boolean printStarting) {
    if (t0 != 0)
	System.err.println("(warning: StopWatch already started; resetting)");
    if (printStarting && name != null)
	System.err.println("starting " + name);
    elapsedTime = 0;
    t0 = System.currentTimeMillis();
}

/**
 * Pauses the stop watch.
 */
public void pause() {
    long now = System.currentTimeMillis();
    elapsedTime += now - t0;
    t0 = 0;
}

/**
 * Resumes the stop watch.
 */
public void resume() {
    t0 = System.currentTimeMillis();
}

/**
 * Prints the current elapsed time without stopping.
 */
public void mark() {
    stop(null, true);
}

/**
 * Prints the current elapsed time without stopping, along with the
 * stop watch name if <var>printMark</var> is <code>true</code>.
 *
 * @param printMark if <code>true</code>, the stop watch name will
 * be printed
 */
public void mark(boolean printMark) {
    stop(null, printMark);
}

/**
 * Prints the current elapsed time without stopping, along with the
 * stop watch name and <var>msg</var>.
 *
 * @param msg a message to print
 */
public void mark(String msg) {
    stop(msg, true);
}

/**
 * Prints the current elapsed time without stopping, along with, along with
 * the stop watch name if <var>printMark</var> is <code>true</code> and the
 * <var>msg</var> if it's not <code>null</code>.
 *
 * @param msg a message to print
 * @param printMark if <code>true</code>, the stop watch name will
 * be printed
 */
public void mark(String msg, boolean printMark) {
    stop(msg, printMark);
}

/**
 * Stops the stop watch and prints the name of this stop watch and the current
 * elapsed time.
 */
public void stop() {
    stop(null, true);
}

/**
 * Stops the stop watch and prints the name of this stop watch,
 * <var>msg</var> if non-<code>null</code>, and the current elapsed time.
 *
 * @param msg a message to print; may be <code>null</code>
 */
public void stop(String msg) {
    stop(msg, true);
}

/**
 * Prints the current elapsed time, along with the stop watch name if
 * <var>printMark</var> is <code>true</code> and the <var>msg</var> if it's
 * not <code>null</code>.
 *
 * @param msg a message to print; may be <code>null</code>
 * @param printName if <code>true</code>, the stop watch name will
 * be printed
 */
public void stop(String msg, boolean printName) {
    long now = System.currentTimeMillis();

    if (elapsedTime == -1) {
	System.err.println("(StopWatch"
			   + (name != null ? (" \"" + name + '"') : "")
			   + " was stopped without ever being started)");
	return;
    }

    long total = elapsedTime;
    if (t0 != 0)
	total += now - t0;

    String separator = null;
    if (printName && name != null) {
	System.err.print(name);
	separator = ": ";
    }
    if (msg != null) {
	if (separator != null)
	    System.err.print(' ');
	System.err.print("(" + msg + ")");
	separator = ": ";
    }
    if (separator != null)
	System.err.print(separator);

    System.err.println("" + (total / 1000.0) + " seconds");
}

}

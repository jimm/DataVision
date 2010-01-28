package jimm.datavision;

/**
 * This exception is thrown when the user cancels an action, usually
 * by clicking "Cancel" in a dialog. It's a runtime exception so it can
 * "sneak through" while parsing a report XML file -- I don't have control
 * over the exceptions those methods declare.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class UserCancellationException extends RuntimeException {

public UserCancellationException() {
    super();
}

public UserCancellationException(String msg) {
    super(msg);
}

}



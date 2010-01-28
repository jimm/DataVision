package jimm.datavision.gui.cmd;

/**
 * An abstract adapter class for performing a command. It exists as a
 * convenience for creating command objects. The perform and undo methods
 * in this class are empty. <code>redo</code> calls <code>perform</code>.
 *
 * The <code>getName</code> method provides read-only access to the name
 * provided at construction time. The <code>setName</code> method does
 * nothing.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class CommandAdapter implements Command {

protected String name;

public CommandAdapter(String name) {
    this.name = name;
}

public String getName() { return name; }

/** A command's name is immutable. */
public void setName(String name) { }

/** Performs the command. The default implementation does nothing. */
public void perform() { }

/** Undoes the command. The default implementation does nothing. */
public void undo() { }

/** Redoes the command by calling {@link #perform}. */
public void redo() {
    perform();
}

}

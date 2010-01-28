package jimm.datavision.gui.cmd;
import jimm.util.I18N;
import java.util.ArrayList;
import javax.swing.JMenuItem;

/**
 * A command history holds comands and manages undo and redo behavior. To
 * use a new command for the first time, pass it to <code>perform</code>.
 * You can then call <code>undo</code> and <code>redo</code> to walk the
 * command history chain.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class CommandHistory {

protected ArrayList commands;
protected int commandIndex;
/**
 * If the command index is different than the baseline index then something
 * has changed, and <code>isChanged</code> will return <code>true</code>.
 */
protected int baselineIndex;
protected JMenuItem undoMenuItem;
protected JMenuItem redoMenuItem;

public CommandHistory() {
    this(null, null);
}

public CommandHistory(JMenuItem undoMenuItem, JMenuItem redoMenuItem) {
    this.undoMenuItem = undoMenuItem;
    this.redoMenuItem = redoMenuItem;
    commands = new ArrayList();
    commandIndex = 0;
    baselineIndex = 0;
}

public void setMenuItems(JMenuItem undoMenuItem, JMenuItem redoMenuItem) {
    this.undoMenuItem = undoMenuItem;
    this.redoMenuItem = redoMenuItem;
}

/** Answers the question, "Is there anything to undo?" */
public boolean canUndo() { return commandIndex > 0; }

/** Answers the question, "Is there anything to redo?" */
public boolean canRedo() { return commandIndex < commands.size(); }

/**
 * Resets the baseline index. The method <code>isChanged</code> returns
 * <code>false</code> only when the baseline index is equal to the
 * current command index.
 */
public void setBaseline() { baselineIndex = commandIndex; }
    

/** Answers the question, "Has anything changed?" */
public boolean isChanged() { return baselineIndex != commandIndex; }

/**
 * Add a command to the history list without performing it. If the history
 * cursor is not at the top of the stack, first erase all of the
 * commands after the cursor.
 *
 * @param command the command to add
 */
public synchronized void add(Command command) {
    // Erase commands after this one, if any
    for (int i = commands.size() - 1; i >= commandIndex; --i)
	commands.remove(i);

    // Add new command to end of history list
    commands.add(command);
    ++commandIndex;

    updateMenu();
}

/**
 * Perform a command and add it to the history list. If the history
 * cursor is not at the top of the stack, first erase all of the
 * commands after the cursor.
 *
 * @param command the command to perform
 */
public synchronized void perform(Command command) {
    command.perform();
    add(command);
}

/** Undo the command at the history cursor. */
public synchronized void undo() {
    if (commandIndex > 0) {
	--commandIndex;
	((Command)commands.get(commandIndex)).undo();
	updateMenu();
    }
}

/** Redo the command under the the history cursor. */
public synchronized void redo() {
    if (commandIndex < commands.size()) {
	((Command)commands.get(commandIndex)).redo();
	++commandIndex;
	updateMenu();
    }
}

/**
 * Return the name of the command that would be undone were one to call
 * undo(). Returns <code>null</code> if there is no such command.
 */
public String getUndoName() {
    return commandIndex > 0 ? getCommandName(commandIndex - 1) : null;
}

/**
 * Return the name of the command that would be redone were one to call
 * redo(). Returns <code>null</code> if there is no such command.
 */
public String getRedoName() {
    return commandIndex < commands.size()
	? getCommandName(commandIndex) : null;
}

/**
 * Return the name of the command at index. Returns <code>null</code>
 * if there is no such command.
 */
public String getCommandName(int index) {
    Command cmd = null;
    synchronized(this) {
	cmd = (Command)commands.get(index);
    }
    return (cmd != null) ? cmd.getName() : null;
}

protected void updateMenu() {
    if (undoMenuItem != null)
	updateMenuItem(undoMenuItem, I18N.get("CommandHistory.undo"),
		       getUndoName(), canUndo());
    if (redoMenuItem != null)
	updateMenuItem(redoMenuItem, I18N.get("CommandHistory.redo"),
		       getRedoName(), canRedo());
}

protected void updateMenuItem(JMenuItem item, String verb, String cmdName,
			      boolean canDo)
{
    if (canDo)
	item.setText(verb + " " + cmdName);
    else
	item.setText(verb);

    item.setEnabled(canDo);
}

}

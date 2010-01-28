package jimm.datavision.gui.cmd;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;	// For reverse traversal

/**
 * A compound command holds a list of commands and allows their use as
 * one single command.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class CompoundCommand extends CommandAdapter {

protected ArrayList commands;

public CompoundCommand(String name) {
    super(name);
    commands = new ArrayList();
}

public void add(Command c) {
    commands.add(c);
}

public int numCommands() { return commands.size(); }

public void perform() {
    for (Iterator iter = commands.iterator(); iter.hasNext(); )
	((Command)iter.next()).perform();
}

public void undo() {
    for (ListIterator iter = commands.listIterator(commands.size());
	 iter.hasPrevious(); )
	((Command)iter.previous()).undo();
}

public void redo() {
    for (Iterator iter = commands.iterator(); iter.hasNext(); )
	((Command)iter.next()).redo();
}

}

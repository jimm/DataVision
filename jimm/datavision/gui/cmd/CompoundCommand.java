package jimm.datavision.gui.cmd;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * A compound command holds a list of commands and allows their use as
 * one single command.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public class CompoundCommand extends CommandAdapter {

protected ArrayList<Command> commands;

public CompoundCommand(String name) {
    super(name);
    commands = new ArrayList<Command>();
}

public void add(Command c) {
    commands.add(c);
}

public int numCommands() { return commands.size(); }

public void perform() {
    for (Command c : commands)
	c.perform();
}

@SuppressWarnings("unchecked")
public void undo() {
    List<Command> rc = (List<Command>)commands.clone();
    Collections.reverse(rc);
    for (Command c : rc)
	c.undo();
}

public void redo() {
    for (Command c : commands)
	c.redo();
}

}

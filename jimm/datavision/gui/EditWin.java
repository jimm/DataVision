package jimm.datavision.gui;
import jimm.datavision.gui.cmd.CompoundCommand;
import jimm.util.I18N;
import java.awt.event.*;
import javax.swing.*;

/**
 * The abstract parent of all edit windows except the main design window.
 * Handles common behavior.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class EditWin extends JDialog implements ActionListener
{

protected Designer designer;
protected JButton revertButton;
protected CompoundCommand commands;

/**
 * Constructor for a non-modal dialog.
 *
 * @param designer the design window to which this dialog belongs
 * @param title window title
 * @param commandNameKey the {@link jimm.util.I18N} lookup key for the command
 * name
 */
public EditWin(Designer designer, String title, String commandNameKey) {
    this(designer, title, commandNameKey, false);
}

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param title window title
 * @param commandNameKey the {@link jimm.util.I18N} lookup key for the command
 * name
 * @param modal passed on to superclass
 */
public EditWin(Designer designer, String title, String commandNameKey,
	       boolean modal)
{
    super(designer.getFrame(), title, modal);
    this.designer = designer;
    commands = new CompoundCommand(I18N.get(commandNameKey));
}

/**
 * Builds and returns a panel containing the bottom four buttons.
 *
 * @return the four buttons OK, Apply, Revert, and Cancel
 */
protected JPanel closeButtonPanel() {
    JPanel buttonPanel = new JPanel();
    JButton button;

    buttonPanel.add(button = new JButton(I18N.get("GUI.ok")));
    button.addActionListener(this);
    button.setDefaultCapable(true);

    buttonPanel.add(button = new JButton(I18N.get("GUI.apply")));
    button.addActionListener(this);

    buttonPanel.add(revertButton = new JButton(I18N.get("GUI.revert")));
    revertButton.addActionListener(this);
    revertButton.setEnabled(false);

    buttonPanel.add(button = new JButton(I18N.get("GUI.cancel")));
    button.addActionListener(this);

    return buttonPanel;
}

/**
 * Handles the four buttons.
 *
 * @param e action event
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("GUI.ok").equals(cmd)) {
	save(true);		// Should add commands
	dispose();
    }
    else if (I18N.get("GUI.apply").equals(cmd)) {
	save(false);		// Should add commands
    }
    else if (I18N.get("GUI.revert").equals(cmd)) {
	revert();
    }
    else if (I18N.get("GUI.cancel").equals(cmd)) {
	revert();
	dispose();
    }
}

/**
 * Saves all data by creating and performing a command. If not, that means we
 * are done and we should give the compound command to the design window.
 * <p>
 * Subclasses that implement {@link #doSave} should probably create a command,
 * call its <code>perform</code> method, and add it to the <var>commands</var>
 * compound command.
 *
 * @param closing passed through to <code>doSave</code>
 */
protected void save(boolean closing) {
    doSave();
    revertButton.setEnabled(true);

    if (closing && commands.numCommands() > 0)
	designer.addCommand(commands);
}

/**
 * Saves all data by creating a new command, performing it, and adding it
 * to <var>commands</var>.
 */
protected abstract void doSave();

/**
 * Reverts all state information by undoing any commands previously performed
 * and emptying the compound command.
 *
 * @see #doRevert
 */
protected void revert() {
    commands.undo();
    commands = new CompoundCommand(commands.getName());
    doRevert();
}

/**
 * Gives subclasses a chance to clean up their GUI.
 */
protected abstract void doRevert();

}

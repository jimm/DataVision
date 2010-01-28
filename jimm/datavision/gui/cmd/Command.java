package jimm.datavision.gui.cmd;
import jimm.datavision.Nameable;

/**
 * A Command knows how to perform an action, undo it, and redo it. It has
 * a name that can be used for menu items.
 * <p>
 * The concrete implementor <code>CommandAdapter</code> treats the name
 * as immutable; the <code>setName</code> method does nothing.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface Command extends Nameable {

abstract public void perform();
abstract public void undo();
abstract public void redo();

}

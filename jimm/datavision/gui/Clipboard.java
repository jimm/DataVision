package jimm.datavision.gui;

/**
 * A simple clipboard class. Use <code>instance</code> to get the single
 * global clipboard instance.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public class Clipboard {

protected static Clipboard clipboard = new Clipboard();

protected Object contents;

public static Clipboard instance() {
    return clipboard;
}

protected Clipboard() {}

public void setContents(Object obj) { contents = obj; }

public Object getContents() { return contents; }

public boolean isEmpty() { return contents == null; }

}

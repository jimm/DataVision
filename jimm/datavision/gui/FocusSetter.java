package jimm.datavision.gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Gives focus to a component, since often after building a frame the
 * component we want to have focus doesn't get it.
 * <p>
 * Based on code found at <a href="http://privat.schlund.de/b/bossung/prog/java/tips.html">http://privat.schlund.de/b/bossung/prog/java/tips.html</a>.
 * Modified to perform the focus request only once.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FocusSetter implements ActionListener {

protected static final int WAIT_MILLISECS = 200;
protected JComponent component;
protected Timer timer;

public FocusSetter(JComponent comp) {
    component = comp;
    if (component != null) {
	timer = new Timer(WAIT_MILLISECS, this);
	timer.setRepeats(false);
	timer.start();
    }
}

public void actionPerformed(ActionEvent evt) {
    if (evt != null && evt.getSource() == timer && component != null)
	component.requestFocus();
}

}

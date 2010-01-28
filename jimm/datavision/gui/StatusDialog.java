package jimm.datavision.gui;
import jimm.util.I18N;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * An extremely simple status display dialog.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class StatusDialog extends JDialog {

protected JLabel label;
protected boolean cancelled;

/**
 * Constructor.
 *
 * @param parent parent frame; may be <code>null</code>
 * @param title title string; may be <code>null</code>
 * @param initialString initial string to display; may be <code>null</code>
 */
public StatusDialog(Frame parent, String title, boolean displayCancelButton,
		    String initialString)
{
    super(parent, title, false);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(label = new JLabel(), BorderLayout.CENTER);
    label.setHorizontalAlignment(SwingConstants.CENTER);

    if (displayCancelButton) {
	JPanel buttonPanel = new JPanel();
	JButton button = new JButton(I18N.get("GUI.cancel"));
	buttonPanel.add(button);
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		cancelled = true;
		update(I18N.get("StatusDialog.cancelling"));
	    }
	    });
	panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    if (initialString != null)
	label.setText(initialString);

    panel.setPreferredSize(new Dimension(300, 100));
    getContentPane().add(panel);

    pack();
    setVisible(true);
}

public boolean isCancelled() { return cancelled; }

public void update(String message) {
    label.setText(message);
}

}

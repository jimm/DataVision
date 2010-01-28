package jimm.datavision.gui.sql;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import jimm.util.I18N;
import javax.swing.*;

public class SQLQueryWin extends JDialog {

protected static final int PREFERRED_COLUMNS = 40;
protected static final Dimension PREFERRED_SIZE = new Dimension(340, 275);

public SQLQueryWin(Frame owner, String queryString) {
    super(owner, I18N.get("SQLQueryWin.title"));
    buildWindow(queryString);
    pack();
    show();
}

protected void buildWindow(String queryString) {
    getContentPane().setLayout(new BorderLayout());

    // Copy the string and add some newlines
    char[] queryChars = queryString.toCharArray();

    int pos = queryString.indexOf(" from ");
    if (pos >= 0) queryChars[pos] = '\n';
    else pos = 0;

    pos = queryString.indexOf(" where ", pos);
    if (pos >= 0) queryChars[pos] = '\n';
    else pos = 0;

    pos = queryString.indexOf(" order by ", pos);
    if (pos >= 0) queryChars[pos] = '\n';

    JTextArea text = new JTextArea(new String(queryChars), 0,
				   PREFERRED_COLUMNS);
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);

    JScrollPane scroller = new JScrollPane(text);
    scroller.setPreferredSize(PREFERRED_SIZE);
    getContentPane().add(scroller, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    JButton button = new JButton(I18N.get("GUI.ok"));
    button.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) { dispose(); }
	});
    buttonPanel.add(button);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
}

}

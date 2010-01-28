package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
 * A modal dialog used to ask the user for all runtime report parameter
 * values. The cards used to dispay editable values are lazily instantiated.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ParamAskWin
    extends JDialog
    implements ActionListener, ListSelectionListener
{

protected static final int HORIZ_GAP = 20;
protected static final int VERT_GAP = 20;
protected static final int EDIT_PANEL_WIDTH = 300;
protected static final int EDIT_PANEL_HEIGHT = 200;
protected static final int TEXT_FIELD_COLS = 24;
protected static final int MAX_LIST_VISIBLE = 4;
protected static final String CARD_BOOL_NAME = "bool";
protected static final String CARD_SINGLE_STRING_NAME = "single-string";
protected static final String CARD_RANGE_STRING_NAME = "range-string";
protected static final String CARD_LIST_SINGLE_STRING_NAME =
    "list-single-string";
protected static final String CARD_LIST_MULTIPLE_STRING_NAME =
    "list-multiple-string";
protected static final String CARD_SINGLE_DATE_NAME = "single-date";
protected static final String CARD_RANGE_DATE_NAME = "range-date";

protected List parameters;
protected Parameter selectedParameter;
protected boolean cancelled;
protected JList questionList;
protected JPanel cardPanel;
protected HashMap createdInquisitors;

/**
 * Constructor.
 *
 * @param parent frame with which this dialog should be associated
 * @param parameters a list of parameters
 */
public ParamAskWin(Frame parent, List parameters) {
    super(parent, I18N.get("ParamAskWin.title"), true); // Modal
    this.parameters = parameters;
    createdInquisitors = new HashMap();
    selectedParameter = null;
    buildWindow();
    questionList.setSelectedIndex(0); // Select first question
    questionList.setVisibleRowCount(Math.max(parameters.size(),
					     MAX_LIST_VISIBLE));
    pack();
    setVisible(true);
}

protected void buildWindow() {
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(questionPanel(), BorderLayout.NORTH);
    getContentPane().add(editPanel(), BorderLayout.CENTER);
    getContentPane().add(buttonPanel(), BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	    dispose();
	    cancelled = true;
	}
	});
}

protected JPanel questionPanel() {
    JPanel panel = new JPanel();

    DefaultListModel model = new DefaultListModel();
    questionList = new JList(model);
    questionList.addListSelectionListener(this);
    questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    for (Iterator iter = parameters.iterator(); iter.hasNext(); )
	model.addElement(((Parameter)iter.next()).getQuestion());

    panel.add(new JScrollPane(questionList));
    return panel;
}

/**
 * We create a dummy blank panel. Additional panels are created as they
 * are needed.
 *
 * @return a dummy blank panel
 */
protected JPanel editPanel() {
    cardPanel = new JPanel();
    cardPanel.setLayout(new CardLayout(HORIZ_GAP, VERT_GAP));

    // Panels are created and added as they are needed. We start with
    // a dummy blank one.
    JPanel panel = new JPanel();
    panel.setPreferredSize(new Dimension(EDIT_PANEL_WIDTH, EDIT_PANEL_HEIGHT));
    cardPanel.add(panel, "dummy-blank-panel");

    return cardPanel;
}

protected JPanel buttonPanel() {
    // OK and Cancel buttons
    JPanel buttonPanel = new JPanel();
    JButton button;

    buttonPanel.add(button = new JButton(I18N.get("ParamAskWin.run_report")));
    button.addActionListener(this);
    button.setDefaultCapable(true);
    getRootPane().setDefaultButton(button);

    buttonPanel.add(button = new JButton(I18N.get("GUI.cancel")));
    button.addActionListener(this);

    return buttonPanel;
}

public boolean userCancelled() { return cancelled; }

/**
 * Displays parameter fill-in-the-blanks whenever a new question is selected.
 * Before displaying the new values we save the old values for the previously
 * selected question.
 */
public void valueChanged(ListSelectionEvent e) {
    copyValuesToSelectedParameter();	// Previously selected param
    int i = questionList.getSelectedIndex();
    if (i >= 0) {
	selectedParameter = (Parameter)parameters.get(i);
	selectAndFillCard();
    }
    else
	selectedParameter = null;
}

/**
 * Displays and fills the edit field for the currently selected parameter.
 * Inquisitors are lazily instantiated.
 */
protected void selectAndFillCard() {
    Inquisitor inq = (Inquisitor)createdInquisitors.get(selectedParameter);
    if (inq == null) {
	inq = Inquisitor.create(selectedParameter);
	createdInquisitors.put(selectedParameter, inq);
	cardPanel.add(inq.getPanel(), inq.getPanelName());
    }

    CardLayout cardLayout = (CardLayout)cardPanel.getLayout();
    cardLayout.show(cardPanel, inq.getPanelName());

    // Fill card
    inq.copyParamIntoGUI();
}

/**
 * Copy all values in GUI into the associated selected parameter.
 */
protected void copyValuesToSelectedParameter() {
    if (selectedParameter != null) {
	Inquisitor inq = (Inquisitor)createdInquisitors.get(selectedParameter);
	inq.copyGUIIntoParam();
    }
}

/**
 * Handles the buttons.
 *
 * @param e action event
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("ParamAskWin.run_report").equals(cmd)) {
	cancelled = false;
	copyValuesToSelectedParameter();
	dispose();
    }
    else if (I18N.get("GUI.cancel").equals(cmd)) {
	cancelled = true;
	dispose();
    }
}

}

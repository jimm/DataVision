package jimm.datavision.gui;
import jimm.datavision.ErrorHandler;
import jimm.util.I18N;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

/**
 * A help window. Opens on docs/DataVision/DataVision.html.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class HelpWin extends JFrame implements HyperlinkListener {

protected static final int START_WIDTH = 400;
protected static final int START_HEIGHT = 500;
protected static final String DOCS_DIR = "docs";
protected static final String HTML_DIR = "DataVision";
protected static final String START_CONTENT_FILE = "DataVision.html";

protected static final String HOME_ICON = "images/Home16.gif";
protected static final String PREV_ICON = "images/Back16.gif";
protected static final String NEXT_ICON = "images/Forward16.gif";

protected static HelpWin helpWin;

protected JEditorPane contentEditorPane;
protected HelpURLStack pages;
protected Action goHomeAction;
protected Action goPrevAction;
protected Action goNextAction;
protected JTextField urlField;

public static synchronized HelpWin instance() {
    if (helpWin == null)
	helpWin = new HelpWin();
    return helpWin;
}

protected HelpWin() {
    super(I18N.get("HelpWin.title"));
    buildWindow();
    pages = new HelpURLStack(contentEditorPane, urlField);
    pack();
    setVisible(true);

    new Thread(new Runnable() {
	public void run() { loadHelp(); }
	}).start();
}

protected void buildWindow() {
    makeActions();

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(makeToolbar(), BorderLayout.NORTH);
    getContentPane().add(buildContentPane(), BorderLayout.CENTER);
}

protected JComponent buildContentPane() {
    contentEditorPane = new JEditorPane();
    contentEditorPane.setEditable(false);

    // Scroll pane.
    JScrollPane scrollPane = new JScrollPane(contentEditorPane);
    scrollPane.setVerticalScrollBarPolicy(
	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(new Dimension(START_WIDTH, START_HEIGHT));

    // Start listening to hyperlink events.
    contentEditorPane.addHyperlinkListener(this);

    return scrollPane;
}

/**
 * Creates the actions used by menu items and toolbar widgets.
 */
protected void makeActions() {
    URL url = getClass().getClassLoader().getResource(HOME_ICON);
    String str = I18N.get("HelpWin.cmd_home");
    goHomeAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) {
	    pages.goToHomePage();
	    updateNavActions();
	}
	};
    goHomeAction.putValue(Action.SHORT_DESCRIPTION, str);
    goHomeAction.setEnabled(true);

    url = getClass().getClassLoader().getResource(PREV_ICON);
    str = I18N.get("HelpWin.cmd_prev");
    goPrevAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) {
	    pages.goToPreviousPage();
	    updateNavActions();
	}
	};
    goPrevAction.putValue(Action.SHORT_DESCRIPTION, str);
    goPrevAction.setEnabled(false);

    url = getClass().getClassLoader().getResource(NEXT_ICON);
    str = I18N.get("HelpWin.cmd_next");
    goNextAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) {
	    pages.goToNextPage();
	    updateNavActions();
	}
	};
    goNextAction.putValue(Action.SHORT_DESCRIPTION, str);
    goNextAction.setEnabled(false);
}

/**
 * Creates and returns a new tool bar.
 */
protected JToolBar makeToolbar() {
    JToolBar bar = new JToolBar(javax.swing.SwingConstants.HORIZONTAL);
    bar.add(goHomeAction);
    bar.add(goPrevAction);
    bar.add(goNextAction);

    urlField = new JTextField(40);
    urlField.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    pages.goTo(urlField.getText());
	}
	});
    bar.add(urlField);

    return bar;
}

/**
 * Updates navigation buttons based on number of pages and current
 * display page.
 */
protected void updateNavActions() {
    goPrevAction.setEnabled(pages.hasPrevious());
    goNextAction.setEnabled(pages.hasNext());
}

protected void loadHelp() {
    // Create a URL object for the file docs/DataVision/DataVision.html.
    String s = null;
    URL helpURL = null;
    try {
	String sep = System.getProperty("file.separator");
	s = "file:" + System.getProperty("user.dir") + sep
	    + DOCS_DIR + sep + HTML_DIR + sep + START_CONTENT_FILE;
	helpURL = new URL(s);
    } catch (Exception e) {
	ErrorHandler.error(I18N.get("HelpWin.error") + ' ' + s, e);
    }

    // Load the URL.
    if (helpURL != null) {
	pages.goTo(helpURL);
	updateNavActions();
    }
}

public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
	return;

    if (e instanceof HTMLFrameHyperlinkEvent) {
	HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
	HTMLDocument doc = (HTMLDocument)contentEditorPane.getDocument();
	doc.processHTMLFrameHyperlinkEvent(evt);
    }
    else {
	try {
	    pages.goTo(e.getURL());
	    updateNavActions();
	}
	catch (Throwable t) {
	    ErrorHandler.error(t);
	}
    }
}

}

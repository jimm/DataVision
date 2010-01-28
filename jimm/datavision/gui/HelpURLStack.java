package jimm.datavision.gui;
import jimm.datavision.ErrorHandler;
import jimm.util.I18N;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Stack;
import javax.swing.JTextField;
import javax.swing.JEditorPane;

/**
 * A URL stack manages the browser-like behaviour of having a current URL
 * and a list of previous and next URLs. It also updates a URL text field
 * and gives URLs to the <code>JEditorPane</code> that displays them.
 * <p>
 * We define the home page to be the first page loaded.
 */
class HelpURLStack {

protected Stack back;
protected URL home;
protected URL current;
protected Stack forward;
protected JEditorPane contentField;
protected JTextField urlField;

HelpURLStack(JEditorPane htmlField, JTextField textField) {
    contentField = htmlField;
    urlField = textField;
    back = new Stack();
    current = null;
    forward = new Stack();
}

boolean hasPrevious() {
    return !back.empty();
}
boolean hasNext() {
    return !forward.empty();
}

void goTo(String urlString) {
    try {
	URL url = new URL(urlString);
	goTo(url);
    }
    catch (MalformedURLException e) {
	ErrorHandler.error(I18N.get("HelpURLStack.error_parsing")
			   + ' ' + urlString, e);
    }
}

void goTo(URL url) {
    if (current != null)
	back.push(current);
    if (home == null)
	home = url;
    current = url;
    forward.clear();
    updateGUI();
}

/** The home page is the same as the first page we loaded. */
void goToHomePage() {
    goTo(home);
}

void goToPreviousPage() {
    if (hasPrevious()) {
	if (current != null)
	    forward.push(current);
	current = (URL)back.pop();
    }
    updateGUI();
}

void goToNextPage() {
    if (hasNext()) {
	if (current != null)
	    back.push(current);
	current = (URL)forward.pop();
    }
    updateGUI();
}

protected void updateGUI() {
    urlField.setText(current == null ? "" : current.toString());
    try {
	contentField.setPage(current);
    }
    catch (IOException e) {
	ErrorHandler.error(I18N.get("HelpURLStack.error_loading")
			   + ' ' + current, e);
    }

}

}

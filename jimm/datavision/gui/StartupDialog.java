package jimm.datavision.gui;
import jimm.util.I18N;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.net.*;
import java.util.prefs.Preferences;


/**
 * This class is called from the DataVision class when the designer is
 * started with no command line.  It is a startup dialog with the
 * DataVision splash screen and two buttons, one to create a new
 * report and one to open an existing report.
 *
 * @author Frank W. Zammetti, <a href="mailto:fzammetti@omnytex.com">fzammetti@omnytex.com</a>
 */
public class StartupDialog extends JDialog implements ActionListener {

/** This string is what is returned when we're creating a new report */
public static final String NEW_REPORT_STRING = "*StartANewReport*";

/** The all-seeing eye. */
protected static final String TITLE_IMAGE = "images/DVTitle.png";

// The two buttons
private JButton newReport      = new JButton(I18N.get("StartupDialog.new"));
private JButton existingReport = new JButton(I18N.get("StartupDialog.open"));
private JButton quit           = new JButton(I18N.get("StartupDialog.quit"));

// The button for the title.  Am I just missing something or is there really
// no Image widget, or something along those lines, for displaying an image
// in Swing?  Do you really have to make it a button?!?
private JButton titleImage = null;

// The result of this dialog.  Will either be null, meaning the dialog
// was dismissed with the close button, the value of NEW_REPORT_STRING
// if creating a new report, or the path to the selected file
// (only one is selectable!)
private String selectedFile = null;


/** Constructor for our class.  The dialog is built here. */
public StartupDialog() {
    super((Frame)null, I18N.get("StartupDialog.title"), true);

    // Set it's size and resizability
    setResizable(false);
    setSize(580, 420);

    // What to do when the close button is pressed (default didn't
    // work, I don't know why, so this had to be done explicitly)
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent we) {
	    dispose();
	}
	});

    // Center it on the screen
    Toolkit   kit          = this.getToolkit();
    Dimension screenSize   = kit.getScreenSize();
    int       screenWidth  = screenSize.width;
    int       screenHeight = screenSize.height;
    Dimension windowSize   = getSize();
    int       windowWidth  = windowSize.width;
    int       windowHeight = windowSize.height;
    int       upperLeftX   = (screenWidth  - windowWidth)  / 2;
    int       upperLeftY   = (screenHeight - windowHeight) / 2;
    setLocation(upperLeftX, upperLeftY);

    // Add it's content... first, the image
    URL    url = getClass().getClassLoader().getResource(TITLE_IMAGE);
    Image  img = Toolkit.getDefaultToolkit().getImage(url);
    JPanel p1  = new JPanel();
    titleImage = new JButton(new ImageIcon(img));
    titleImage.setBorderPainted(false);
    titleImage.setContentAreaFilled(false);
    titleImage.setFocusPainted(false);
    p1.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
    p1.add(titleImage);
    getContentPane().add("Center", p1);

    // Now the two buttons, along with their event handlers...
    JPanel p2 = new JPanel();
    p2.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 10));
    newReport.addActionListener(this);
    p2.add(newReport);
    existingReport.addActionListener(this);
    p2.add(existingReport);
    quit.addActionListener(this);
    p2.add(quit);
    getContentPane().add("South", p2);

    // Show it
    show();

}  // End StartupDialog()


  /** Callback to handle all user interactions */
  public void actionPerformed(ActionEvent ae) {

    String ac = ae.getActionCommand();

    // Set the starting report directory (null is default home directory)
    // Store the report directory in the preferences for this package
    Preferences prefs = Preferences.userRoot().node("/jimm/datavision");
    String reportDir = prefs.get("reportDir", null);
    System.out.println("reportDir(1) = " + reportDir);

    // Open an existing report
    if (ac.equalsIgnoreCase(I18N.get("StartupDialog.open"))) {
	    JFileChooser jfc = new JFileChooser();
      if (reportDir != null) {
        jfc.setCurrentDirectory(new File(reportDir));
      }
	    jfc.setMultiSelectionEnabled(false);
	    int rv = jfc.showOpenDialog(this);
	    if (rv == JFileChooser.APPROVE_OPTION) {
	      selectedFile = jfc.getSelectedFile().getPath();
        // If the file path has changed then store it in the preferences
        String jfcap = jfc.getSelectedFile().getAbsolutePath();
        if (jfcap != null) {
          File f = new File(jfcap);
          String newReportDir = f.getParent();
          if (newReportDir == null) {
            newReportDir = reportDir;
          }
          if (newReportDir != null) {
            boolean changed = true;
            if (reportDir != null && newReportDir.compareTo(reportDir) == 0) {
              changed = false;
            }
            if (changed) {
              prefs.put("reportDir", newReportDir);
            }
          }
        }
	      dispose();
	    }

    // Start a new report
    } else if (ac.equalsIgnoreCase(I18N.get("StartupDialog.new"))) {
	    selectedFile = NEW_REPORT_STRING;
	    dispose();

    // Quit
    } else if (ac.equalsIgnoreCase(I18N.get("StartupDialog.quit"))) {
	    dispose();
	    System.exit(0);
    }

  } // End actionPerformed()


/** Method to return the selected File object. */
public String getSelectedFile() {
    return selectedFile;
} // End getSelectedFile()


} // End Class

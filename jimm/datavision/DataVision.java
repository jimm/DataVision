package jimm.datavision;
import jimm.datavision.source.charsep.CharSepSource;
import jimm.datavision.layout.*;
import jimm.datavision.layout.swing.SwingLE;
import jimm.datavision.layout.pdf.PDFLE;
import jimm.datavision.layout.excel.ExcelLE;
import jimm.datavision.gui.DesignWin;
import jimm.datavision.gui.StartupDialog;
import jimm.util.XMLWriter;
import jimm.util.Getopts;
import jimm.util.I18N;
import java.io.*;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * This is the DataVision application class. It opens a design window
 * for each report specified on the command line. If none are specified,
 * it openes a new design window on a new, empty report.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DataVision {

protected static final String DEFAULT_CHARACTER_SEPARATOR = ",";

char layoutEngineChoice;
String layoutEngineFileName;
String dbPassword;
String paramXMLFile;
int numReports;
boolean usesGUI;
String charSepFile;
char sepChar;
String reportDir;
String outputDir;

/**
 * This main application method opens a design window for each report
 * specified on the command line. If none are specified, it openes a new
 * design window on a new, empty report.
 *
 * @param args command line array; each element is assumed to be a report
 * file name.
 */
public static void main(String[] args) {

  Getopts g = new Getopts("a:c:d:e:f:g:h:i:l:np:qr:s:wx:E:R:o:", args);
  if (g.error()) {		// Any bad command line argument?
	  usage(null);		// If so, whine and exit
	}

  // Get user's preferences, if any.
  Preferences prefs = Preferences.userRoot().node("/jimm/datavision");

  // Set look & feel.
  try {
    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
  } catch (Exception e) {
    // Default L&F will be used if any problems occur, most probably,
    // ClassNotFound.
  }

    DataVision dv = new DataVision();

    // Language
    if (g.hasOption('g') || g.hasOption('i'))
	I18N.setLanguage(new Locale(g.option('g', "").toLowerCase(),
				    g.option('i', "").toUpperCase()));

    dv.layoutEngineChoiceFromOptions(g);
    dv.dataSourceFromOptions(g);

    if (dv.hasLayoutEngine()) {
	if (dv.usesGUI())	// Can ask for password via GUI
	    ErrorHandler.useGUI(true);
	else if (!g.hasOption('n') && !g.hasOption('p') && !g.hasOption('e'))
	    usage(I18N.get("DataVision.n_or_p"));
    }

    dv.paramXMLFile = g.option('r', null); // Parameter XML file name or null
    dv.reportDir = g.option('R', null); // Report Directory or null
    dv.outputDir = g.option('o', null); // Output Directory or null

    // Store the report directory in the preferences for this package
    // These values are stored in the root package jimm.datavision
    if (dv.reportDir != null) {
        prefs.put("reportDir",dv.reportDir);
    }
    if (dv.outputDir != null) {
        prefs.put("outputDir",dv.outputDir);
    }

    if (g.argc() == 0) {
	if (startupDialog())	// Returns true if we should exit
	    return;

	if (dv.hasLayoutEngine()) // Have layout engine but no file
	    usage(I18N.get("DataVision.xml_req"));
	else {
	    try {
		dv.designReport(g, null);
	    }
	    catch (Exception e) { // Global catch-all
		ErrorHandler.error(e);
	    }
	}
    }
    else {			// Loop over input files
	dv.numReports = g.argc();
	for (int i = 0; i < g.argc(); ++i) {
            File f = new File(g.argv(i));
	    try {
		if (dv.hasLayoutEngine())
		    dv.runReport(g, f);
		else
		    dv.designReport(g, f);
	    }
	    catch (Exception e) { // Global catch-all
		ErrorHandler.error(e);
	    }
	}
    }

    // For some odd reason, on Mac OS X with Java 1.4.1 the app hangs
    // below, just before the last closing brace when run from the command
    // line unless I add this System.exit(). I hate fixing bugs without
    // knowing what is going wrong.
    if (dv.hasLayoutEngine() && dv.getLayoutEngineChoice() != 'w')
	System.exit(0);
}

void designReport(Getopts g, File reportXMLFile) throws FileNotFoundException {
    DesignWin win = new DesignWin(reportXMLFile, dbPassword);
    Report report = win.getReport();

    if (charSepFile != null) { // Must come after file read
	CharSepSource src = (CharSepSource)report.getDataSource();
	src.setSepChar(sepChar);
	src.setInput(charSepFile);
    }

    if (g.hasOption('q'))
	report.setCaseSensitiveDatabaseNames(false);
}

/**
 * Shows startup dialog and returns <code>true</code> if we should exit
 * the application.
 *
 * @return <code>true</code> if we should exit the application
 */
static boolean startupDialog() {
    // Show the startup dialog and await return from it
    StartupDialog sd = new StartupDialog();

    // Get the file the user selected on the startup dialog, if any
    String selectedFile = sd.getSelectedFile();

    // If they clicked the close window button, we'll get back null, and we'll
    // return true so the app exits.
    if (selectedFile == null)
	return true;

    // If we DID NOT get the value "*StartANewReport*", then they must have
    // selected a file, so let's insert it into the parameters array and call
    // back to this main method so it looks like we started the app with the
    // file in the command line
    if (!selectedFile.equalsIgnoreCase(StartupDialog.NEW_REPORT_STRING)) {
	String[] newArgs = { selectedFile };
	main(newArgs);
	return true;
    }

    return false;		// The user didn't select a file
}

void runReport(Getopts g, File reportXMLFile) throws Exception {
    Report report = new Report();

    if (dbPassword != null)
	report.setDatabasePassword(dbPassword);

    report.read(reportXMLFile); // Must come after password set

    if (paramXMLFile != null) // Must come after file read
	report.setParameterXMLInput(new File(paramXMLFile));

    if (charSepFile != null) { // Must come after file read
	CharSepSource src = (CharSepSource)report.getDataSource();
	src.setSepChar(sepChar);
	src.setInput(charSepFile);
    }

    if (g.hasOption('q'))
	report.setCaseSensitiveDatabaseNames(false);

    report.setLayoutEngine(createLayoutEngine(reportXMLFile, g));
    report.runReport();
}


boolean hasLayoutEngine() { return layoutEngineChoice != '\0'; }
char getLayoutEngineChoice() { return layoutEngineChoice; }

boolean usesGUI() { return usesGUI; }

void layoutEngineChoiceFromOptions(Getopts g) {
    String errMsg = I18N.get("DataVision.le_one");

    layoutEngineChoice = '\0';
    if (g.hasOption('c')) {
	layoutEngineChoice = 'c';
	layoutEngineFileName = g.option('c', null);
    }
    if (g.hasOption('d')) {
	if (layoutEngineChoice != '\0') usage(errMsg);
	layoutEngineChoice = 'd';
	layoutEngineFileName = g.option('d', null);
    }
    if (g.hasOption('f')) {
	if (layoutEngineChoice != '\0') usage(errMsg);
	layoutEngineChoice = 'f';
	layoutEngineFileName = g.option('f', null);
    }
    if (g.hasOption('h')) {
	if (layoutEngineChoice != '\0') usage(errMsg);
	layoutEngineChoice = 'h';
	layoutEngineFileName = g.option('h', null);
    }
    if (g.hasOption('l')) {
	if (layoutEngineChoice != '\0') usage(errMsg);
	layoutEngineChoice = 'l';
	layoutEngineFileName = g.option('l', null);
    }
    if (g.hasOption('x')) {
  if (layoutEngineChoice != '\0') usage(errMsg);
	layoutEngineChoice = 'x';
	layoutEngineFileName = g.option('x', null);
    }
    if (g.hasOption('E')) {
  if (layoutEngineChoice != '\0') usage(errMsg);
  layoutEngineChoice = 'E';
  layoutEngineFileName = g.option('E', null);
    }
    if (g.hasOption('w')) {
	if (layoutEngineChoice != '\0') usage(errMsg);
	layoutEngineChoice = 'w';
	usesGUI = true;
    }
    if (g.hasOption('t')) {
  if (layoutEngineChoice != '\0') usage(errMsg);
	layoutEngineChoice = 't';
	layoutEngineFileName = g.option('t', null);
    }
}

LayoutEngine createLayoutEngine(File f, Getopts g) throws IOException
{
    LayoutEngine le = null;

    // Create new file name.
    String fileName = f.getName();
    String fileNameSansExtension = null;
    if (layoutEngineFileName != null) {
	int pos = fileName.lastIndexOf('.');
	if (pos == -1)
	    fileNameSansExtension = fileName;
	else
	    fileNameSansExtension = fileName.substring(0, pos);
    }

    String outFileName = null;
    PrintWriter out = null;
    switch (layoutEngineChoice) {
    case 'c':
	char sep = g.option('s', DEFAULT_CHARACTER_SEPARATOR).charAt(0);
	if (layoutEngineFileName != null)
	    outFileName = layoutEngineFileName;
	else {
	    outFileName = fileNameSansExtension
		+ (sep == ',' ? ".csv" : ".tab");
	    if (outFileName.equals(fileName))
		outFileName = fileNameSansExtension + "_out"
		    + (sep == ',' ? ".csv" : ".tab");
	}
	out = new PrintWriter(new FileWriter(outFileName));
	le = new CharSepLE(out, sep);
	break;
    case 'd':
	if (layoutEngineFileName != null)
	    outFileName = layoutEngineFileName;
	else {
	    outFileName = fileNameSansExtension + ".sgml";
	    if (outFileName.equals(fileName))
		outFileName = fileNameSansExtension + "_out.sgml";
	}
	out = new PrintWriter(new FileWriter(outFileName));
	le = new DocBookLE(out);
	break;
    case 'f':
	if (layoutEngineFileName != null)
	    outFileName = layoutEngineFileName;
	else {
	    outFileName = fileNameSansExtension + ".pdf";
	    if (outFileName.equals(fileName))
		outFileName = fileNameSansExtension + "_out.pdf";
	}
	OutputStream outStream = new FileOutputStream(outFileName);
	le = new PDFLE(outStream);
	break;
    case 'h':
	if (layoutEngineFileName != null)
	    outFileName = layoutEngineFileName;
	else {
	    outFileName = fileNameSansExtension + ".html";
	    if (outFileName.equals(fileName))
		outFileName = fileNameSansExtension + "_out.html";
	}
	out = new PrintWriter(new FileWriter(outFileName));
	le = new HTMLLE(out);
	break;

    case 't':
	if (layoutEngineFileName != null)
	    outFileName = layoutEngineFileName;
	else {
	    outFileName = fileNameSansExtension + ".html";
	    if (outFileName.equals(fileName))
		outFileName = fileNameSansExtension + "_out.html";
	}
	out = new PrintWriter(new FileWriter(outFileName));
	le = new CSSHTMLLE(out);
	break;

    case 'l':
	if (layoutEngineFileName != null)
	    outFileName = layoutEngineFileName;
	else {
	    outFileName = fileNameSansExtension + ".tex";
	    if (outFileName.equals(fileName))
		outFileName = fileNameSansExtension + "_out.tex";
	}
	out = new PrintWriter(new FileWriter(outFileName));
	le = new LaTeXLE(out);
	break;
    case 'x':
	if (layoutEngineFileName != null)
	    outFileName = layoutEngineFileName;
	else {
	    outFileName = fileNameSansExtension + ".xml";
	    if (outFileName.equals(fileName))
		outFileName = fileNameSansExtension + "_out.xml";
	}
	XMLWriter iout = new XMLWriter(new FileWriter(outFileName));
	le = new XMLLE(iout);
	break;
    case 'E':
  if (layoutEngineFileName != null)
    outFileName = layoutEngineFileName;
  else {
    outFileName = fileNameSansExtension + ".xls";
    if (outFileName.equals(fileName))
    outFileName = fileNameSansExtension + "_out.xls";
  }
  OutputStream xlsStream = new FileOutputStream(outFileName);
  le = new ExcelLE(xlsStream,false);
  break;
    case 'w':
  le = new SwingLE() {
	    public void close() { // Override close() to possibly exit app
		super.close();
		swingLayoutEngineClosed();
	    }
	    };
	break;
    }
    return le;
}

/**
 * We arrange this method to be called by a Swing layout engine when
 * it closes. If appropriate, exit the app.
 */
protected void swingLayoutEngineClosed() {
    if (--numReports == 0)
	System.exit(0);
}

/**
 * Use the options related to the data source defined in the report XML file.
 */
protected void dataSourceFromOptions(Getopts g) {
    if (g.hasOption('n')) {	// No password/empty password
	if (g.hasOption('p') || g.hasOption('e'))
	    usage(I18N.get("DataVision.n_and_p"));
	dbPassword = "";
    }
    else if (g.hasOption('p')) {
	if (g.hasOption('n') || g.hasOption('e'))
	    usage(I18N.get("DataVision.n_and_p"));
	dbPassword = g.option('p', null); // Grab password; default is null
    }
    else if (g.hasOption('e')) {
	if (g.hasOption('n') || g.hasOption('p'))
	    usage(I18N.get("DataVision.n_and_p"));
	charSepFile = g.option('e');
	sepChar = g.option('a', DEFAULT_CHARACTER_SEPARATOR).charAt(0);
    }
}

public String toString() {
    return "DataVision [layoutEngineChoice=" + layoutEngineChoice
	+ ", dbPassword = " + dbPassword
	+ ", paramXMLFile = " + paramXMLFile
	+ ", numReports = " + numReports
	+ ", usesGUI = " + usesGUI
	+ ", charSepFile = " + charSepFile
	+ ", sepChar = " + sepChar
	+ "]";
}

/**
 * Prints a usage message and an optional extra error message to System.err
 * and exits.
 *
 * @param errMsg string to print; may be <code>null</code>
 */
public static void usage(String errMsg) {
    if (errMsg != null)
	System.err.println(errMsg);
    System.err.println("DataVision version " + info.Version);
    System.err.println(I18N.get("DataVision.usage"));

    System.exit(1);
}

}

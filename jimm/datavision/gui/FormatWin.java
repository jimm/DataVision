package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.gui.cmd.FormatCommand;
import jimm.util.I18N;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * A field format editing dialog box. There are tabs for text format and
 * borders. The initial format and border values are retrieved from one of
 * two places: either a field passed in to the constructor or the first
 * selected field in the desing window. The format and border are applied to
 * either a single field or all selected fields.
 * <p>
 * <i>Warning</i>: this code depends upon the fact that the strings in
 * <code>edgeCountChoices</code> equals the integer value of the string
 * (zero, one, two, etc.) and that the strings in
 * <code>edgeStyleChoices</code> correspond to the numeric values of the
 * <code>BorderEdge.STYLE_*</code> constants.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FormatWin extends EditWin implements FieldWalker {

// ================================================================
/**
 * Holds a border edge and the widgets used for editing it.
 */
static class EdgeWidgets {

protected BorderEdge edge;
protected String name;
protected JComboBox numberComboBox;
protected JComboBox styleComboBox;
protected JTextField thicknessText;

EdgeWidgets(BorderEdge e, String name) {
    edge = e;
    this.name = name;
}

String getName() { return name; }

}
// ================================================================

/**
 * List of pre-approved font size choices. Don't use this directly; instead
 * call {@link #sizeChoices}.
 */
protected static Integer[] SIZE_CHOICES;

protected static final int FORMAT_TEXT_FIELD_COLS = 20;
protected static final int THICKNESS_COLS = 8;

protected static final int TOP = 0;
protected static final int LEFT = 1;
protected static final int RIGHT = 2;
protected static final int BOTTOM = 3;

protected static String[] fontFamilyNames;

protected Field field;
protected Format format;
protected Border border;
protected Format origFormat;
protected Border origBorder;
protected boolean saveRevertInfo;
protected JComboBox fontFamily;
protected JComboBox size;
protected JCheckBox bold;
protected JCheckBox italic;
protected JCheckBox underline;
protected JCheckBox wrap;
protected JComboBox align;
protected JTextField formatText;
protected JLabel fieldColorLabel;
protected JLabel borderColorLabel;
protected EdgeWidgets[] edgeWidgets;

/**
 * This method loads all the font family names in a separate thread. It is
 * called each time a design window is created, though it only does its
 * thang the first time it is called.
 */
public static void loadFontChoices() {
    if (fontFamilyNames == null) {
	new Thread(new Runnable() {
	    public void run() {
		fontFamilyNames =
		    GraphicsEnvironment.getLocalGraphicsEnvironment()
		    .getAvailableFontFamilyNames();
	    }
	    }).start();
    }
}

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param f a field from which we will take the format and border
 * @param whichTab the index of the tab to display when opened
 */
public FormatWin(Designer designer, Field f, int whichTab) {
    super(designer,
	  I18N.get("FormatWin.title")
	  + (f.designLabel().startsWith("{") ? " " : " (")
	  + f.designLabel()
	  + (designer.countSelectedFields() > 1 ? " +" : "")
	  + (f.designLabel().startsWith("{") ? " " : " )"),
	  "FormatCommand.name");

    field = f;

    origFormat = field.getFormat();
    if (origFormat != null) origFormat = (Format)origFormat.clone();
    origBorder = field.getBorder();
    if (origBorder != null) origBorder = (Border)origBorder.clone();

    copyFormatAndBorder(field.getFormat(), field.getBorder());

    buildWindow(whichTab);
    pack();
    setVisible(true);
}

/**
 * Saves copies of format and border into the objects that we really edit.
 * Either may be <code>null</code>. Called from constructor and
 * {@link #doRevert}.
 *
 * @param origFormat the format we are copying; not necessarily that of
 * the field
 * @param origBorder the border we are copying; not necessarily that of
 * the field
 */
protected void copyFormatAndBorder(Format origFormat, Border origBorder) {
    format = (Format)origFormat.clone();

    if (origBorder == null) {
	border = (Border)field.getReport().getDefaultField().getBorder()
	    .clone();
	border.setField(field);
    }
    else
	border = (Border)origBorder.clone();

    if (border.getTop() == null)
	border.setTop(new BorderEdge(BorderEdge.STYLE_LINE, 1, 0));
    if (border.getBottom() == null)
	border.setBottom(new BorderEdge(BorderEdge.STYLE_LINE, 1, 0));
    if (border.getLeft() == null)
	border.setLeft(new BorderEdge(BorderEdge.STYLE_LINE, 1, 0));
    if (border.getRight() == null)
	border.setRight(new BorderEdge(BorderEdge.STYLE_LINE, 1, 0));
}

/**
 * Builds the window contents.
 *
 * @param whichTab the index of the tab to display when opened
 */
protected void buildWindow(int whichTab) {
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab(I18N.get("FormatWin.format_tab"), buildFormatTab());
    tabbedPane.addTab(I18N.get("FormatWin.border_tab"), buildBorderTab());
    tabbedPane.setSelectedIndex(whichTab);

    // Ok, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add edit panes and buttons to window
    getContentPane().add(tabbedPane, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    fillFormatTab();
    fillBorderTab();
}

/**
 * Builds the format tab contents.
 */
protected java.awt.Container buildFormatTab() {
    EditFieldLayout efl = new EditFieldLayout();

    fontFamily = efl.addComboBox(I18N.get("FormatWin.font"), fontChoices(),
				 true);
    size = efl.addComboBox(I18N.get("FormatWin.size"), sizeChoices(), true);
    bold = efl.addCheckBox(I18N.get("FormatWin.bold"), KeyEvent.VK_B);
    italic = efl.addCheckBox(I18N.get("FormatWin.italic"), KeyEvent.VK_I);
    underline = efl.addCheckBox(I18N.get("FormatWin.underline"),
				KeyEvent.VK_U);
    wrap = efl.addCheckBox(I18N.get("FormatWin.wrap"), KeyEvent.VK_W);
    align = efl.addComboBox(I18N.get("FormatWin.align"), alignChoices());
    formatText = efl.addTextField(I18N.get("FormatWin.format"),
				  FORMAT_TEXT_FIELD_COLS);
    efl.add(I18N.get("FormatWin.color"), buildFieldColorWidgets());

    // Put a wrapper around that panel so everything is centered in the tab.
    Box outerBox = Box.createVerticalBox();
    outerBox.add(Box.createGlue());
    outerBox.add(efl.getPanel());
    outerBox.add(Box.createGlue());

    return outerBox;
}

protected Box buildFieldColorWidgets() {
    Box box = Box.createHorizontalBox();
    fieldColorLabel = new JLabel(I18N.get("FormatWin.sample_text"));
    box.add(fieldColorLabel);
    setFieldExampleColor();
    box.add(Box.createHorizontalStrut(16));
    box.add(createFieldColorChooserButton());
    return box;
}

protected Box buildBorderColorWidgets() {
    Box box = Box.createHorizontalBox();
    borderColorLabel = new JLabel(I18N.get("FormatWin.sample_text"));
    box.add(borderColorLabel);
    setBorderExampleColor();
    box.add(Box.createHorizontalStrut(16));
    box.add(createBorderColorChooserButton());

    Box widgetBox = Box.createVerticalBox();
    widgetBox.add(Box.createVerticalGlue());
    widgetBox.add(new JLabel(I18N.get("FormatWin.color")));
    widgetBox.add(Box.createVerticalStrut(6));
    widgetBox.add(box);
    widgetBox.add(Box.createVerticalGlue());

    return widgetBox;
}


protected void setFieldExampleColor() {
    fieldColorLabel.setForeground(format.getColor());
}

protected void setBorderExampleColor() {
    borderColorLabel.setForeground(border.getColor());
}

protected JButton createFieldColorChooserButton() {
    JButton b = new JButton(I18N.get("FormatWin.choose"));
    b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    Color c = JColorChooser.showDialog(FormatWin.this,
					       I18N.get("FormatWin.field_color_title"),
					       format.getColor());
	    if (c != null) {
		format.setColor(c);
		setFieldExampleColor();
	    }
	}
	});
    return b;
}

protected JButton createBorderColorChooserButton() {
    JButton b = new JButton(I18N.get("FormatWin.choose"));
    b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    Color c = JColorChooser.showDialog(FormatWin.this,
					       I18N.get("FormatWin.border_color_title"),
					       border.getColor());
	    if (c != null) {
		border.setColor(c);
		setBorderExampleColor();
	    }
	}
	});
    return b;
}

/**
 * Returns the list of font size choices, lazily instantiating it if
 * necessary.
 *
 * @return an array of <code>Integer</code> size choices
 */
protected Integer[] sizeChoices() {
    if (SIZE_CHOICES == null) {
	ArrayList list = new ArrayList();

	for (int size = 6; size <= 12; ++size)
	    list.add(new Integer(size));
	for (int size = 14; size <= 36; size += 2)
	    list.add(new Integer(size));

	SIZE_CHOICES = new Integer[list.size()];
	list.toArray(SIZE_CHOICES);
    }
    return SIZE_CHOICES;
}

/**
 * Returns the index of the specified size value in the SIZE_CHOICES list.
 * Returns -1 if not found.
 *
 * @return an index, or -1 if not found
 */
protected int sizeIndexOf(int size) {
    Integer[] sizeChoices = sizeChoices();
    for (int i = 0; i < sizeChoices.length; ++i)
	if (sizeChoices[i].intValue() == size)
	    return i;
    return -1;
}

/**
 * Returns the list of font choices.
 *
 * @return an array of font family names
 */
protected String[] fontChoices() {
    return fontFamilyNames;
}

/**
 * Returns the index of the specified font family name. Returns -1 if
 * not found.
 *
 * @return an index, or -1 if not found
 */
protected int fontIndexOf(String fontFamilyName) {
    if (fontFamilyName == null || fontFamilyName.length() == 0)
	return -1;

    String[] names = fontChoices();
    for (int i = 0; i < names.length; ++i)
	if (names[i].equals(fontFamilyName))
	    return i;

    return -1;
}

protected String[] alignChoices() {
    String[] choices = new String[3];
    int i = 0;
    choices[i++] = I18N.get("FormatWin.align_left");
    choices[i++] = I18N.get("FormatWin.align_center");
    choices[i++] = I18N.get("FormatWin.align_right");
    return choices;
}

protected String[] edgeCountChoices() {
    String[] choices = new String[4];
    for (int i = 0; i < 4; ++i)
	choices[i] = I18N.get("FormatWin.edge_count_" + i);
    return choices;
}

protected String[] edgeStyleChoices() {
    String[] choices = new String[3];
    int i = 0;
    choices[i++] = I18N.get("FormatWin.edge_style_line");
    choices[i++] = I18N.get("FormatWin.edge_style_dashed");
    choices[i++] = I18N.get("FormatWin.edge_style_dotted");
    return choices;
}

/**
 * Builds the border tab contents.
 */
protected Box buildBorderTab() {
    edgeWidgets = new EdgeWidgets[4];

    Box vertBox = Box.createVerticalBox();

    Box horizBox = Box.createHorizontalBox();
    horizBox.add(Box.createHorizontalGlue());
    horizBox.add(buildBorderEdge(TOP, I18N.get("FormatWin.edge_top"),
				 border.getTop()));
    horizBox.add(Box.createHorizontalGlue());
    vertBox.add(horizBox);

    horizBox = Box.createHorizontalBox();
    horizBox.add(buildBorderEdge(LEFT, I18N.get("FormatWin.edge_left"),
				 border.getLeft()));
    horizBox.add(buildBorderColorWidgets());
    horizBox.add(buildBorderEdge(RIGHT, I18N.get("FormatWin.edge_right"),
				 border.getRight()));
    vertBox.add(horizBox);

    horizBox = Box.createHorizontalBox();
    horizBox.add(Box.createHorizontalGlue());
    horizBox.add(buildBorderEdge(BOTTOM, I18N.get("FormatWin.edge_bottom"),
				 border.getBottom()));
    horizBox.add(Box.createHorizontalGlue());
    vertBox.add(horizBox);

    return vertBox;
}

/**
 * Builds one of the edges of the border.
 * <p>
 * <i>Warning</i>: this code depends upon the fact that the strings in
 * <code>edgeCountChoices</code> equals the integer value of the string
 * (zero, one, two, etc.) and that the strings in
 * <code>edgeStyleChoices</code> correspond to the numeric values of the
 * <code>BorderEdge.STYLE_*</code> constants.
 *
 * @param edgeIndex one of <code>TOP</code>, <code>LEFT</code>, etc.
 * @param edgeName the text name of the widget
 * @param edge the edge we are representing visually
 * @return one box to rule them all, one box to bind them
 */
protected Box buildBorderEdge(int edgeIndex, String edgeName,
			      final BorderEdge edge)
{
    // Create container for all widgets associated with this edge
    EdgeWidgets ew = new EdgeWidgets(edge, edgeName);
    edgeWidgets[edgeIndex] = ew;

    EditFieldLayout efl = new EditFieldLayout();
    ew.numberComboBox = efl.addComboBox(I18N.get("FormatWin.count"),
					edgeCountChoices());
    ew.styleComboBox = efl.addComboBox(I18N.get("FormatWin.style"),
				       edgeStyleChoices());
    ew.thicknessText = efl.addTextField(I18N.get("FormatWin.thickness"),
					THICKNESS_COLS);

    // Behavior for line count combo box
    final JComboBox edgeBox = ew.numberComboBox;
    edgeBox.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    String countStr = (String)edgeBox.getSelectedItem();
	    String[] choices = edgeCountChoices();
	    for (int i = 0; i < choices.length; ++i)
		if (choices[i].equals(countStr)) {
		    edge.setNumber(i);
		    break;
		}
	}
	});

    // Behavior for line style combo box
    final JComboBox styleBox = ew.styleComboBox;
    styleBox.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    String countStr = (String)styleBox.getSelectedItem();
	    String[] choices = edgeStyleChoices();
	    for (int i = 0; i < choices.length; ++i)
		if (choices[i].equals(countStr)) {
		    edge.setStyle(i);
		    break;
		}
	}
	});

    // Parent panel
    Box box = Box.createVerticalBox();
    box.add(new JLabel(ew.getName()));
    box.add(Box.createVerticalStrut(8));
    box.add(efl.getPanel());

    return box;
}

/**
 * Fills the format tab edit fields with values of format.
 */
protected void fillFormatTab() {
    String fontFamilyName = format.getFontFamilyName();
    if (fontFamilyName == null) fontFamilyName = "";
    int index = fontIndexOf(fontFamilyName);
    if (index == -1) {
	fontFamily.setSelectedItem(null);
	fontFamily.configureEditor(fontFamily.getEditor(), fontFamilyName);
    }
    else
	fontFamily.setSelectedIndex(index);

    int sizeVal = (int)format.getSize();
    index = sizeIndexOf(sizeVal);
    if (index == -1) {
	size.setSelectedItem(null);
	size.configureEditor(size.getEditor(), "" + sizeVal);
    }
    else
	size.setSelectedIndex(index);

    bold.setSelected(format.isBold());
    italic.setSelected(format.isItalic());
    underline.setSelected(format.isUnderline());
    wrap.setSelected(format.isWrap());

    // Assumes format align value == index in alignChoices()
    align.setSelectedIndex(format.getAlign());

    formatText.setText(format.getFormat());
}

/**
 * Fills the border tab edit fields with values of border.
 */
protected void fillBorderTab() {
    for (int i = 0; i < 4; ++i) {
	EdgeWidgets ew = edgeWidgets[i];

	// Fill count. Assumes edge number (line count) value == index in
	// edgeCountChoices().
	ew.numberComboBox.setSelectedIndex(ew.edge.getNumber());

	// Fill styles. Assumes edge style value == index in
	// edgeStyleChoices().
	ew.styleComboBox.setSelectedIndex(ew.edge.getStyle());

	// Fill thicknesses.
	ew.thicknessText.setText("" + ew.edge.getThickness());
    }
}

protected void doSave() {
    Object selectedFont = fontFamily.getSelectedItem();
    if (selectedFont == null || selectedFont.toString().length() == 0)
	format.setFontFamilyName(null);
    else
	format.setFontFamilyName(selectedFont.toString());

    double fontSize = Double.parseDouble(size.getSelectedItem().toString());
    format.setSize(fontSize);
    format.setBold(bold.isSelected());

    format.setItalic(italic.isSelected());
    format.setUnderline(underline.isSelected());
    format.setWrap(wrap.isSelected());

    String alignText = (String)align.getSelectedItem();
    if (alignText.equals(I18N.get("FormatWin.align_left")))
	format.setAlign(Format.ALIGN_LEFT);
    else if (alignText.equals(I18N.get("FormatWin.align_center")))
	format.setAlign(Format.ALIGN_CENTER);
    else			// Right
	format.setAlign(Format.ALIGN_RIGHT);

    format.setFormat(formatText.getText());

    // The border count (number) and style save themselves, but we need to
    // read and set the thickness.
    for (int i = 0; i < 4; ++i) {
	EdgeWidgets ew = edgeWidgets[i];
	ew.edge.setThickness(Double.parseDouble(ew.thicknessText.getText()));
    }

    if (designer.countSelectedFields() == 0
	|| field == field.getReport().getDefaultField()) // "==", not "equals"
	step(field);
    else			// Call step() for all selected fields
	designer.withSelectedFieldsDo(this);
}

/**
 * Creates and performs a command that gives the format and borders to the
 * specified field.
 *
 * @param f the field
 */
public void step(Field f) {
    FormatCommand cmd = new FormatCommand(f, format, border);
    cmd.perform();
    commands.add(cmd);
}

protected void doRevert() {
    fillFormatTab();
    fillBorderTab();
}

}

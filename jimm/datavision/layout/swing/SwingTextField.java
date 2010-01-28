package jimm.datavision.layout.swing;
import jimm.datavision.field.Field;
import jimm.datavision.field.Format;
import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;

/**
 * A Swing field is the visual representation of a report field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see SwingLE
 */
public class SwingTextField extends AbstractSwingField {

public static final Color HIDDEN_FG_COLOR = Color.gray;

/**
 * Constructor.
 *
 * @param f report field
 */
public SwingTextField(Field f) {
    this(f, f.toString());
}

/**
 * Constructor.
 *
 * @param f report field
 * @param str label text
 */
public SwingTextField(Field f, String str) {
    super(f, new JTextPane());
    JTextPane textPane = (JTextPane)getComponent();
    textPane.setText(str);
    textPane.setEditable(false);
    format();
}

/**
 * Formats the label according to the field's formatting specifications.
 */
public void format() {
    JTextPane textPane = (JTextPane)getComponent();

    Format format = field.getFormat();
    Style style = StyleContext.getDefaultStyleContext()
	.getStyle(StyleContext.DEFAULT_STYLE);

    // Save selection and select all text
    int selStart = textPane.getSelectionStart();
    int selEnd = textPane.getSelectionEnd();
    textPane.selectAll();

    StyleConstants.setBold(style, format.isBold());
    StyleConstants.setItalic(style, format.isItalic());
    StyleConstants.setUnderline(style, format.isUnderline());
    StyleConstants.setFontSize(style, (int)format.getSize());
    StyleConstants.setFontFamily(style, format.getFontFamilyName());

    // Color is based on visibility flag of field
    StyleConstants.setForeground(style, getColor(format));

    // Align
    switch (format.getAlign()) {
    case Format.ALIGN_CENTER:
	StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
	break;
    case Format.ALIGN_RIGHT:
	StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
	break;
    default:
	StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
	break;
    }
    textPane.setParagraphAttributes(style, true);

    // Restore selection
    textPane.setCaretPosition(selStart);
    textPane.moveCaretPosition(selEnd);

    makeBorders();
}

/**
 * Returns field color based on visibility.
 */
public Color getColor() {
    return getColor(field.getFormat());
}

/**
 * Returns field color based on visibility.
 */
public Color getColor(Format format) {
    Color color = format.getColor();
    if (!field.isVisible()) {	// If hidden, lighten color
	if (color.equals(Color.black))
	    color = HIDDEN_FG_COLOR;
	else {
	    float[] hsb = new float[3];

	    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(),
			   hsb);
	    hsb[1] = (float)0.5; // Saturation
	    hsb[2] = (float)0.9; // Brightness
	    color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
    }
    return color;
}


}

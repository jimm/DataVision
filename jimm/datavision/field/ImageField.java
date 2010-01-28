package jimm.datavision.field;
import jimm.datavision.Report;
import jimm.datavision.Section;
import jimm.datavision.ErrorHandler;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.ImageFieldWidget;
import jimm.datavision.gui.SectionWidget;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.MediaTracker;
import javax.swing.ImageIcon;
import javax.swing.GrayFilter;

/**
 * Represents an external image. The <code>value</code> instance value
 * stores the images file's path.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ImageField extends Field {

public static final String TYPE_STRING = "image";

protected URL imageURL;
protected ImageIcon imageIcon;
protected ImageIcon hiddenImageIcon;

/**
 * Constructor. We make sure the value (a file path) is an absolute file
 * path.
 *
 * @param id the unique identifier for the new field
 * @param report the report containing this line
 * @param section the section containing this line
 * @param value the value; a file path string
 * @param visible show/hide flag
 */
public ImageField(Long id, Report report, Section section, Object value,
		  boolean visible)
{
    super(id, report, section, null, visible);
    setValue(value);
}

/**
 * Always returns the bounds height.
 */
public double getOutputHeight() {
    return bounds.height;
}

/**
 * Returns the image URL.
 *
 * @return the image URL
 */
public URL getImageURL() { return imageURL; }

/**
 * Returns the image icon, visually dimmed if the field is hidden.
 *
 * @return the image icon, dimmed if the field is not visible
 */
public ImageIcon getImageIcon() {
    if (isVisible())
	return getVisibleImageIcon();
    else
	return getHiddenImageIcon();
}

/**
 * Returns the image icon.
 *
 * @return the image icon
 */
public ImageIcon getVisibleImageIcon() {
    if (imageIcon == null && value != null)
	imageIcon = new ImageIcon(getImageURL());
    return imageIcon;
}

/**
 * Returns a dimmed version of the the image icon.
 *
 * @return a dimmed version of the the image icon
 */
public ImageIcon getHiddenImageIcon() {
    if (hiddenImageIcon == null && value != null && canLoad()) {
	ImageIcon ii = getVisibleImageIcon();
	if (ii != null) {
	    hiddenImageIcon =
		new ImageIcon(GrayFilter.createDisabledImage(ii.getImage()));
	}
    }
    return hiddenImageIcon;
}

/**
 * Sets our value and image URL.
 * <p>
 * 
 */
public void setValue(Object newValue) {
    imageIcon = null;
    String str = newValue.toString();

    // If the string is not a URL, add "file:" to the beginning of the
    // string.
    if (str.indexOf(":/") == -1 && !str.startsWith("file:"))
	str = "file:" + str;

    try {
	imageURL = (str == null || str.length() == 0) ? null : new URL(str);
	super.setValue(newValue);	// Notify observers
    }
    catch (MalformedURLException e) {
	ErrorHandler.error(e);
    }
}

public boolean canLoad() {
    return getVisibleImageIcon() != null
	&& getVisibleImageIcon().getImageLoadStatus() == MediaTracker.COMPLETE;
}

public FieldWidget makeWidget(SectionWidget sw) {
    return new ImageFieldWidget(sw, this);
}

public String dragString() {
    return typeString() + ":" + value;
}

public String typeString() { return TYPE_STRING; }

public String formulaString() { return "{" + value + "}"; }

public String toString() {
    if (!visible) return null;

    Object v = getValue();
    return v == null ? "" : v.toString();
}

}

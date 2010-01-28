/*
 * PermField.java
 *
 * Created on June 24, 2004, 2:22 PM
 */

package jimm.datavision.layout.excel;

import jimm.datavision.*;
import jimm.datavision.field.Border;
import jimm.datavision.field.Field;
import jimm.datavision.field.Format;
import jimm.datavision.field.Rectangle;

/**
 *
 * @author  dbeeler
 */
public class PermField {
    
    private String strFieldText;
    private Rectangle boundField;
    private Border borderField;
    private Format formatField;
        
    /** Creates a new instance of PermField */
    public PermField(Field useField) {
        strFieldText = useField.toString();
        boundField = useField.getBounds();
        borderField = useField.getBorder();
        formatField = useField.getFormat();
        
    }

    public String getStringValue() { return this.strFieldText; }
    public Rectangle getBounds() { return this.boundField; }
    public Border getBorder() { return this.borderField; }
    public Format getFormat() { return this.formatField; }
    
}

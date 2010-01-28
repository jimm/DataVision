/*
 * RowContainer.java
 *
 * Created on June 24, 2004, 10:53 AM
 */

package jimm.datavision.layout.excel;

import java.util.*;

import jimm.datavision.field.Field;

/**
 *
 * @author  dbeeler
 */
public class RowContainer {
    
    public ArrayList reportFields;
    public FieldMap  parentMap;
    
    /** Creates a new instance of RowContainer */
    public RowContainer(FieldMap ownerMap) {
        reportFields = new ArrayList();
        parentMap = ownerMap;
    }
    
   
    public void addField(Field useField) {
        reportFields.add(new PermField(useField));
        parentMap.realignColumns();
    }
    
    protected void finalize() {
        reportFields.clear();
        
    }
        
}

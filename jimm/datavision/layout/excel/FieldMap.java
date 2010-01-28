/*
 * FieldHierachy.java
 *
 * Created on June 24, 2004, 10:48 AM
 */

package jimm.datavision.layout.excel;

import java.util.*;
import jimm.datavision.*;
import jimm.datavision.field.Rectangle;

/**
 *
 * @author  dbeeler
 */
public class FieldMap {
    
    public ArrayList reportRows;
    public double[] colWidths = new double[256];
    public boolean[] colAlloc = new boolean[256];
    public static final int MAXCOL = 256;
    
    /** Creates a new instance of FieldHierachy */
    public FieldMap() {
        // 8.00 pels or 61 pixels
        this(8.00);
    }
    
    public FieldMap(double defaultColWidth) {
        int i;
        for(i=0;i<MAXCOL;i++) {
            colWidths[i] = defaultColWidth * 7.625;
            colAlloc[i] = false;
        }
        reportRows = new ArrayList();
    }
    
    public RowContainer createRow() {
        RowContainer tmpRow = new RowContainer(this);
        reportRows.add(tmpRow);
        return tmpRow;
    }
    
    /* Transverses all rows and columns and establishes the best column allotment */
    public void realignColumns() {
        int i;
        Iterator it = reportRows.iterator(); 
        while(it.hasNext()) {
            RowContainer tmpRow = (RowContainer)it.next();
            /* Iterate through the fields in this line and find acceptable column allocations */
            Iterator rowit = tmpRow.reportFields.iterator();
            while(rowit.hasNext()) {
                PermField tmpField = (PermField)rowit.next();
                Rectangle fbound = tmpField.getBounds();
                /* Find a column that fits or has a position near it */
                
                for(i=0;i<MAXCOL;i++) {
                    int curvalue = getColOffset(i);
                    if(fbound.x == curvalue) {
                        /* Found a perfect match.  Lets make sure its allocated. */
                        if(!colAlloc[i]) colAlloc[i] = true;
                        break;
                    }
                    if(fbound.x < curvalue) {
                        /* We've passed our ideal column.  Lets allocate this one and change
                         * its size to fit, if its not already allocated.  If it is, we'll 
                         * shift this column and the others to the right and insert ours here */
                        if(!colAlloc[i]) {
                            colAlloc[i] = true;
                            double prePosition = getColOffset(i-1);
                            double postPosition = getColOffset(i+1);
                            colWidths[i-1] = fbound.x - prePosition;
                            colWidths[i] = postPosition - fbound.x;
                            break;
                        } else {
                            /* Column is allocated.  Shift it to the right */
                            double prePosition = getColOffset(i-1);
                            double postPosition = getColOffset(i);
                            shiftColsRight(i);
                            colAlloc[i] = true;
                            colWidths[i-1] = fbound.x - prePosition;
                            colWidths[i] = postPosition - fbound.x;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Calculates the column offset given the stored array of widths 
     */
    public int getColOffset(int colNum) {
        int offset = 0;
        int i=0;
        for(i=0;i<colNum;i++) offset += colWidths[i];
        return offset;
    }
    
    public void shiftColsRight(int startColNum) {
        int i;
        for(i=MAXCOL-1;i>startColNum;--i) {
            colWidths[i] = colWidths[i-1];
            colAlloc[i] = colAlloc[i-1];
        }
            
    }
    
    /**
     * Routine to prepare for garbage collection
     */
    public void delete() {
        reportRows.clear();
        
    }
}

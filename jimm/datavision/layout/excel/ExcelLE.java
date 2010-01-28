/*
 * ExcelLE.java
 *
 * Created on June 18, 2004, 4:43 PM
 */

package jimm.datavision.layout.excel;

import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.field.Format;
import jimm.datavision.field.ImageField;
import jimm.datavision.field.Rectangle;
import jimm.datavision.layout.LayoutEngine;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import java.io.OutputStream;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import java.awt.*;

/**
 *
 * @author  dbeeler
 */
public class ExcelLE extends LayoutEngine {

    protected OutputStream outStream;

    private HSSFWorkbook wb;
    private HSSFSheet s;
    private RowContainer row ;
    private Section lastSection;
    private FieldMap fm;

    int rowAt;
    short colAt;
    int fieldNum;
    double pastPageUsed;
    boolean showPageHeaders;
    int pageNum;

    /** Creates a new instance of the Excel LayoutEngine.
     *@param out This is a binary outputstream for receiving the generated Excel file
     *@param showAllPageHeaders This is a flag that instructs the layoutengine to reprint all
     *       page headers sent by the report engine.  Setting this to false will print
     *       the page header only the first time and then ignore the rest.
     */
    public ExcelLE(OutputStream out, boolean showAllPageHeaders) {
        super(null);
        fieldNum = 0;
        outStream = out;
        row = null;
        lastSection = null;
        pastPageUsed = -1;
        showPageHeaders = showAllPageHeaders;
        pageNum = 0;
    }

    protected void doStart() {
        /* Create our workbook and worksheet */
        wb = new HSSFWorkbook();
        s = wb.createSheet();
        /* Start our field map helper class */
        fm = new FieldMap(s.getDefaultColumnWidth());
    }


    /* This is called when the report generation is complete.  This outputs all the fields
     * according to the final column arrangement as decided by the FieldMapper
     */
    private void dumpFieldMap() {
        short i;

        int[] offsetSizes = new int[FieldMap.MAXCOL];
        for(i=0;i<FieldMap.MAXCOL-1;i++) offsetSizes[i] = (int)fm.getColOffset(i);

        /* Iterate throught the stored, mapped rows */
        Iterator it = fm.reportRows.iterator();
        rowAt = 0;
        while(it.hasNext()) {
            RowContainer rowCont = (RowContainer)it.next();
            HSSFRow row = s.createRow(rowAt);
            Iterator colit = rowCont.reportFields.iterator();
            colAt = 0;
            /* Iterate throught the stored, mapped columns */
            while(colit.hasNext()) {
                PermField tmpField = (PermField)colit.next();
                int fieldLoc = (int)tmpField.getBounds().x;
                int fieldEnd = (int)tmpField.getBounds().x + (int)tmpField.getBounds().width;
                for(i=0;i<FieldMap.MAXCOL-1;i++) {
                    if (offsetSizes[i] == fieldLoc) {
                        // Find ending cell
                        int endCell;
                        for(endCell = i+1;endCell<FieldMap.MAXCOL-1;endCell++) {
                            if(offsetSizes[endCell] > fieldEnd ) {
                                break;
                            }
                        }
                        HSSFCell cell = row.createCell(i);
                        org.apache.poi.hssf.util.Region region;

                        /* Merge cells if required */
                        --endCell;
                        if(endCell != i) {
                            region = new org.apache.poi.hssf.util.Region(rowAt, (short)i, rowAt, (short)endCell);
                            s.addMergedRegion(region);
                        }


                        cell.setCellValue(
                          new HSSFRichTextString(tmpField.getStringValue()));

                        /* Setup the correct cell formating */
                        HSSFFont tmpFont = wb.createFont();
                        String fontName = tmpField.getFormat().getFont().getFontName();
                        String useFontName = "Times New Roman";
                        // TODO: Add these as I find other font translations
                        if(fontName.startsWith("Times New Roman")) useFontName = "Times New Roman";
                        tmpFont.setFontName(useFontName);
                        tmpFont.setColor((short)0);
                        tmpFont.setFontHeightInPoints((short)tmpField.getFormat().getFont().getSize());

                        if(tmpField.getFormat().isBold()) tmpFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                        if(tmpField.getFormat().isUnderline()) tmpFont.setUnderline(HSSFFont.U_SINGLE);
                        HSSFCellStyle hcs = wb.createCellStyle();
                        hcs.setFont(tmpFont);

                        /* Set proper alignment */
                        if(tmpField.getFormat().getAlign() == Format.ALIGN_CENTER)
                            hcs.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                        if(tmpField.getFormat().getAlign() == Format.ALIGN_LEFT)
                            hcs.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                        if(tmpField.getFormat().getAlign() == Format.ALIGN_RIGHT)
                            hcs.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

                        /* Enabling wordwrap seems to break stuff at the moment */
                        //if(tmpField.getFormat().isWrap()) hcs.setWrapText(true);

                        cell.setCellStyle(hcs);


                        break;
                    }
                }
            }
            rowAt++;
        }

        /* Set the appropriate column widths and convert pixels back into whatever Excel uses */
        for(i=0;i<FieldMap.MAXCOL-1;i++)
            s.setColumnWidth(i, (short)(fm.colWidths[i] * 38.46));

        /* Do some garbage collection as some of this stuff generates a lot of waste */
        fm.delete();
        fm = null;
        Runtime.getRuntime().gc();

    }

    protected void doEnd() {
        try {
            dumpFieldMap();
            wb.write(outStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doStartPage() {
        pageNum++;
    }

    protected void doOutputField(Field field) {
        Rectangle rect = null;
        rect = field.getBounds();
        boolean showRow = true;
        if(!this.showPageHeaders) {
            if((this.currentSection.getArea().getArea() == SectionArea.PAGE_HEADER) && (pageNum > 1)) showRow = false;
            if((this.currentSection.getArea().getArea() == SectionArea.PAGE_HEADER) && (pageNum > 1)) showRow = false;
        }

        if(showRow) {

            if(this.pastPageUsed != this.pageHeightUsed) {
                pastPageUsed = this.pageHeightUsed;
                row = fm.createRow();
            }

            if(lastSection != currentSection) {
                lastSection = currentSection;
                row = fm.createRow();
            }

            row.addField(field);
        }
    }

    protected void doOutputImage(ImageField imageField) {
        /* TODO: Implement image export in POI-HSSF and then implement the image output
         * in DataVision
         */
    }

    protected void doOutputLine(jimm.datavision.Line line) {
        /* TODO: Just 'TODO' in general */
    }

}

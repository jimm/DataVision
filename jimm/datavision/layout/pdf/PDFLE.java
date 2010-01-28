package jimm.datavision.layout.pdf;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.layout.LayoutEngine;
import jimm.datavision.layout.LineDrawer;
import jimm.util.StringUtils;
import java.io.OutputStream;
import java.util.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/**
 * A PDF layout engine.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class PDFLE extends LayoutEngine implements LineDrawer {

protected OutputStream outStream;
protected Document doc;
protected PdfContentByte content;
protected HashMap baseFonts;
protected double prevThickness;

//
// The following code are modified to handle CJK fonts correctly
//
protected static HashMap cjkFontEncodingMap=new HashMap();
static{
	cjkFontEncodingMap.put("STSong-Light", new String[]{"UniGB-UCS2-H","UniGB-UCS2-V"});
	cjkFontEncodingMap.put("STSongStd-Light", new String[]{"UniGB-UCS2-H","UniGB-UCS2-V"});

	cjkFontEncodingMap.put("MHei-Medium", new String[]{"UniCNS-UCS2-H","UniCNS-UCS2-V"});
	cjkFontEncodingMap.put("MSung-Light", new String[]{"UniCNS-UCS2-H","UniCNS-UCS2-V"});
	cjkFontEncodingMap.put("MSungStd-Light", new String[]{"UniCNS-UCS2-H","UniCNS-UCS2-V"});

	cjkFontEncodingMap.put("HeiseiMin-W3", new String[]{"UniJIS-UCS2-H","UniJIS-UCS2-V","UniJIS-UCS2-HW-H","UniJIS-UCS2-HW-V"});
	cjkFontEncodingMap.put("HeiseiKakuGo-W5", new String[]{"UniJIS-UCS2-H","UniJIS-UCS2-V","UniJIS-UCS2-HW-H","UniJIS-UCS2-HW-V"});
	cjkFontEncodingMap.put("KozMinPro-Regular", new String[]{"UniJIS-UCS2-H","UniJIS-UCS2-V","UniJIS-UCS2-HW-H","UniJIS-UCS2-HW-V"});

	cjkFontEncodingMap.put("HYGoThic-Medium", new String[]{"UniKS-UCS2-H","UniKS-UCS2-V"});
	cjkFontEncodingMap.put("HYSMyeongJo-Medium", new String[]{"UniKS-UCS2-H","UniKS-UCS2-V"});
	cjkFontEncodingMap.put("HYSMyeongJoStd", new String[]{"UniKS-UCS2-H","UniKS-UCS2-V"});
}
////////////////////////////////////////////////////////////////

public PDFLE(OutputStream out) {
    super(null);
    outStream = out;
}

/**
 * Outputs the beginning of the document.
 */
protected void doStart() {
    baseFonts = new HashMap();

    PaperFormat fmt = report.getPaperFormat();
    doc = new Document(new com.lowagie.text.Rectangle(0, 0,
						      (int)fmt.getWidth(),
						      (int)fmt.getHeight()),
		       (float)fmt.getImageableX(),
		       (float)(fmt.getWidth() + fmt.getImageableX()),
		       (float)fmt.getImageableY(),
		       (float)(fmt.getHeight() + fmt.getImageableY()));

    PdfWriter writer = null;
    try {
	writer = PdfWriter.getInstance(doc, outStream);
	baseFonts.put("Helvetica", BaseFont.createFont("Helvetica",
						       BaseFont.CP1252,
						       BaseFont.NOT_EMBEDDED));
    }
    catch (DocumentException e) {
	ErrorHandler.error(e);
	wantsMoreData = false;	// Stop!
	return;
    }
    catch (java.io.IOException ioe) {
	ErrorHandler.error(ioe);
	wantsMoreData = false;	// Stop!
	return;
    }

    String str = null;
    if ((str = report.getTitle()) != null)
	doc.addTitle(str);
    if ((str = report.getAuthor()) != null)
	doc.addAuthor(str);
    doc.addCreator("DataVision version " + info.Version
		   + " <" + info.URL + ">");
    doc.addCreationDate();

    doc.open();

    content = writer.getDirectContent();
}

protected void doEnd() {
    doc.close();
}

protected void doStartPage() {
    try {
	prevThickness = 0;
	doc.newPage();
    }
    // I don't quite get this... looking at the iText API, DocumentException can
    // be thrown by the call to newPage(), but trying to catch that here
    // results in an "exception never thrown" compile error.  I didn't think the
    // best idea was to remove the try...catch entirely, so it's now catching
    // Exception, which should at least not break anything, but I don't like
    // catching Exception, it's a code smell, so if anyone ever figures out
    // why this is happening (it started with the upgrade to iText 2.0.1 by
    // the way), I'd love to hear the reason!
    catch (Exception e) {
	ErrorHandler.error(e);
	wantsMoreData = false;	// Stop!
    }
}

/**
 * Outputs a field.
 *
 * @param field the field to output
 */
protected void doOutputField(Field field) {
    String fieldAsString = field.toString();
    if (fieldAsString == null || fieldAsString.length() == 0) {
	makeBorders(field);
	return;
    }

    Format format = field.getFormat();
    BaseFont baseFont = getFontForFormat(format);
    float fontSize = (float)format.getSize();

    jimm.datavision.Point bottomLeft =
	bottomLeftOfField(field, format.getSize(), baseFont);

    int align;
    switch (format.getAlign()) {
    case Format.ALIGN_CENTER:
	align = PdfContentByte.ALIGN_CENTER;
	bottomLeft.x += field.getBounds().width / 2; // x is center of bounds
	break;
    case Format.ALIGN_RIGHT:
	align = PdfContentByte.ALIGN_RIGHT;
	bottomLeft.x += field.getBounds().width; // x is right side of bounds
	break;
    case Format.ALIGN_LEFT: // fall through
    default:
	align = PdfContentByte.ALIGN_LEFT;
	break;
    }

    content.beginText();
    content.setFontAndSize(baseFont, fontSize);
    content.setColorFill(format.getColor());

    java.util.List lines = StringUtils.splitIntoLines(fieldAsString);
    double lineHeight = field.getOutputHeight() / lines.size();
    for (Iterator iter = lines.iterator(); iter.hasNext(); ) {
	String line = (String)iter.next();
	content.showTextAligned(align, line, (float)bottomLeft.x,
				(float)bottomLeft.y, 0f);
	bottomLeft.y -= lineHeight;
    }

    content.endText();

    // Borders
    makeBorders(field);
}

protected jimm.datavision.Point bottomLeftOfField(Field f, double size,
						  BaseFont baseFont)
{
    jimm.datavision.field.Rectangle r = f.getBounds();
    jimm.datavision.Point bottomLeft = new jimm.datavision.Point(r.x, r.y);

    // Translate to PDF coordinates, subtract (negative) font descent, and
    // reflect vertically
    translateToPDFCoords(bottomLeft);
    bottomLeft.y -=
	baseFont.getFontDescriptor(BaseFont.DESCENT, (float)size)
	+ r.height;

    return bottomLeft;
}

protected void translateToPDFCoords(jimm.datavision.Point p) {
    // Avoid setter methods; no one is observing this point
    if (currentSection.getArea().getArea() != SectionArea.PAGE_FOOTER)
	p.y = (pageHeight() - pageHeightUsed) - p.y;
    else
	p.y = currentSection.getOutputHeight() - p.y;
}

//
// The following code are modified to handle CJK fonts correctly
//
protected BaseFont getFontForFormat(Format f) {
    String name = baseFontName(f.getFont());
		BaseFont bf = (BaseFont)baseFonts.get(name);
		//System.out.println(name);
		//original code is difficult to handle CJK fonts, so I have to modify it
		if(bf==null)
		{
			//start guessing
			try{
				bf = BaseFont.createFont(name, BaseFont.CP1252,
				     BaseFont.NOT_EMBEDDED);
	    	baseFonts.put(name, bf);
	    	return bf;
			}catch(Exception e1){
				if(cjkFontEncodingMap.containsKey(name))
				{
					//guessing in CJK fonts
					String [] encodings=(String[])(cjkFontEncodingMap.get(name));
					for(int i=0;i<encodings.length;i++)
					{
						try{
							bf = BaseFont.createFont(name, encodings[i],
							     BaseFont.NOT_EMBEDDED);
				    	baseFonts.put(name, bf);
				    	return bf;
						}catch(Exception eCJK){}
					}
					//if all in vain
					return (BaseFont)baseFonts.get("Helvetica");
				}
				else{
					//otherwise, we have no choice
					return (BaseFont)baseFonts.get("Helvetica");
				}
			}
		}
		return bf;
    /*BaseFont bf = (BaseFont)baseFonts.get(name);
    if (bf == null) {
	try {
	    bf = BaseFont.createFont(name, BaseFont.CP1252,
				     BaseFont.NOT_EMBEDDED);
	    baseFonts.put(name, bf);
	}
	catch (Exception e) {	// DocumentException or IOException
	    ErrorHandler.error(e);
	    bf = (BaseFont)baseFonts.get("Helvetica");
	}
    }
    return bf;*/
}

protected String baseFontName(java.awt.Font font) {
    String family = font.getFamily().toLowerCase();
    if (family.startsWith("courier") || family.startsWith("monospace"))
	return "Courier" + fontAttributes(font, "Bold", "Oblique");
    else if (family.startsWith("helvetica") || family.startsWith("sansserif"))
	return "Helvetica" + fontAttributes(font, "Bold", "Oblique");
    else if (family.startsWith("symbol"))
	return "Symbol";
    else if (family.startsWith("zapfdingbats"))
	return "ZapfDingbats";
    else {
    	//if this is a known iText CJK font name, just return as is
    	if(cjkFontEncodingMap.containsKey(font.getName()))
    		return font.getName();
    	//otherwise, return a default setting
    	String fontAttrs = fontAttributes(font, "Bold", "Italic");
    	return "Times" + (fontAttrs.length() > 0 ? fontAttrs : "-Roman");
	//String fontAttrs = fontAttributes(font, "Bold", "Italic");
	//return "Times" + (fontAttrs.length() > 0 ? fontAttrs : "-Roman");
    }
}

//////////////////////////////////////////////////////////////////////////////////
protected String fontAttributes(java.awt.Font font, String bold, String italic)
{
    if (font.isBold() && font.isItalic())
	return "-" + bold + italic;
    else if (font.isBold())
	return "-" + bold;
    else if (font.isItalic())
	return "-" + italic;
    else
	return "";
}

/**
 * Ignores image output
 *
 * @param field an image field
 */
protected void doOutputImage(ImageField field) {
    try {
	Image img = Image.getInstance(field.getImageURL());

	// Translate to PDF coordinates and reflect vertically
	jimm.datavision.field.Rectangle r = field.getBounds();
	jimm.datavision.Point p = new jimm.datavision.Point(r.x, r.y);
	translateToPDFCoords(p);
	p.y -= field.getOutputHeight();

	img.setAbsolutePosition((float)p.x, (float)p.y);
	content.addImage(img);
    }
    catch (Exception e) {	// DocumentException or MalformedURLException
	wantsMoreData = false;
	ErrorHandler.error(e);
    }
}

/**
 * Outputs a line. Calls {@link #drawLine}.
 *
 * @param line a line
 */
protected void doOutputLine(Line line) {
    drawLine(line, Boolean.TRUE);
}

/**
 * Outputs borders.
 */
protected void makeBorders(Field field) {
    field.getBorderOrDefault().eachLine(this, Boolean.FALSE);
    content.stroke();
}

/**
 * Draw a single line.
 *
 * @param line a line
 */
public void drawLine(Line line, Object arg) {
    if (line.getThickness() != prevThickness) {
	prevThickness = line.getThickness();
	content.setLineWidth((float)prevThickness);
    }
    jimm.datavision.Point p0 = new jimm.datavision.Point(line.getPoint(0));
    jimm.datavision.Point p1 = new jimm.datavision.Point(line.getPoint(1));
    translateToPDFCoords(p0);
    translateToPDFCoords(p1);
    content.moveTo((float)p0.x, (float)p0.y);
    content.lineTo((float)p1.x, (float)p1.y);
    if (arg != Boolean.FALSE)	// Yes "!=" instead of "equals"
	content.stroke();
}

}

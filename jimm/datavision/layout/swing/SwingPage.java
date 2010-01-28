package jimm.datavision.layout.swing;
import java.awt.*;
import java.awt.print.*;
import javax.swing.JPanel;

/**
 * A swing page is a single printable page from a report.
 *
 * @see SwingLE
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class SwingPage extends JPanel implements Printable {

protected Dimension preferredSize;

/**
 * Constructor.
 */
public SwingPage() {
    setLayout(null);
    setBackground(Color.white);
}

/** Needed because we use a null layout. */
public void setPreferredSize(Dimension dim) {
    preferredSize = dim;
}

/** Needed because we use a null layout. */
public Dimension getPreferredSize() {
    return preferredSize;
}

/**
 * Print a single page.
 */
public int print(Graphics g, PageFormat pf, int pageIndex)
    throws PrinterException
{
    // As suggested by Jaume (Tau Ingenieros <tauinge@menta.net>), use
    // paint instead of paintComponent and resize to page format.
    Dimension oldSize = getSize();
    setSize((int)pf.getWidth(), (int)pf.getHeight());
    print(g);
    setSize(oldSize);

    return Printable.PAGE_EXISTS;
}

}

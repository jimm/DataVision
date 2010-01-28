package jimm.datavision.gui;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.border.AbstractBorder;

/**
 * A border for field widgets.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class FWBorder extends AbstractBorder {

protected static final int CORNER_LINE_LEN = 5;
protected static final int SELECTED_BORDER_THICKNESS = 1;

protected FieldWidget fw;

FWBorder(FieldWidget fw) {
    this.fw = fw;
}

public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height)
{
    --width;
    --height;

    if (fw.isSelected()) {
	g.setColor(Color.gray);
	for (int i = 0; i < SELECTED_BORDER_THICKNESS; ++i)
	    g.drawRect(x+i, y+i, width-2*i, height-2*i);
    }
    else {
	g.setColor(fw.getColor());
	g.setPaintMode();
	// top left
	g.drawLine(x, y, x + CORNER_LINE_LEN, y);
	g.drawLine(x, y, x, y + CORNER_LINE_LEN);
	// top right
	g.drawLine(x + width - CORNER_LINE_LEN, y, x + width, y);
	g.drawLine(x + width, y, x + width, y + CORNER_LINE_LEN);
	// bottom right
	g.drawLine(x + width - CORNER_LINE_LEN, y + height, x + width,
		   y + height);
	g.drawLine(x + width, y + height - CORNER_LINE_LEN, x + width,
		   y + height);
	// bottom left
	g.drawLine(x, y + height, x + CORNER_LINE_LEN, y + height);
	g.drawLine(x, y + height - CORNER_LINE_LEN, x, y + height);
    }
}

}

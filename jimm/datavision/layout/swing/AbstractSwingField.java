package jimm.datavision.layout.swing;
import jimm.datavision.field.Field;
import java.awt.Color;
import javax.swing.JComponent;

public abstract class AbstractSwingField implements SwingField /*, LineDrawer */ {

protected Field field;
protected JComponent component;

public AbstractSwingField(Field f, JComponent c) {
    field = f;
    component = c;
}

public Field getField() { return field; }

public JComponent getComponent() { return component; }

/**
 * Returns a default color for this field. {@link SwingTextField#getColor}
 * overrides this method.
 *
 * @return <code>Color.black</code>
 */
public Color getColor() { return Color.black; }

/**
 * Does whatever it takes to prepare the field for rendering: applies
 * formatting, graying-out, etc.
 */
public abstract void format();

/**
 * Makes borders using the field's border. <em>Unimplemented</em>.
 */
protected void makeBorders() {
//     field.getBorderOrDefault().eachLine(this, null);
}

//  public void drawLine(Line line, Object arg) {
//  }

}

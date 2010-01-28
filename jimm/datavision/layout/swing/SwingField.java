package jimm.datavision.layout.swing;
import jimm.datavision.field.Field;
import javax.swing.JComponent;

public interface SwingField /* implements LineDrawer */ {

public Field getField();

public JComponent getComponent();

public void format();

}

package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.source.Column;
import jimm.datavision.gui.parameter.ParamEditWin;

/**
 * The classes in this file are leaf nodes for the {@link FieldPickerTree}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */

// ================================================================
public abstract class FPLeafInfo {
protected Object leaf;
protected Designer designer;
FPLeafInfo(Draggable obj, Designer designer) {
    leaf = obj;
    this.designer = designer;
}
public Object getLeaf() { return leaf; }
public String toString() {
    return (leaf instanceof Nameable)
	? ((Nameable)leaf).getName() : leaf.toString();
}
public String dragString() { return ((Draggable)leaf).dragString(); }
abstract boolean isDeletable();
// Only used for formulas, parameters, and user columns
void openEditor() {}
}

// ================================================================
class ColumnInfo extends FPLeafInfo {
ColumnInfo(Column c, Designer designer) { super(c, designer); }
boolean isDeletable() { return false; }
}

// ================================================================
class FormulaInfo extends FPLeafInfo {
protected Report report;
FormulaInfo(Report r, Formula f, Designer designer) {
    super(f, designer);
    report = r;
}
boolean isDeletable() { return !report.containsReferenceTo((Formula)leaf); }
void openEditor() { new FormulaWin(designer, report, (Formula)leaf); }
}

// ================================================================
class ParameterInfo extends FPLeafInfo {
protected Report report;
ParameterInfo(Report r, Parameter p, Designer designer) {
    super(p, designer);
    report = r;
}
boolean isDeletable() { return !report.containsReferenceTo((Parameter)leaf); }
void openEditor() { new ParamEditWin(designer, (Parameter)leaf); }
}

// ================================================================
class UserColumnInfo extends FPLeafInfo {
protected Report report;
UserColumnInfo(Report r, UserColumn uc, Designer designer) {
    super(uc, designer);
    report = r;
}
boolean isDeletable() { return !report.containsReferenceTo((UserColumn)leaf); }
void openEditor() { new UserColumnWin(designer, report, (UserColumn)leaf); }
}

// ================================================================
class SpecialInfo extends FPLeafInfo {
protected String specialString;
protected String dragString;
SpecialInfo(String ss, String ds, Designer designer) {
    super(null, designer);
    specialString = ss;
    dragString = ds;
}
public String toString() { return specialString; }
public String dragString() { return dragString; }
boolean isDeletable() { return false; }
}

//  class AggregateInfo extends FPLeafInfo {
//  AggregateInfo(AggregateField f, Designer designer) { super(f, designer); }
//  public String toString() { return f.designLabel(); }
//  boolean isDeletable() { return false; }
//  }

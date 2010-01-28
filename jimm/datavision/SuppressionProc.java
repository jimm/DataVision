package jimm.datavision;
import jimm.datavision.field.Field;
import jimm.util.XMLWriter;

/**
 * A suppression proc is an object used to decide if data should be
 * displayed or not. It returns <code>true</code> if the data should
 * be displayed or <code>false</code> if the data should be supressed
 * (should not be displayed).
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SuppressionProc implements Writeable {

protected Formula formula;
protected Report report;
protected boolean hiding;

public SuppressionProc(Report report) {
    this.report = report;
    hiding = false;
}

public boolean isHidden() { return hiding; }
public void setHidden(boolean val) { hiding = val; }

/**
 * Returns formula used when not hiding.
 *
 * @return formula used when not hiding
 */
public Formula getFormula() {
    if (formula == null) {
	formula = new Formula(null, report, "");
	report = null;		// Don't need it anymore
    }
    return formula;
}

public boolean refersTo(Field f) {
    return formula != null && formula.refersTo(f);
}

public boolean refersTo(Formula f) {
    return formula != null && (f == formula || formula.refersTo(f));
}

public boolean refersTo(UserColumn uc) {
    return formula != null && formula.refersTo(uc);
}

public boolean refersTo(Parameter p) {
    return formula != null && formula.refersTo(p);
}


/**
 * Returns <code>true</code> if the data should be suppressed (not displayed).
 * Returns <code>false</code> if the data should not be supressed (it should
 * be displayed).
 *
 * @return <code>true</code> if the data should be suppressed (not displayed)
 */
public boolean suppress() {
    if (hiding)
	return true;
    if (formula == null)
	return false;

    String expr = formula.getExpression();
    if (expr == null || expr.length() == 0)
	return false;

    Object obj = formula.eval();
    if (obj == null)	// Bogus BSF code format (bad column)
	return false;
    return ((Boolean)obj).booleanValue();
}

/**
 * Writes this suppression proc as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    String expression = null;
    boolean hasFormula = formula != null
	&& (expression = formula.getExpression()) != null
	&& expression.length() > 0;

    if (!hiding && !hasFormula)
	return;

    out.startElement("suppression-proc");
    if (hiding)
	out.attr("hide", true);

    if (hasFormula)
	formula.writeXML(out);

    out.endElement();
}

}

package jimm.datavision.source;
import jimm.datavision.Writeable;
import jimm.util.XMLWriter;

/**
 * A join represents the relationship between two columns in the database.
 * It is used by a query to build the SQL string necessary to retrieve data.
 *
 * @see jimm.datavision.source.Query
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Join implements Writeable, Cloneable {

public static final String[] RELATIONS = {
    "=", "!=", "<", "<=", ">", ">=", "like", "not like", // "null", "not null",
    "in", "not in"
};

protected Column from;
protected String relation;
protected Column to;

/**
 * Constructor.
 *
 * @param fromCol a database column
 * @param relation a string like "=" or "&lt;" used to join the two database
 * columns
 * @param toCol another database column
 * @see Column
 */
public Join(Column fromCol, String relation, Column toCol) {
    from = fromCol;
    this.relation = relation;
    to = toCol;
}

public Object clone() {
    return new Join(from, relation, to);
}

/**
 * Returns the "from" column.
 *
 * @return a column
 */
public Column getFrom() { return from; }

/**
 * Sets the "from" column.
 *
 * @param newFrom the new column
 */
public void setFrom(Column newFrom) { from = newFrom; }

/**
 * Returns the relation string (for example, "=" or "&lt;").
 *
 * @return a string used to define the relationship between the two
 * database columns
 */
public String getRelation() { return relation; }

/**
 * Sets the relation string (for example, "=" or "&lt;").
 *
 * @param newRelation the new string
 */
public void setRelation(String newRelation) { relation = newRelation; }

/**
 * Returns the "to" column.
 *
 * @return a column
 */
public Column getTo() { return to; }

/**
 * Sets the "to" column.
 *
 * @param newTo the new column
 */
public void setTo(Column newTo) { to = newTo; }

/**
 * Returns a string representation of this join, usable as a where
 * clause in a SQL query.
 */
public String toString() {
    return from.fullName() + " " + relation + " " + to.fullName();
}

/**
 * Writes this join as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("join");
    out.attr("from", from.fullName());
    out.attr("relation", relation);
    out.attr("to", to.fullName());
    out.endElement();
}

}

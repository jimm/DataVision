package jimm.datavision.source;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.util.XMLWriter;
import java.util.*;

/**
 * A data source query.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Query implements Writeable {

public static final int SORT_UNDEFINED = -1;
public static final int SORT_DESCENDING = 0;
public static final int SORT_ASCENDING = 1;

protected Report report;
protected ArrayList joins;
protected String whereClause;
protected ArrayList sortSelectables;
protected ArrayList sortOrders;
protected ArrayList selectables; // Can't be a Set; we need selectable indices

/**
 * Constructor.
 *
 * @param r the report for which this query will retrieve data
 */
public Query(Report r) {
    report = r;
    joins = new ArrayList();
    whereClause = null;
    sortSelectables = new ArrayList();
    sortOrders = new ArrayList();
    selectables = new ArrayList();
}

/**
 * Returns <code>true</code> if the specified parameter exists within this
 * query's where clause.
 *
 * @param p a parameter
 * @return <code>true</code> if the specified parameter exists within
 * the where clause
 */
public boolean containsReferenceTo(Parameter p) {
    if (whereClause == null || whereClause.indexOf("{") == -1)
	return false;

    int pos, endPos;
    for (pos = 0, endPos = -1;
	 (pos = whereClause.indexOf("{", endPos + 1)) >= 0;
	 pos = endPos + 1)
    {
	endPos = whereClause.indexOf("}", pos);
	if (endPos == -1)
	    return false;

	switch (whereClause.charAt(pos + 1)) {
	case '@':		// Formula
	    String idAsString = whereClause.substring(pos + 2, endPos);
	    Formula f = report.findFormula(idAsString);
	    if (f.refersTo(p))
		return true;
	    break;
	case '?':		// Parameter
	    idAsString = whereClause.substring(pos + 2, endPos);
	    if (p.getId().toString().equals(idAsString))
		return true;
	    break;
	}
	pos = endPos + 1;
    }

    return false;
}

/**
 * Adds a join to the list of joins used by this query.
 *
 * @param join a join
 */
public void addJoin(Join join) {
    joins.add(join);
}

/**
 * Adds all joins in the collection to our list.
 *
 * @param coll a collection of joins
 */
public void addAllJoins(Collection coll) {
    joins.addAll(coll);
}

/**
 * Removes a join from the list of joins used by this query.
 *
 * @param join a join
 */
public void removeJoin(Join join) {
    joins.remove(join);
}

/**
 * Removes all joins in the collection from our list.
 */
public void clearJoins() {
    joins.clear();
}

/**
 * Returns an iterator over all the joins used by this query.
 *
 * @return an iterator over all the joins
 */
public Iterator joins() { return joins.iterator(); }

/**
 * Returns the where clause fit for human consumption. This mainly means
 * that we substitute formula, parameter, and user column id numbers with
 * names. Called from a where clause editor. This code assumes that curly
 * braces are never nested.
 *
 * @return the eval string with formula, parameter, and user column id
 * numbers replaced with names
 * @see jimm.datavision.gui.sql.WhereClauseWin
 */
public String getEditableWhereClause() {
    return Expression.expressionToDisplay(report, whereClause);
}

/**
 * Returns the raw where clause string; may be <code>null</code>.
 *
 * @return the where clause string; may be <code>null</code>
 */
public String getWhereClause() {
    return whereClause;
}

/**
 * Sets the where clause (may be <code>null</code>). The string passed
 * to us contains parameter and formula display strings, not their
 * "real" <code>formulaString</code> representations. Translate the latter
 * into the former before saving this string.
 *
 * @param newWhereClause a where clause string; may be <code>null</code>
 */
public void setEditableWhereClause(String newWhereClause) {
    setWhereClause(Expression.displayToExpression(report, newWhereClause));
}

/**
 * Sets the where clause (may be <code>null</code>).
 *
 * @param newWhereClause a where clause string; may be <code>null</code>
 */
public void setWhereClause(String newWhereClause) {
    whereClause = newWhereClause;
}

/**
 * Adds a sort order for the specified selectable. The first character
 * of the <i>order</i> string is inspected. If it is a 'd' or 'D',
 * then it's taken to mean "descending". Anything else will result
 * in an "ascending" sort order.
 *
 * @param sel a selectable
 * @param order either <code>SORT_DESCENDING</code> or
 * <code>SORT_ASCENDING</code>
 */
public void addSort(Selectable sel, int order) {
    sortSelectables.add(sel);
    sortOrders.add(new Integer(order));
}

/**
 * Removes a sorting from the list.
 *
 * @param sel a selectable
 */
public void removeSort(Selectable sel) {
    // I used to use a map, but then sorts would not keep their order,
    // which is important. That's why this code isn't just a map lookup
    // any more.
    for (int i = 0; i < sortSelectables.size(); ++i) {
	if (sortSelectables.get(i) == sel) {
	    sortSelectables.remove(i);
	    sortOrders.remove(i);
	    return;
	}
    }
}

/**
 * Removes all sorts from our list.
 */
public void clearSorts() {
    sortSelectables = new ArrayList();
    sortOrders = new ArrayList();
}

/**
 * Returns an iterator over all selectables.
 *
 * @return an iterator over all selectables
 */
public Iterator selectables() { return selectables.iterator(); }

/**
 * Returns an iterator over all the sorted selectables used by this query.
 *
 * @return an iterator
 */
public Iterator sortedSelectables() { return sortSelectables.iterator(); }

/**
 * Returns the sort order (<code>SORT_DESCENDING</code>,
 * <code>SORT_ASCENDING</code>, or <code>SORT_UNDEFINED</code>) of the
 * specified selectable.
 *
 * @param sel a database selectable
 * @return the sort order (<code>SORT_DESCENDING</code>,
 * <code>SORT_ASCENDING</code>, or <code>SORT_UNDEFINED</code>) of the
 * specified selectable.
 */
public int sortOrderOf(Selectable sel) {
    // I used to use a map, but then sorts would not keep their order,
    // which is important. That's why this code isn't just a map lookup
    // any more.
    for (int i = 0; i < sortSelectables.size(); ++i) {
	if (sortSelectables.get(i) == sel)
	    return ((Integer)sortOrders.get(i)).intValue();
    }
    return SORT_UNDEFINED;
}

/**
 * Returns the index of the specified selectable.
 *
 * @param selectable a database selectable
 */
public int indexOfSelectable(Selectable selectable) {
    return selectables.indexOf(selectable);
}

/**
 * Builds collections of the selectables actually used in the report.
 */
public void findSelectablesUsed() {
    // It would be nice if the selectables collection was a set, so we
    // could avoid the contains() calls below. However, we need it to be
    // indexable.

    selectables.clear();
    report.withFieldsDo(new FieldWalker() {
	public void step(Field f) {
	    if (f instanceof ColumnField) {
		Column col = ((ColumnField)f).getColumn();
		if (!selectables.contains(col)) selectables.add(col);
	    }
	    else if (f instanceof FormulaField) {
		FormulaField ff = (FormulaField)f;
		for (Iterator iter = ff.columnsUsed().iterator();
		     iter.hasNext(); )
		{
		    Column col = (Column)iter.next();
		    if (!selectables.contains(col)) selectables.add(col);
		}
		for (Iterator iter = ff.userColumnsUsed().iterator();
		     iter.hasNext(); )
		{
		    UserColumn uc = (UserColumn)iter.next();
		    if (!selectables.contains(uc)) selectables.add(uc);
		}
	    }
	    else if (f instanceof UserColumnField) {
		UserColumn uc = ((UserColumnField)f).getUserColumn();
		if (!selectables.contains(uc)) selectables.add(uc);
	    }
	}
	});

    // Add groups' selectables, which may or may not be used in any
    // report field
    for (Iterator iter = report.groups(); iter.hasNext(); ) {
	Group g = (Group)iter.next();
	Selectable s = g.getSelectable();
	if (!selectables.contains(s)) selectables.add(s);
    }

    // Add all selectables used in sorts.
    for (Iterator iter = sortedSelectables(); iter.hasNext(); ) {
	Selectable s = (Selectable)iter.next();
	if (!selectables.contains(s)) selectables.add(s);
    }

    // Add all columns used by subreports' joins. Though only a report
    // that uses a SQL data source can have subreports right now, that may
    // not be true in the future. There is no harm in implementing this
    // here (rather than in SQLQuery).
    for (Iterator iter = report.subreports(); iter.hasNext(); ) {
	Subreport sub = (Subreport)iter.next();
	for (Iterator subIter = sub.parentColumns(); subIter.hasNext(); ) {
	    Column col = (Column)subIter.next();
	    if (!selectables.contains(col)) selectables.add(col);
	}
    }
}

/**
 * Returns the number of selectables in the query. Does not recalculate the
 * selectables used; we assume this is being called after the qurey has been
 * run or {@link #findSelectablesUsed} has been called.
 *
 * @return the number of selectables (database and user columns) in the query
 */
public int getNumSelectables() { return selectables.size(); }

/**
 * Called from <code>DataSource.reloadColumns</code>, this method gives the
 * query source a chance to tell its ancillary objects (such as joins and
 * the sort) to reload selectable objects.
 * <p>
 * This is necessary, for example, after a SQL database data source has
 * reloaded all of its table and column information. The old column
 * objects no longer exist. New ones (with the same ids, we assume) have
 * taken their place.
 *
 * @param dataSource the data source
 */
public void reloadColumns(DataSource dataSource)
{
    // Joins
    for (Iterator iter = joins(); iter.hasNext(); ) {
	Join j = (Join)iter.next();
	j.setFrom(dataSource.findColumn(j.getFrom().getId()));
	j.setTo(dataSource.findColumn(j.getTo().getId()));
    }

    // Selectables
    ArrayList newSelectables = new ArrayList();
    for (Iterator iter = selectables.iterator(); iter.hasNext(); ) {
	Selectable g = (Selectable)iter.next();
	newSelectables.add(g.reloadInstance(dataSource));
    }
    selectables = newSelectables;

    // Sort selectables
    ArrayList newSortCols = new ArrayList();
    for (Iterator iter = sortSelectables.iterator(); iter.hasNext(); ) {
	Selectable s = (Selectable)iter.next();
	newSortCols.add(s.reloadInstance(dataSource));
    }
    sortSelectables = newSortCols;
}

/**
 * Writes this query as an XML tag. Writes all joins, where clauses,
 * and sorts as well.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("query");

    ListWriter.writeList(out, joins);

    if (whereClause != null && whereClause.length() > 0)
	out.cdataElement("where", whereClause);

    for (int i = 0; i < sortSelectables.size(); ++i) {
	int sortOrder = ((Integer)sortOrders.get(i)).intValue();
	Selectable selectable = (Selectable)sortSelectables.get(i);
	out.startElement("sort");
	out.attr("order", sortOrder == Query.SORT_DESCENDING ? "desc" : "asc");
	out.attr("groupable-id", selectable.getId());
	out.attr("groupable-type", selectable.fieldTypeString());
	out.endElement();
    }
    writeExtras(out);

    out.endElement();
}

/** This method exists so subclasses can write out extra information. */
protected void writeExtras(XMLWriter out) { }

}

package jimm.datavision.gui.cmd;
import jimm.datavision.source.Query;
import jimm.util.I18N;

/**
 * A command for changing a {@link Query}'s where clause text.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class WhereClauseEditCommand extends CommandAdapter {

protected Query query;
protected String newWhereClause;
protected String oldWhereClause;

public WhereClauseEditCommand(Query query, String whereClause) {
    super(I18N.get("WhereClauseEditCommand.name"));
    this.query = query;
    newWhereClause = whereClause;
    oldWhereClause = query.getEditableWhereClause();
}

public void perform() {
    query.setEditableWhereClause(newWhereClause);
}

public void undo() {
    query.setEditableWhereClause(oldWhereClause);
}

}

package jimm.datavision.gui.cmd;
import jimm.datavision.Parameter;
import jimm.util.I18N;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A command for changing a {@link Parameter}'s values---not the runtime
 * values, but the default values presented to the user as initial choices.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ParamEditCommand extends CommandAdapter {

Parameter param;
String newName;
String newQuestion;
int newType;
int newArity;
List newDefaultValues;
String oldName;
String oldQuestion;
int oldType;
int oldArity;
List oldDefaultValues;

/**
 * Constructor.
 *
 * @param param the parameter
 * @param name the new name of this parameter
 * @param question the new question
 * @param type the new type; one of the <code>Parameter</code> constants
 * <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code>
 * @param arity one of the <code>Parameter</code> constants
 * <code>ARITY_ONE</code>, <code>ARITY_RANGE</code>,
 * <code>ARITY_LIST_SINGLE</code>, or <code>ARITY_LIST_MULTIPLE</code>
 * @param defaultValues the new list of parameter default values
 */
public ParamEditCommand(Parameter param, String name, String question,
			    int type, int arity, List defaultValues)
{
    super(I18N.get("ParamEditCommand.name"));

    this.param = param;
    newName = name;
    newQuestion = question;
    newType = type;
    newArity = arity;
    newDefaultValues = defaultValues;

    oldName = param.getName();
    oldQuestion = param.getQuestion();
    oldType = param.getType();
    oldArity = param.getArity();
    oldDefaultValues = new ArrayList();
    for (Iterator iter = param.defaultValues(); iter.hasNext(); )
	oldDefaultValues.add(iter.next());
}

public void perform() {
    editParam(newName, newQuestion, newType, newArity, newDefaultValues);
}

public void undo() {
    editParam(oldName, oldQuestion, oldType, oldArity, oldDefaultValues);
}

protected void editParam(String name, String question, int type, int arity,
			 List defaultValues)
{
    param.setName(name);
    param.setQuestion(question);
    param.setType(type);
    param.setArity(arity);

    param.removeDefaultValues();
    switch (arity) {
    case Parameter.ARITY_ONE:
	// Dates don't store a default value
	if (type != Parameter.TYPE_DATE)
	    param.addDefaultValue(defaultValues.get(0));
	break;
    case Parameter.ARITY_RANGE:
	// Dates don't store a default value
	if (type != Parameter.TYPE_DATE) {
	    param.addDefaultValue(defaultValues.get(0));
	    param.addDefaultValue(defaultValues.get(1));
	}
	break;
    case Parameter.ARITY_LIST_SINGLE:
    case Parameter.ARITY_LIST_MULTIPLE:
	for (Iterator iter = defaultValues.iterator(); iter.hasNext(); )
	    param.addDefaultValue(iter.next());
	break;
    }
}

}

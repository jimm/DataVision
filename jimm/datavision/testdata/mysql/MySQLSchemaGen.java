package jimm.datavision.testdata.mysql;
import jimm.datavision.testdata.SchemaGen;

public class MySQLSchemaGen extends SchemaGen {

/** Max MySQL varchar length. */
protected static final int MAX_VARCHAR_LEN = 255;

protected String printableName(String name) {
    if (name.indexOf(' ') >= 0 || !name.toLowerCase().equals(name))
      return "`" + name + "`";
    return name;
}

protected void makeTable(String tableName) {
    String name = printableName(tableName);
    System.out.println("drop table if exists " + name + ";");
    System.out.print("create table " + name + " (");
}

protected void endTable() {
    System.out.println();
    System.out.println(");");
}

protected void printType(String type, int size) {
    System.out.print(" ");
    if ("integer".equals(type))
      System.out.print("int");
    else if ("date".equals(type))
	System.out.print("date");
    else if ("boolean".equals(type))
	System.out.print("enum('F','T') default 'F'");
    else if ("string".equals(type)) {
	if (size > MAX_VARCHAR_LEN)
	    size = MAX_VARCHAR_LEN;
	System.out.print("varchar(" + size + ")");
    }
}

protected void printNotNull() {
    System.out.print(" not null");
}

protected void printPrimaryKey() {
    System.out.print(" primary key");
}

public static void main(String[] args) {
    new MySQLSchemaGen().run("../schema.xml");
}
}

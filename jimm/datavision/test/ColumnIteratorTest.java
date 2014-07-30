package jimm.datavision.test;
import jimm.datavision.source.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import java.util.*;

class TestTable extends Table {
TestTable(String name) {
    super(null, name);
    columns = new TreeMap<String, Column>();
}
void add(Column col) { columns.put(col.getName(), col); }
}

// ----------------------------------------------------------------

class TestColumn extends Column {
TestColumn(String name) {
    super(name, name, java.sql.Types.INTEGER);
}
}

// ----------------------------------------------------------------

public class ColumnIteratorTest extends TestCase {

protected static final int NUM_TABLES = 3;
protected static final int NUM_COLUMNS = 9;

protected TestTable[] tables;
protected TestColumn[] columns;
protected ArrayList<Table> tableList;

public static Test suite() {
    return new TestSuite(ColumnIteratorTest.class);
}

public ColumnIteratorTest(String name) {
    super(name);
}

public void setUp() {
    tables = new TestTable[NUM_TABLES];
    for (int i = 0; i < NUM_TABLES; ++i)
	tables[i] = new TestTable("table" + i);

    columns = new TestColumn[NUM_COLUMNS];
    for (int i = 0; i < NUM_COLUMNS; ++i)
	columns[i] = new TestColumn("col" + i);

    tableList = new ArrayList<Table>();
}

@SuppressWarnings("unused")
public void testEmpty() {
    for (Column col : new ColumnIterator(tableList))
	fail("should not return anything");
}

@SuppressWarnings("unused")
public void testAllEmptyTables() {
    ArrayList<Table> tables = new ArrayList<Table>();
    for (Column col : new ColumnIterator(tables))
	fail("should not return anything");
}

public void testOneTable() {
    for (int i = 0; i < 3; ++i)	// One table, three columns
	tables[0].add(columns[i]);

    tableList.add(tables[0]);

    int i = 0;
    for (Column col : new ColumnIterator(tableList)) {
	assertSame(columns[i], col);
        ++i;
    }
    assertEquals(3, i);
}

public void testManyTables() {
    for (int i = 0; i < NUM_TABLES; ++i) { // Three tables
	tableList.add(tables[i]);
	for (int j = 0; j < 3; ++j) // Three columns each
	    tables[i].add(columns[i * 3 + j]);
    }

    int i = 0;
    for (Column col : new ColumnIterator(tableList)){
	assertSame(columns[i], col);
        ++i;
    }
    assertEquals(NUM_COLUMNS, i);
}

protected void skipTableTest(int skip) {
    int j = 0;
    for (int i = 0; i < NUM_TABLES; ++i) {
	tableList.add(tables[i]);
	if (i != skip) {
	    tables[i].add(columns[j++]);
	    tables[i].add(columns[j++]);
	    tables[i].add(columns[j++]);
	}
    }

    int i = 0;
    for (Column col : new ColumnIterator(tableList)) {
	assertSame(columns[i], col);
        ++i;
    }
    assertEquals((NUM_TABLES - 1) * 3, i);
}

public void testEmptyTable0() {
    skipTableTest(0);
}

public void testEmptyTable1() {
    skipTableTest(1);
}

public void testEmptyTable2() {
    skipTableTest(2);
}

public void testBeyondEnd() {
    Iterator<Column> iter = new ColumnIterator(tableList);
    assertTrue("should not have more", !iter.hasNext());
    try {
	iter.next();
	fail("should throw exception");
    }
    catch (NoSuchElementException e) {}
}

public void testRemove() {
    try {
	tableList.add(tables[0]);
	new ColumnIterator(tableList).remove();
	fail("remove not supported");
    }
    catch (UnsupportedOperationException e) {}
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}

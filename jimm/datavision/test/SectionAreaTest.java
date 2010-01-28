package jimm.datavision.test;
import jimm.datavision.SectionArea;
import jimm.datavision.Section;
import jimm.datavision.Report;
import java.util.List;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Tests {@link SectionArea}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SectionAreaTest extends TestCase {

protected SectionArea area;
protected Section sect;
protected Report report;

public static Test suite() {
    return new TestSuite(SectionAreaTest.class);
}

public SectionAreaTest(String name) {
    super(name);
}

public void setUp() {
    area = new SectionArea(SectionArea.DETAIL);
    report = new Report();
    sect = new Section(report);
}

public void testBasicStuff() {
    assertEquals(SectionArea.DETAIL, area.getArea());
}

public void testIsDetail() {
    assertTrue(area.isDetail());

    area = new SectionArea(SectionArea.REPORT_HEADER);
    assertTrue(!area.isDetail());
}

public void testListBehavior() {
    assertNull(sect.getArea());
    assertEquals(0, area.size());

    area.add(sect);
    assertEquals(1, area.size());
    assertSame(area, sect.getArea());
    assertEquals(0, area.indexOf(sect));
    assertSame(sect, area.first());

    Section sect2 = new Section(report);
    area.add(sect2);
    assertEquals(2, area.size());
    assertSame(area, sect2.getArea());
    assertEquals(1, area.indexOf(sect2));
    assertSame(sect, area.first());

    Section sect3 = new Section(report);
    area.insertAfter(sect3, sect);
    assertEquals(1, area.indexOf(sect3));
    assertEquals(2, area.indexOf(sect2));
}

public void testInsertAfter() {
    area.add(sect);
    Section newSection = area.insertAfter(null, sect);

    assertNotSame(newSection, sect);
    assertEquals(1, area.indexOf(newSection));
    assertEquals(area.getName(), newSection.getName());
    assertSame(area, newSection.getArea());
}

public void testRemove() {
    area.add(sect);
    Section newSection = area.insertAfter(null, sect);

    assertEquals(2, area.size());
    area.remove(sect);
    assertNull(sect.getArea());
    assertEquals(1, area.size());
    assertSame(newSection, area.first());

    area.remove(newSection);
    assertNull(newSection.getArea());
    assertEquals(0, area.size());
}

public void testIllegalArg() {
    try {
	area.insertAfter(null, null);
	fail("should have thrown IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
	assertTrue(true);
    }
}

public void testSectionDelegation() {
    assertNull(sect.getName());

    area.add(sect);
    assertEquals(area.getName(), sect.getName());
    assertEquals(area.isDetail(), sect.isDetail());
}

public void testUnmodifiable() {
    area.add(sect);
    List sections = area.sections();
    try {
	sections.add(new Section(report));
    }
    catch (UnsupportedOperationException e) {
	assertTrue(true);
    }
}

public void testClear() {
    Section sect2 = new Section(report);
    area.add(sect);
    area.add(sect2);
    assertEquals(2, area.size());

    area.clear();
    assertEquals(0, area.size());

    assertNull(sect.getArea());
    assertNull(sect2.getArea());
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}

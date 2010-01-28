package jimm.datavision;

/**
 * Enough things have names that it's time to give them a common interface.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface Nameable {

/**
 * Returns the name.
 */
public String getName();

/**
 * Sets the name.
 *
 * @param name the new name
 */
public void setName(String name);

}

package jimm.datavision;

/**
 * Unique identifiers. IDs are common to enough different kinds of objects
 * (for example, columns, formulas, parameters, and user columns) that it
 * makes sense to have this interface.
 * <p>
 * The name &quot;Identifiable&quot;, though more consistent with other
 * interface names like &quot;Nameable&quot; and &quot;Writeable&quot;,
 * would have been too darned verbose.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface Identity {

/**
 * Returns the identity.
 */
public Object getId();

}

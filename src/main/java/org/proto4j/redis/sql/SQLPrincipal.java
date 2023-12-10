package org.proto4j.redis.sql; //@date 15.03.2022

import javax.security.auth.Destroyable;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;

/**
 * A user principal identified by a username or account name.
 * <p>
 * This principal object should be destroyed after successful initiating a
 * new database connection. In order to wipe the stored password from memory.
 *
 * @since 1.0
 * @see UserPrincipal
 * @see Destroyable
 */
public class SQLPrincipal implements UserPrincipal, Destroyable {

    /**
     * The principal's name
     *
     * @serial
     */
    private final String name;

    /**
     * The principal's password
     *
     * @serial
     * @see #destroy()
     */
    private final char[] password;

    /**
     * Creates a principal.
     *
     * @param name The principal's string name.
     * @param password The principal's password used for authentication
     * @exception NullPointerException If the <code>name</code> is
     * <code>null</code>.
     */
    public SQLPrincipal(String name, char[] password) {
        if (name == null) {
            throw new NullPointerException("null name is illegal");
        }

        this.name     = name;
        this.password = password;
    }

    /**
     * Returns the name of this principal.
     *
     * @return The principal's name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the password of this principal.
     *
     * @return The principal's password.
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * Returns a string representation of this principal.
     *
     * @return The principal's name.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns a hash code for this principal.
     *
     * @return The principal's hash code.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Compares this principal to the specified object.
     *
     * @param obj The object to compare this principal against.
     * @return true if they are equal; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SQLPrincipal) {
            return getName().equals(((SQLPrincipal) obj).getName());
        }
        return false;
    }

    /**
     * Destroy the password of this {@code SQLPrincipal}.
     */
    @Override
    public void destroy() {
        Arrays.fill(password, (char) 0);
    }
}

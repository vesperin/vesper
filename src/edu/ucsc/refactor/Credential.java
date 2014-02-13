package edu.ucsc.refactor;

import com.google.common.base.Objects;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class Credential {
    private final String username;
    private final String password;

    /**
     * Constructs a new {@code Credential}
     *
     * @param username The username
     * @param password The password
     */
    public Credential(String username, String password){
        this.username = username;
        this.password = password;
    }

    /**
     * Creates the NoCredential object.
     *
     * @return THe NoCredential object.
     */
    public static Credential none(){
        return new Credential("NONE", "NONE");
    }


    /**
     * @return The username
     */
    public String getUsername() { return username; }

    /**
     * @return The password
     */
    public String getPassword() { return password; }

    /**
     * @return {@code true} if no credentials have been set, {@code false} otherwise.
     */
    public boolean isNoneCredential(){
        return "NONE".equals(getUsername()) && "NONE".equals(getPassword());
    }

    @Override public String toString() {
        return Objects.toStringHelper("Credential")
                .add("username", getUsername())
                .add("password", (!getPassword().isEmpty() ? "..." : getPassword())).toString();
    }
}

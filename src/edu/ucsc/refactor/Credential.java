package edu.ucsc.refactor;

import edu.ucsc.refactor.util.ToStringBuilder;

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

    @Override public String toString() {
        return new ToStringBuilder("Credential")
                .add("username", getUsername())
                .add("password", (!getPassword().isEmpty() ? "..." : getPassword())).toString();
    }
}

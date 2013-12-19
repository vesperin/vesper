package edu.ucsc.refactor.util;

import java.util.Date;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MessageBuilder {
    private final StringBuilder core;

    /**
     * Constructs a {@code MessageBuilder}.
     *
     */
    public MessageBuilder(){
        this.core = new StringBuilder();
    }

    public MessageBuilder commit(String id){
        return add("commit\t", id + "\n");
    }

    public MessageBuilder author(String name){
        return add("Author:\t", name + "\n");
    }


    public MessageBuilder date(Date date){
        return add("Date:\t", date + "\n\n\t\t");
    }


    public MessageBuilder comment(String key, String message){
        return add(key, message + "\n");
    }

    public MessageBuilder error(String message){
        return add("error:\n", message + "\n");
    }

    /**
     * Adds name-value pairs to the {@code MessageBuilder}.
     *
     * @param name The name of the property
     * @param value The value of the property
     *
     * @return self
     */
    MessageBuilder add(String name, String value) {
        this.core.append(name).append(": ").append(value);
        return this;
    }


    @Override public String toString(){
        return this.core.toString();
    }
}

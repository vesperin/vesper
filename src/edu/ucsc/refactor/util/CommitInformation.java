package edu.ucsc.refactor.util;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitInformation {
    private final Map<String, Object> map;

    /**
     * Constructs a {@code CommitInformation}.
     *
     */
    public CommitInformation(){
        this.map  = new LinkedHashMap<String, Object>();

    }

    public CommitInformation commit(String id){
        return add("commit", id + "\n");
    }

    public CommitInformation url(String url){
        return add("url", url + "\n");
    }

    public CommitInformation author(String name){
        return add("Author", name + "\n");
    }


    public CommitInformation date(Date date){
        return add("Date", date + "\n\n\t\t");
    }


    public CommitInformation comment(String message){
        return add("", message + "\n");
    }

    public CommitInformation error(String message){
        return add("error:\n", message + "\n");
    }

    /**
     * Adds name-value pairs to the {@code CommitInformation}.
     *
     * @param name The name of the property
     * @param value The value of the property
     *
     * @return self
     */
    CommitInformation add(String name, String value) {
        this.map.put(name, value);
        return this;
    }


    @Override public String toString(){
        return this.map.toString()
                .replace("{", "")
                .replace("}", "")
                .replace("=", ": ")
                .replace(", ", "");
    }
}

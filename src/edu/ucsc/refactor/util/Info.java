package edu.ucsc.refactor.util;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Info {
    private final Map<String, Object> map;

    /**
     * Constructs a {@code Info}.
     *
     */
    public Info(){
        this.map  = new LinkedHashMap<String, Object>();

    }

    public Info commit(String id){
        return add("commit", id + "\n");
    }

    public Info author(String name){
        return add("Author", name + "\n");
    }


    public Info date(Date date){
        return add("Date", date + "\n\n\t\t");
    }


    public Info comment(String key, String message){
        return add(key, message + "\n");
    }

    public Info error(String message){
        return add("error:\n", message + "\n");
    }

    /**
     * Adds name-value pairs to the {@code Info}.
     *
     * @param name The name of the property
     * @param value The value of the property
     *
     * @return self
     */
    Info add(String name, String value) {
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

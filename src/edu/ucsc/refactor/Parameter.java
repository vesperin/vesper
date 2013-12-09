package edu.ucsc.refactor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents some user input used to create code changes.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Parameter {

    private String title;
    private Object value;
    private List<Constraint> constraints;

    /**
     * Instantiates a new Parameter with a title and a value.
     *
     * @param title The parameter's title.
     * @param value The parameter's value.
     */
    public Parameter(final String title, final Object value) {
        this.title       = title;
        this.value       = value;
        this.constraints = new ArrayList<Constraint>();
    }

    /**
     * Returns the parameter's title.
     *
     * @return The parameter's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the parameter's title.
     *
     * @param title The parameter's title.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Gets the parameter's value.
     *
     * @return The parameter's value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the parameter's value.
     *
     * @param value The parameter's value.
     */
    public void setValue(final Object value) {
        if (!isValidValue(value)) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for parameter '" + title + "'.");
        }
        this.value = value;
    }

    /**
     * Return the parameter's constraints.
     *
     * @return The parameter's constraints.
     */
    public List<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Sets the parameter's constraints.
     *
     * @param constraints The parameter's constraints.
     */
    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        return "Parameter<" + value.getClass() + ":" + value + ">";
    }

    /**
     * Tests whether a value complies with all constraints.
     *
     * @param value The value to test.
     * @return Whether the value is valid.
     */
    public boolean isValidValue(Object value) {
        for (Constraint constraint : constraints) {
            if (!constraint.isValid(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Constrains the value of a parameter.
     */
    public static interface Constraint {

        /**
         * Test whether the value complies with the constraint.
         *
         * @param value The value to test.
         * @return Whether the value is valid.
         */
        public boolean isValid(Object value);

    }
}


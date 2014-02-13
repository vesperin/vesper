package edu.ucsc.refactor.cli.results;

import com.google.common.collect.ImmutableList;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultVisitor;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class LocationsResult implements Result {
    private final String                message;
    private final List<NamedLocation>   locations;

    /**
     * Constructs a new {@code LocationsResult}
     *
     * @param message the brief message
     * @param locations the found locations where a program unit was found.
     */
    public LocationsResult(String message, List<NamedLocation> locations){
        this.message    = message;
        this.locations  = locations;
    }

    @Override public void accepts(ResultVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String getBriefDescription() {
        return message;
    }

    /**
     * @return the list of detected locations for some program unit.
     */
    public List<NamedLocation> getLocations(){
        return ImmutableList.copyOf(locations);
    }

    @Override public String toString() {
        return getBriefDescription();
    }
}

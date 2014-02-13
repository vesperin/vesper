package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.spi.CommitSummary;
import edu.ucsc.refactor.util.CommitHistory;
import edu.ucsc.refactor.util.Notes;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Results {

    static final Result UNIT = new UnitResult();

    private Results(){}

    /**
     * Returns the UNIT result.
     */
    public static Result unit(){
        return UNIT;
    }

    /**
     * Returns an info result.
     */
    public static Result infoResult(String message){
        return new InfoResult(message);
    }

    /**
     * Return the commit summary info result.
     */
    public static Result commitSummaryInfo(String description, CommitSummary summary){
        return new CommitSummaryInfoResult(description, summary);
    }

    /**
     * Return the commit history result.
     */
    public static Result commitHistoryResult(String message, CommitHistory history){
        return new CommitHistoryResult(message, history);
    }

    /**
     * Return the error result.
     */
    public static Result errorResult(String message){
        return new ErrorResult(message);
    }

    /**
     * Return the issues result.
     */
    public static Result issuesResult(String message, List<Issue> issues){
        return new IssuesResult(message, issues);
    }

    /**
     * Return the locations result.
     */
    public static Result locationsResult(String message, List<NamedLocation> locations){
        return new LocationsResult(message, locations);
    }

    /**
     * Return the notes result.
     */
    public static Result notesResult(String message, Notes notes){
        return new NotesResult(message, notes);
    }

    /**
     * Return the source result.
     */
    public static Result sourceResult(String message, Source source){
        return new SourceResult(message, source);
    }
}

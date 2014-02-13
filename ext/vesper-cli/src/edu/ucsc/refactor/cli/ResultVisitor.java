package edu.ucsc.refactor.cli;

import edu.ucsc.refactor.cli.results.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface ResultVisitor {
    /**
     * Visits an info result
     */
    void visit(InfoResult info);

    /**
     * Visits a commit summary info result
     */
    void visit(CommitSummaryInfoResult info);

    /**
     * Visits an error result
     */
    void visit(ErrorResult error);

    /**
     * Visits a unit result
     */
    void visit(UnitResult unit);

    /**
     * Visits a program unit locations result
     */
    void visit(LocationsResult locations);

    /**
     * Visits a notes result
     */
    void visit(NotesResult notes);

    /**
     * Visits an issues result
     */
    void visit(IssuesResult issues);

    /**
     * Visits a commit history result
     */
    void visit(CommitHistoryResult history);

    /**
     * Visits a source result
     */
    void visit(SourceResult source);
}

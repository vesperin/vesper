package edu.ucsc.refactor.cli;

import edu.ucsc.refactor.cli.results.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface ResultVisitor {
    void visit(InfoResult info);
    void visit(CommitSummaryInfoResult info);
    void visit(ErrorResult error);
    void visit(UnitResult unit);
    void visit(LocationsResult locations);
    void visit(NotesResult notes);
    void visit(IssuesResult issues);
    void visit(CommitHistoryResult history);
    void visit(SourceResult source);
}

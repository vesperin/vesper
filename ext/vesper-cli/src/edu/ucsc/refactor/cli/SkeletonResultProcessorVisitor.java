package edu.ucsc.refactor.cli;

import edu.ucsc.refactor.cli.results.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class SkeletonResultProcessorVisitor implements ResultProcessorVisitor {
    protected SkeletonResultProcessorVisitor(){}

    @Override public void visit(InfoResult info) {}

    @Override public void visit(CommitSummaryInfoResult info) {}

    @Override public void visit(ErrorResult error) {}

    @Override public void visit(UnitResult unit) {}

    @Override public void visit(LocationsResult locations) {}

    @Override public void visit(NotesResult notes) {}

    @Override public void visit(IssuesResult issues) {}

    @Override public void visit(CommitHistoryResult history) {}

    @Override public void visit(SourceResult source) {}
}

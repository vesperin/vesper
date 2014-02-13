package edu.ucsc.refactor.cli;

import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.Note;
import edu.ucsc.refactor.cli.results.*;
import edu.ucsc.refactor.util.Checkpoint;
import edu.ucsc.refactor.util.Notes;

import java.util.Iterator;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SysResultVisitor extends SkeletonResultVisitor {
    @Override public void visit(InfoResult info) {
        System.out.println("= " + info);
    }

    @Override public void visit(CommitSummaryInfoResult info) {
        System.out.println("= \n" + info.getCommitSummary().more());
    }

    @Override public void visit(ErrorResult error) {
        System.out.println("! " + error);
    }

    @Override public void visit(UnitResult unit) {
        System.out.println("= " + unit);
    }

    @Override public void visit(LocationsResult locations) {

        final StringBuilder           message   = new StringBuilder();
        final Iterator<NamedLocation> itr       = locations.getLocations().iterator();

        message.append("at").append("(");

        while(itr.hasNext()){
            final NamedLocation each = itr.next();
            message.append("[");
            message.append(each.getStart().getOffset())
                    .append(",").append(each.getEnd().getOffset())
                    .append(",").append(each.getName());
            message.append("]");
            if(itr.hasNext()){
                message.append(", ");
            }
        }

        message.append(")");

        System.out.println("= " + message.toString());
    }

    @Override public void visit(NotesResult notes) {
        final Notes found = notes.getNotes();
        final StringBuilder text    = new StringBuilder();

        text.append("\n");
        int count = 1;
        for(Note each : found){
            text.append(count++).append(". ").append(each.getContent()).append("\n");
        }

        System.out.println("= " + text.toString());
    }

    @Override public void visit(IssuesResult issues) {
        final StringBuilder toIssues = new StringBuilder(issues.getBriefDescription());

        final Iterator<Issue> itr = issues.getIssues().iterator();
        while(itr.hasNext()){
            final Issue issue = itr.next();
            toIssues.append(issue.getName().getKey()).append(".");
            if(itr.hasNext()){
                toIssues.append("\n\t\t");
            }
        }

        toIssues.append("\n");

        System.out.println("? " + toIssues.toString());
    }

    @Override public void visit(CommitHistoryResult history) {

        final StringBuilder toScreen = new StringBuilder(history.getCommitHistory().size() * 10000);

        final Iterator<Checkpoint> itr = history.getCommitHistory().iterator();
        while(itr.hasNext()){
            final Checkpoint c = itr.next();

            toScreen.append(c.getCommitSummary().more());

            if(itr.hasNext()){
                toScreen.append("\n\t\t");
            }
        }

        System.out.println("= \n" + toScreen.toString());

    }

    @Override public void visit(SourceResult source) {
        System.out.println("= " + source.getSource().getContents());
    }
}

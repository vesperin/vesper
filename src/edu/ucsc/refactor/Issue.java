package edu.ucsc.refactor;

import com.google.common.base.Objects;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Name;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.util.Locations;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class Issue extends AbstractCause {
    private final IssueDetector detector;
    private final Smell         issueName;

    /**
     * Instantiate a new Issue.
     *
     * @param detector The detector that detected this issue.
     */
    Issue(IssueDetector detector) {
        super();
        this.detector  = detector;
        this.issueName = Smell.from(getContextScanner().getName());
    }

    /**
     * Creates a new Issue object.
     *
     * @param detector the class which detected this issue.
     * @return a new Issue
     */
    public static Issue make(IssueDetector detector){
        return new SingleIssue(detector);
    }

    @Override public Name getName() {
        return issueName;
    }

    /**
     * Returns the IssueDetector that detected this issue.
     *
     * @return The IssueDetector that detected this issue.
     */
    public IssueDetector getContextScanner() {
        return detector;
    }


    @Override public String more() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        builder.add("name", getName().getKey());
        builder.add("summary", getName().getSummary());
        if(!getAffectedNodes().isEmpty()){
            final Location location = Locations.locate(getAffectedNodes().get(0));
            int from = location.getStart().getLine();
            int to   = location.getEnd().getLine();
            builder.add("from(line)", from);
            builder.add("to(line)", to);
        }
        return builder.toString();
    }


    static class SingleIssue extends Issue {
        SingleIssue(IssueDetector detector){
          super(detector);
        }

        @Override public boolean isSame(Name otherName) {
            return getName().isSame(otherName);
        }

        @Override public String toString() {
            return more();
        }
    }
}

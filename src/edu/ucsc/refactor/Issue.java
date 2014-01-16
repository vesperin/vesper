package edu.ucsc.refactor;

import com.google.common.base.Objects;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.util.Locations;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Issue extends AbstractCauseOfChange {
    private final Smell issueName;
    private final IssueDetector detector;

    /**
     * Instantiate a new Issue.
     *
     * @param detector The detector that detected this issue.
     */
    public Issue(IssueDetector detector) {
        super();
        this.detector  = detector;
        this.issueName = Smell.from(this.detector.getName());
    }

    /**
     * Returns the IssueDetector that detected this issue.
     *
     * @return The IssueDetector that detected this issue.
     */
    @Override public IssueDetector getContextScanner() {
        return detector;
    }

    @Override public Smell getName() {
        return issueName;
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
}

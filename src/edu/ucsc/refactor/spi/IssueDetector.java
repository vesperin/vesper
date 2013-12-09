package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Issue;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class IssueDetector implements SourceScanner {
    private static final String CLASS_NAME  = IssueDetector.class.getName();
    private static final Logger LOGGER      = Logger.getLogger(CLASS_NAME);

    private   final String     name;           // The detector's name
    private   final String     description;    // The detector's description
    protected final Set<Issue> issues;         // The set of detected issues.

    /**
     * Instantiate a new issue detector.
     *
     * @param name The detector's name.
     * @param description The detector's description.
     */
    public IssueDetector(String name, String description) {
        this(name, description, new HashSet<Issue>());
    }

    /**
     * Instantiate a new issue detector given a set of issues.
     *
     * @param name The detector's name.
     * @param description The detector's description.
     * @param issues The set of issues.
     *
     */
    IssueDetector(String name, String description, Set<Issue> issues){
        this.name           = name;
        this.description    = description;
        this.issues         = issues;
    }

    /**
     * Scans the context for issues. Resets the detector before the context is
     * scanned, removing all previously detected issues. After resetting,
     * the call is forwarded to an abstract protected method for subclass specific tasks.
     *
     * @param context The context containing all the source code to scan through.
     */
    public Set<Issue> detectIssues(Context context) {
        resetThisDetector();

        LOGGER.info("Searching for issues...");
        scanJava(context);
        LOGGER.info("Found " + issues.size() + " issue(s).");

        // once creating issues, add their location in the file
        return Collections.unmodifiableSet(issues);
    }


    /**
     * Handles the actual detection of issues in the context. Meant to be implemented
     * by IssueDetector subclasses.
     *
     * @param context The context containing all the source code to scan through.
     */
    public abstract void scanJava(Context context);

    /**
     * Returns the detector's name.
     *
     * @return The detector's name.
     */
    public String getName(){
        return name;
    }

    /**
     * Returns the detector's description.
     *
     * @return The detector's description.
     */
    public String getDescription(){
        return description;
    }

    /**
     * Reset the detector. Clears the set of detected issues.
     */
    public void resetThisDetector() {
        issues.clear();
    }

    /**
     * Returns the set of detected issues.
     *
     * @return The set of detected issues.
     */
    public Set<Issue> getIssues() {
        return Collections.unmodifiableSet(issues);
    }

    /**
     * Convenience method for creating a new issue. Automatically adds the issue to
     * the set of detected issues, and associates the issue with this detector.
     *
     * @return The created issue.
     */
    protected Issue createIssue() {
        Issue issue = new Issue(this);
        issues.add(issue);
        return issue;
    }

    /**
     * Convenience method for creating a new issue, specifying the node that's the root
     * to the issue. Automatically adds the issue to the set of detected issues,
     * and associates the issue with this detector.
     *
     * @param node The source node for the issue.
     * @return The created issue.
     */
    protected Issue createIssue(ASTNode node) {
        final Issue  issue  = createIssue();
        issue.addNode(node);
        return issue;
    }

    /**
     * Convenience method for creating new issues, specifying the nodes that are the root
     * to their issues. Automatically adds the issue to the set of detected issues,
     * and associates the issue with this detector.
     *
     * @param nodes The source nodes to create issues for.
     */
    protected void createIssues(Set<ASTNode> nodes) {
        for (ASTNode node : nodes) {
            issues.add(createIssue(node));
        }
    }
}

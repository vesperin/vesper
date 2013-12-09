package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.CommitRequest;
import edu.ucsc.refactor.Source;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class GistCommitRequest implements CommitRequest {
    private static final Logger LOGGER      = Logger.getLogger(GistCommitRequest.class.getName());
    private static final String DOT_JAVA    = ".java";

    private final Change        change;
    private final Queue<Delta>  load;

    private final AtomicReference<Source> fileMatchingLastDelta;


    /**
     * Instantiates a new {@link GistCommitRequest}
     * @param change The change to be applied and transmitted.
     */
    public GistCommitRequest(Change change){
        this.change = change;
        this.load   = new LinkedList<Delta>();

        for(Delta each : change.getDeltas()){ // in order
            this.load.add(each);
        }


        this.fileMatchingLastDelta  = new AtomicReference<Source>();
    }

    @Override public boolean isValid() {
        return change.isValid();
    }

    @Override public void commit() throws RuntimeException {
        LOGGER.fine("Committing change...");

        while(!load.isEmpty()){
            final Delta each = this.load.remove();

            final String name    = each.getSourceFile().getName();
            final String before  = each.getBefore();
            final String after   = each.getAfter();

            // talking to gist.github.com will occur here...
            System.out.println("");
            System.out.println("Establishing a Gist connection ... \n");
            System.out.println("Processing " + name + " ... ");
            System.out.println("... " + before + "\n");
            System.out.println(name + " has been processed!");
            System.out.println(" ... ");
            System.out.println(after + "\n");

            if(this.load.isEmpty()){
                this.fileMatchingLastDelta.set(new Source(name + DOT_JAVA, after));
                System.out.println("Closing the opened Gist connection ... ");
            }
        }

        LOGGER.fine("Done committing changes...");
    }

    @Override public Source getUpdatedSource() {
        return this.fileMatchingLastDelta.get();
    }


    @Override public String more() {
        // todo(Huascar) add `more` information
        return "";
    }
}

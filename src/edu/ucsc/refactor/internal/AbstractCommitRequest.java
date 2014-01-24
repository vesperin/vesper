package edu.ucsc.refactor.internal;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.CommitStatus;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class AbstractCommitRequest implements CommitRequest {
    private static final String DOT_JAVA    = ".java";

    private final Change                    change;
    private final Queue<Delta>              load;
    private final AtomicReference<Source>   fileMatchingLastDelta;

    private CommitStatus                    status;

    /**
     * Creates a new {@link AbstractCommitRequest}
     * @param change The change to be applied and transmitted.
     */
    protected AbstractCommitRequest(Change change){
        this.change     = change;
        this.load       = new LinkedList<Delta>();

        for(Delta each : change.getDeltas()){ // in order
            this.load.add(each);
        }

        this.fileMatchingLastDelta  = new AtomicReference<Source>();
        this.status                 = CommitStatus.unknownStatus();
    }


    static String fixPrefixTooShort(String name){
        if(name.trim().length() < 3) return name + "temp";
        return name;
    }

    static String squashedDeltas(String name, Queue<Delta> deltas, ASTNode node) throws RuntimeException {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(fixPrefixTooShort(name), DOT_JAVA);
            while (!deltas.isEmpty()){
                final Delta next = deltas.remove();
                Files.write(next.getAfter().getBytes(), tempFile);
                if(deltas.isEmpty()){  // optimization
                    AstUtil.syncSourceProperty(next.getSource(), node);
                }
            }

            Files.readLines(tempFile, Charset.defaultCharset());

            return Joiner.on("\n").join(
                    Files.readLines(
                            tempFile,
                            Charset.defaultCharset()
                    )
            );
        } catch (Throwable ex){
            throw new RuntimeException(ex);
        } finally {
            if(tempFile != null){
                if(tempFile.exists()){
                    final boolean deleted = tempFile.delete();
                    assert deleted;
                }
            }
        }
    }

    @Override public boolean isValid() { return this.change.isValid(); }

    @Override public Source getSource() {
        assert this.fileMatchingLastDelta.get() != null;

        return this.fileMatchingLastDelta.get();
    }

    protected Queue<Delta> getLoad() { return this.load; }

    protected Change getChange() { return this.change; }


    @Override public CommitStatus getStatus() { return this.status; }


    @Override public String more() { return status.more(); }


    protected void updateStatus(CommitStatus status){
        this.status = this.status.update(status);
    }

    protected boolean updateSource(Source src){
        return this.fileMatchingLastDelta.compareAndSet(
                this.fileMatchingLastDelta.get(),
                src
        );
    }


    @Override public String toString() { return more(); }
}

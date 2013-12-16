package edu.ucsc.refactor.util;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class IoTest {
    static final String CONTENT         = "public class Source {}";

    @Test public void testReadingAFile() throws Exception {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("Source", ".java");
            Files.write(CONTENT.getBytes(), tempFile);

            final String joined = Joiner.on("\n").join(
                    Files.readLines(tempFile,
                    Charset.defaultCharset())
            );
            assertEquals(CONTENT, joined);
        } finally {
            if(tempFile != null){
                if(tempFile.exists()){
                    final boolean deleted = tempFile.delete();
                    assertThat(deleted, is(true));
                }
            }
        }
    }
}

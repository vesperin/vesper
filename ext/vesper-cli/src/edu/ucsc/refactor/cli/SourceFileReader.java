package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceFileReader {
    private SourceFileReader(){}

    public static String readContent(String path){
        Preconditions.checkArgument(
                "java".equals(Files.getFileExtension(path)),
                "unknown file extension"
        );

        return readContent(new File(path));
    }

    public static String readContent(File file) {
        final StringBuilder content = new StringBuilder();
        try {

            final List<String> lines = Files.readLines(file, Charset.defaultCharset());
            final Iterator<String> itr = lines.iterator();
            while(itr.hasNext()){
                content.append(itr.next());
                if(itr.hasNext()){
                    content.append(System.getProperty("line.separator"));
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return content.toString();
    }
}

package edu.ucsc.refactor.util;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class StringUtil {
    private StringUtil(){}

    public static String extractName(String filePathName) {
        if ( filePathName == null )
            return null;

        int dotPos      = filePathName.lastIndexOf( '.' );
        int slashPos    = filePathName.lastIndexOf( '\\' );

        if ( slashPos == -1 ){
            slashPos = filePathName.lastIndexOf( '/' );
        }

        if ( dotPos > slashPos ){
            return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0,
                    dotPos );
        }

        return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0 );
    }
}

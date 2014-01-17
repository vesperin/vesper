package edu.ucsc.refactor.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class AskQuestion {
    private AskQuestion(){}

    public static boolean ask(String question, boolean defaultValue){
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (true) {
                System.out.print(question + (defaultValue ? " [Y/n] " : " [y/N] "));

                String line = null;
                try { line = reader.readLine(); } catch (IOException ignored) {}

                if (line == null) {
                    throw new IllegalArgumentException("Error reading from standard in");
                }

                line = line.trim().toLowerCase();

                if (line.isEmpty()) { return defaultValue; }
                if ("y".equalsIgnoreCase(line) || "yes".equalsIgnoreCase(line)) { return true;  }
                if ("n".equalsIgnoreCase(line) || "no".equalsIgnoreCase(line))  { return false; }
            }
        } finally {
            System.out.println();
        }
    }
}

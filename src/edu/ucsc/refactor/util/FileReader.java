package edu.ucsc.refactor.util;

import java.io.*;
import java.nio.charset.Charset;

public class FileReader {
    private FileInputStream stream;
    private BufferedReader  reader;

    public FileReader(File file) throws FileNotFoundException {
        stream = new FileInputStream(file);
        InputStreamReader input = new InputStreamReader(stream,
                Charset.forName("UTF-8"));
        reader = new BufferedReader(input);
    }

    public FileReader(String path) throws IOException {
        stream = new FileInputStream(path);
        InputStreamReader input = new InputStreamReader(stream,
                Charset.forName("UTF-8"));
        reader = new BufferedReader(input);
    }


    public static String read(String path) throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(path);
            return reader.readAll();
        } finally {
            if (reader != null) reader.close();
        }
    }

    public static String read(File file) throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            return reader.readAll();
        } finally {
            if (reader != null) reader.close();
        }
    }


    public boolean isOpen() {
        return stream != null;
    }

    public void close() {
        // Do nothing if already closed.
        if (stream == null) return;

        try {
            stream.close();
            stream = null;
            reader = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public String readAll() throws IOException {
        return IO.readAll(reader);
    }

}
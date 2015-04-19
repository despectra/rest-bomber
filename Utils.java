package com.despectra.restbomber;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by dmitry on 4/2/15.
 */
public class Utils {
    public static final RandomDataGenerator GLOBAL_RANDOM_GENERATOR = new RandomDataGenerator();

    public static final List<String> RANDOM_STRINGS = new ArrayList<>();
    static {
        try {
            try (Stream<String> lines = Files.lines(FileSystems.getDefault().getPath("", "strings"), Charset.defaultCharset())) {
                lines.forEachOrdered(RANDOM_STRINGS::add);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int REQUESTS_COUNTER = 0;
    public static synchronized void increaseRequestsCounter() {
        REQUESTS_COUNTER++;
        if(REQUESTS_COUNTER % 500 == 0) {
            System.out.print("\r    " + REQUESTS_COUNTER + " requests done");
        }
    }

}

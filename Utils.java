package com.despectra.restbomber;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public static int BOMBERS_COUNT = 0;
    public static int REQUESTS_COUNTER = 0;

    public static synchronized void increaseRequestsCounter() {
        REQUESTS_COUNTER++;
        if(REQUESTS_COUNTER % 500 == 0) {
            System.out.print("\r    " + REQUESTS_COUNTER + " requests done");
        }
    }

    public static synchronized void increaseBombersCount(List<EventModel> countEventsList) {
        BOMBERS_COUNT++;
        if(countEventsList != null) {
            registerBombersCountChangedEvent(countEventsList);
        }
    }

    public static synchronized void decreaseBombersCount(List<EventModel> countEventsList) {
        BOMBERS_COUNT--;
        if(countEventsList != null) {
            registerBombersCountChangedEvent(countEventsList);
        }
    }

    private static void registerBombersCountChangedEvent(List<EventModel> countEventsList) {
        EventModel event = EventModel.createLastingEvent(System.currentTimeMillis());
        event.setProperty("count", BOMBERS_COUNT);
        countEventsList.add(event);
    }


    public static void preloadIds(String resourceUrl, IdsBucket bucket) {
        URL url = null;
        try {
            url = new URL(resourceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            InputStream stream = conn.getInputStream();
            StringWriter responseWriter = new StringWriter();
            IOUtils.copy(stream, responseWriter, "UTF-8");
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONArray entities = (JSONArray) parser.parse(responseWriter.toString());
            for(int i = 0; i < entities.size(); i++) {
                JSONObject item = (JSONObject) entities.get(i);
                long id = (long) item.get("id");
                bucket.putId(id);
            }

        } catch (IOException | ParseException e) {
            System.out.println("PANIC!!: " + e.getMessage() + " while executing preloading ids from " + url.toString());
        }
    }
}

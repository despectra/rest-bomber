package com.despectra.restbomber;

import com.despectra.restbomber.rest.GETBomber;
import com.despectra.restbomber.rest.POSTBomber;

import java.io.*;
import java.util.*;

public class Main {

    public static final String URL_STR = "http://localhost:3000/";

    public static final int READERS_COUNT = 500;
    public static final int WRITERS_COUNT = 100;
    public static final int READ_REQUESTS_COUNT = 1000;
    public static final int WRITE_REQUESTS_COUNT = 100;

    public static void main(String[] args) {
        runMain();
    }

    public static void runMain() {
        List<EventModel> responsesEvents = new LinkedList<>();
        List<EventModel> bombersCountEvents = new LinkedList<>();
        IdsStore store = new IdsStore();
        store.addBucket("users", new IdsBucket());

        Bomber readers[] = new Bomber[READERS_COUNT];
        Thread readersThreads[] = new Thread[READERS_COUNT];
        System.out.println("Started");
        long globalStartTime = System.currentTimeMillis();
        for(int i = 0; i < READERS_COUNT; i++) {
            readers[i] = new GETBomber(responsesEvents, READ_REQUESTS_COUNT, 10, 300, x -> 1 - x, URL_STR, "users/#", store);
            readersThreads[i] = new Thread(readers[i]);
            readersThreads[i].start();
            EventModel readerCreatedEvent = EventModel.createOneMomentEvent(System.currentTimeMillis());
            readerCreatedEvent.setProperty("type", "reader");
            readerCreatedEvent.setProperty("count", i + 1);
            bombersCountEvents.add(readerCreatedEvent);
        }
        Bomber writers[] = new Bomber[WRITERS_COUNT];
        Thread writersThread[] = new Thread[WRITERS_COUNT];
        for(int i = 0; i < WRITERS_COUNT; i++) {
            writers[i] = new POSTBomber(responsesEvents, WRITE_REQUESTS_COUNT, 50, 1500, x -> 1 - x, URL_STR, "users/", store,
                    "first_name", "last_name", "email");
            writersThread[i] = new Thread(writers[i]);
            writersThread[i].start();
            EventModel writerCreatedEvent = EventModel.createOneMomentEvent(System.currentTimeMillis());
            writerCreatedEvent.setProperty("type", "writer");
            writerCreatedEvent.setProperty("count", i + 1);
            bombersCountEvents.add(writerCreatedEvent);
        }

        for(int i = 0; i < READERS_COUNT; i++) {
            try {
                readersThreads[i].join();
                EventModel readerFinishedEvent = EventModel.createOneMomentEvent(System.currentTimeMillis());
                readerFinishedEvent.setProperty("type", "reader");
                readerFinishedEvent.setProperty("count", READERS_COUNT - i - 1);
                bombersCountEvents.add(readerFinishedEvent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < WRITERS_COUNT; i++) {
            try {
                writersThread[i].join();
                EventModel writerFinishedEvent = EventModel.createOneMomentEvent(System.currentTimeMillis());
                writerFinishedEvent.setProperty("type", "writer");
                writerFinishedEvent.setProperty("count", WRITERS_COUNT - i - 1);
                bombersCountEvents.add(writerFinishedEvent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Bombing completed. Writing results...");
        Collections.sort(responsesEvents, (EventModel e1, EventModel e2) -> ((Long)e1.getStartTime()).compareTo(e2.getStartTime()));
        FileWriter responsesWriter = null, bombersWriter = null;
        try {
            FileWriter innerResponseWriter, innerBombersWriter;
            responsesWriter = new FileWriter("responses", false);
            bombersWriter = new FileWriter("bombers", false);
            innerResponseWriter = responsesWriter;
            innerBombersWriter = bombersWriter;

            writeStringToFile(String.format("# GET (Index 0)%n# start duration bytes_read%n"), responsesWriter);
            responsesEvents.stream()
                    .filter(e -> e.getProperty("method").equals("GET"))
                    .forEach(e -> writeStringToFile(String.format("%d %d %d%n", e.getStartTime() - globalStartTime,
                            e.getEndTime() - e.getStartTime(),
                            e.getProperty("bytes read")), innerResponseWriter));
            writeStringToFile(String.format("%n%n%n# POST (Index 1)%n# start duration bytes_read%n"), responsesWriter);
            responsesEvents.stream()
                    .filter(e -> e.getProperty("method").equals("POST"))
                    .forEach(e -> writeStringToFile(String.format("%d %d %d%n", e.getStartTime() - globalStartTime,
                            e.getEndTime() - e.getStartTime(),
                            e.getProperty("bytes read")), innerResponseWriter));

            responsesWriter.flush();
            responsesWriter.close();

            bombersWriter.write("time count\n");
            bombersCountEvents.stream()
                    .filter(e -> e.getProperty("type").equals("reader"))
                    .forEach(e -> writeStringToFile(String.format("%d %d\n",
                            e.getStartTime() - globalStartTime,
                            e.getProperty("count")), innerBombersWriter));
            writeStringToFile(String.format("%n"), bombersWriter);
            bombersCountEvents.stream()
                    .filter(e -> e.getProperty("type").equals("writer"))
                    .forEach(e -> writeStringToFile(String.format("%d %d\n",
                            e.getStartTime() - globalStartTime,
                            e.getProperty("count")), innerBombersWriter));

            bombersWriter.flush();
            bombersWriter.close();
            System.out.println("Done");
            //Runtime r = Runtime.getRuntime();
            //r.exec("gnuplot -persist behav_graph.plot").waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(responsesWriter != null) {
                try { responsesWriter.close(); } catch (IOException e) {}
            }
            if(bombersWriter != null) {
                try { bombersWriter.close(); } catch (IOException e) {}
            }
        }
    }

    public static void writeStringToFile(String str, FileWriter writer) {
        try {
            writer.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runSandbox() {
        List<EventModel> responsesEvents = new LinkedList<>();
        IdsStore store = new IdsStore();
        store.addBucket("users", new IdsBucket());

        store.addBucket("props", new IdsBucket());

        POSTBomber bomber = new POSTBomber(responsesEvents, 1000, 10, 10, x -> 0, URL_STR, "users/", store, "first_name", "last_name", "email");
        bomber.run();

        GETBomber getBomber = new GETBomber(responsesEvents, 1, 10, 20, x -> 0, URL_STR, "users/#", store);
        for(int i = 0; i < 10; i++) {
            System.out.println(getBomber.generateRequestUrl());
        }

    }

}

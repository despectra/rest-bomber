package com.despectra.restbomber;

import com.despectra.restbomber.rest.GETBomber;
import com.despectra.restbomber.rest.POSTBomber;

import java.io.*;
import java.util.*;

public class Main {

    public static final String URL_STR = "http://dbtest.io/";

    public static void main(String[] args) {
        runMain(args);
    }

    public static void runMain(String[] args) {

        List<EventModel> responsesEvents = new LinkedList<>();
        List<EventModel> bombersCountEvents = new LinkedList<>();

        IdsStore store = new IdsStore();
        IdsBucket usersBucket = new IdsBucket();
        System.out.println("Preloading ids for users...");
        Utils.preloadIds(URL_STR + "users", usersBucket);
        System.out.println("Preloading completed. " + usersBucket.getSize() + " users");
        store.addBucket("users", usersBucket);
        //store.addBucket("groups", new IdsBucket());

        Bomber.OnStartedListener startedListener = () -> Utils.increaseBombersCount(bombersCountEvents);
        Bomber.OnFinishedListener finishedListener = () -> Utils.decreaseBombersCount(bombersCountEvents);

        Bomber reader = new GETBomber(responsesEvents, 50, 1, 1, x -> 1, URL_STR, "users/#", store);
        reader.setOnStartedListener(startedListener);
        reader.setOnFinishedListener(finishedListener);
        Bomber writer = new POSTBomber(responsesEvents, 100, 1, 100, x -> 1 - x, URL_STR, "users/", store,  "first_name", "last_name", "email");

        TestingScenario scenario = new TestingScenario.Builder()
                .addBombers("user_readers", reader, 500, 0, false)
                //.addBombers("user_writers", writer, 20, 0, false)
                /*.addBombers("user_writers",
                        new POSTBomber(responsesEvents, 15, 10, 10, x -> 1, URL_STR, "users/", store,
                                "first_name", "last_name", "email"),
                        10)
                .addBombers("group_writers",
                        new POSTBomber(responsesEvents, 10, 10, 10, x -> 1, URL_STR, "groups/", store,
                                "establishment_date", "description"),
                        10)
                .addBombers("attachers",
                        new GETBomber(responsesEvents, 100, 30, 500, x -> 1, URL_STR, "groups/#/users/#/attach", store),
                        100)
                .addBombers("related_readers",
                        new GETBomber(responsesEvents, 100, 10, 500, x -> 1, URL_STR, "groups/#/users", store),
                        500)*/
                .build();

        System.out.println("Started " + new Date().toString());
        long globalStartTime = System.currentTimeMillis();
        scenario.startTesting();
        scenario.waitForFinish();

        System.out.println("\nBombing completed. Writing results...");
        Collections.sort(responsesEvents, (EventModel e1, EventModel e2) -> ((Long)e1.getStartTime()).compareTo(e2.getStartTime()));
        FileWriter responsesWriter = null;
        FileWriter bombersWriter = null;
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
            writeStringToFile(String.format("# COUNT(Index 0)%n# time count%n"), bombersWriter);
            bombersCountEvents.stream()
                    .forEach(e -> writeStringToFile(String.format("%d %d%n", e.getStartTime() - globalStartTime, e.getProperty("count")),
                            innerBombersWriter));

            responsesWriter.flush();
            responsesWriter.close();
            bombersWriter.flush();
            bombersWriter.close();

            System.out.println("Done " + new Date().toString());
            Runtime r = Runtime.getRuntime();
            r.exec("./visualize.sh Apache_mysql_single_hiload_r2").waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(responsesWriter != null) {
                try { responsesWriter.close(); } catch (IOException e) {}
            }
            if (bombersWriter != null) {
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

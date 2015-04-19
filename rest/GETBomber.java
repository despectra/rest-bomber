package com.despectra.restbomber.rest;

import com.despectra.restbomber.Bomber;
import com.despectra.restbomber.EventModel;
import com.despectra.restbomber.IdsStore;
import com.despectra.restbomber.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Created by dmitry on 3/24/15.
 */
public class GETBomber extends RestBomber {

    public GETBomber(List<EventModel> eventsList,
                     int requestsCount,
                     long minPause, long maxPause, DoubleUnaryOperator easingFunc,
                     String host, String entityPath, IdsStore store) {
        super(eventsList, requestsCount, minPause, maxPause, easingFunc, host, entityPath, store);
    }

    @Override
    protected void doRequest() {
        URL url = null;
        try {
            url = new URL(generateRequestUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            EventModel event = startEvent();
            conn.connect();
            InputStream stream = conn.getInputStream();
            int bytesRead = 0;
            while (stream.read() != -1) bytesRead++;
            stream.close();
            event.setEndTime(System.currentTimeMillis());
            event.setProperty("method", "GET");
            event.setProperty("path", mRawEntityPath);
            event.setProperty("bytes read", bytesRead);
            conn.disconnect();
        } catch (IOException e) {
            System.out.println("PANIC!!: " + e.getMessage() + " while executing GET " + url.toString());
        }
    }

    @Override
    protected Bomber setupSpecificClone(Bomber absClone) {
        return absClone;
    }
}

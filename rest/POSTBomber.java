package com.despectra.restbomber.rest;

import com.despectra.restbomber.*;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Created by dmitry on 4/1/15.
 */
public class POSTBomber extends RestBomber {

    private List<String> mEntityPropsNames;
    private StringBuilder mBodyBuilder;

    public POSTBomber(List<EventModel> eventsList,
                      int requestsCount,
                      long minPause, long maxPause, DoubleUnaryOperator easingFunc,
                      String host, String entityPath, IdsStore store, String... entityPropsNames) {
        super(eventsList, requestsCount, minPause, maxPause, easingFunc, host, entityPath, store);
        if(mPathType != EntityPathType.List) {
            throw new IllegalArgumentException("POST requests URLs should relate only to entities list (NOT a single one, like ../entity/#/");
        }
        mEntityPropsNames = Arrays.asList(entityPropsNames);
        mBodyBuilder = new StringBuilder();
    }

    @Override
    protected void doRequest() {
        URL url = null;
        String body = null;
        try {
            url = new URL(generateRequestUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter osWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            body = generateUrlencodedBody();
            osWriter.write(body);
            osWriter.close();
            EventModel event = startEvent();

            conn.connect();
            StringWriter responseWriter = new StringWriter();
            InputStream is = null;
            is = conn.getInputStream();
            IOUtils.copy(is, responseWriter, "UTF-8");
            int bytesRead = responseWriter.getBuffer().length() * 2;
            is.close();

            event.setEndTime(System.currentTimeMillis());
            event.setProperty("method", "POST");
            event.setProperty("path", mRawEntityPath);
            event.setProperty("bytes read", bytesRead);
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject responseObject = (JSONObject) parser.parse(responseWriter.toString());
            long id = (long) responseObject.get("id");
            mStore.getBucket(mMainEntity).putId(id);

        } catch (IOException | ParseException e) {
            System.out.println("PANIC!!: " + e.getMessage() + " while executing POST " + url.toString() + ": " + body);
        }
    }

    @Override
    protected Bomber setupSpecificClone(Bomber absClone) {
        ((POSTBomber)absClone).mBodyBuilder = new StringBuilder();
        return absClone;
    }

    public String generateUrlencodedBody() {
        mBodyBuilder.delete(0, mBodyBuilder.toString().length());
        for (String propName : mEntityPropsNames) {
            mBodyBuilder.append(propName);
            mBodyBuilder.append("=");
            mBodyBuilder.append(Utils.RANDOM_STRINGS.get((int)(Math.random() * (Utils.RANDOM_STRINGS.size() - 1))));
            mBodyBuilder.append("&");
        }
        mBodyBuilder.deleteCharAt(mBodyBuilder.toString().length() - 1);
        return mBodyBuilder.toString();
    }
}

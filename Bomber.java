package com.despectra.restbomber;

import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.DoubleUnaryOperator;

/**
* Created by dmitry on 3/16/15.
*/
public abstract class Bomber implements Runnable {

    private int mRequestsCount;
    private long mMinPause;
    private long mMaxPause;
    private DoubleUnaryOperator mEasing;
    private final List<EventModel> mEventsList;

    public Bomber(List<EventModel> eventsList,
                  int requestsCount,
                  long minPause,
                  long maxPause,
                  DoubleUnaryOperator easingFunc) {
        mRequestsCount = requestsCount;
        mMinPause = minPause;
        mMaxPause = maxPause;
        mEasing = easingFunc;
        mEventsList = eventsList;
    }

    protected EventModel startEvent() {
        EventModel event;
        synchronized (mEventsList) {
            event = EventModel.createLastingEvent(System.currentTimeMillis());
            mEventsList.add(event);
        }
        return event;
    }

    protected abstract void doRequest();

    @Override
    public void run() {
        long pause;
        for(int i = 0; i < mRequestsCount; i++) {
            doRequest();
            try {
                pause = (long) (mEasing.applyAsDouble(i / (double)mRequestsCount) * (mMaxPause - mMinPause)) + mMinPause;
                if(pause > 10) {
                    pause += Utils.GLOBAL_RANDOM_GENERATOR.getRandomNearZero(5);
                }
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

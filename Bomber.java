package com.despectra.restbomber;

import java.util.*;
import java.util.function.DoubleUnaryOperator;

/**
* Created by dmitry on 3/16/15.
*/
public abstract class Bomber implements Runnable, Cloneable {

    protected int mRequestsCount;
    protected long mMinPause;
    protected long mMaxPause;
    protected DoubleUnaryOperator mEasing;
    protected List<EventModel> mEventsList;

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
            Utils.increaseRequestsCounter();
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Bomber absClone = (Bomber) super.clone();
        return setupSpecificClone(absClone);
    }

    protected abstract Bomber setupSpecificClone(Bomber absClone);
}

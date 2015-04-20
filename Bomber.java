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

    private long mStartDelay;
    private OnStartedListener mStartedListener;
    private OnFinishedListener mFinishedListener;

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
        mStartDelay = 0;
    }

    public void setDelay(long delayms) {
        if(delayms > 0) {
            mStartDelay = delayms;
        }
    }

    public void setOnStartedListener(OnStartedListener listener) {
        mStartedListener = listener;
    }

    public void setOnFinishedListener(OnFinishedListener listener) {
        mFinishedListener = listener;
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
        //sleep if delay is setup
        try {
            Thread.sleep(mStartDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //THEN DO THE BOMBING
        //notify start
        if (mStartedListener != null) {
            mStartedListener.onStarted();
        }
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
        //notify finish
        if (mFinishedListener != null) {
            mFinishedListener.onFinished();
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Bomber absClone = (Bomber) super.clone();
        return setupSpecificClone(absClone);
    }

    protected abstract Bomber setupSpecificClone(Bomber absClone);

    public interface OnStartedListener {
        void onStarted();
    }

    public interface OnFinishedListener {
        void onFinished();
    }
}

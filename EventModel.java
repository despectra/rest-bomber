package com.despectra.restbomber;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmitry on 3/24/15.
 */
public class EventModel {

    private long mStartTime;
    private long mEndTime;
    private boolean mEnded;
    private Map<String, Object> mProperties;

    private EventModel(long timeStart) {
        mStartTime = timeStart;
        mEnded = false;
        mProperties = new HashMap<>();
    }

    public static EventModel createLastingEvent(long timeStart) {
        return new EventModel(timeStart);
    }

    public static EventModel createOneMomentEvent(long time) {
        EventModel event = new EventModel(time);
        event.setEndTime(time);
        return event;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        if(!mEnded) {
            return  0;
        }
        return mEndTime;
    }

    public void setEndTime(long time) {
        if(mEnded) {
            return;
        }
        mEndTime = time;
        mEnded = true;
    }

    public Object getProperty(String name) {
        return mProperties.get(name);
    }

    public void setProperty(String name, Object value) {
        mProperties.put(name, value);
    }
}

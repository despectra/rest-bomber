package com.despectra.restbomber.rest;

import com.despectra.restbomber.Bomber;
import com.despectra.restbomber.EventModel;
import com.despectra.restbomber.IdsBucket;
import com.despectra.restbomber.IdsStore;

import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Created by dmitry on 4/1/15.
 */
public abstract class RestBomber extends Bomber {

    public enum EntityPathType {
        Single,
        List
    };

    protected IdsStore mStore;
    protected String mHost;
    protected String mRawEntityPath;
    protected List<String> mUsingEntities;
    protected String mMainEntity;
    protected EntityPathType mPathType;

    public RestBomber(List<EventModel> eventsList,
                      int requestsCount, long minPause, long maxPause, DoubleUnaryOperator easingFunc,
                      String host, String entityPath, IdsStore store) {
        super(eventsList, requestsCount, minPause, maxPause, easingFunc);
        mStore = store;
        mHost = host;
        mRawEntityPath = entityPath;
        mUsingEntities = new LinkedList<>();
        parseEntityPath();
    }

    private void parseEntityPath() {
        String[] levels = mRawEntityPath.split("/");

        String curEntity = "";
        boolean wasEntity = false;
        for(String level : levels) {
            if(level.isEmpty()) {
                throw new IllegalArgumentException("Entity path elements should be non-empty (NOT ../ent1//ent2..)");
            }

            if(!wasEntity && !level.equals("#")) {
                curEntity = level;
                wasEntity = true;
            } else if(wasEntity && level.equals("#")) {
                mUsingEntities.add(curEntity);
                curEntity = "";
                wasEntity = false;
            } else {
                throw new IllegalArgumentException("Entity path is incorrect (../ent1/ent2/.. or ../ent1/#/#/..");
            }
        }
        mPathType = wasEntity ? EntityPathType.List : EntityPathType.Single;
        mMainEntity = curEntity;
    }

    public final String generateRequestUrl() {
        StringBuilder pathBuilder = new StringBuilder(mHost);
        if(mHost.charAt(mHost.length() - 1) != '/') {
            pathBuilder.append("/");
        }
        IdsBucket curBucket;
        for (String entity : mUsingEntities) {
            curBucket = mStore.getBucket(entity);
            if(curBucket == null) {
                return null;
            }
            pathBuilder.append(entity);
            pathBuilder.append("/");
            pathBuilder.append(curBucket.getRandomId());
            //pathBuilder.append("/");
        }
        if(mPathType == EntityPathType.List) {
            pathBuilder.append("/");
            pathBuilder.append(mMainEntity);
        }
        return  pathBuilder.toString();
    }

    public String getMainEntity() {
        return mMainEntity;
    }

    public EntityPathType getEntityPathType() {
        return mPathType;
    }

    public List<String> getUsingEntities() {
        return mUsingEntities;
    }
}

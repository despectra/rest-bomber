package com.despectra.restbomber;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmitry on 4/1/15.
 */
public class IdsStore {

    private Map<String, IdsBucket> mStore;

    public IdsStore() {
        mStore = new HashMap<>();
    }

    public void addBucket(String name, IdsBucket bucket) {
        mStore.put(name, bucket);
    }

    public IdsBucket getBucket(String name) {
        return mStore.get(name);
    }

}

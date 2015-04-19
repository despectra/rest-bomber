package com.despectra.restbomber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by dmitry on 4/1/15.
 */
public class IdsBucket {

    private Set<Long> mIds;
    private Random mRandom;

    public IdsBucket() {
        mIds = new HashSet<>(100);
        mRandom = new Random(System.currentTimeMillis());
    }

    public synchronized void putId(long id) {
        mIds.add(id);
        if(mIds.size() == 1) {
            notifyAll();
        }
    }

    public long getRandomId() {
        long id = -1;
        try {
            synchronized (this) {
                while (mIds.size() == 0) {
                    wait();
                }
                int pos = mRandom.nextInt(mIds.size());
                int i = 0;
                for (long curId : mIds) {
                    if (i >= pos) {
                        id = curId;
                        break;
                    }
                    i++;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return id;
    }

    public int getSize() {
        return mIds.size();
    }

}

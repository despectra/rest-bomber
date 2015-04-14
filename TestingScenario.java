package com.despectra.restbomber;

import com.despectra.restbomber.rest.GETBomber;
import com.despectra.restbomber.rest.POSTBomber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dmitry on 4/14/15.
 */
public class TestingScenario {

    private Map<String, BombersGroup> mBombersGroups;
    private List<Thread> mThreads;

    public TestingScenario() {
        mBombersGroups = new HashMap<>();
        mThreads = new ArrayList<>();
    }

    private void addBomberInGroup(String groupName, Bomber bomber) {
        BombersGroup group = mBombersGroups.get(groupName);
        if(group == null) {
            group = new BombersGroup();
        }
        mBombersGroups.put(groupName, group);
        mThreads.add(new Thread(bomber));
        group.addBomber(bomber);
    }

    public void startTesting() {
        System.out.println(mThreads.size());
        mThreads.forEach(Thread::start);
    }

    public void waitForFinish() {
        mThreads.forEach((Thread t) -> { try { t.join(); } catch (InterruptedException e) {} });
    }

    public static class Builder {

        private TestingScenario mScenario;

        public Builder() {
            mScenario = new TestingScenario();
        }

        public Builder addBombers(String groupName, Bomber prototype, int amount) {
            try {
                for(int i = 0; i < amount; i++) {
                    mScenario.addBomberInGroup(groupName, (Bomber) prototype.clone());
                }
            } catch (CloneNotSupportedException e) {}
            return this;
        }

        public TestingScenario build() {
            return mScenario;
        }
    }
}

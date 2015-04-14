package com.despectra.restbomber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmitry on 4/14/15.
 */
public class BombersGroup {
    private List<Bomber> mGroup;

    public BombersGroup() {
        mGroup = new ArrayList<>();
    }

    public void addBomber(Bomber bomber) {
        mGroup.add(bomber);
    }

}

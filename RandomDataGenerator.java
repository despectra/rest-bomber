package com.despectra.restbomber;

import java.util.Random;

/**
 * Created by dmitry on 4/1/15.
 */
public class RandomDataGenerator {

    public static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";

    private Random mRandom = new Random(System.currentTimeMillis());
    private StringBuilder mBuilder = new StringBuilder();

    public String generateString(int lengthRangeFrom, int lengthRangeTo) {
        if(lengthRangeFrom > lengthRangeTo || lengthRangeFrom < 1 || lengthRangeTo < 1) {
            throw new IllegalArgumentException("Illegal range");
        }
        int length = mRandom.nextInt(lengthRangeTo - lengthRangeFrom) + lengthRangeFrom;
        return generateString(length);
    }

    public String generateString(int length) {
        mBuilder.delete(0, mBuilder.toString().length());
        for(int i = 0; i < length; i++) {
            mBuilder.append(CHARS.charAt(mRandom.nextInt(CHARS.length())));
        }
        return mBuilder.toString();
    }

    public boolean sayYes(double probability) {
        return mRandom.nextDouble() < probability;
    }

    public int getRandomNearZero(int epsilon) {
        return mRandom.nextInt(epsilon * 2) - epsilon;
    }

}

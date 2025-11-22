package com.notesSharingApp.notesSharingApp.Util.RateLimit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OTP_RATELIMITER {
    private static final int MAX_REQUEST_PER_DAY = 3;
    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
    private static final ConcurrentHashMap<String, List<Long>> IPS_LIST = new ConcurrentHashMap<String, List<Long>>();
    public static boolean isAllowed(String ip){
       long now = System.currentTimeMillis();
       IPS_LIST.putIfAbsent(ip, new ArrayList<>());
       List<Long> timestamps = IPS_LIST.get(ip);
       timestamps.removeIf(t -> now - t > ONE_DAY_MS);

       if(timestamps.size() >= MAX_REQUEST_PER_DAY){
           return false;
       }
       timestamps.add(now);
       return true;
    }
}

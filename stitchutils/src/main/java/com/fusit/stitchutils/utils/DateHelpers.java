package com.fusit.stitchutils.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by tamarraviv.
 */
public class DateHelpers {
    public static void main(String[]args) {
        Map<TimeUnit,Long> result = computeDiff(
                new Date(System.currentTimeMillis()-100000000),
                new Date()
        );
        System.out.println(result);
    }

    public static Map<TimeUnit,Long> computeDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);

        Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit,Long>();
        long milliesRest = diffInMillies;
        for ( TimeUnit unit : units ) {
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit,diff);
        }
        return result;
    }

    public static Long unixTime(){
        return System.currentTimeMillis() / 1000L;
    }

    public static Long unixTimeMilliseconds(){
        return System.currentTimeMillis();
    }

    public static Long timeSpentMilliseconds(long begin){
        return unixTimeMilliseconds() - begin;
    }
}

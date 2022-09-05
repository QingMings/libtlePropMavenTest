package org.bytedeco.libtleprop;

import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    public static final String UTCtimeStampPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String UTCtimeStampMsPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TimeStampPattern = "yyyy-MM-dd HH:mm:ss";

    public static JulianDate localToJulianDate(Date local){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(local.getTime());
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        calendar.add(Calendar.MILLISECOND,-(zoneOffset+dstOffset));
        return new JulianDate(new GregorianDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.HOUR_OF_DAY)
                ,calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        ));
    }

    public static Date getUtcTime(){
        // 1. 本地时间
        Calendar calendar = Calendar.getInstance();
        // 2. 时间偏移量
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        // 3. 夏令时差
        int dstOffsest = calendar.get(Calendar.DST_OFFSET);
        // 4. 从本地时间扣除差量，得到utc时间
        calendar.add(Calendar.MILLISECOND,-(zoneOffset+dstOffsest));
        return calendar.getTime();
    }

    public static Date localToUTC(Date local){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(local.getTime());
        // 2. 时间偏移量
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        // 3. 夏令时差
        int dstOffsest = calendar.get(Calendar.DST_OFFSET);
        // 4. 从本地时间扣除差量，得到utc时间
        calendar.add(Calendar.MILLISECOND,-(zoneOffset+dstOffsest));
        return new Date(calendar.getTimeInMillis());
    }

    public static Date utcToLocal(Date utcDate){
        DateFormat df = new SimpleDateFormat(TimeStampPattern);
        df.setTimeZone(TimeZone.getDefault());
        String utc = df.format(utcDate.getTime());
        return utcToLocal(utc);
    }
    public static Date utcToLocal(String utcTime){
        DateFormat df = new SimpleDateFormat(TimeStampPattern);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate =null;
        try {
            df.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        df.setTimeZone(TimeZone.getDefault());
        Date localDate = null;
        String localTime = df.format(utcDate.getTime());
        try {
            localDate = df.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return localDate;

    }
    /**
     *
     * @param epoch  eg. 2022-08-31 11:23:56
     * @return JulianDate
     */
    public static JulianDate epochToJulianDate(String epoch){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(cn.hutool.core.date.DateUtil.parseDateTime(epoch));
        return  new JulianDate(new GregorianDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.HOUR_OF_DAY)
                ,calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        ));

    }

    /**
     *
     * @param epochTZ eg. 2022-08-31T11:23:56Z
     * @return JulianDate
     */
    public static JulianDate epochTZToJulianDate(String epochTZ){
        String epoch = epochTZ.replace("T"," ").replace("Z","");
        return epochToJulianDate(epoch);
    }
}

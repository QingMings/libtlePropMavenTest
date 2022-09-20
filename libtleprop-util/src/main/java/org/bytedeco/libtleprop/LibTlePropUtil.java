package org.bytedeco.libtleprop;

import cesiumlanguagewriter.JulianDate;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.libtleprop.global.libtleprop;
import org.joda.time.DateTimeField;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class LibTlePropUtil {
    static {
        Loader.load(libtleprop.class);
        libtleprop.InitializationConst();
    }

    private static final double TO_JUTC1 = 2400000.5d;

    public static Date mjd2date(double mjd) {

        IntPointer yr = new IntPointer(1);
        IntPointer mo = new IntPointer(1);
        IntPointer dy = new IntPointer(1);
        IntPointer hr = new IntPointer(1);
        IntPointer mi = new IntPointer(1);
        DoublePointer se = new DoublePointer(1);

        libtleprop.MJD2YMDHMS(mjd, yr, mo, dy, hr, mi, se);
        int seconds = (int) se.get();
        int milliseconds = (int) ((se.get() - seconds) * 1000);
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.YEAR,  yr.get());
//            cal.set(Calendar.MONTH, mo.get());
//            cal.set(Calendar.DAY_OF_MONTH, dy.get());
//            cal.set(Calendar.HOUR_OF_DAY, hr.get());
//            cal.set(Calendar.MINUTE,  mi.get());
//            cal.set(Calendar.SECOND, seconds);
//            cal.set(Calendar.MILLISECOND,milliseconds);
//        System.out.println(hr.get());
        return new org.joda.time.DateTime(yr.get(), mo.get(), dy.get(), hr.get(), mi.get(), seconds, milliseconds).toDate();

    }

    public static double date2mjd(org.joda.time.DateTime date) {
        int yr = date.getYear();
        int mo = date.getMonthOfYear() + 1;
        int dy = date.getDayOfMonth();
        int hr = date.getHourOfDay();
        int mi = date.getMinuteOfHour();
        int sec = date.getSecondOfMinute();
        int millise = date.getMillisOfSecond();
        double se = sec + millise / 100f;

        return libtleprop.YMDHMS2MJD(yr, mo, dy, hr, mi, se);
    }

    private static double formatSec3(double sec) {
        String format = String.format("%.3f", sec);
        return Double.parseDouble(format);
    }

    public static double mjd2JUTC1(double mjd) {
        return mjd + TO_JUTC1;
    }

    public static double jutc12mjd(double junc1) {
        return junc1 - TO_JUTC1;
    }
}


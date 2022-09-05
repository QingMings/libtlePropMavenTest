package org.bytedeco.libtleprop;

import cesiumlanguagewriter.JulianDate;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.libtleprop.global.libtleprop;

import java.time.LocalDateTime;
import java.util.Date;

public class LibTlePropUtil {
    static {
        libtleprop.InitializationConst();
    }
    private static final double TO_JUTC1 = 2400000.5d;
    public static Date mjd2date(double mjd) {
        try (
                IntPointer yr = new IntPointer(1);
                IntPointer mo = new IntPointer(1);
                IntPointer dy = new IntPointer(1);
                IntPointer hr = new IntPointer(1);
                IntPointer mi = new IntPointer(1);
                DoublePointer se = new DoublePointer(1)
        ) {
            libtleprop.MJD2YMDHMS(mjd, yr, mo, dy, hr, mi, se);
            double sec = formatSec3(se.get());
            return DateUtil.parse(StrUtil.format("{}-{}-{} {}:{}:{}", yr.get(), mo.get(), dy.get(), hr.get(), mi.get(), sec));
        }
    }

    public static double date2mjd(DateTime date){
        int yr =date.getField(DateField.YEAR);
        int mo  = date.getField(DateField.MONTH)+1;
        int dy = date.getField(DateField.DAY_OF_MONTH);
        int hr = date.getField(DateField.HOUR_OF_DAY);
        int mi = date.getField(DateField.MINUTE);
        int sec = date.getField(DateField.SECOND);
        int  millise  =date.getField(DateField.MILLISECOND);
        double se = sec+ millise/100f;

        return libtleprop.YMDHMS2MJD(yr,mo,dy,hr,mi,se);
    }
    private static double formatSec3(double sec) {
        String format = String.format("%.3f", sec);
        return Double.parseDouble(format);
    }
    public static double mjd2JUTC1(double mjd) {
        return mjd + TO_JUTC1;
    }
    public static  double jutc12mjd(double junc1){
        return junc1 - TO_JUTC1;
    }
}


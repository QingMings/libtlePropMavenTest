package org.bytedeco.libtleprop;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.libtleprop.global.libtleprop;

public class Main2 {
    static {
        libtleprop.InitializationConst();
    }

    public static final double TO_JUTC1 = 2400000.5d;

    public static void main(String[] args) {
        double mjd = libtleprop.YMDHMS2MJD(2022, 8, 30, 23, 35, 59.023);
        String line1 = "1 00005U 58002B   22194.90455603  .00000355  00000-0  44510-3 0  9990";
        String line2 = "2 00005  34.2486  57.8961 1847235 156.6009 213.0824 10.84976421287411";


        BytePointer line1P = new BytePointer(line1.getBytes().length);
        BytePointer line2P = new BytePointer(line2.getBytes().length);
        DELE_TLE tle_rec = new DELE_TLE();
        IntPointer yr = new IntPointer(4);
        IntPointer mo = new IntPointer(2);
        IntPointer dy = new IntPointer(2);
        IntPointer hr = new IntPointer(2);
        IntPointer mi = new IntPointer(2);
        DoublePointer se = new DoublePointer(8);
        elsetrec sat_rec = new elsetrec();


        line1P.put(line1.getBytes());
        line2P.put(line2.getBytes());

        libtleprop.InitializeTwoLineElement(line1P, line2P, tle_rec);
        tle_rec.satname().put("5".getBytes());
        libtleprop.MJD2YMDHMS(tle_rec.mjd(), yr, mo, dy, hr, mi, se);
        libtleprop.InitializeTleSetRec(tle_rec, sat_rec);

        double mjdStart = mjd;
        double mjdStop = mjd + 1.0;
        double mjdCur = mjdStart;
        float stepEph = 1.0f / 30.0f;

        float stepRef = 60.0f;
        //初始化参考点1
        double jutc1 = mjd2JUTC1(mjdStart);
        DTIME_ID TT1 = libtleprop.GetTIMEValue_JUTC(jutc1);
        DoublePointer sateph_array_t1 = new DoublePointer(54);
        int flag = libtleprop.GetSatEph_Array_SGP4_TT_2(tle_rec.jtt(),sat_rec,TT1,sateph_array_t1);
        //初始化参考点2
        double jutc2 = jutc1+stepRef/8640.0f;
        DTIME_ID TT2 = libtleprop.GetTIMEValue_JUTC(jutc2);
        DoublePointer sateph_array_t2 = new DoublePointer(54);
        flag = libtleprop.GetSatEph_Array_SGP4_TT_2(tle_rec.jtt(),sat_rec,TT2,sateph_array_t2);
        int nStep=0;
        DoublePointer jtt_t = new DoublePointer(8);
        DoublePointer jut1_t = new DoublePointer(8);
        double jutc_t = mjd2JUTC1(mjdCur);
        libtleprop.GetTT_UT1(jutc_t,jtt_t,jut1_t);
        DoublePointer sateph_array_t = new DoublePointer(54);
        flag = libtleprop.GetSatEph_Array_SGP4_J2_2(tle_rec.jtt(),sat_rec,
                stepRef,sateph_array_t1,sateph_array_t2,jutc_t,jtt_t.get(),jut1_t.get(),sateph_array_t);
        //J2000
        float x = -(float) (sateph_array_t.get(22)/10000); //-y
        float y = (float) (sateph_array_t.get(23)/10000); // z
        float z = (float) (sateph_array_t.get(21)/10000); //x

        //地固系
        float xx = -(float) (sateph_array_t.get(28)/10000); // -y
        float yy = (float) (sateph_array_t.get(29)/10000); // z
        float zz = (float) (sateph_array_t.get(27)/10000); // x

        double L = sateph_array_t.get(51);
        double B = sateph_array_t.get(52);
        double H = sateph_array_t.get(53);
//        System.out.println(String.join(",",String.valueOf(L),String.valueOf(B),String.valueOf(H)));
        for (int i = 0; i < 54; i++) {
            System.out.println(sateph_array_t.get(i));
        }
}

    private static double mjd2JUTC1(double mjd) {
        return mjd + TO_JUTC1;
    }
}

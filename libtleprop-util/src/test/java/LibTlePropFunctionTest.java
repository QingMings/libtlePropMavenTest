import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.libtleprop.DELE_TLE;
import org.bytedeco.libtleprop.DTIME_ID;
import org.bytedeco.libtleprop.LibTlePropUtil;
import org.bytedeco.libtleprop.global.libtleprop;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.format.DateTimeFormatter;
import java.util.Date;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_MS_PATTERN;

public class LibTlePropFunctionTest {
    @Before
    public void init() {
        Loader.load(libtleprop.class);
        // 全局变量初始化，全局调用一次
        libtleprop.InitializationConst();
    }


    @Test
    public void test_YMDHMS2MJD() {
        double mjdReesult = 59821.98332202528;
        double mjd = libtleprop.YMDHMS2MJD(2022, 8, 30, 23, 35, 59.023);
        System.out.println("call YMDHMS2MJD with params 2022,8,30,23,35,59.023 . result is " + mjd);
//        Assert.assertEquals(mjd, mjdReesult, 0.1);
        double mjd2Result = 54783.5311154071;
        double mjd2 = libtleprop.YMDHMS2MJD(2008, 11, 13, 12, 44, 48.3712005615234);
        System.out.println("call YMDHMS2MJD with params 2008,11 ,13 ,12, 44, 48.3712005615234 . result is " + mjd2);
//        Assert.assertEquals(mjd2, mjd2Result, 0.1);
    }

    @Test
    public void test_MJD2YMDHMS() {
//        double mjd = 54783.5311443393;
        double mjd =  59943.98332202528;
        try (
                IntPointer yr = new IntPointer(1);
                IntPointer mo = new IntPointer(1);
                IntPointer dy = new IntPointer(1);
                IntPointer hr = new IntPointer(1);
                IntPointer mi = new IntPointer(1);
                DoublePointer se = new DoublePointer(8)
        ) {
            libtleprop.MJD2YMDHMS(mjd, yr, mo, dy, hr, mi, se);
            System.out.println(
                    "call YMDHMS2MJD with params " + mjd +
                            " result is " +
                            "yr=" + yr.get() +
                            ", mo=" + mo.get() +
                            ", dy=" + dy.get() +
                            ", hr=" + hr.get() +
                            ", mi=" + mi.get() +
                            ", se=" + se.get());
        }
    }

    @Test
    public void test_GetTIMEValue_JUTC() {
        double jUTC = 54783.5311154071; // 54783.5311154071 2008 11 13 12 44 48.3712005615234
        try (DTIME_ID dtime_id = libtleprop.GetTIMEValue_JUTC(jUTC)) {

            System.out.println(
                    "dtime_id.MJD = " + dtime_id.MJD() +
                            ",dtime_id.UT1_UTC = " + dtime_id.UT1_UTC() +
                            ",dtime_id.xp = " + dtime_id.xp() +
                            ",dtime_id.yp = " + dtime_id.yp() +
                            ",dtime_id.JUTC = " + dtime_id.JUTC());
            System.out.println("MHPT[9];//PTOD-->ECI");
            for (int i = 0; i < 9; i++) {
                System.out.println("dtime_id.MHPT[" + i + "] = " + dtime_id.MHPT(i));
            }
            System.out.println("MGQT[9];//TEME-->ECI");
            for (int i = 0; i < 9; i++) {
                System.out.println("dtime_id.MGQT[" + i + "] = " + dtime_id.MGQT(i));
            }
            System.out.println("MHG[9], DHG[9];//ECI-->ECF");
            for (int i = 0; i < 9; i++) {
                System.out.println("dtime_id.MHG[" + i + "] = " + dtime_id.MHG(i));
            }
            for (int i = 0; i < 9; i++) {
                System.out.println("dtime_id.DHG[" + i + "] = " + dtime_id.DHG(i));
            }
        }


    }

    @Test
    public void test_GetTT_UT1() {
        double jUTC = 54783.5311154071;
        try (
                DoublePointer jtt = new DoublePointer(8);
                DoublePointer jut1 = new DoublePointer(8);
        ) {
            libtleprop.GetTT_UT1(jUTC, jtt, jut1);
            System.out.println("call GetTT_UT1 with param jUTC = 54783.5311154071 " +
                    " result jtt= " + jtt.get() +
                    " jut1=" + jut1.get()
            );
        }


    }

    @Test
    public void test_dll_CalGMST() {
        double result = 0.5494240412252248;
        double jut1 = 90000;
        double result1 = libtleprop.dll_CalGMST(jut1);
        System.out.println("call dll_CalGMST with params " + jut1 + " , result is " + result1);
        Assert.assertEquals(result, result1, 0);
    }
    @Test
    public void test_InitializeTwoLineElement() {
        String tle1 = "1 25544U 98067A   08264.51782528 -.00002182  00000-0 -11606-4 0  2927";
        String tle2 = "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537";
        try (
                BytePointer line1 = new BytePointer(tle1.getBytes().length);
                BytePointer line2 = new BytePointer(tle2.getBytes().length);
                DELE_TLE dele_tle = new DELE_TLE();
        ) {
            line1.put(tle1.getBytes());
            line2.put(tle2.getBytes());
//            dele_tle.satname().put("ISS (ZARYA)".getBytes());
            libtleprop.InitializeTwoLineElement(line1,line2,dele_tle);
            dele_tle.satname().put("ISS (ZARYA)".getBytes());
            System.out.println("satname="+dele_tle.satname().getString()
                    +", id_nrd="+dele_tle.id_nrd()
                    +", mjd="+dele_tle.mjd()
                    +", ha="+dele_tle.ha()
                    +", hp="+dele_tle.hp()
                    +", incl="+dele_tle.incl()
                    +", perd="+dele_tle.perd()
                    +", bstar="+dele_tle.bstar()
                    +", AM="+dele_tle.AM()
                    +", AM_fit="+dele_tle.AM_fit()
                    +", ndot="+dele_tle.ndot()
                    +", dadt="+dele_tle.dadt()
                    +", tle.line1="+dele_tle.tle().line1().getString()
                    +", tle.line2="+dele_tle.tle().line2().getString()
                    +", lon="+dele_tle.lon());
            System.out.println("LBH[3] = ");
            for (int i = 0; i < 3; i++) {
                System.out.println(dele_tle.LBH().get(i));
            }
        }
    }

    @Test
    public void test_CalSunMoonPosition(){
        double mjd =  59821.98332202528;
        DoublePointer rI_sun = new DoublePointer(3);
        DoublePointer rF_sun = new DoublePointer(3);
        DoublePointer rI_moon = new DoublePointer(3);
        DoublePointer rF_moon = new DoublePointer(3);
        libtleprop.CalSunMoonPosition(mjd,rI_sun,rF_sun,rI_moon,rF_moon);
        System.out.printf("rI_sun (%f,%f,%f)%n", rI_sun.get(0), rI_sun.get(1), rI_sun.get(2));
        System.out.printf("rF_sun (%f,%f,%f)%n", rF_sun.get(0), rF_sun.get(1), rF_sun.get(2));
        System.out.printf("rI_moon (%f,%f,%f)%n", rI_moon.get(0), rI_moon.get(1), rI_moon.get(2));
        System.out.printf("rF_moon (%f,%f,%f)%n", rF_moon.get(0), rF_moon.get(1), rF_moon.get(2));
    }
    @Test
    public void test_CalSatPositionArray_SingleRev(){
        double mjd = libtleprop.YMDHMS2MJD(2022, 8, 30, 23, 35, 59.023);
        String line1 = "1 00005U 58002B   22194.90455603  .00000355  00000-0  44510-3 0  9990";
        String line2 = "2 00005  34.2486  57.8961 1847235 156.6009 213.0824 10.84976421287411";
        BytePointer line1P = new BytePointer(line1.getBytes().length);
        BytePointer line2P = new BytePointer(line2.getBytes().length);
        line1P.put(line1.getBytes());
        line2P.put(line2.getBytes());
        DoublePointer mjd_lower  = new DoublePointer(1);
        DoublePointer mjd_upper = new DoublePointer(1);
        final int  NPOINT = 360;
        DoublePointer rxp = new DoublePointer(NPOINT);
        DoublePointer ryp = new DoublePointer(NPOINT);
        DoublePointer rzp = new DoublePointer(NPOINT);
        DoublePointer Lp = new DoublePointer(NPOINT);
        DoublePointer Bp = new DoublePointer(NPOINT);
        DoublePointer Hp = new DoublePointer(NPOINT);

        libtleprop.CalSatPositionArray_SingleRev(mjd,line1P,line2P,1,NPOINT,mjd_lower,mjd_upper,
                rxp,ryp,rzp,Lp,Bp,Hp);
        System.out.printf("mjd_lower: %f, mjd_upper: %f  %n",mjd_lower.get(),mjd_upper.get());
        Date upperDate = LibTlePropUtil.mjd2date(mjd_upper.get());
        Date lowerDate = LibTlePropUtil.mjd2date(mjd_lower.get());
        System.out.println("UpperDate Format:       "+DateUtil.format(upperDate, DateTimeFormatter.ISO_INSTANT));
        System.out.println("LowerDate Format:       "+DateUtil.format(lowerDate, DateTimeFormatter.ISO_INSTANT));
        long betweenSeconds = DateUtil.between(upperDate, lowerDate, DateUnit.SECOND);
        System.out.println("between seconds (upperDate-lowerDate):      "+betweenSeconds);
        double interval = (double) betweenSeconds/NPOINT;
        System.out.println("betweenSecond/NPOINT =      "+interval);
        for (int i = 0; i < NPOINT; i++) {
            System.out.printf("%f,%f,%f,%f%n",0+interval*i,rxp.get(i),ryp.get(i),rzp.get(i));
        }
    }

    @Test
    public void test_mjd2date(){
//        double mjd =  59821.98332202528;
        double mjd =  59821.983324421104;
        Date date = LibTlePropUtil.mjd2date(mjd);
        System.out.println(DateUtil.format(date,NORM_DATETIME_MS_PATTERN));
    }
    @Test
    public void test_date2mjd(){
        DateTime dateTime =DateUtil.parse("2022-08-30 23:35:59.023");
        double mjd = LibTlePropUtil.date2mjd(dateTime);
        System.out.println(mjd);
    }


}

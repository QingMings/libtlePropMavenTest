package org.bytedeco.libtleprop;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.libtleprop.global.libtleprop;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Main {
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
        Assert.assertEquals(mjd, mjdReesult, 0.1);
        double mjd2Result = 54783.5311154071;
        double mjd2 = libtleprop.YMDHMS2MJD(2008, 11, 13, 12, 44, 48.3712005615234);
        System.out.println("call YMDHMS2MJD with params 2008,11 ,13 ,12, 44, 48.3712005615234 . result is " + mjd2);
        Assert.assertEquals(mjd2, mjd2Result, 0.1);
    }

    @Test
    public void test_MJD2YMDHMS() {
        double mjd = 54783.5311443393;
        try (
                IntPointer yr = new IntPointer(4);
                IntPointer mo = new IntPointer(2);
                IntPointer dy = new IntPointer(2);
                IntPointer hr = new IntPointer(2);
                IntPointer mi = new IntPointer(2);
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
}
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using UnityEngine;
using WPM;

public class J2000Demo : MonoBehaviour
{
    public double lat = 121.545497f;
    public double lon = 25.301831f;
    public double heiglt = 1000;
    public GameObject gaga;
    public GameObject ga2000;
    public GameObject gaEarth;
    public GameObject gablh;
    //地球自转速度 = 7.2921159*180/PI*0.00001
    public GameObject Sun;
    public GameObject earth;
    public GameObject moon;
    public GameObject ax2000;
    //public Matrix4x4 matrix4X4 = Matrix4x4.identity;
    public double[] rI_sun;
    public double[] rF_sun;
    public double[] rI_moon;
    public double[] rF_moon;

    public int year;
    public int month;
    public int data;
    public int hour;
    public int minute;
    public int second;
    public double mjd;
    public double radian;//弧度
    public double angle;//角度
    public Vector3 eulerangle;
    public Matrix4x4 m4;
    public Quaternion newQ;
    DateTime dt;
    DTIME_ID TIME;
    public bool isFixed;

    //-------------------------------
    public string fname_lep = Application.streamingAssetsPath + @"\publicpara\LEAP.txt";
    public string fname_eop = Application.streamingAssetsPath + @"\publicpara\EOP-All.txt";
    string line1 = "1 00005U 58002B   22194.90455603  .00000355  00000-0  44510-3 0  9990";
    string line2 = "2 00005  34.2486  57.8961 1847235 156.6009 213.0824 10.84976421287411";
    double jutc_t, jtt_t, jut1_t, mjd_cur;
    double jutc1, jutc2;
    DTIME_ID TT1, TT2;
    /// <summary>
    /// tle返回结构体
    /// </summary>
    DELE_TLE tle_rec;
    /// <summary>
    /// sgp返回结构体
    /// </summary>
    elsetrec sat_rec;
    DSATEPH sateph;
    double mjds, mjde, step_eph, step_ref;
    public int flag, nstep;
    double[] sateph_array_t1 = new double[54];
    double[] sateph_array_t2 = new double[54];
    double[] sateph_array_t = new double[54];
    int yr, mo, dy, hr, mi;
    double se;
    void Start()
    {

        rI_sun = new double[3];
        rF_sun = new double[3];
        rI_moon = new double[3];
        rF_moon = new double[3];



    }
    /// <summary>
    /// List按固定大小分割
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="ary"></param>
    /// <param name="subSize"></param>
    /// <returns></returns>
    public List<List<T[]>> splitAry<T>(List<T[]> ary, int subSize = 2000)
    {
        int count = ary.Count % subSize == 0 ? ary.Count / subSize : ary.Count / subSize + 1;
        List<List<T[]>> subAryList = new List<List<T[]>>();
        for (int i = 0; i < count; i++)
        {
            int index = i * subSize;
            //List<string[]> subary = ary.Skip(index).Take(subSize).ToList();        
            subAryList.Add(ary.Skip(index).Take(subSize).ToList());
        }
        return subAryList;
    }
    // Update is called once per frame
    void Update()
    {
        j2000ax();
        if (isFixed)
            Fixed();
        else
            j2000();
        //---------计算卫星位置----------------------------
        dt = new DateTime(year, Mathf.Clamp(month, 0, 12), Mathf.Clamp(data, 0, 30), Mathf.Clamp(hour, 0, 23), Mathf.Clamp(minute, 0, 59), Mathf.Clamp(second, 0, 59));
        mjd = Dll_TlePropTools.DateTime2MJD(dt);
        jtt_t = 0;
        jut1_t = 0;
        List<string[]> listTle = new List<string[]>();
        listTle.Add(new string[] { "5", line1, line2 });
        //双行根数初始化
        int size = Marshal.SizeOf(typeof(DELE_TLE));
        IntPtr intptr_tle_rec = Marshal.AllocHGlobal(size);
        Dll_TlePropTools.InitializeTwoLineElement(line1, line2, intptr_tle_rec);
        tle_rec = new DELE_TLE();
        tle_rec = Marshal.PtrToStructure<DELE_TLE>(intptr_tle_rec);
        Dll_TlePropTools.MJD2YMDHMS(tle_rec.mjd, ref yr, ref mo, ref dy, ref hr, ref mi, ref se);
        Marshal.FreeHGlobal(intptr_tle_rec);
        //SGP结构体初始化
        int size_sat_rec = Marshal.SizeOf(typeof(elsetrec));
        IntPtr intptr_sat_rec = Marshal.AllocHGlobal(size_sat_rec);
        Dll_TlePropTools.InitializeTleSetRec(tle_rec, intptr_sat_rec);
        sat_rec = new elsetrec();
        sat_rec = Marshal.PtrToStructure<elsetrec>(intptr_sat_rec);
        Marshal.FreeHGlobal(intptr_sat_rec);

        mjds = mjd;//仿真开始时间
        mjde = mjd + 1.0;//仿真结束时间
        mjd_cur = mjds;//当前仿真时间
        step_eph = 1.0f / 30.0f;//每个点的步长
        step_ref = 60.0f;//高精度算法点的步长

        //初始化参考点1
        jutc1 = mjds + 2400000.5f;
        TT1 = Dll_TlePropTools.GetTIMEValue_JUTC(jutc1);
        flag = Dll_TlePropTools.GetSatEph_Array_SGP4_TT_2(tle_rec.jtt, sat_rec, TT1, sateph_array_t1);//初始化参考点1
        //初始化参考点2
        jutc2 = jutc1 + step_ref / 86400.0f;
        TT2 = Dll_TlePropTools.GetTIMEValue_JUTC(jutc2);
        flag = Dll_TlePropTools.GetSatEph_Array_SGP4_TT_2(tle_rec.jtt, sat_rec, TT2, sateph_array_t2);//初始化参考点2
        //计算位置
        nstep = 0;
        sateph = new DSATEPH();
        jtt_t = new double();
        jut1_t = new double();
        int size_sateph = Marshal.SizeOf(typeof(DSATEPH));
        jutc_t = mjd_cur + 2400000.5f;
        Dll_TlePropTools.GetTT_UT1(jutc_t, ref jtt_t, ref jut1_t);
        flag = Dll_TlePropTools.GetSatEph_Array_SGP4_J2_2(tle_rec.jtt, sat_rec,
        step_ref, sateph_array_t1, sateph_array_t2,
        jutc_t, jtt_t, jut1_t,
        sateph_array_t);
        //J2000
        float x = -(float)sateph_array_t[22] / 10000;//-y
        float y = (float)sateph_array_t[23] / 10000;//z
        float z = (float)sateph_array_t[21] / 10000;//x
        ga2000.transform.position = new Vector3(x, y, z);
        //地固系
        float xx = -(float)sateph_array_t[28] / 10000;//-y
        float yy = (float)sateph_array_t[29] / 10000;//z
        float zz = (float)sateph_array_t[27] / 10000;//x
        Vector3 dg = new Vector3(xx, yy, zz);
        //dg = earth.transform.TransformPoint(dg);
        //dg = earth.transform.InverseTransformPoint(dg);
        //Matrix4x4 ms = earth.transform.localToWorldMatrix;
        //dg = ms * dg;
        dg = earth.transform.rotation * dg;
        
        //dg = earth.transform.TransformPoint(dg);
        //gaEarth.transform.position = dg;
        gaEarth.transform.localPosition = dg;
        //BLH
        double L = sateph_array_t[51];//
        double B = sateph_array_t[52];//
        double H = sateph_array_t[53];
        double rlat = publicState.RadianT0Angle(L);
        double rlon = publicState.RadianT0Angle(B);

        var pos = Conversion.GetSpherePointFromLatLon(rlon, rlat, heiglt);  
        pos = earth.transform.TransformPoint(pos);
        gablh.transform.position = pos;

        //var po = Conversion.GetSpherePointFromLatLon(lat, lon, heiglt);
        //po = earth.transform.TransformPoint(po);
        //gaga.transform.position = po;

    }

    /// <summary>
    /// 惯性系：J2000坐标轴不转。计算太阳、月亮位置和地球转角
    /// </summary>
    void j2000()
    {
        dt = new DateTime(year, Mathf.Clamp(month, 0, 12), Mathf.Clamp(data, 0, 30), Mathf.Clamp(hour, 0, 23), Mathf.Clamp(minute, 0, 59), Mathf.Clamp(second, 0, 59));
        mjd = Dll_TlePropTools.DateTime2MJD(dt);
        Dll_TlePropTools.CalSunMoonPosition(mjd, rI_sun, rF_sun, rI_moon, rF_moon);
        float x = -(float)rI_sun[1] * 63.78137f * 0.01f;//-y
        float y = (float)rI_sun[2] * 63.78137f * 0.01f;//z
        float z = (float)rI_sun[0] * 63.78137f * 0.01f;//x

        float mx = -(float)rI_moon[1] * 63.78137f;//-y
        float my = (float)rI_moon[2] * 63.78137f;//z
        float mz = (float)rI_moon[0] * 63.78137f;//x
        //float x = (float)rI_sun[1] / 1;//x
        //float y = (float)rI_sun[2] / 1;//y
        //float z = -(float)rI_sun[0] / 1;//z
        moon.transform.position = new Vector3(mx, my, mz);
        Sun.transform.position = new Vector3(x, y, z);
        Sun.transform.LookAt(earth.transform);
        //---------------地球弧度-----------------------------
        double jtt = 0;
        double jut1 = 0;
        Dll_TlePropTools.GetTT_UT1(mjd + 2400000.5, ref jtt, ref jut1);
        TIME = Dll_TlePropTools.GetTIMEValue_JUTC(mjd + 2400000.5);

        m4.SetRow(0, new Vector4((float)TIME.MHG[0], (float)TIME.MHG[1], (float)TIME.MHG[2], 0));
        m4.SetRow(1, new Vector4((float)TIME.MHG[3], (float)TIME.MHG[4], (float)TIME.MHG[5], 0));
        m4.SetRow(2, new Vector4((float)TIME.MHG[6], (float)TIME.MHG[7], (float)TIME.MHG[8], 0));
        m4.SetRow(3, new Vector4(0, 0, 0, 1));
        m4=m4.inverse;
        Vector4 vy = m4.GetColumn(1);
        Vector4 vz = m4.GetColumn(2);
        newQ = Quaternion.LookRotation(new Vector3(vz.x, vz.y, vz.z), new Vector3(vy.x, vy.y, vy.z));
        eulerangle = newQ.eulerAngles;


        //radian = Dll_TlePropTools.dll_CalGMST(jut1);//弧度
        //angle = publicState.RadianT0Angle(radian);


        //earth.transform.Rotate(0, -(float)angle * Mathf.Rad2Deg, 0);
        //earth.transform.eulerAngles = new(0, (float)angle, 0);
        earth.transform.eulerAngles = new Vector3(eulerangle.y , -eulerangle.z, eulerangle.x);
    }

    void j2000ax()
    {
        dt = new DateTime(year, Mathf.Clamp(month, 0, 12), Mathf.Clamp(data, 0, 30), Mathf.Clamp(hour, 0, 23), Mathf.Clamp(minute, 0, 59), Mathf.Clamp(second, 0, 59));
        mjd = Dll_TlePropTools.DateTime2MJD(dt);
        Dll_TlePropTools.CalSunMoonPosition(mjd, rI_sun, rF_sun, rI_moon, rF_moon);
        float x = -(float)rI_sun[1] * 63.78137f * 0.01f;//-y
        float y = (float)rI_sun[2] * 63.78137f * 0.01f;//z
        float z = (float)rI_sun[0] * 63.78137f * 0.01f;//x

        float mx = -(float)rI_moon[1] * 63.78137f;//-y
        float my = (float)rI_moon[2] * 63.78137f;//z
        float mz = (float)rI_moon[0] * 63.78137f;//x
        //float x = (float)rI_sun[1] / 1;//x
        //float y = (float)rI_sun[2] / 1;//y
        //float z = -(float)rI_sun[0] / 1;//z
        moon.transform.position = new Vector3(mx, my, mz);
        Sun.transform.position = new Vector3(x, y, z);
        Sun.transform.LookAt(earth.transform);
        //---------------地球弧度-----------------------------
        double jtt = 0;
        double jut1 = 0;
        Dll_TlePropTools.GetTT_UT1(mjd + 2400000.5, ref jtt, ref jut1);
        TIME = Dll_TlePropTools.GetTIMEValue_JUTC(mjd + 2400000.5);

        m4.SetRow(0, new Vector4((float)TIME.MHG[0], (float)TIME.MHG[1], (float)TIME.MHG[2], 0));
        m4.SetRow(1, new Vector4((float)TIME.MHG[3], (float)TIME.MHG[4], (float)TIME.MHG[5], 0));
        m4.SetRow(2, new Vector4((float)TIME.MHG[6], (float)TIME.MHG[7], (float)TIME.MHG[8], 0));
        m4.SetRow(3, new Vector4(0, 0, 0, 1));
        //m4 = m4.inverse;
        Vector4 vy = m4.GetColumn(1);
        Vector4 vz = m4.GetColumn(2);
        newQ = Quaternion.LookRotation(new Vector3(vz.x, vz.y, vz.z), new Vector3(vy.x, vy.y, vy.z));
        eulerangle = newQ.eulerAngles;


        //radian = Dll_TlePropTools.dll_CalGMST(jut1);//弧度
        //angle = publicState.RadianT0Angle(radian);


        //earth.transform.Rotate(0, -(float)angle * Mathf.Rad2Deg, 0);
        //earth.transform.eulerAngles = new(0, (float)angle, 0);
        ax2000.transform.eulerAngles = new Vector3(eulerangle.y, -eulerangle.z, eulerangle.x);
    }
    /// <summary>
    /// 地固系：地球不转。计算太阳、月亮位置和J2000坐标轴转角
    /// </summary>
    private void Fixed()
    {
        dt = new DateTime(year,Mathf.Clamp(month,0,12) , Mathf.Clamp( data,0,30), Mathf.Clamp(hour,0,23), Mathf.Clamp( minute,0,59), Mathf.Clamp(second,0,59));
        mjd = Dll_TlePropTools.DateTime2MJD(dt);
        Dll_TlePropTools.CalSunMoonPosition(mjd, rI_sun, rF_sun, rI_moon, rF_moon);
        float x = -(float)rF_sun[1] * 63.78137f * 0.01f;//-y
        float y = (float)rF_sun[2] * 63.78137f * 0.01f;//z
        float z = (float)rF_sun[0] * 63.78137f * 0.01f;//x

        float mx = -(float)rF_moon[1] * 63.78137f;//-y
        float my = (float)rF_moon[2] * 63.78137f;//z
        float mz = (float)rF_moon[0] * 63.78137f;//x
        //float x = (float)rI_sun[1] / 1;//x
        //float y = (float)rI_sun[2] / 1;//y
        //float z = -(float)rI_sun[0] / 1;//z
        moon.transform.position = new Vector3(mx, my, mz);
        Sun.transform.position = new Vector3(x, y, z);
        Sun.transform.LookAt(earth.transform);
        //---------------地球弧度-----------------------------
        //double jtt = 0;
        //double jut1 = 0;
        //Dll_TlePropTools.GetTT_UT1(mjd + 2400000.5, ref jtt, ref jut1);
        //radian = Dll_TlePropTools.dll_CalGMST(jut1);//弧度
        //angle = publicState.RadianT0Angle(radian);
        //earth.transform.Rotate(0, -(float)angle * Mathf.Rad2Deg, 0);
        //earth.transform.eulerAngles = new(0, (float)angle, 0);
    }

    Vector3 world2Local(Transform _worldPosition)
    {
        Matrix4x4 trans = new Matrix4x4(
          new Vector4(1, 0, 0, 0),
          new Vector4(0, 1, 0, 0),
          new Vector4(0, 0, 1, 0),
          new Vector4(-transform.position.x, -transform.position.y, -transform.position.z, 1));

        Matrix4x4 rotZ = new Matrix4x4(
            new Vector4(Mathf.Cos(-transform.eulerAngles.z * Mathf.PI / 180), Mathf.Sin(-transform.eulerAngles.z * Mathf.PI / 180), 0, 0),
            new Vector4(-Mathf.Sin(-transform.eulerAngles.z * Mathf.PI / 180), Mathf.Cos(-transform.eulerAngles.z * Mathf.PI / 180), 0, 0),
            new Vector4(0, 0, 1, 0),
            new Vector4(0, 0, 0, 1));

        Matrix4x4 rotX = new Matrix4x4(
            new Vector4(1, 0, 0, 0),
            new Vector4(0, Mathf.Cos(-transform.eulerAngles.x * Mathf.PI / 180), Mathf.Sin(-transform.eulerAngles.x * Mathf.PI / 180), 0),
            new Vector4(0, -Mathf.Sin(-transform.eulerAngles.x * Mathf.PI / 180), Mathf.Cos(-transform.eulerAngles.x * Mathf.PI / 180), 0),
            new Vector4(0, 0, 0, 1));

        Matrix4x4 rotY = new Matrix4x4(
               new Vector4(Mathf.Cos(-transform.eulerAngles.y * Mathf.PI / 180), 0, -Mathf.Sin(-transform.eulerAngles.y * Mathf.PI / 180), 0),
               new Vector4(0, 1, 0, 0),
               new Vector4(Mathf.Sin(-transform.eulerAngles.y * Mathf.PI / 180), 0, Mathf.Cos(-transform.eulerAngles.y * Mathf.PI / 180), 0),
               new Vector4(0, 0, 0, 1));

        Matrix4x4 Mview = (new Matrix4x4(
            new Vector4(1, 0, 0, 0),
            new Vector4(0, 1, 0, 0),
            new Vector4(0, 0, 1, 0),
            new Vector4(0, 0, 0, 1)
            )) * rotZ * rotX * rotY * trans;

        Vector4 Pworld = new Vector4(_worldPosition.position.x, _worldPosition.position.y, _worldPosition.position.z, 1);

        Vector3 PLocal = Mview * Pworld;
        //Vector4 Pcamera = transform.worldToLocalMatrix * Pworld;
        Debug.Log(PLocal);

        return PLocal;
    }

    private Vector3 GetRelativePosition(Transform origin, Vector3 position)
    {
        Vector3 distance = position - origin.position;
        Vector3 relativePosition = Vector3.zero;
        relativePosition.x = Vector3.Dot(distance, origin.right.normalized);
        relativePosition.y = Vector3.Dot(distance, origin.up.normalized);
        relativePosition.z = Vector3.Dot(distance, origin.forward.normalized);
        return relativePosition;
    }
}

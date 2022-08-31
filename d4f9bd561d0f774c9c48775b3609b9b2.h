#ifndef DLL_TLEPROP_H
#define DLL_TLEPROP_H

#ifdef _WINDLL
#define DLL_TLEPROP_API extern "C" _declspec(dllexport)
#else
#define DLL_TLEPROP_API extern "C" _declspec(dllimport)
#endif

#define LTH_TLE		70			// TLE根数每行字符数(包含字符串结束符'\0')

////结构体定义: 单点卫星星历数据(ECI)
//struct DSATEPH
//{
//	double jutc;
//	double jtt;
//	double jut1;
//	double MHG[9], DHG[9];//ECI-->ECF
//	double rI[3];//惯性系位置速度矢量[m][m/s]
//	double vI[3];
//	double rF[3];//地固系位置速度矢量[m][m/s]
//	double vF[3];
//	double ele_kpl_osc[6];
//	double ele_kpl_avg[6];
//	double dsigma[6];
//	double LBH[3];//[Lon-rad][lat-rad][m]
//};

//2022.04.29数组后补地固系LBH参数[0..50]->[0..53]
////sateph_array[0..53]
//void SatEphStruct2Array(struct DSATEPH sateph_struct, double* sateph_array)
//{
//	int i;
//
//	sateph_array[0] = sateph_struct.jutc;
//	sateph_array[1] = sateph_struct.jtt;
//	sateph_array[2] = sateph_struct.jut1;
//	for (i = 0; i < 9; i++)
//	{
//		sateph_array[3 + i] = sateph_struct.MHG[i];//3-11
//		sateph_array[12 + i] = sateph_struct.DHG[i];//12-20
//	}
//	for (i = 0; i < 3; i++)
//	{
//		sateph_array[21 + i] = sateph_struct.rI[i];//21-23
//		sateph_array[24 + i] = sateph_struct.vI[i];//24-26
//		sateph_array[27 + i] = sateph_struct.rF[i];//27-29
//		sateph_array[30 + i] = sateph_struct.vF[i];//30-32
//	}
//	for (i = 0; i < 6; i++)
//	{
//		sateph_array[33 + i] = sateph_struct.ele_kpl_osc[i];//33-38
//		sateph_array[39 + i] = sateph_struct.ele_kpl_avg[i];//39-44
//		sateph_array[45 + i] = sateph_struct.dsigma[i];		//45-50
//	}
//	for (i = 0; i < 3; i++)
//		sateph_array[51 + i] = sateph_struct.LBH[i];//51-53
//}

//时间相关参数结构体定义
struct DTIME_ID
{
	double MJD;
	double UT1_UTC;	//sec
	double xp;		//rad
	double yp;		//rad

	double JUTC;
	double JTT;
	double JUT1;
	double MHPT[9];//PTOD-->ECI
	double MGQT[9];//TEME-->ECI
	double MHG[9], DHG[9];//ECI-->ECF
};

//结构体定义
struct twoline
{
	char line1[LTH_TLE];
	char line2[LTH_TLE];
};

struct DELE_TLE
{
	char satname[100];	//需要在外部补充
	int id_nrd;			//目标编号
	double mjd;			//根数历元
	double jtt, jut1;
	double ele_kpl[6];
	double ha, hp, incl;//[km],[km],[deg]
	double perd;		//轨道周期[sec]
	double bstar;
	double AM;			//面质比
	double AM_fit;
	double ndot;
	double dadt;
	struct twoline tle;	//两行根数字符串
	double lon;			//[rad]
	double LBH[3];		//[Lon-rad][lat-rad][m]
};

// -------------------------- structure decarations ----------------------------
struct elsetrec
{
	int satnum;
	int epochyr, epochtynumrev;
	int error;
	char init, method;

	/* Near Earth */
	int    isimp;
	double aycof, con41, cc1, cc4, cc5, d2, d3, d4,
		delmo, eta, argpdot, omgcof, sinmao, t, t2cof, t3cof,
		t4cof, t5cof, x1mth2, x7thm1, mdot, nodedot, xlcof, xmcof,
		nodecf;

	/* Deep Space */
	int    irez;
	double d2201, d2211, d3210, d3222, d4410, d4422, d5220, d5232,
		d5421, d5433, dedt, del1, del2, del3, didt, dmdt,
		dnodt, domdt, e3, ee2, peo, pgho, pho, pinco,
		plo, se2, se3, sgh2, sgh3, sgh4, sh2, sh3,
		si2, si3, sl2, sl3, sl4, gsto, xfact, xgh2,
		xgh3, xgh4, xh2, xh3, xi2, xi3, xl2, xl3,
		xl4, xlamo, zmol, zmos, atime, xli, xni;

	double a, altp, alta, epochdays, jdsatepoch, nddot, ndot,
		bstar, rcse, inclo, nodeo, ecco, argpo, mo,
		no, perd, jttsatepoch;
};

//全局变量初始化,全局调用一次.
DLL_TLEPROP_API void InitializationConst(void);

//跳秒参数初始化(全局调用一次?或者用一个进程单独维护,根据当前日期定时更新.)
DLL_TLEPROP_API void LoadLeapData(char fname_leap[]);

//读入地球自转参数(全局调用一次?或者用一个进程单独维护,根据当前日期定时更新.)
DLL_TLEPROP_API void LoadEOPData(char fname_eop[], int yr, int mo, int dy);

//约简儒略日与年月日时分秒之间的转换: yr/mo/dy/hr/mi/se-->mjd.
DLL_TLEPROP_API double YMDHMS2MJD(int yr, int mo, int dy, int hr, int mi, double se);

//约简儒略日与年月日时分秒之间的转换: mjd-->yr/mo/dy/hr/mi/se.
DLL_TLEPROP_API void MJD2YMDHMS(double mjd, int* yr, int* mo, int* dy, int* hr, int* mi, double* se);

//单点时刻相关参数计算
DLL_TLEPROP_API struct DTIME_ID GetTIMEValue_JUTC(double JUTC);

//利用UTC计算TT和UT1
DLL_TLEPROP_API void GetTT_UT1(double jutc, double* jtt, double* jut1);

// 利用UT1儒略日计算格林尼治平恒星时,GMST的单位为rad
DLL_TLEPROP_API double dll_CalGMST(double JUT1);

//TLE根数初始化
//需要在外部补充目标名称信息
DLL_TLEPROP_API void InitializeTwoLineElement(char line1[], char line2[],
	struct DELE_TLE* tle_rec);

//struct elsetrec初始化
DLL_TLEPROP_API void InitializeTleSetRec(struct DELE_TLE tle_rec, struct elsetrec* sat_rec);

//利用SGP4模型计算目标轨道数据(利用sateph结构体作为中间参数进行转换)
//2022.04.29数组后补地固系LBH参数[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_TT_1(struct DELE_TLE tle_rec, struct elsetrec sat_rec, struct DTIME_ID TT_PROP, double* sateph_array);
//TT_PROP: 预报点时间结构体(TT)
//sateph_array[0..53]

//利用SGP4模型计算目标轨道数据(直接利用sateph数组作为输出)
//2022.04.29数组后补地固系LBH参数[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_TT_2(double jtt_epoch, struct elsetrec sat_rec, struct DTIME_ID TT_PROP, double* sateph_array);
//TT_PROP: 预报点时间结构体(TT)
//sateph_array[0..53]

//为降低逻辑复杂度,参考点预报计算、拟合系数计算、任意点预报均在一个线程内完成.
DLL_TLEPROP_API int GetSatEph_Struct_SGP4_J2(struct DELE_TLE tle_rec, struct elsetrec sat_rec,
	double step_ref,
	struct DSATEPH* sateph_t1, struct DSATEPH* sateph_t2,
	double jutc_t, double jtt_t, double jut1_t, struct DSATEPH* sateph);
	//在定义sateph_t1和sateph_t2结构体时,只需要初始化jutc=0.0即可.
	//step_ref: 参考点外推步长[sec],一般设置为5分钟.
	//sateph_t1/sateph_t2为参考时刻t1/t2(t2>t1,且t2=t1+step_ref)时的目标轨道数据
	//cof_eci为利用t1/t2时的目标轨道数据计算的内插系数
	//mjd_t: 预报点时间

//为降低逻辑复杂度,参考点预报计算、拟合系数计算、任意点预报均在一个线程内完成.
//(利用sateph结构体作为中间参数进行转换)
//2022.04.29数组后补地固系LBH参数[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_J2_1(struct DELE_TLE tle_rec, struct elsetrec sat_rec,
	double step_ref,
	double* sateph_array_t1,
	double* sateph_array_t2,
	double jutc_t, double jtt_t, double jut1_t, double* sateph_array_t);
//sateph_array_t1[0..53]
//sateph_array_t2[0..53]
//sateph_array_t[0..53]
//---- 轨道预报  add by lph 
//为降低逻辑复杂度,参考点预报计算、拟合系数计算、任意点预报均在一个线程内完成.
//2022.04.29数组后补地固系LBH参数[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_J2_2(double jtt_epoch, struct elsetrec sat_rec,
	double step_ref,
	double* sateph_array_t1,
	double* sateph_array_t2,
	double jutc_t, double jtt_t, double jut1_t, double* sateph_array_t);
//sateph_array_t1[0..53]
//sateph_array_t2[0..53]
//sateph_array_t[0..53]

// 计算太阳及月亮的惯性系位置(rI_sun/rI_moon)及地固系位置(rF_sun/rF_moon)
DLL_TLEPROP_API void CalSunMoonPosition(double mjd, double* rI_sun, double* rF_sun, double* rI_moon, double* rF_moon);
//rI_sun[0..2],rF_sun[0..2],rI_moon[0..2],rF_moon[0..2]
// 输出结果单位:地球半径[R_Earth=6378137.0]
// --------- 输出一个轨道周期
//对给定的MJD及双行根数，计算一个轨道周期内[mjd,mjd+T]的NPOINT组位置数组,T为利用双行根数计算的轨道周期.
DLL_TLEPROP_API int CalSatPositionArray_SingleRev(double mjd, char line1[], char line2[],
	int direction,
	int NPOINT, double* mjd_lower, double* mjd_upper,
	double* rx, double* ry, double* rz, double* L, double* B, double* H);
	//flag_fram: 输出数据的坐标系标识,1-惯性系,2-地固系.
	//direction: 时间方向,1-正向,-1-反向.
	//NPOINT: 在一个轨道周期内的采样点数(近圆轨道取360,小椭圆轨道取720,大椭圆轨道取1440,...)
	//rx[0..NPOINT-1],ry[0..NPOINT-1],rz[0..NPOINT-1]为ECI位置分量数组.
	//L[0..NPOINT-1],B[0..NPOINT-1],H[0..NPOINT-1]为地固系星下点经度/纬度/高程数组. 
	//返回值: 0-正常返回,<0-异常返回.

// 利用地固系位置计算相应的星下点位置
// 矢量在不同坐标系之间的转换:空间直角坐标XYZ-->大地坐标LBH
DLL_TLEPROP_API void VEC_Transform_ECFXYZ2ECFLBH_Simple(double rECF[], double* LBH);
//  input: rECF[m]
// output: L[rad],B[rad],H[m],H为相对于地面的高度;

#endif
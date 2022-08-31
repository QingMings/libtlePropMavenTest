#ifndef DLL_TLEPROP_H
#define DLL_TLEPROP_H

#ifdef _WINDLL
#define DLL_TLEPROP_API extern "C" _declspec(dllexport)
#else
#define DLL_TLEPROP_API extern "C" _declspec(dllimport)
#endif

#define LTH_TLE		70			// TLE����ÿ���ַ���(�����ַ���������'\0')

////�ṹ�嶨��: ����������������(ECI)
//struct DSATEPH
//{
//	double jutc;
//	double jtt;
//	double jut1;
//	double MHG[9], DHG[9];//ECI-->ECF
//	double rI[3];//����ϵλ���ٶ�ʸ��[m][m/s]
//	double vI[3];
//	double rF[3];//�ع�ϵλ���ٶ�ʸ��[m][m/s]
//	double vF[3];
//	double ele_kpl_osc[6];
//	double ele_kpl_avg[6];
//	double dsigma[6];
//	double LBH[3];//[Lon-rad][lat-rad][m]
//};

//2022.04.29����󲹵ع�ϵLBH����[0..50]->[0..53]
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

//ʱ����ز����ṹ�嶨��
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

//�ṹ�嶨��
struct twoline
{
	char line1[LTH_TLE];
	char line2[LTH_TLE];
};

struct DELE_TLE
{
	char satname[100];	//��Ҫ���ⲿ����
	int id_nrd;			//Ŀ����
	double mjd;			//������Ԫ
	double jtt, jut1;
	double ele_kpl[6];
	double ha, hp, incl;//[km],[km],[deg]
	double perd;		//�������[sec]
	double bstar;
	double AM;			//���ʱ�
	double AM_fit;
	double ndot;
	double dadt;
	struct twoline tle;	//���и����ַ���
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

//ȫ�ֱ�����ʼ��,ȫ�ֵ���һ��.
DLL_TLEPROP_API void InitializationConst(void);

//���������ʼ��(ȫ�ֵ���һ��?������һ�����̵���ά��,���ݵ�ǰ���ڶ�ʱ����.)
DLL_TLEPROP_API void LoadLeapData(char fname_leap[]);

//���������ת����(ȫ�ֵ���һ��?������һ�����̵���ά��,���ݵ�ǰ���ڶ�ʱ����.)
DLL_TLEPROP_API void LoadEOPData(char fname_eop[], int yr, int mo, int dy);

//Լ����������������ʱ����֮���ת��: yr/mo/dy/hr/mi/se-->mjd.
DLL_TLEPROP_API double YMDHMS2MJD(int yr, int mo, int dy, int hr, int mi, double se);

//Լ����������������ʱ����֮���ת��: mjd-->yr/mo/dy/hr/mi/se.
DLL_TLEPROP_API void MJD2YMDHMS(double mjd, int* yr, int* mo, int* dy, int* hr, int* mi, double* se);

//����ʱ����ز�������
DLL_TLEPROP_API struct DTIME_ID GetTIMEValue_JUTC(double JUTC);

//����UTC����TT��UT1
DLL_TLEPROP_API void GetTT_UT1(double jutc, double* jtt, double* jut1);

// ����UT1�����ռ����������ƽ����ʱ,GMST�ĵ�λΪrad
DLL_TLEPROP_API double dll_CalGMST(double JUT1);

//TLE������ʼ��
//��Ҫ���ⲿ����Ŀ��������Ϣ
DLL_TLEPROP_API void InitializeTwoLineElement(char line1[], char line2[],
	struct DELE_TLE* tle_rec);

//struct elsetrec��ʼ��
DLL_TLEPROP_API void InitializeTleSetRec(struct DELE_TLE tle_rec, struct elsetrec* sat_rec);

//����SGP4ģ�ͼ���Ŀ��������(����sateph�ṹ����Ϊ�м��������ת��)
//2022.04.29����󲹵ع�ϵLBH����[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_TT_1(struct DELE_TLE tle_rec, struct elsetrec sat_rec, struct DTIME_ID TT_PROP, double* sateph_array);
//TT_PROP: Ԥ����ʱ��ṹ��(TT)
//sateph_array[0..53]

//����SGP4ģ�ͼ���Ŀ��������(ֱ������sateph������Ϊ���)
//2022.04.29����󲹵ع�ϵLBH����[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_TT_2(double jtt_epoch, struct elsetrec sat_rec, struct DTIME_ID TT_PROP, double* sateph_array);
//TT_PROP: Ԥ����ʱ��ṹ��(TT)
//sateph_array[0..53]

//Ϊ�����߼����Ӷ�,�ο���Ԥ�����㡢���ϵ�����㡢�����Ԥ������һ���߳������.
DLL_TLEPROP_API int GetSatEph_Struct_SGP4_J2(struct DELE_TLE tle_rec, struct elsetrec sat_rec,
	double step_ref,
	struct DSATEPH* sateph_t1, struct DSATEPH* sateph_t2,
	double jutc_t, double jtt_t, double jut1_t, struct DSATEPH* sateph);
	//�ڶ���sateph_t1��sateph_t2�ṹ��ʱ,ֻ��Ҫ��ʼ��jutc=0.0����.
	//step_ref: �ο������Ʋ���[sec],һ������Ϊ5����.
	//sateph_t1/sateph_t2Ϊ�ο�ʱ��t1/t2(t2>t1,��t2=t1+step_ref)ʱ��Ŀ��������
	//cof_eciΪ����t1/t2ʱ��Ŀ�������ݼ�����ڲ�ϵ��
	//mjd_t: Ԥ����ʱ��

//Ϊ�����߼����Ӷ�,�ο���Ԥ�����㡢���ϵ�����㡢�����Ԥ������һ���߳������.
//(����sateph�ṹ����Ϊ�м��������ת��)
//2022.04.29����󲹵ع�ϵLBH����[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_J2_1(struct DELE_TLE tle_rec, struct elsetrec sat_rec,
	double step_ref,
	double* sateph_array_t1,
	double* sateph_array_t2,
	double jutc_t, double jtt_t, double jut1_t, double* sateph_array_t);
//sateph_array_t1[0..53]
//sateph_array_t2[0..53]
//sateph_array_t[0..53]
//---- ���Ԥ��  add by lph 
//Ϊ�����߼����Ӷ�,�ο���Ԥ�����㡢���ϵ�����㡢�����Ԥ������һ���߳������.
//2022.04.29����󲹵ع�ϵLBH����[0..50]->[0..53]
DLL_TLEPROP_API int GetSatEph_Array_SGP4_J2_2(double jtt_epoch, struct elsetrec sat_rec,
	double step_ref,
	double* sateph_array_t1,
	double* sateph_array_t2,
	double jutc_t, double jtt_t, double jut1_t, double* sateph_array_t);
//sateph_array_t1[0..53]
//sateph_array_t2[0..53]
//sateph_array_t[0..53]

// ����̫���������Ĺ���ϵλ��(rI_sun/rI_moon)���ع�ϵλ��(rF_sun/rF_moon)
DLL_TLEPROP_API void CalSunMoonPosition(double mjd, double* rI_sun, double* rF_sun, double* rI_moon, double* rF_moon);
//rI_sun[0..2],rF_sun[0..2],rI_moon[0..2],rF_moon[0..2]
// ��������λ:����뾶[R_Earth=6378137.0]
// --------- ���һ���������
//�Ը�����MJD��˫�и���������һ�����������[mjd,mjd+T]��NPOINT��λ������,TΪ����˫�и�������Ĺ������.
DLL_TLEPROP_API int CalSatPositionArray_SingleRev(double mjd, char line1[], char line2[],
	int direction,
	int NPOINT, double* mjd_lower, double* mjd_upper,
	double* rx, double* ry, double* rz, double* L, double* B, double* H);
	//flag_fram: ������ݵ�����ϵ��ʶ,1-����ϵ,2-�ع�ϵ.
	//direction: ʱ�䷽��,1-����,-1-����.
	//NPOINT: ��һ����������ڵĲ�������(��Բ���ȡ360,С��Բ���ȡ720,����Բ���ȡ1440,...)
	//rx[0..NPOINT-1],ry[0..NPOINT-1],rz[0..NPOINT-1]ΪECIλ�÷�������.
	//L[0..NPOINT-1],B[0..NPOINT-1],H[0..NPOINT-1]Ϊ�ع�ϵ���µ㾭��/γ��/�߳�����. 
	//����ֵ: 0-��������,<0-�쳣����.

// ���õع�ϵλ�ü�����Ӧ�����µ�λ��
// ʸ���ڲ�ͬ����ϵ֮���ת��:�ռ�ֱ������XYZ-->�������LBH
DLL_TLEPROP_API void VEC_Transform_ECFXYZ2ECFLBH_Simple(double rECF[], double* LBH);
//  input: rECF[m]
// output: L[rad],B[rad],H[m],HΪ����ڵ���ĸ߶�;

#endif
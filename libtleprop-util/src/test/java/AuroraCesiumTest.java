import aurora.cesium.Czml;
import aurora.cesium.CzmlGenerator;
import aurora.cesium.element.Document;
import aurora.cesium.element.Entity;
import aurora.cesium.element.graphics.BillboardGraphics;
import aurora.cesium.element.graphics.LabelGraphics;
import aurora.cesium.element.graphics.PathGraphics;
import aurora.cesium.element.property.*;
import aurora.cesium.utils.Times;
import cesiumlanguagewriter.*;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.libtleprop.LibTlePropUtil;
import org.bytedeco.libtleprop.global.libtleprop;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AuroraCesiumTest {

    @Before
    public void init() {
//        Loader.load(libtleprop.class);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("init libTleprop");
        // 全局变量初始化，全局调用一次
        libtleprop.InitializationConst();
        stopWatch.stop();
        stopWatch.start("init joda time ");
        new DateTime();
        new LocalDateTime();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

    public static final String SavePath = "C:\\javacpp\\libtlePropMavenTest\\libtlePropMavenTest\\libtleprop-util";

    @Test
    public void testBuildSimpleCzml() {
        String id = "testBuildSimpleCzml";
        StopWatch stopWatch = new StopWatch(id);
        stopWatch.start("date2mjd");
        double mjd = libtleprop.YMDHMS2MJD(2022, 8, 30, 23, 35, 59.023);
        stopWatch.stop();
        String line1 = "1 00005U 58002B   22194.90455603  .00000355  00000-0  44510-3 0  9990";
        String line2 = "2 00005  34.2486  57.8961 1847235 156.6009 213.0824 10.84976421287411";
        BytePointer line1P = new BytePointer(line1.getBytes().length);
        BytePointer line2P = new BytePointer(line2.getBytes().length);
        line1P.put(line1.getBytes());
        line2P.put(line2.getBytes());
        DoublePointer mjd_lower = new DoublePointer(1);
        DoublePointer mjd_upper = new DoublePointer(1);
        final int NPOINT = 360;
        DoublePointer rxp = new DoublePointer(NPOINT);
        DoublePointer ryp = new DoublePointer(NPOINT);
        DoublePointer rzp = new DoublePointer(NPOINT);
        DoublePointer Lp = new DoublePointer(NPOINT);
        DoublePointer Bp = new DoublePointer(NPOINT);
        DoublePointer Hp = new DoublePointer(NPOINT);
        stopWatch.start("gen orbit data");
        libtleprop.CalSatPositionArray_SingleRev(mjd, line1P, line2P, 1, NPOINT, mjd_lower, mjd_upper,
                rxp, ryp, rzp, Lp, Bp, Hp);
        stopWatch.stop();
        System.out.printf("mjd_lower: %f, mjd_upper: %f  %n", mjd_lower.get(), mjd_upper.get());
        stopWatch.start("mjd2Date x2");
        Date upperDate = LibTlePropUtil.mjd2date(mjd_upper.get());
        Date lowerDate = LibTlePropUtil.mjd2date(mjd_lower.get());

//        IntPointer yr = new IntPointer(1);
//        IntPointer mo = new IntPointer(1);
//        IntPointer dy = new IntPointer(1);
//        IntPointer hr = new IntPointer(1);
//        IntPointer mi = new IntPointer(1);
//        DoublePointer se = new DoublePointer(1);
//        stopWatch.start("mjd2date1");
//        libtleprop.MJD2YMDHMS(mjd_upper.get(), yr, mo, dy, hr, mi, se);
//        stopWatch.stop();
//        int seconds = (int) se.get();
//        int milliseconds = (int) ((se.get() - seconds) * 1000);
//        stopWatch.start("new joda datetime1");
//        Date upperDate = new org.joda.time.DateTime(yr.get(), mo.get(), dy.get(), hr.get(), mi.get(), seconds, milliseconds).toDate();
//        stopWatch.stop();
//        stopWatch.start("mjd2date2");
//        libtleprop.MJD2YMDHMS(mjd_upper.get(), yr, mo, dy, hr, mi, se);
//        stopWatch.stop();
//        int seconds2 = (int) se.get();
//        int milliseconds2 = (int) ((se.get() - seconds) * 1000);
//        stopWatch.start("new joda datetime2");
//        Date lowerDate = new org.joda.time.DateTime(yr.get(), mo.get(), dy.get(), hr.get(), mi.get(), seconds2, milliseconds2).toDate();
        stopWatch.stop();
        stopWatch.start("pre process orbit data");
        long betweenSeconds = DateUtil.between(upperDate, lowerDate, DateUnit.SECOND);
        double interval = (double) betweenSeconds / NPOINT;
        List<JulianDate> dates = new ArrayList<>();
        List<Cartesian> values = new ArrayList<>();
        for (int i = 0; i < NPOINT; i++) {
            dates.add(Times.trans2JulianDate(lowerDate).addSeconds(interval * i));
            values.add(new Cartesian(rxp.get(i), ryp.get(i), rzp.get(i)));
        }
        stopWatch.stop();
        stopWatch.start("gen czml content ");
        Czml czml = Czml.create();
        czml.setDocument(Document.newBuilder()
                .withId("document")
                .withName("libTleProp")
                .withVersion("1.0")
                .withClock(clockProperty(lowerDate, upperDate, 2.0))
                .build());
        czml.push(entity(positionProperty(dates, values), lowerDate, upperDate));
        String czmlContent = CzmlGenerator.on(true).generate(czml, new StringWriter()).toString();
        stopWatch.stop();
        stopWatch.start("write czml to file ");
        FileUtil.writeUtf8String(czmlContent, Paths.get(SavePath).resolve("00005.czml").toString());
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));

    }

    @Test
    public void testNewJodaTime(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("new joda DateTime");
        DateTime dateTime = DateTime.now();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
    @Test
    public void testNewJodaLocalDateTime(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("new joda LocalDateTime");
        for (int i = 0; i < 50; i++) {
            LocalDateTime localDateTime = new LocalDateTime();
        }
        LocalDateTime localDateTime = new LocalDateTime();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
    @Test
    public void testNewUtilDate(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("new java util date");
        Date date =new Date();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
    @Test
    public void testCalenderInstance(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("get Calender Instance");
        Calendar.getInstance();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

    private ClockProperty clockProperty(Date lowerDate, Date upperDate, Double multiplier) {
        return ClockProperty.newBuilder()
                .withInterval(timeInterval(lowerDate, upperDate))
                .withCurrentTime(Times.trans2JulianDate(lowerDate))
                .withMultiplier(multiplier)
                .withRange(ClockRange.LOOP_STOP)
                .withStep(ClockStep.SYSTEM_CLOCK_MULTIPLIER).build();
    }

    private TimeInterval timeInterval(Date lowerDate, Date upperDate) {
        return new TimeInterval(Times.trans2JulianDate(lowerDate), Times.trans2JulianDate(upperDate));
    }

    private Entity entity(PositionProperty positionProperty, Date lowerDate, Date upperDate) {
        return Entity.newBuilder()
                .withId("00005")
                .withName("00005")
                .withAvailability(AvailabilityProperty.from(timeInterval(lowerDate, upperDate)))
                .withLabel(labelGraphics())
                .withBillboard(billboardGraphics())
                .withPosition(positionProperty)
                .withPath(pathGraphics())
                .build();
    }

    private BillboardGraphics billboardGraphics() {
        return BillboardGraphics.newBuilder()
                .withScale(DoubleProperty.from(1.5))
                .withImage(
                        UriProperty.newBuilder()
                                .withValue(CesiumResource.fromStream(FileUtil.getInputStream(this.getClass().getResource("/").getPath().concat("icon.png")), CesiumImageFormat.PNG))
                                .build()
                ).build();

    }

    private PathGraphics pathGraphics() {
        return PathGraphics.newBuilder()
                .withShow(BooleanProperty.from(true))
                .withMaterial(polylineMaterialProperty())
                .withWidth(DoubleProperty.from(2.0)).build();
    }

    private PolylineMaterialProperty polylineMaterialProperty() {
        return PolylineMaterialProperty.newBuilder()
                .withSolidColorMaterial(solidColorMaterialProperty()).build();
    }

    private SolidColorMaterialProperty solidColorMaterialProperty() {
        return SolidColorMaterialProperty.from(ColorProperty.fromRgba(Color.GREEN));
    }

    private LabelGraphics labelGraphics() {
        return LabelGraphics.newBuilder()
                .withText(StringProperty.from("00005"))
                .build();
    }

    private PositionProperty positionProperty(List<JulianDate> dates, List<Cartesian> values) {
        return PositionProperty.newBuilder()
                .withCartesian(CartesianProperty.newBuilder().withValues(dates, values).build())
                .withInterpolations(Interpolations.newBuilder()
                        .withInterpolationDegree(5)
                        .withInterpolationAlgorithm(CesiumInterpolationAlgorithm.LAGRANGE)
                        .build()).build();
    }
}

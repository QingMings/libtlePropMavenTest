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
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.libtleprop.LibTlePropUtil;
import org.bytedeco.libtleprop.global.libtleprop;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class ReadTleFile {

    class Tle{
        @Override
        public String toString() {
            return "Tle{" +
                    "name='" + name + '\'' +
                    ", line1='" + line1 + '\'' +
                    ", line2='" + line2 + '\'' +
                    '}';
        }

        public Tle() {
        }

        public Tle(String name, String line1, String line2) {
            this.name = name;
            this.line1 = line1;
            this.line2 = line2;
        }

         String name;
         String line1;
         String line2;

        public String getName() {
            return name;
        }

        public String getLine1() {
            return line1;
        }

        public String getLine2() {
            return line2;
        }
    }
    @Before
    public void init(){
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
    public List<Tle> tleList = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Date upperDate = new Date();
    Date lowerDate = new Date();
    public static final String SavePath = "C:\\javacpp\\libtlePropMavenTest\\libtlePropMavenTest\\libtleprop-util";

    @Test
    public void readTleFile() {

        String id = "tleFile read";
        StopWatch stopWatch = new StopWatch();
        String filePath = "C:\\javacpp\\libtlePropMavenTest\\libtlePropMavenTest\\3le-20220715.txt";
        stopWatch.start("read lines");
        List<String> tleLines = FileUtil.readUtf8Lines(filePath);
        stopWatch.stop();
        stopWatch.start("convert to Tle Objects");
        System.out.println("fileLine count = "+tleLines.size());
        for (int i = 0; i < tleLines.size(); i+=3) {
            tleList.add(new Tle(StrUtil.replace(tleLines.get(i),"0 ",""), tleLines.get(i+1),tleLines.get(i+2)));
        }
        stopWatch.stop();
        System.out.println("tle count = "+ tleList.size());
        stopWatch.start("defined start date date2mjd");
        double mjd = libtleprop.YMDHMS2MJD(2022, 9, 20, 23, 36, 59.023);
        stopWatch.stop();
        stopWatch.start("gen all orbit data,size= "+tleList.size());

        List<List<Tle>> splitTleData = ListUtil.split(tleList, 1000);
        List<OrbitsTask> orbitsTasks = new ArrayList<>();
        for (int i = 0; i < splitTleData.size(); i++) {
            List<Tle> data = splitTleData.get(i);
            orbitsTasks.add(new OrbitsTask(data,mjd,"task_"+i));
        }


        List<Entity> orbits = new ArrayList<>();
        System.out.println("taskSize="+orbitsTasks.size());
        try {
            List<Future<OrbitsResult>> futures = executorService.invokeAll(orbitsTasks);
            for (Future<OrbitsResult> future : futures) {
                orbits.addAll(future.get().getEntities());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        orbits.stream().parallel().forEach(czml::push);
        String czmlContent = CzmlGenerator.on(true).generate(czml, new StringWriter()).toString();
        stopWatch.stop();
        stopWatch.start("write czml to file ");
        FileUtil.writeUtf8String(czmlContent, Paths.get(SavePath).resolve("allOrbit.czml").toString());
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));

    }
    class OrbitsResult {
        public OrbitsResult() {
        }

        public OrbitsResult(List<Entity> orbits) {
            this.entities = orbits;
        }

        List<Entity> entities;

        public List<Entity> getEntities() {
            return entities;
        }

        public void setEntities(List<Entity> entities) {
            this.entities = entities;
        }
    }

    class OrbitsTask implements Callable<OrbitsResult>{
        String taskName;
        List<Tle> tleData;
        double mjd;

        public OrbitsTask(List<Tle> tleData, double mjd,String taskName) {
            this.tleData = tleData;
            this.mjd = mjd;
            this.taskName = taskName;
        }

        @Override
        public OrbitsResult call() throws Exception {
//            StopWatch stopWatch = new StopWatch(taskName);
//            stopWatch.start();
            List<Entity> entityList = new ArrayList<>();
            for (int i = 0; i < tleData.size(); i++) {
                Tle tle = tleData.get(i);
                BytePointer line1P = new BytePointer(tle.getLine1().getBytes().length);
                BytePointer line2P = new BytePointer(tle.getLine2().getBytes().length);
                line1P.put(tle.getLine1().getBytes());
                line2P.put(tle.getLine2().getBytes());
                DoublePointer mjd_lower = new DoublePointer(1);
                DoublePointer mjd_upper = new DoublePointer(1);
                final int NPOINT = 360;
                DoublePointer rxp = new DoublePointer(NPOINT);
                DoublePointer ryp = new DoublePointer(NPOINT);
                DoublePointer rzp = new DoublePointer(NPOINT);
                DoublePointer Lp = new DoublePointer(NPOINT);
                DoublePointer Bp = new DoublePointer(NPOINT);
                DoublePointer Hp = new DoublePointer(NPOINT);
                libtleprop.CalSatPositionArray_SingleRev(mjd, line1P, line2P, 1, NPOINT, mjd_lower, mjd_upper,
                        rxp, ryp, rzp, Lp, Bp, Hp);
//                System.out.println();
                try {
                    Date m_upperDate = LibTlePropUtil.mjd2date(mjd_upper.get());
                    Date m_lowerDate = LibTlePropUtil.mjd2date(mjd_lower.get());
                    if (DateUtil.date(m_lowerDate).getField(DateField.YEAR)<2000){
                        System.out.println(tle.getName());
                        System.out.println(DateUtil.formatDateTime(m_lowerDate));
                        System.out.println(DateUtil.formatDateTime(m_upperDate));
                    }

                    if (m_lowerDate.before(lowerDate)){
                        lowerDate = m_lowerDate;
                    }
                    if (m_upperDate.after(upperDate)){
                        upperDate = m_upperDate;
                    }
                    long betweenSeconds = DateUtil.between(m_upperDate, m_lowerDate, DateUnit.SECOND);
                    double interval = (double) betweenSeconds / NPOINT;
                    List<JulianDate> dates = new ArrayList<>();
                    List<Cartesian> values = new ArrayList<>();
                    for (int j = 0; j < NPOINT; j++) {
                        dates.add(Times.trans2JulianDate(m_lowerDate).addSeconds(interval * j));
                        values.add(new Cartesian(rxp.get(j), ryp.get(j), rzp.get(j)));
                    }
                    Entity entity = entity(positionProperty(dates, values), m_lowerDate, m_upperDate);
                    entityList.add(entity);
                }catch (Exception e){
                    System.out.println("生成轨道失败"+tle.toString());
                }


            }
//            stopWatch.stop();
//            System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
            return new OrbitsResult(entityList);
        }
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

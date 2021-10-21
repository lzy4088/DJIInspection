package com.dji.sdkdemo;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import androidx.recyclerview.widget.RecyclerView;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.gimbal.Attitude;
import dji.common.gimbal.GimbalState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecuteState;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class waypoint_activity extends AppCompatActivity implements View.OnClickListener {

    // UI视图
    boolean Settable=false;
    boolean Run_judgment=false;
    private TextView mTvStatusWaypointMission, mTvStatusWaypointMissionExecute;
    private Button mBtnLoadWaypointMission, mBtnUploadWaypointMission, mBtnStartWaypointMission,mBtnclearwaypoint;
    private Button mBtnPauseWaypointMission, mBtnResumeWaypointMission, mBtnStopWaypointMission,mBtnmakeWaypointMission,mBtnnewwaypointmission,mBtnrecordwaypoint;
    private Switch mSwitchPhotograph;
    private FlightController mFlightController;
    private boolean mIsCameraRecording, mIsCameraStoringPhoto;
    private SettingsDefinitions.CameraMode mCameraMode;
    Attitude mGimbalangle;
    float Gimbal_PITCH;
    int Photograph_switch=0;
    String str1;
    byte[] buffer;
    File file;
    ArrayList lon_list = new ArrayList();
    ArrayList lat_list = new ArrayList();
    ArrayList height_list=new ArrayList();
    ArrayList mis_turnMode=new ArrayList();//
    ArrayList mis_heading=new ArrayList();//
    ArrayList mis_gimbalPitch=new ArrayList();//
    ArrayList ShootPhoto=new ArrayList();

    private List<MediaFile> mMediaFiles;
    // 文件下载对话框
    private ProgressDialog mPgsDlgDownload;

    Double[][] LatLng = new Double[0][0];

    private File dir = Environment.getExternalStorageDirectory();
    //private File dataFile = new File(dir,"3D_map_flight.kml");
    private File dataFile1 = new File(dir,"3D_map_flight.kml");
    private WaypointMissionOperatorListener mWaypointMissionOperatorListener;
    private Document document;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);
        // 初始化UI界面
        initUI();
        // 初始化航点任务操作器
        initWaypointMissionOperator();
        waypointlistener();
        //analysis_kml();
    }

    // 初始化UI界面
    private void initUI() {
        mTvStatusWaypointMission = findViewById(R.id.tv_status_waypoint_mission);
        mTvStatusWaypointMissionExecute = findViewById(R.id.tv_status_waypoint_mission_execute);

        mBtnLoadWaypointMission = findViewById(R.id.btn_load_waypoint_mission); // 【加载任务】按钮
        mBtnUploadWaypointMission = findViewById(R.id.btn_upload_waypoint_mission); // 【上传任务】按钮
        mBtnStartWaypointMission = findViewById(R.id.btn_start_waypoint_mission); // 【开始任务】按钮
        mBtnPauseWaypointMission = findViewById(R.id.btn_pause_waypoint_mission); // 【暂停任务】按钮
        mBtnResumeWaypointMission = findViewById(R.id.btn_resume_waypoint_mission); // 【继续任务】按钮
        mBtnStopWaypointMission = findViewById(R.id.btn_stop_waypoint_mission); // 【停止任务】按钮
        mBtnmakeWaypointMission = findViewById(R.id.btn_make_waypoint_mission); // 【手动飞行以获取一组航点任务】按钮
        mBtnnewwaypointmission = findViewById(R.id.btn_new_waypoint_mission); // 【加载任务】按钮
        mBtnrecordwaypoint = findViewById(R.id.btn_record_waypoint); // 【加载任务】按钮
        mBtnclearwaypoint = findViewById(R.id.btn_clear_waypoint); // 【加载任务】按钮
        mSwitchPhotograph = findViewById(R.id.switch_photograph);

        mBtnLoadWaypointMission.setOnClickListener(this);
        mBtnUploadWaypointMission.setOnClickListener(this);
        mBtnStartWaypointMission.setOnClickListener(this);
        mBtnPauseWaypointMission.setOnClickListener(this);
        mBtnResumeWaypointMission.setOnClickListener(this);
        mBtnStopWaypointMission.setOnClickListener(this);
        mBtnmakeWaypointMission.setOnClickListener(this);
        mBtnnewwaypointmission.setOnClickListener(this);
        mBtnrecordwaypoint.setOnClickListener(this);
        mBtnclearwaypoint.setOnClickListener(this);
        mSwitchPhotograph.setOnClickListener(this);
    }

    // 初始化航点任务操作器
    private void initWaypointMissionOperator() {

        mWaypointMissionOperatorListener = new WaypointMissionOperatorListener() {
            @Override
            public void onDownloadUpdate(WaypointMissionDownloadEvent event) {
            }

            @Override
            public void onUploadUpdate(WaypointMissionUploadEvent event) {
            }

            @Override
            public void onExecutionUpdate(WaypointMissionExecutionEvent event) {

                // 当前航点任务状态
                WaypointMissionState state = event.getCurrentState();
                // 当前航点任务执行状态
                String executeState = waypointMissionExecuteStateToString(event.getProgress().executeState);
                // 目标航点序号
                int index = event.getProgress().targetWaypointIndex;
                // 总航点数
                int count = event.getProgress().totalWaypointCount;
                // 是否已经到达航点
                final String reached = event.getProgress().isWaypointReached ? "已到达" : "未到达";
                final String strState = String.format("航点:%d(%s) 总航点数:%d 状态:%s", index + 1, reached, count, executeState);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // mTvStatusWaypointMission.setText("航点任务状态:" + waypointMissionStateToString(state));
                        mTvStatusWaypointMission.setText("航点任务状态:" + state.getName());
                        mTvStatusWaypointMissionExecute.setText(strState);
                    }
                });
            }

            @Override
            public void onExecutionStart() {
                showToast("开始执行任务!");
            }

            @Override
            public void onExecutionFinish(DJIError djiError) {
                if (djiError != null) {
                    showToast("航点任务结束错误:" + djiError.getDescription());
                    return;
                }
                // 更新界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvStatusWaypointMission.setText("航点任务状态:已经结束");
                        mTvStatusWaypointMissionExecute.setText("已结束");
                    }
                });
            }
        };

        // 设置航点任务操作器监听器
        getWaypointMissionOperator().addListener(mWaypointMissionOperatorListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消设置航点任务操作器的监听器
        getWaypointMissionOperator().removeListener(mWaypointMissionOperatorListener);
    }

    // region UI事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_load_waypoint_mission: loadWaypointMission(); break;
            case R.id.btn_upload_waypoint_mission: uploadWaypointMission(); break;
            case R.id.btn_start_waypoint_mission: startWaypointMission(); break;
            case R.id.btn_pause_waypoint_mission: pauseWaypointMission(); break;
            case R.id.btn_resume_waypoint_mission: resumeWaypointMission(); break;
            case R.id.btn_stop_waypoint_mission: stopWaypointMission(); break;
            case R.id.btn_make_waypoint_mission: makeWaypointMission(); break;
            case R.id.btn_new_waypoint_mission: newwaypointmission(); break;
            case R.id.btn_record_waypoint: recordwaypoint(); break;
            case R.id.btn_clear_waypoint: clearwaypoint(); break;
            case R.id.switch_photograph: Photograph_switch();break;
        }
    }

    // 加载航点任务
    private void loadWaypointMission() {
        Double waypoint_lat,waypoint_lon = null;
        Float waypoint_height,waypoint_heading;
        int waypoint_gimbalPitch;
        // 创建航点动作
        // 悬停2秒，第二个参数的单位为毫秒
        WaypointAction actionStay = new WaypointAction(WaypointActionType.STAY, 2000);
        // 拍摄照片，第二个参数无效
        WaypointAction actionTakePhoto = new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0);
        // 开始录像，第二个参数无效
        WaypointAction actionStartRecord = new WaypointAction(WaypointActionType.START_RECORD, 0);
        // 停止录像，第二个参数无效
        WaypointAction actionStopRecord = new WaypointAction(WaypointActionType.STOP_RECORD, 0);
        // 飞机航向转向正北，第二个参数为航向，单位为°
        WaypointAction actionAircraftToNorth = new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, 0);
        // 云台竖直朝下，第二个参数单位为°
        WaypointAction actionGimbalStraightDown = new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90);
        // 云台向下45度角，第二个参数单位为°
        WaypointAction actionGimbal45degree = new WaypointAction(WaypointActionType.GIMBAL_PITCH, -45);
        // 云台水平向前，第二个参数单位为°
        WaypointAction actionGimbalHorizontal = new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0);


        //Data_analysis();
        WaypointMission.Builder builder = new WaypointMission.Builder();
        analysis_kml();
        //showToast("正在运行" );

        if(lon_list.size()<1){
            showToast("无点输入");

        }else if(lon_list.size()<2){
            showToast("点太少不能构建航点任务");

        }
        else{
            for(int i=0;i<lon_list.size();i++){


                waypoint_lon = Double.valueOf(lon_list.get(i).toString());
                waypoint_lat = Double.valueOf(lat_list.get(i).toString());
                waypoint_height=Float.valueOf(height_list.get(i).toString());

                //waypoint_gimbalPitch= Integer.valueOf(mis_gimbalPitch.get(i).toString());
                //Waypoint waypoint = new Waypoint(Coordinate_point.get(i).toString());
                int course = (int)Math.round(Float.valueOf(mis_heading.get(i).toString()));
                Waypoint waypoint = new Waypoint(waypoint_lat,waypoint_lon,waypoint_height);
                if(Integer.valueOf(mis_gimbalPitch.get(i).toString())!=null ){
                    waypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH,Integer.valueOf(mis_gimbalPitch.get(i).toString())));
                }
                waypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, course));
                waypoint.addAction(actionStay);

                if (Integer.valueOf(ShootPhoto.get(i).toString()).equals(1) ) {
                    waypoint.addAction( new WaypointAction( WaypointActionType.START_TAKE_PHOTO, 0 ) );
                }

                builder.addWaypoint(waypoint);
            }

            builder.autoFlightSpeed(5)
                    .maxFlightSpeed(5)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
                    .finishedAction(WaypointMissionFinishedAction.GO_FIRST_WAYPOINT)
                    .headingMode(WaypointMissionHeadingMode.AUTO);

            // 创建航点任务
            WaypointMission mission = builder.build();

            // 航点任务检查
            DJIError error =  mission.checkParameters();
            if (error != null) {
                showToast(error.toString());
                return;
            }

            // 航点任务操作器加载航点任务
            error = getWaypointMissionOperator().loadMission(mission);
            if (error == null) {
                showToast("加载航点任务成功!");
            } else {
                showToast("加载航点任务失败:" + error.getDescription());
            }


        }
    }
//        String str1 =  "113.9585";//lon_list.get(0).toString();
//        waypoint_lon = Double.valueOf(str1);
        // 创建航点 1
//        Waypoint waypoint1 = new Waypoint(waypoint_lat, waypoint_lon, 20);//经纬高
//        //Waypoint waypoint1 = new Waypoint(22.5425, 113.9585, 20);//经纬高
//        waypoint1.addAction(actionGimbalStraightDown);
//        waypoint1.addAction(actionTakePhoto);
//        waypoint1.addAction(actionStay);
//        waypoint1.addAction(actionGimbalHorizontal);
//        waypoint1.addAction(actionAircraftToNorth);
//        waypoint1.addAction(actionTakePhoto);
//        // 创建航点 2
//        Waypoint waypoint2 = new Waypoint(22.5435, 113.9585, 40);
//        waypoint2.addAction(actionStay);
//        waypoint2.addAction(actionStartRecord);
//        // 创建航点 3
//        Waypoint waypoint3 = new Waypoint(22.5425, 113.9540, 40);
//        waypoint3.addAction(actionStopRecord);
//        waypoint3.addAction(actionGimbal45degree);
//        waypoint3.shootPhotoTimeInterval = 2;
//        // 创建航点 4
//        Waypoint waypoint4 = new Waypoint(22.5425, 113.9560, 30);

//        builder.addWaypoint(waypoint1);
//        builder.addWaypoint(waypoint2);
//        builder.addWaypoint(waypoint3);
//        builder.addWaypoint(waypoint4);
        //Waypoint waypoint1 = new Waypoint(22.5425, 113.9585, 20);//LatLng[i][j]

        // 构建航点任务
        //ReadSysFile("Waypoint_mission.txt" );



        //Waypoint waypoint = new Waypoint(waypoint_lon, waypoint_lat, 40);
        //builder.addWaypoint(waypoint);
        //waypoint.addAction(actionStay);
//        waypoint_lon = (double) (lon_list.get(1));
//        waypoint_lat = (double) (lat_list.get(1));
//        //Waypoint waypoint = new Waypoint(waypoint_lon, waypoint_lat, 40);
//        builder.addWaypoint(new Waypoint(waypoint_lon, waypoint_lat, 40));

        // 航点任务的飞行速度为5m/s，常规飞行路径，自动航向，结束后返航。
//        builder.autoFlightSpeed(5)
//                .maxFlightSpeed(5)
//                .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
//                .finishedAction(WaypointMissionFinishedAction.GO_HOME)
//                .headingMode(WaypointMissionHeadingMode.AUTO);
//
//        // 创建航点任务
//        WaypointMission mission = builder.build();
//
//        // 航点任务检查
//        DJIError error =  mission.checkParameters();
//        if (error != null) {
//            showToast(error.toString());
//            return;
//        }
//
//        // 航点任务操作器加载航点任务
//        error = getWaypointMissionOperator().loadMission(mission);
//        if (error == null) {
//            showToast("加载航点任务成功!");
//        } else {
//            showToast("加载航点任务失败:" + error.getDescription());
//        }

    // 上传航点任务
    private void uploadWaypointMission() {
        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    showToast("上传航点模式成功!");
                } else {
                    showToast("上传航点模式失败:" + error.getDescription() + ". 正在重试上传...");
                    getWaypointMissionOperator().retryUploadMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (error == null) {
                                showToast("上传航点模式成功!");
                            } else {
                                showToast("上传航点模式失败:" + error.getDescription());
                            }
                        }
                    });
                }
            }
        });
    }

    // 开始航点任务
    private void startWaypointMission() {
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                showToast("开始任务: " + (djiError == null ? "成功!" : djiError.getDescription()));
            }
        });
    }

    // 暂停航点任务
    private void pauseWaypointMission() {

        getWaypointMissionOperator().pauseMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                showToast("暂停任务: " + (djiError == null ? "成功!" : djiError.getDescription()));
            }
        });
    }

    // 继续航点任务
    private void resumeWaypointMission() {
        getWaypointMissionOperator().resumeMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                showToast("继续任务: " + (djiError == null ? "成功!" : djiError.getDescription()));
            }
        });

    }

    // 停止航点任务
    private void stopWaypointMission() {

        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                showToast("停止任务: " + (djiError == null ? "成功!" : djiError.getDescription()));
            }
        });
    }

    // 测试按钮
    private void makeWaypointMission() {//Gimbal=new GimbalBalanceDetectionState();

        Button btnmakewaypointmission = (Button) findViewById(R.id.btn_make_waypoint_mission);
        btnmakewaypointmission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                //////////////////////////////////////////////////////////////////////////////////////////////////////////
            }
        });
    }

    //新建航点任务 防误触
    private void newwaypointmission() {
        if(Settable==false) {
            Settable = true;
            showToast("开始设置航点");
        }
        else {
            Settable = false;
            showToast("结束设置航点");
        }

    }

    //记录当前航点
    private void recordwaypoint() {
        if(Settable==true){
            Run_judgment=true;//开始记录
            if(Photograph_switch==1){
                Camera camera = getCamera();
                setCameraMode(camera, SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                takePicture();
            }
        }
        else{
            String Aircraft_information = String.format("请新建航点任务" );
            showToast(Aircraft_information);
        }
    }

    //拍照选择建
    private void Photograph_switch(){

        Switch switchphotograph= (Switch) findViewById(R.id.switch_photograph);
        switchphotograph.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    showToast(String.format("拍照模式" ));;
                    Photograph_switch=1;
                }else {
                    //showToast(String.format("结束" ));;
                    Photograph_switch=0;
                }

            }

        });

    }

    //新建or清空航点
    private void clearwaypoint() {
        if(Settable==true) {
            lon_list.clear();
            lat_list.clear();
            height_list.clear();
            mis_turnMode.clear();
            mis_heading.clear();
            mis_gimbalPitch.clear();
            createkml();
            //Write_data("");
            String Aircraft_information = String.format("已清除");
            showToast(Aircraft_information);
        }
        else{
            String Aircraft_information = String.format("请新建航点任务" );
            showToast(Aircraft_information);
        }
    }

    //写数据到txt文件
    public void Write_data(String data) {


        try {
            FileOutputStream fileout = openFileOutput("Waypoint_mission.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(data);
            fileout.flush();
            outputWriter.flush();
            fileout.close();
            outputWriter.close();

            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //外部存储读txt航点文件
    private String ReadSysFile_external() {
        try {
            FileInputStream fis = new FileInputStream(dataFile1);
            //读取本地小文件
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            fis.close();
            String str = new String(bytes,"utf-8");

            return str;

        }catch (Exception e) {
            e.printStackTrace();
        }
        return "error1";
    }

    //内部存储读txt航点文件
    public String ReadSysFile(String filename) {
        try {
            FileInputStream fis = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            char[] input = new char[fis.available()];  //available()用于获取filename内容的长度,但是对中文有问题，建议使用BufferReader
            isr.read(input);  //读取并存储到input中
            isr.close();
            fis.close();//读取完成后关闭
            String str = new String(input);
            //System.out.println(str);
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    //读txt航点字符串数据解析
    public void Data_analysis(){
        String str = ReadSysFile("Waypoint_mission.txt");
        String Aircraft_information = String.format("当前航点列表\n"+str);
        showToast(Aircraft_information);
        String[] strarray=str.split("\n");

        for(int i=0;i<strarray.length; i++) {
            String[] strarray1=strarray[i].split(" ");
            lon_list.add(strarray1[1]);
            lat_list.add(strarray1[0]);
        }

    }

    //kml读 解析函数  传入kml字符串，获取到标签内的内容
    public void analysis_kml(){
        //int j=0;
        lon_list.clear();
        mis_turnMode.clear();
        mis_gimbalPitch.clear();
        mis_heading.clear();
        lat_list.clear();
        height_list.clear();
        ShootPhoto.clear();
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(dataFile1);
            Element element1= document.getDocumentElement();
            NodeList nodeList  = element1.getElementsByTagName("Placemark");

            for (int i = 0; i < nodeList .getLength(); i++) {

                Element Placemark = (Element) nodeList .item(i);

                if(Placemark.getElementsByTagName("mis:gimbalPitch").item(0)!=null) {
                    mis_gimbalPitch.add(Placemark.getElementsByTagName("mis:gimbalPitch").item(0).getTextContent());
                }
                else{mis_gimbalPitch.add(1);}
                //showToast( String.valueOf( String.valueOf(Placemark.getElementsByTagName( "mis:actions" ).item( 0 ).getTextContent()).equals(  "ShootPhoto") ) );
                if(Placemark.getElementsByTagName("mis:actions").item(0)!=null) {

                    if (String.valueOf(Placemark.getElementsByTagName( "mis:actions" ).item( 0 ).getTextContent()).equals(  "ShootPhoto") ) {

                        ShootPhoto.add( 1 );
                    } else {

                        ShootPhoto.add( 0 );
                    }
                }else {
                    ShootPhoto.add( 0 );
                }


                mis_turnMode.add(Placemark.getElementsByTagName("mis:turnMode").item(0).getTextContent());
                //mis_gimbalPitch.add(Placemark.getElementsByTagName("mis:gimbalPitch").item(0).getTextContent());
                mis_heading.add(Placemark.getElementsByTagName("mis:heading").item(0).getTextContent());
                lon_list.add(Placemark.getElementsByTagName("coordinates").item(0).getTextContent().split(",")[0]);
                lat_list.add(Placemark.getElementsByTagName("coordinates").item(0).getTextContent().split(",")[1]);
                height_list.add(Placemark.getElementsByTagName("coordinates").item(0).getTextContent().split(",")[2]);


            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    //
    private static String getType(Object a) {
        return a.getClass().toString();
    }

    //kml添加点
    public void add_waypoint_tokml(String name, double longitude,double latitude,float altitude,int yaw,int gimbalPitch,int ShootPhoto){
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(dataFile1);

            //Element element = document.getDocumentElement();
            Element root= document.getDocumentElement();
            Element Placemark = document.createElement("Placemark");

            Element name_point=document.createElement("name");
            name_point.setTextContent(name);
            Placemark.appendChild(name_point);

            Element description=document.createElement("description");
            description.setTextContent("Waypoint");
            Placemark.appendChild(description);

            Element visibility=document.createElement("visibility");
            visibility.setTextContent("1");
            Placemark.appendChild(visibility);

            Element mis_heading = document.createElement("mis:heading");
            mis_heading.setTextContent(String.valueOf(yaw));
            Placemark.appendChild(mis_heading);

            Element mis_turnMode = document.createElement("mis:turnMode");
            mis_turnMode.setTextContent("clockwise");
            Placemark.appendChild(mis_turnMode);

            Element mis_gimbalPitch = document.createElement("mis:gimbalPitch");
            mis_gimbalPitch.setTextContent(String.valueOf(gimbalPitch));
            Placemark.appendChild(mis_gimbalPitch);

            if(ShootPhoto==1){
                Element actions_ShootPhoto = document.createElement("mis:actions");
                actions_ShootPhoto.setTextContent("ShootPhoto");
                Placemark.appendChild(actions_ShootPhoto);
            }

            Element Point = document.createElement("Point");
            Element altitudeMode = document.createElement("altitudeMode");
            altitudeMode.setTextContent("relativeToGround");
            Point.appendChild(altitudeMode);
            Element coordinates = document.createElement("coordinates");
            coordinates.setTextContent(String.valueOf(longitude) +","+ String.valueOf(latitude) +","+ String.valueOf(altitude));
            Point.appendChild(coordinates);
            Placemark.appendChild(Point);

            root.appendChild(Placemark);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //DOMSource source = new DOMSource(doc);
            Source source = new DOMSource(document);
            //StreamResult result = new StreamResult();
            Result result = new StreamResult(dataFile1);
            transformer.transform(source, result);//将 XML==>Source 转换为 Result


            showToast( String.format("写入成功"));
        }catch(Exception e) {
            e.printStackTrace();
            showToast( String.format("写入失败"));
        }

    }

    //kml写函数
    public void createkml() {//创建xml文档
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            this.document = db.newDocument();//创建xml与解析xml不同的地方
            document.normalize();
        }
        catch(ParserConfigurationException e)
        {
            showToast( String.format(e.getMessage()));
        }

        Element root = document.createElement("Folder");//创建根节点
        document.appendChild(root);

        TransformerFactory tf = TransformerFactory.newInstance();
        try
        {
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            PrintWriter pw = new PrintWriter(new FileOutputStream(dataFile1,false));
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
            showToast( String.format("生成XML文件成功"));
        }
        catch(TransformerConfigurationException e)
        {
            showToast( String.format(e.getMessage()));
        }
        catch(IllegalArgumentException e)
        {
            showToast( String.format(e.getMessage()));
        }
        catch(FileNotFoundException e)
        {
            showToast( String.format(e.getMessage()));
        }
        catch(TransformerException e)
        {
            showToast( String.format(e.getMessage()));
        }

    }

    //拍照
    private void takePicture() {
        final Camera camera = getCamera();
        if (camera != null ){
            // 判断是否为拍照模式
            if (mCameraMode != SettingsDefinitions.CameraMode.SHOOT_PHOTO) {
                showToast("未处在拍照模式下!");
                return;
            }
            // 判断是否正在存储照片数据
            if (mIsCameraStoringPhoto) {
                showToast("相机繁忙，请稍后!");
                return;
            }
            // 设置单拍模式
            camera.setShootPhotoMode(SettingsDefinitions.ShootPhotoMode.SINGLE, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        // 开始拍照
                        camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    showToast("拍照成功!");
                                } else {
                                    showToast("拍照失败:" + djiError.getDescription());
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    // 获得无人机（或手持云台相机）的相机对象
    private Camera getCamera() {
        BaseProduct product = DJISDKManager.getInstance().getProduct();
        if (product != null && product.isConnected()) {
            return product.getCamera();
        }
        return null;
    }

    // 航点任务状态转字符串
    private String waypointMissionStateToString(WaypointMissionState state) {
        if (state.equals(WaypointMissionState.UNKNOWN)){
            return "UNKNOWN 未知";
        }else if (state.equals(WaypointMissionState.DISCONNECTED)){
            return "DISCONNECTED 断开连接";
        }else if (state.equals(WaypointMissionState.NOT_SUPPORTED)){
            return "NOT_SUPPORTED 不支持";
        }else if (state.equals(WaypointMissionState.RECOVERING)){
            return "RECOVERING 恢复连接中";
        }else if (state.equals(WaypointMissionState.READY_TO_UPLOAD)){
            return "READY_TO_UPLOAD 待上传";
        }else if (state.equals(WaypointMissionState.UPLOADING)){
            return "UPLOADING 上传中";
        }else if (state.equals(WaypointMissionState.READY_TO_EXECUTE)){
            return "READY_TO_EXECUTE 待执行";
        }else if (state.equals(WaypointMissionState.EXECUTING)){
            return "EXECUTING 执行中";
        }else if (state.equals(WaypointMissionState.EXECUTION_PAUSED)){
            return "EXECUTION_PAUSED 暂停中";
        }
        return "N/A";
    }

    // 航点任务执行枚举值转字符串
    private String waypointMissionExecuteStateToString(WaypointMissionExecuteState state) {

        switch (state)
        {
            case INITIALIZING:
                return "INITIALIZING 初始化";
            case MOVING:
                return "MOVING 移动中";
            case CURVE_MODE_MOVING:
                return "CURVE_MODE_MOVING 曲线模式移动中";
            case CURVE_MODE_TURNING:
                return "CURVE_MODE_TURNING 曲线模式拐弯中";
            case BEGIN_ACTION:
                return "BEGIN_ACTION 开始动作";
            case DOING_ACTION:
                return "DOING_ACTION 执行动作";
            case FINISHED_ACTION:
                return "FINISHED_ACTION 结束动作";
            case RETURN_TO_FIRST_WAYPOINT:
                return "RETURN_TO_FIRST_WAYPOINT 返回到第一个航点";
            case PAUSED:
                return "PAUSED 暂停中";
            default:
                return "N/A";
        }
    }

    // 获取航点任务操作器
    public WaypointMissionOperator getWaypointMissionOperator() {
        return DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
    }

    private void downloadWithMissionTime(long startTime,long endTime) {
        ArrayList<MediaFile> mMediaFilesInOneMission =new ArrayList<MediaFile>();
        for(int i = 0;i < mMediaFiles.size();i++){
            //文件创建时间在航点飞行任务开始和结束时间之间的才加入到列表中
            if(mMediaFiles.get(i).getTimeCreated() <= endTime && mMediaFiles.get(i).getTimeCreated() >= startTime){
                mMediaFilesInOneMission.add(mMediaFiles.get(i));
            }
        }

        // 设置下载位置
        File downloadDir = new File(getExternalFilesDir(null) + "/media/");
        for (MediaFile tmpMediaFile: mMediaFilesInOneMission) {
            // 开始下载文件
            tmpMediaFile.fetchFileData(downloadDir, null, new DownloadListener<String>() {
                @Override
                public void onFailure(DJIError error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPgsDlgDownload.cancel();
                        }
                    });
                    showToast("文件下载失败!");
                }
                @Override
                public void onProgress(long total, long current) {
                }
                @Override
                public void onRateUpdate(final long total, final long current, long persize) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int tmpProgress = (int) (1.0 * current / total * 100);
                            mPgsDlgDownload.setProgress(tmpProgress);
                        }
                    });
                }

                @Override
                public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {

                }

                @Override
                public void onStart() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPgsDlgDownload.incrementProgressBy(-mPgsDlgDownload.getProgress()); // 将下载进度设置为0
                            mPgsDlgDownload.show();
                        }
                    });
                }
                @Override
                public void onSuccess(String filePath) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mPgsDlgDownload.dismiss();
                        }
                    });
                    showToast("文件" + tmpMediaFile.getFileName() + "下载成功,下载位置为:" + filePath);
                }
            });
        }

    }

    //飞行器位置监听器
    private void waypointlistener() {

        //获得无人机（或手持云台相机）的云台对象
        Gimbal gimbal = getGimbal();
        if (gimbal != null) {
            gimbal.setStateCallback(new GimbalState.Callback() {
                @Override
                public void onUpdate(@NonNull GimbalState gimbalState) {
                    Attitude Gimbal_Attitude = gimbalState.getAttitudeInDegrees();
                    Gimbal_PITCH =Gimbal_Attitude.getPitch();

                    //showToast(str);

                }
            });
        }

        FlightController flightController = getFlightController();
        if (flightController != null) {

            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState state) {

                    DecimalFormat  df= new DecimalFormat("######0.00000000");

                    // 获取飞行模式
                    final String flightMode= state.getFlightModeString();
                    // 获取无人机经度
                    final double longitude = state.getAircraftLocation().getLongitude();
                    // 获取无人机纬度
                    final double latitude = state.getAircraftLocation().getLatitude();
                    // 获取无人机X方向移动速度
                    final double velocityX = state.getVelocityX();
                    // 获取无人机Y方向移动速度
                    final double velocityY = state.getVelocityY();
                    // 获取无人机Z方向移动速度
                    final double velocityZ = state.getVelocityZ();

                    // 获取无人机高度
                    final float altitude=state.getAircraftLocation().getAltitude();
                    // 获取卫星连接数量
                    final double satelliteCount = state.getSatelliteCount();
                    // 获取无人机航向
                    final double yaw = state.getAttitude().yaw ;//* Math.PI / 180;
//                    // 获取无人机航向
//                    final double yaw = state.getAttitude().yaw * Math.PI / 3.1415;



                    if(Run_judgment==true) {
                        if(longitude-0.0<1e-6 || latitude-0.0<1e-6){
                            String Aircraft_information = String.format("无法定位");
                            showToast(Aircraft_information);
                        }
                        else {


                            add_waypoint_tokml("waypoint",longitude, latitude,(float)altitude, (int)yaw,(int)Gimbal_PITCH,Photograph_switch);
                            String Aircraft_information = String.format("经度:%s, 纬度:%s,高度%s,航向:%s,云台角：%s,拍照：%s\r\n", longitude, latitude,altitude, yaw,Gimbal_PITCH,Photograph_switch);
                            showToast(Aircraft_information);
                            Run_judgment = false;
                        }
                    }
                }
            });
        } else {

            showToast("飞行控制器获取失败，请检查飞行器连接是否正常!");
        }




    }

    private void changeCameraMode() {
        Camera camera = getCamera();
        if (camera != null ){
            // 如果处在非拍照模式，则进入拍照模式
            setCameraMode(camera, SettingsDefinitions.CameraMode.SHOOT_PHOTO);

        }
    }

    private void setCameraMode(Camera camera, final SettingsDefinitions.CameraMode cameraMode) {

        camera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("相机模式设置成功:" );
                } else {
                    showToast("相机模式设置失败:" + djiError.getDescription());
                }
            }
        });
    }

    //云台回调
    private Gimbal getGimbal() {
        BaseProduct product = DJISDKManager.getInstance().getProduct();
        if (product != null && product.isConnected()) {
            return product.getGimbal();
        }
        return null;
    }

    private FlightController getFlightController() {
        BaseProduct product = DJISDKManager.getInstance().getProduct();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                return ((Aircraft) product).getFlightController();
            }
        }
        return null;
    }

    // 在主线程中显示提示
    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

}

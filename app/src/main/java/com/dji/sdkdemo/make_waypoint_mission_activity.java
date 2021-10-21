package com.dji.sdkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
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
import dji.midware.data.model.P3.Na;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class make_waypoint_mission_activity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtnnewwaypointmission,mBtnrecordwaypoint,mBtnshowwaypoint;
    boolean Settable=false;
    private WebView mWebView;
    boolean Run_judgment=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_waypoint_mission);
        initUI();
        waypointlistener();
    }


    private void initUI() {


        mBtnnewwaypointmission = findViewById(R.id.btn_new_waypoint_mission); // 【加载任务】按钮
        mBtnrecordwaypoint = findViewById(R.id.btn_record_waypoint); // 【加载任务】按钮
        mBtnshowwaypoint = findViewById(R.id.btn_show_waypoint); // 【显示任务】按钮

        mBtnnewwaypointmission.setOnClickListener(this);
        mBtnrecordwaypoint.setOnClickListener(this);
        mBtnshowwaypoint.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_new_waypoint_mission: newwaypointmission(); break;
            case R.id.btn_record_waypoint: recordwaypoint(); break;
            case R.id.btn_show_waypoint: showwaypoint(); break;
        }
    }

    public void onDestroy() {

        super.onDestroy();
        waypointlistener();
    }


    //新建航点任务
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

    //显示航点任务
    private void showwaypoint() {
        //ReadSysFile(this,"Waypoint_mission.txt");

    }

    //记录当前航点
    private void recordwaypoint() {
        if(Settable=true){
            Run_judgment=true;
        }
        else{
            String Aircraft_information = String.format("请新建航点任务" );
            showToast(Aircraft_information);
        }
    }

    //飞行器位置监听器
    private void waypointlistener() {
        FlightController flightController = getFlightController();
        if (flightController != null) {

            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState state) {
                    // 获取无人机经度
                    final double longitude = state.getAircraftLocation().getLongitude();
                    // 获取无人机纬度
                    final double latitude = state.getAircraftLocation().getLatitude();
                    // 获取无人机航向
                    final double yaw = state.getAttitude().yaw * Math.PI / 180;
                    //22.5425, 113.9560, 30
                    if(Run_judgment==true) {
                        if(longitude-0.0<1e-6 || latitude-0.0<1e-6){
                            String Aircraft_information = String.format("无法定位");
                            showToast(Aircraft_information);
                        }
                        else {
                            String longitude1 = Double.toString(longitude);
                            String latitude1 = Double.toString(latitude);
                            String yaw1 = Double.toString(yaw);
                            Write_data(longitude1 + " " + latitude1 + " " + yaw1);
                            String Aircraft_information = String.format("经度:%s, 纬度:%s,航向:%s\r\n", longitude, latitude, yaw);
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




    //写数据到文件
    public void Write_data(String data) {


        try {
            FileOutputStream fileout = openFileOutput("Waypoint_mission.txt", MODE_APPEND);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(data);
            fileout.flush();
            outputWriter.flush();
            fileout.close();
            outputWriter.close();

            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //从文件读数据
//    public void ReadSysFile(Context context, String filename) {
//        try {
//            FileInputStream fis = context.openFileInput(filename);
//            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
//            char[] input = new char[fis.available()];  //available()用于获取filename内容的长度,但是对中文有问题，建议使用BufferReader
//            isr.read(input);  //读取并存储到input中
//            isr.close();
//            fis.close();//读取完成后关闭
//            String str = new String(input);
//            System.out.println(str);
//
//            String[] strarray=str.split(" ");
//            Double longitude1 = Double.parseDouble(strarray[0]);
//
//            Toast.makeText(getBaseContext(), str,
//                    Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



    private FlightController getFlightController() {
        BaseProduct product = DJISDKManager.getInstance().getProduct();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                return ((Aircraft) product).getFlightController();
            }
        }
        return null;
    }

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }


}
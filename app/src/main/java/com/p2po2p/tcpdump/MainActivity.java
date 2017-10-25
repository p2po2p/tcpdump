package com.p2po2p.tcpdump;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //申请写入SD卡权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        //如果文件夹不存在，则创建文件夹
        String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        File file = new File(sdpath + "EFiles");

        if (!file.exists()) {
            file.mkdirs();
        }

        final TextView textView = (TextView) findViewById(R.id.textView);

        findViewById(R.id.start_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                SimpleDateFormat format = new SimpleDateFormat("MMdd_HHmmss");
                String date = format.format(new Date(System.currentTimeMillis()));

                CommandsHelper.FILE_NAME = date + ".pcap";
                CommandsHelper.DEST_FILE = CommandsHelper.FILE_PATH + CommandsHelper.FILE_NAME;

                textView.setText("\n\n" + "目标文件： " + CommandsHelper.DEST_FILE);

                v.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean retVal = CommandsHelper.startCapture(MainActivity.this);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!retVal) {
                                    v.setEnabled(true);
                                    //Toast.makeText(MainActivity.this, "startCapture result = " + retVal, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(MainActivity.this, "开启失败，请确认是否授予root权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        findViewById(R.id.stop_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandsHelper.stopCapture(MainActivity.this);
                findViewById(R.id.start_capture).setEnabled(true);
            }
        });
    }

}

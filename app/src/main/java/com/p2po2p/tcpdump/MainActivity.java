package com.p2po2p.tcpdump;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private NotificationManager notificationManager;

    private TextView textView;
    private Button button_start_capture;
    private Button button_stop_capture;
    private Button button_export;
    private EditText editText_ip;
    private EditText editText_port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNotification();

        textView = findViewById(R.id.textView);
        editText_ip = findViewById(R.id.editText_ip);
        editText_port = findViewById(R.id.editText_port);

        button_start_capture = findViewById(R.id.start_capture);
        button_start_capture.setOnClickListener(onStartCaptureClickListener);
        button_stop_capture = findViewById(R.id.stop_capture);
        button_stop_capture.setOnClickListener(onStopCaptureClickListener);
        button_export = findViewById(R.id.export);
        button_export.setOnClickListener(onExportClickListener);
        button_export.setEnabled(false);
    }

    private void initNotification() {

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{new Intent(this, MainActivity.class)}, 0);

        //第一步：获取状态通知栏管理：
        notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        //第二步：实例化通知栏构造器NotificationCompat.Builder：
        Notification.Builder builder = new Notification.Builder(this);
        //第三步：对Builder进行配置：
        builder.setContentTitle(getString(R.string.app_name))//设置通知栏标题
                .setContentText("触摸进行配置") //<span style="font-family: Arial;">/设置通知栏显示内容</span>
                /*Notification.FLAG_SHOW_LIGHTS              //三色灯提醒，在使用三色灯提醒时候必须加该标志符
                Notification.FLAG_ONGOING_EVENT          //发起正在运行事件（活动中）
                Notification.FLAG_INSISTENT   //让声音、振动无限循环，直到用户响应 （取消或者打开）
                Notification.FLAG_ONLY_ALERT_ONCE  //发起Notification后，铃声和震动均只执行一次
                Notification.FLAG_AUTO_CANCEL      //用户单击通知后自动消失
                Notification.FLAG_NO_CLEAR          //只有全部清除时，Notification才会清除 ，不清楚该通知(QQ的通知无法清除，就是用的这个)
                Notification.FLAG_FOREGROUND_SERVICE    //表示正在运行的服务*/
                .setContentIntent(pendingIntent) //设置通知栏点击意图
//  .setNumber(number) //设置通知集合的数量
                .setTicker(getString(R.string.app_name) + "开始工作") //通知首次出现在通知栏，带上升动画效果的
                //.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
//  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_launcher)//设置通知小ICON
                .setOngoing(true);//// 将Ongoing设为true 那么notification将不能滑动删除


        //第五步，最简单的一部，发送通知请求
        Notification notification = builder.build();
        notificationManager.notify(R.mipmap.ic_launcher, notification);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //点击一次返回键返回桌面而不是退出应用
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private View.OnClickListener onStartCaptureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            button_export.setEnabled(false);

            SimpleDateFormat format = new SimpleDateFormat("MMdd_HHmmss");
            String date = format.format(new Date(System.currentTimeMillis()));

            final String ip = editText_ip.getText().toString();
            final String port = editText_port.getText().toString();


            CommandsHelper.FILE_NAME = date + ".pcap";
            CommandsHelper.DEST_FILE = getExternalFilesDir("") +"/"+ CommandsHelper.FILE_NAME;

            textView.setText("\n" + "保存路径： " + CommandsHelper.DEST_FILE);

            v.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean retVal = CommandsHelper.startCapture(MainActivity.this, ip, port);
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
    };

    private View.OnClickListener onStopCaptureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CommandsHelper.stopCapture(MainActivity.this);
            button_start_capture.setEnabled(true);
            button_export.setEnabled(true);
        }
    };

    private View.OnClickListener onExportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = CommandsHelper.DEST_FILE;

            ShareUtil.shareSingle(MainActivity.this, "抓包", path);
        }
    };
}

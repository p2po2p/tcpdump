package com.p2po2p.tcpdump;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private TextView textView_capture_path;
    private TextView textView_logcat_path;
    private Button button_export_capture;
    private Button button_export_logcat;
    private Button button_export_all;
    private EditText editText_ip;
    private EditText editText_port;
    private Spinner spinner_mode;
    private CheckBox checkBox_capture;
    private CheckBox checkBox_logcat;


    private String capture_mode = "any";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
        }
        sendChatMsg();
        textView_capture_path = findViewById(R.id.textView_capture_path);
        textView_logcat_path = findViewById(R.id.textView_logcat_path);
        editText_ip = findViewById(R.id.editText_ip);
        editText_port = findViewById(R.id.editText_port);
        spinner_mode = findViewById(R.id.spinner_mode);
        checkBox_capture = findViewById(R.id.checkBox_capture);
        checkBox_logcat = findViewById(R.id.checkBox_logcat);

        button_export_capture = findViewById(R.id.export_capture);
        button_export_capture.setOnClickListener(onExportClickListener);
        button_export_capture.setEnabled(false);

        button_export_logcat = findViewById(R.id.export_logcat);
        button_export_logcat.setOnClickListener(onExportClickListener);
        button_export_logcat.setEnabled(false);

        button_export_all = findViewById(R.id.export_all);
        button_export_all.setOnClickListener(onExportClickListener);
        button_export_all.setEnabled(false);

        spinner_mode.setOnItemSelectedListener(onItemSelectedListener);
        checkBox_capture.setOnCheckedChangeListener(captureCheckListener);
        checkBox_logcat.setOnCheckedChangeListener(logcatCheckListener);
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

    private CompoundButton.OnCheckedChangeListener captureCheckListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                //开始抓包
                startCapture();
            } else {
                stopCapture();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener logcatCheckListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                //开始抓日志
                startLogcat();
            } else {
                stopLogcat();
            }
        }
    };

    private void startCapture() {
        button_export_capture.setEnabled(false);
        button_export_all.setEnabled(false);

        SimpleDateFormat format = new SimpleDateFormat("MMdd_HHmmss");
        String date = format.format(new Date(System.currentTimeMillis()));

        final String ip = editText_ip.getText().toString();
        final String port = editText_port.getText().toString();

        CommandsHelper.CAPTURE_DEST_FILE = getExternalFilesDir("") + "/" + date + ".pcap";

        String tip = "\n" + "报文保存路径： " + CommandsHelper.CAPTURE_DEST_FILE;
        textView_capture_path.setText(tip);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean retVal = CommandsHelper.startCapture(MainActivity.this, ip, port, capture_mode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!retVal) {
                            //Toast.makeText(MainActivity.this, "startCapture result = " + retVal, Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "开启失败，请确认授予root权限，程序即将退出", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                if (!retVal) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        }).start();
    }

    private void stopCapture() {
        CommandsHelper.stopCapture(MainActivity.this);
        button_export_capture.setEnabled(true);
        button_export_all.setEnabled(true);
    }

    private void startLogcat() {
        button_export_logcat.setEnabled(false);
        button_export_all.setEnabled(false);
        
        SimpleDateFormat format = new SimpleDateFormat("MMdd_HHmmss");
        String date = format.format(new Date(System.currentTimeMillis()));

        CommandsHelper.LOGCAT_DEST_FILE = getExternalFilesDir("") + "/" + date + ".txt";

        String tip = "\n" + "日志保存路径： " + CommandsHelper.LOGCAT_DEST_FILE;
        textView_logcat_path.setText(tip);

        new Thread(new Runnable() {
            @Override
            public void run() {
                CommandsHelper.startLogcat();
            }
        }).start();
    }

    private void stopLogcat() {
        CommandsHelper.stopLogcat();
        button_export_logcat.setEnabled(true);
        button_export_all.setEnabled(true);
    }

    private View.OnClickListener onExportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tag = String.valueOf(v.getTag());

            if (tag.equalsIgnoreCase("capture")) {
                Log.i("h02659", "分享报文 " + tag);

                String path = CommandsHelper.CAPTURE_DEST_FILE;
                String tip = "报文";
                ShareUtil.shareSingle(MainActivity.this, tip, path);
            } else if (tag.equalsIgnoreCase("logcat")) {
                Log.i("h02659", "分享日志 " + tag);
                String path = CommandsHelper.LOGCAT_DEST_FILE;
                String tip = "日志";
                ShareUtil.shareSingle(MainActivity.this, tip, path);
            } else if (tag.equalsIgnoreCase("all")) {
                Log.i("h02659", "分享全部 " + tag);
                List<String> listPath = new ArrayList<>();
                listPath.add(CommandsHelper.CAPTURE_DEST_FILE);
                listPath.add(CommandsHelper.LOGCAT_DEST_FILE);
                String[] paths = listPath.toArray(new String[listPath.size()]);
                String tip = "报文+日志";
                ShareUtil.shareMutiple(MainActivity.this, tip, paths);
            }

        }
    };

    private Spinner.OnItemSelectedListener onItemSelectedListener = new Spinner.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.i("h02659", "position:" + position);
            if (position == 0) {
                capture_mode = "any";
            } else if (position == 1) {
                capture_mode = "lo";
            } else {
                capture_mode = "any";
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    public void sendChatMsg() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel("chat");
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
                startActivity(intent);
                Toast.makeText(this, "请手动将通知打开", Toast.LENGTH_SHORT).show();
            }
        }
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{new Intent(this, MainActivity.class)}, 0);


        /*
        * builder.setContentTitle(getString(R.string.app_name))//设置通知栏标题
                .setContentText("触摸进行配置") //<span style="font-family: Arial;">/设置通知栏显示内容</span>
                /*Notification.FLAG_SHOW_LIGHTS              //三色灯提醒，在使用三色灯提醒时候必须加该标志符
                Notification.FLAG_ONGOING_EVENT          //发起正在运行事件（活动中）
                Notification.FLAG_INSISTENT   //让声音、振动无限循环，直到用户响应 （取消或者打开）
                Notification.FLAG_ONLY_ALERT_ONCE  //发起Notification后，铃声和震动均只执行一次
                Notification.FLAG_AUTO_CANCEL      //用户单击通知后自动消失
                Notification.FLAG_NO_CLEAR          //只有全部清除时，Notification才会清除 ，不清楚该通知(QQ的通知无法清除，就是用的这个)
                Notification.FLAG_FOREGROUND_SERVICE    //表示正在运行的服务
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
        *
        * */


        Notification notification = new NotificationCompat.Builder(this, "chat")
                .setContentTitle("tcpdump")
                .setContentText("触摸进行抓包配置")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                //.setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);
    }
}

package com.p2po2p.tcpdump;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Author: p2po2p
 * Email: p2po2p@gmail.com
 * Date:  2017/9/26
 * Description:
 */

public class CommandsHelper {
    public static String CAPTURE_MODE_ANY = "any";
    public static String CAPTURE_MODE_LO = "lo";
    private static final String NAME = "tcpdump";
    private String CAPTURE_DEST_FILE = "/capture.pcap";
    static String LOGCAT_DEST_FILE = "/log.txt";
    static String LOGCAT_ZIP = "/log.zip";
    private String capture_mode = CAPTURE_MODE_ANY;

    public CommandsHelper(String capture_mode) {
        this.capture_mode = capture_mode;
    }

    public String getCAPTURE_DEST_FILE() {
        return CAPTURE_DEST_FILE;
    }

    public void setCAPTURE_DEST_FILE(String CAPTURE_DEST_FILE) {
        Log.i("h02659",CAPTURE_DEST_FILE);
        this.CAPTURE_DEST_FILE = CAPTURE_DEST_FILE;
    }

    public String getCapture_mode() {
        return capture_mode;
    }

    public void setCapture_mode(String capture_mode) {
        this.capture_mode = capture_mode;
    }

    public boolean startCapture(Context context, String ip, String port) {
        Log.i("h02659", "startCapture start ip:" + ip +" port:" + port);
        //准备tcpdump文件+赋予tcpdump权限
        prepareTCPDumpFile(context);
        //获取any网卡的id
        /*int num = tcpdump_d_any();
        Log.i("h02659", "startCapture num:" + num);

        String captureCommand = "tcpdump -i "+ num +" -p -vv -s 0";*/

        String captureCommand = "./tcpdump -i "+capture_mode+" -p -vv -s 0";

        if (ip != null && !ip.isEmpty()) {
            captureCommand = captureCommand + " host " + ip;
        }

        if (port != null && !port.isEmpty()) {
            captureCommand = captureCommand + " port " + port;
        }

        captureCommand = captureCommand + " -w " + CAPTURE_DEST_FILE;

        Log.i("h02659",captureCommand);

        boolean retVal = false;
        try {
            String[] commands = new String[4];
            commands[0] = "adb shell";
            commands[1] = "su";
            commands[2] = "cd /data/local";
            commands[3] = captureCommand;
            Process process = execCmd(commands);
            if (process != null) {
                Log.i("h02659", "process not null");
                retVal = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("h02659", "startCapture end "+retVal);
        return retVal;
    }

    public void stopCapture() {
        Log.i("h02659", "stopCapture start");
        // 找出所有的带有tcpdump的进程
        String[] commands = new String[2];
        commands[0] = "adb shell";
        //commands[1] = "ps|grep tcpdump|grep root|awk '{print $2}'";
        commands[1] = "ps|grep tcpdump|grep root";
        Process process = execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        Log.i("h02659",result );
        if (!TextUtils.isEmpty(result)) {
            String[] pids = result.split("\n");
            String[] killCmds = new String[pids.length];
            for (int i = 0; i < pids.length; ++i) {
                killCmds[i] = "kill -9 " + pids[i];
                Log.i("h02659",killCmds[i] );
            }
            execCmd(killCmds);
        }
        Log.i("h02659", "stopCapture end");
    }

    static boolean startLogcat() {
        Log.i("h02659", "startLogcat ");

        //输出logadb logcat -f /data/local/tmp/log.txt -n 10 -r 1

        String logcatCommand = "logcat -v time -f "+LOGCAT_DEST_FILE;

        Log.i("h02659",logcatCommand);

        boolean retVal = false;
        try {
            String[] commands = new String[3];
            commands[0] = "adb shell";
            commands[1] = "logcat -c";
            commands[2] = logcatCommand;
            Process process = execCmd(commands);
            if (process != null) {
                retVal = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("h02659", "startCapture end");
        return retVal;
    }

    static void stopLogcat() {
        Log.i("h02659", "stopLogcat start");
        // 找出所有的带有tcpdump的进程
        String[] commands = new String[2];
        commands[0] = "adb shell";
        //commands[1] = "ps|grep tcpdump|grep root|awk '{print $2}'";
        commands[1] = "ps|grep logcat";
        Log.i("h02659",commands[1] );
        Process process = execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        if (!TextUtils.isEmpty(result)) {
            String[] pids = result.split("\n");
            String[] killCmds = new String[pids.length];
            for (int i = 0; i < pids.length; ++i) {
                killCmds[i] = "kill -9 " + pids[i];
                Log.i("h02659",killCmds[i] );
            }
            execCmd(killCmds);
        }
        Log.i("h02659", "stopCapture end");
    }

    /**
     * 执行TCPDUMP -D ，返回 any的网卡序号
     * sagit:/ # tcpdump -D
     * tcpdump -D
     * 1.wlan0 [Up, Running]
     * 2.rmnet_ipa0 [Up, Running]
     * 3.dummy0 [Up, Running]
     * 4.any (Pseudo-device that captures on all interfaces) [Up, Running]
     * 5.lo [Up, Running, Loopback]
     * 6.p2p0 [Up]
     * 7.nflog (Linux netfilter log (NFLOG) interface)
     * 8.nfqueue (Linux netfilter queue (NFQUEUE) interface)
     */
    private int tcpdump_d_any() {
        Log.i("h02659", "tcpdump_d_any start");
        int num = 1;
        String[] commands = new String[2];
        commands[0] = "adb shell";
        commands[1] = "tcpdump -D";
        Process process = execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        if (!TextUtils.isEmpty(result)) {
            String[] lines = result.split("\n");
            for (String line : lines) {
                if (line.contains("any")) {
                    Log.i("h02659", "any id = " + line.substring(0, 1));
                    num = Integer.valueOf(line.substring(0, 1));
                    break;
                }
            }
        }
        Log.i("h02659", "tcpdump_d_any end");
        return num;
    }

    private void prepareTCPDumpFile(Context context) {
        Log.i("h02659", "prepareTCPDumpFile start");
        InputStream is = null;
        OutputStream os = null;
        boolean retVal = false;
        try {
            AssetManager am = context.getAssets();
            is = am.open(NAME);
            File dstFile = new File(context.getExternalFilesDir(""), NAME);
            Log.i("h02659", dstFile.getAbsolutePath());
            os = new FileOutputStream(dstFile);
            copyStream(is, os);
            String[] commands = new String[7];
            commands[0] = "adb shell";
            commands[1] = "su";
            commands[2] = "cp -rf " + dstFile.toString() + " /data/local/tcpdump";
            commands[3] = "rm -r " + dstFile.toString();
            commands[4] = "chmod 777 /data/local/tcpdump";
            commands[5] = "exit";
            execCmd(commands, false);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSafely(is);
            closeSafely(os);
        }
        Log.i("h02659", "prepareTCPDumpFile end");
    }

    /**
     * adb pull /sdcard/keeplive.pcap D:/log/
     */
    public void exportFile() {
        Log.i("h02659", "exportFile start");
        String[] commands = new String[1];
        commands[0] = "adb pull "+ CAPTURE_DEST_FILE +" D:/log/";
        Process process = execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        if (!TextUtils.isEmpty(result)) {
            Log.i("h02659", "exportFile   " + result);
        }
        Log.i("h02659", "exportFile end");
    }

    public static Process execCmd(String command) {
        return execCmd(new String[]{command}, true);
    }

    public static Process execCmd(String[] commands) {
        return execCmd(commands, true);
    }

    public static Process execCmd(String[] commands, boolean waitFor) {
        Process suProcess = null;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            for (String cmd : commands) {
                if (!TextUtils.isEmpty(cmd)) {
                    os.writeBytes(cmd + "\n");
                }
            }
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("h02659", "no permission");
            return null;
        }
        if (waitFor) {
            boolean retval = false;
            try {
                int suProcessRetval = suProcess.waitFor();
                Log.i("h02659", "suProcessRetval:" + suProcessRetval);
                if (255 != suProcessRetval) {
                    retval = true;
                } else {
                    retval = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return suProcess;
    }

    private static void copyStream(InputStream is, OutputStream os) {
        final int BUFFER_SIZE = 1024;
        try {
            byte[] bytes = new byte[BUFFER_SIZE];
            for (; ; ) {
                int count = is.read(bytes, 0, BUFFER_SIZE);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeSafely(Closeable is) {
        try {
            if (null != is) {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parseInputStream(InputStream is) {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}

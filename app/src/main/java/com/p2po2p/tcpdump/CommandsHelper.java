package com.p2po2p.tcpdump;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
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
    private static final String NAME = "tcpdump";
    private static final String TAG = "CommandsHelper";
    public static String DEST_FILE = Environment.getExternalStorageDirectory() + "/EFiles/capture.pcap";
    public static String FILE_PATH = Environment.getExternalStorageDirectory() + "/EFiles/";
    public static String FILE_NAME = "capture.pcap";

    public static boolean startCapture(Context context) {
        Log.i("hqr", "startCapture start");
        //准备tcpdump文件+赋予tcpdump权限
        prepareTCPDumpFile(context);
        //获取any网卡的id
        int num = tcpdump_d_any();
        Log.i("hqr", "startCapture num:" + num);
        boolean retVal = false;
        try {
            String[] commands = new String[7];
            commands[0] = "adb shell";
            commands[1] = "su";
            commands[5] = "cd /data/local";
            commands[6] = "tcpdump -i "+ num +" -p -vv -s 0 -w " + DEST_FILE;
            execCmd(commands);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("hqr", "startCapture end");
        return retVal;
    }

    public static void stopCapture(Context context) {
        Log.i("hqr", "stopCapture start");
        // 找出所有的带有tcpdump的进程
        String[] commands = new String[2];
        commands[0] = "adb shell";
        commands[1] = "ps|grep tcpdump|grep root|awk '{print $2}'";
        Process process = execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        if (!TextUtils.isEmpty(result)) {
            String[] pids = result.split("\n");
            String[] killCmds = new String[pids.length];
            for (int i = 0; i < pids.length; ++i) {
                killCmds[i] = "kill -9 " + pids[i];
            }
            execCmd(killCmds);
        }
        Log.i("hqr", "stopCapture end");
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
    private static int tcpdump_d_any() {
        Log.i("hqr", "tcpdump_d_any start");
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
                    Log.i("hqr", "any id = " + line.substring(0, 1));
                    num = Integer.valueOf(line.substring(0, 1));
                    break;
                }
            }
        }
        Log.i("hqr", "tcpdump_d_any end");
        return num;
    }

    private static void prepareTCPDumpFile(Context context) {
        Log.i("hqr", "prepareTCPDumpFile start");
        InputStream is = null;
        OutputStream os = null;
        boolean retVal = false;
        try {
            AssetManager am = context.getAssets();
            is = am.open(NAME);
            File sdcardFile = Environment.getExternalStorageDirectory();
            File dstFile = new File(sdcardFile, NAME);
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
        Log.i("hqr", "prepareTCPDumpFile end");
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
        }
        if (waitFor) {
            boolean retval = false;
            try {
                int suProcessRetval = suProcess.waitFor();
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

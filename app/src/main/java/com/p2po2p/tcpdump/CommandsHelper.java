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
            commands[5] = "cd /data/local";
            commands[6] = "tcpdump -p -vv -s 0 -w " + DEST_FILE;
            execCmd(commands);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, " error: " + e.getMessage());
        } finally {
            closeSafely(is);
            closeSafely(os);
        }
        return retVal;
    }

    public static void stopCapture(Context context) {
        // 找出所有的带有tcpdump的进程
        String[] commands = new String[2];
        commands[0] = "adb shell";
        commands[1] = "ps|grep tcpdump|grep root|awk '{print $2}'";
        Process process = execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        if (!TextUtils.isEmpty(result)) {
            String[] pids = result.split("\n");
            if (null != pids) {
                String[] killCmds = new String[pids.length];
                for (int i = 0; i < pids.length; ++i) {
                    killCmds[i] = "kill -9 " + pids[i];
                }
                execCmd(killCmds);
            }
        }
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
                Log.w("Error ejecutando el comando Root", ex);
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

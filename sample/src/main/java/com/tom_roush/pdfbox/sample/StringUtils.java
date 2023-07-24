package com.tom_roush.pdfbox.sample;

import android.util.Log;

public class StringUtils {
    static final int MAX_LOG = 800;
    static public void HaoLog(String data, int showC) {
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        if(stes.length > showC)
            showC =stes.length -1;
        if (stes != null && stes.length > showC) {
            if (stes[showC].getFileName().equals("MainAppCompatActivity.java")){
                showC++;
            }
            if (stes[showC].getFileName().equals("MessageBaseActivity.java")){
                showC++;
            }
            String tag = "HaoLog";
            tag += " (" + stes[showC].getFileName() + ":" + stes[showC].getLineNumber() + ") ";
            tag += stes[showC].getMethodName() + " Thread=" + Thread.currentThread().getName() + "　 ";
            if (data == null){
                Log.d(tag, "null");
            } else if (data.length() < MAX_LOG){
                Log.d(tag, data);
            } else {
                int p = data.length() / MAX_LOG;
                if (data.length() % MAX_LOG == 0) {
                    for (int i = 0; i < p; i++) {
                        Log.d(tag, data.substring(i * MAX_LOG, (i + 1) * MAX_LOG));
                    }
                } else {
                    for (int i = 0; i < p; i++) {
                        Log.d(tag, data.substring(i * MAX_LOG, (i + 1) * MAX_LOG));
                    }
                    Log.d(tag, data.substring(p * MAX_LOG));
                }
            }
        }
    }

    static public void HaoLog(String data) {
        HaoLog(data, 4);

    }
}

package com.example.fyp_chatapp.utilities;

import android.content.Context;

import java.io.File;

public class FileUtils {

    // 获取应用程序的临时目录路径
    public static String getTempDirPath(Context context) {
        String tempDirPath = context.getApplicationContext().getCacheDir() + "/temp/";
        File tempDir = new File(tempDirPath);

        // 如果临时目录不存在，则创建它
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return tempDirPath;
    }
}

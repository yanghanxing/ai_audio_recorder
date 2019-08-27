package com.rk.demo.soundrecord.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * FileName: FileUtils
 * Author: rockchip Date: 2019/8/13
 * Description:
 */

public class FileUtils {

    public static List<String> readFile(String filePath) {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            File file = new File(filePath);
            if (null == file || !file.isFile() || !file.exists()) {
                return null;
            }
            is = new FileInputStream(file);
            if (null != is) {
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                List<String> strList = new ArrayList<>();
                String line;
                while (null != (line = br.readLine())) {
                    strList.add(line);
                }
                return strList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                br = null;
            }
            if (null != isr) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}

package com.rk.demo.soundrecord.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileName: WelcomeActivity
 * Author: rockchip Date: 2019/8/13
 * Description:
 */

public class DateUtils {

    public static String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyyMMddHHmmss");
        String time = simpleDateFormat.format(new Date(System
                .currentTimeMillis()));
        return time;
    }
}

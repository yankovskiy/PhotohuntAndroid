/*******************************************************************************
 * Copyright (C) 2013-2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package ru.neverdark.photohunt.utils;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

import ru.neverdark.photohunt.BuildConfig;

public class Log {
    /** true if write log to file or false in other case */
    private static final boolean WRITE_FILE = false;

    /**
     * Logs enter to function
     * 
     * @return enter time in milliseconds
     */
    public static long enter() {
        if (BuildConfig.DEBUG) {
            log("Enter");
            return System.currentTimeMillis();
        }
        return 0;
    }

    /**
     * Logs exit from function with worked time
     * 
     * @param enterTime
     *            time in milliseconds (enter time in function). If zero - no
     *            logging working time
     */
    public static void exit(long enterTime) {
        if (BuildConfig.DEBUG) {
            log("Exit");
            if (enterTime != 0) {
                long result = System.currentTimeMillis() - enterTime;
                log("Time: " + result + "ms");
            }
        }
    }

    /**
     * Logs message with class name, method name and line number
     * 
     * @param message
     *            message for logging
     */
    private static void log(String message) {
        Throwable stack = new Throwable().fillInStackTrace();
        StackTraceElement[] trace = stack.getStackTrace();
        String APP = trace[2].getClassName() + "." + trace[2].getMethodName()
                + ":" + trace[2].getLineNumber();
        android.util.Log.i(APP, message);
    }

    /**
     * Function logged message to the LogCat as information message
     * 
     * @param message
     *            message for logging
     */
    public static void message(String message) {
        if (BuildConfig.DEBUG) {
            log(message);
        }
    }

    /**
     * Saves messages from logcat to file This function must be called only once
     * from application !!! WARNING: This function erase all previous logcat
     * messages from your devices
     */
    public static void saveLogcatToFile() {
        if (BuildConfig.DEBUG && WRITE_FILE) {
            String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
            File outputFile = new File(
                    Environment.getExternalStorageDirectory(), fileName);
            try {
                Process process1 = Runtime.getRuntime().exec("logcat -c ");
                Process process = Runtime.getRuntime().exec(
                        "logcat -f " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Function logged values to the LogCat as information message
     * 
     * @param variable
     *            variable name for logging
     * @param value
     *            value of the variable
     */
    public static void variable(String variable, String value) {
        if (BuildConfig.DEBUG) {
            String message = variable + " = " + value;
            log(message);
        }
    }
}

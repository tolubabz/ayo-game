package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GameLogger {

    private static final Object lock = new Object();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void log(String message) {
        synchronized (lock) {
            String threadName = Thread.currentThread().getName();
            String time = sdf.format(new Date());
            System.out.println("[" + time + "][" + threadName + "] " + message);
        }
    }
}

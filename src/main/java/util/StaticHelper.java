package util;

import gui.Dialogs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class StaticHelper {
    public static String GET_AVAILABLE_LICENCES_SKRIPT;
    public static String NEW_EMPOLIS_USER_SKRIPT;

    public static String LOG_FILE;

    static {
        try {GET_AVAILABLE_LICENCES_SKRIPT = (StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../scripts/Get-Available-Licences.ps1").substring(1);
            NEW_EMPOLIS_USER_SKRIPT = (StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../scripts/New-EmpolisUser.ps1").substring(1);
            if (!(new File(GET_AVAILABLE_LICENCES_SKRIPT)).exists() && !(new File(NEW_EMPOLIS_USER_SKRIPT).exists())) {
                File dir = new File((StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../scripts/").substring(1));
                dir.mkdirs();
                Dialogs.infoBox("No Scripts found. Created folder at " + dir.getPath(), "Error Reading Script from File!");
            }
            LOG_FILE = (StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../logs/log_" + TimeTransformations.calendarToString(Calendar.getInstance()).replace(".", "_")+ "_"+System.getProperty("user.name") + ".txt").substring(1);

            File log_dir = new File((StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../logs").substring(1));
            if (!log_dir.exists() || !log_dir.isDirectory()) {
                log_dir.mkdirs();
            }
            File log_file = new File(LOG_FILE);
            if (!log_file.exists()) {
                log_file.createNewFile();
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

    }

    public static Handler logToFile() {
        try {
            FileWriter fileWriter = new FileWriter(LOG_FILE);
            return new Handler() {
                String last_Logger;

                @Override
                public void publish(LogRecord record) {
                    try {
                        if (record.getLoggerName().equals(last_Logger)) {
                            fileWriter.write("\n\t" + record.getLevel() + ": "+ record.getMessage());
                        } else {
                            fileWriter.write("\n" + record.getLoggerName() + "\n" + record.getMessage());
                            last_Logger = record.getLoggerName();
                        }
                        fileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                @Override

                public void flush() {
                    try {
                        fileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void close() throws SecurityException {
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

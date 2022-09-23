package content.adContent.content;

import gui.Dialogs;
import util.StaticHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Hilfsklasse für das interne RACF_File.
 * In diesem sollten alle jemals belegten RACF Adressen gespeichert sein.
 * Enthält einen Reader und Writer für dieses File.
 */
public class RACFFile {
    public static File RACF_FILE;
    private static final Logger logger = Logger.getLogger("RACF_File");

    static {
        logger.addHandler(StaticHelper.logToFile());
        try {
            logger.info("trying to read RACF_File");
            RACF_FILE = new File((StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../resources/racfs_DO_NOT_DELETE").substring(1));
            if (!RACF_FILE.exists()) {
                logger.warning("RACF_File doesn't exist, will create a new one in the appropriate dir");
                File dir = new File((StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../resources/").substring(1));
                if (!dir.mkdirs())
                    logger.severe("Could not create dir");
                if (!RACF_FILE.createNewFile())
                    logger.severe("Could not create new File");
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            Dialogs.infoBox("Can't locate/load RACF File. Please don't make any new Users as long as this error persists", "File Reading Error");
        }
    }

    public static void add(String racf) {
        try (BufferedWriter writer = getRACFWriter(true)) {
            logger.info("Adding " + racf + " to the RACF_File");
            writer.write(racf);
        } catch (Exception e) {
            logger.severe("Error adding an racf to the RACF_File");
            Dialogs.infoBox("Please don't add users as long as this error persists. " + e.getMessage(), "Error Adding User to File");
        }
    }

    public static BufferedReader getRACFReader() throws IOException {
        return new BufferedRACFReader(new FileReader(RACF_FILE));
    }

    public static BufferedWriter getRACFWriter(boolean append) throws IOException {
        return new BufferedRACFWriter(new FileWriter(RACF_FILE, append));
    }

    public static List<String> getList() {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = getRACFReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (Exception e) {
            logger.severe("Error reading RACF_File." + e.getMessage());
            Dialogs.infoBox(e.getMessage(), "Error Reading File");
        }
        logger.info("Read RACF_File. Got " +  list.size() + " entries.");
        return list;
    }

    public static List<Integer> getNumberList(String racf_name) {
        logger.info("Getting all numbers with the racf: " + racf_name);
        List<Integer> givenNumbers = new ArrayList<>();
        try (BufferedReader bufferedReader = getRACFReader()) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.substring(0, 4).equals(racf_name)) {
                    givenNumbers.add(Integer.valueOf(line.substring(4, 7)));
                }
            }
        } catch (Exception err) {
            logger.severe("Error getting numbers with racf " + racf_name + ". " + err.getMessage() + Arrays.toString(err.getStackTrace()));
            Dialogs.infoBox(err.getMessage(), "Exeception while reading racf File");
        }
        logger.info("Found " + givenNumbers.size() + " with the racf " + racf_name);
        return givenNumbers;
    }

    private static class BufferedRACFReader extends BufferedReader {

        public BufferedRACFReader(Reader in) {
            super(in);
        }

        @Override
        public String readLine() throws IOException {
            String line = super.readLine();
            return (line != null) ? line.substring(0, 4) + line.substring(6, 9) : null;
        }
    }

    private static class BufferedRACFWriter extends BufferedWriter {
        public BufferedRACFWriter(Writer out) {
            super(out);
        }

        @Override
        public void write(String s) throws IOException {
            super.write(s.substring(0, 4) + "__" + s.substring(4) + '\n');
        }
    }
}

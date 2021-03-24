
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Command {
    private final String commandString;
    private final boolean shouldWait;

    public Command(String commandString, boolean shouldWait) {
        this.commandString = commandString;
        this.shouldWait = shouldWait;
    }


    public Command(String commandString) {
        this(commandString, true);
    }

    private printOutput getStreamWrapper(InputStream is, String type) {
        return new printOutput(is, type);
    }

    public String[] run() {
        Runtime rt = Runtime.getRuntime();
        printOutput errorReported, outputMessage;

        try {
            Process proc = rt.exec(commandString);
            errorReported = getStreamWrapper(proc.getErrorStream(), "ERROR");
            outputMessage = getStreamWrapper(proc.getInputStream(), "OUTPUT");
            errorReported.start();
            outputMessage.start();
            if (shouldWait) {
                proc.waitFor();
                for (String line : errorReported.lastlines)
                    if (line.contains("ERROR:")) return errorReported.lastlines;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class printOutput extends Thread {
        private InputStream is;
        private String type;

        final int lineSize = 6;
        String[] lastlines = new String[lineSize];
        int i=0;

        printOutput(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            String s;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((s = br.readLine()) != null) {
                    System.out.println(s);
                    lastlines[i%lineSize] = s;
                    i++;
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
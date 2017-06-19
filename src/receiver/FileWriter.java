package receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileWriter implements Runnable {

    private final String filePath;
    private FileOutputStream fileOutputStream;
    private final ReceiverSlidingWindow receiverSlidingWindow;
    private int packetSize = 0;

    FileWriter(String filePath, ReceiverSlidingWindow receiverSlidingWindow) {
        this.receiverSlidingWindow = receiverSlidingWindow;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        String fileName = receiverSlidingWindow.getFileName();
        try {
            fileOutputStream = new FileOutputStream(new File(filePath + fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        boolean isRunning = true;
        try {
            while (isRunning) {
                byte[] buffer = receiverSlidingWindow.get();
                if (packetSize == 0) {
                    packetSize = buffer.length;
                }
                fileOutputStream.write(buffer);

                if (buffer.length < packetSize) {
                    isRunning = false;
                }
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}

package sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;


public class FileReader implements Runnable {

    private final int blockSize;
    private final SlidingWindowController slidingWindowController;
    private final String filePath;
    private String fileName;
    private FileInputStream fileInputStream;
    private MidBuffer midBuffer;

    FileReader(String filePath, int blockSize, SlidingWindowController slidingWindowController) {
        this.filePath = filePath;
        this.blockSize = blockSize;
        this.slidingWindowController = slidingWindowController;
        midBuffer = new MidBuffer(slidingWindowController);
    }

    @Override
    public void run() {
        try {
            Thread thread =  new Thread(midBuffer);
            thread.start();
            File file = new File(filePath);
            fileName = file.getName();
            fileInputStream = new FileInputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int read;
        boolean isNotCompleteRead = true;

        byte buffer[] = new byte[fileName.length() + 4];
        byte tmp[] = fileName.getBytes();
        System.arraycopy(tmp, 0, buffer, 4, tmp.length);
        slidingWindowController.push(buffer);

        int id = 1;
        while (isNotCompleteRead) {
            try {
                byte[] index = ByteBuffer.allocate(4).putInt(id).array();
                buffer = new byte[blockSize + 4];
                read = fileInputStream.read(buffer, 4, blockSize);
                if (read < blockSize) {
                    if (read < 0) read = 0;
                    byte[] lastBuffer = new byte[read + 4];
                    System.arraycopy(buffer, 4, lastBuffer, 4, read);
                    System.arraycopy(index, 0, lastBuffer, 0, 4);
                    isNotCompleteRead = false;

                    if (id == 1) {
                        byte[] zero_buf = ByteBuffer.allocate(4).putInt(2).array();
                        slidingWindowController.push(lastBuffer);
                        slidingWindowController.push(zero_buf);
                        slidingWindowController.setMaxSize(2);
                    } else {
                        slidingWindowController.push(lastBuffer);
                        slidingWindowController.setMaxSize(id);
                    }
                } else {
                    System.arraycopy(index, 0, buffer, 0, 4);
//                    slidingWindowController.push(buffer);
                    midBuffer.addLast(buffer);
                }
                ++id;
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

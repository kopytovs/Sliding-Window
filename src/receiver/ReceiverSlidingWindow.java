package receiver;

import java.util.ArrayList;


class ReceiverSlidingWindow {

    private ArrayList<byte[]> window;
    private ArrayList<Boolean> isReceived;
    private int leftWindowIndex = 0;
    private int indexToWrite = 0;
    private final Object lock = new Object();
    private String fileName;
    private boolean isFileNameReceived = false;

    ReceiverSlidingWindow() {
        window = new ArrayList<>();
        isReceived = new ArrayList<>();
    }

    void push(byte[] buffer, int index) {
        synchronized (lock) {
            if (window.size() <= index) {
                for (int i = window.size(); i <= index; ++i) {
                    window.add(null);
                    isReceived.add(false);
                }
            }
            if (index > 0) {
                window.set(index, buffer);
            } else {
                fileName = new String(buffer);
                isFileNameReceived = true;
                leftWindowIndex++;
                indexToWrite = 1;
                lock.notify();
            }
            isReceived.set(index, true);
        }

        synchronized (lock) {
            int a = leftWindowIndex;
            for (int i = a; i < window.size(); ++i) {
                if (isReceived.get(i)) {
                    leftWindowIndex++;
                    lock.notify();
                } else
                    break;
            }
        }
    }

    boolean isPacketReceived(int index) {
        if (isReceived.size() <= index) {
            return false;
        } else {
            return isReceived.get(index);
        }
    }

    String getFileName() {
        synchronized (lock) {
            while (!isFileNameReceived) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileName;
    }

    byte[] get() {
        byte[] buffer;
        synchronized (lock) {
            while (indexToWrite >= leftWindowIndex) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buffer = window.get(indexToWrite);
            window.set(indexToWrite, null);
            indexToWrite++;
        }
        return buffer;
    }
}


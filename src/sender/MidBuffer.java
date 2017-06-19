package sender;

import common.Channel;


public class MidBuffer implements Runnable {
    Channel midBuffer = new Channel();
    SlidingWindowController slidingWindowController;

    MidBuffer(SlidingWindowController slidingWindowController) {
        this.slidingWindowController = slidingWindowController;
    }
    //TODO Циклический буфер
    public void run() {
        boolean isRunning = true;
        while (isRunning) {
            if (slidingWindowController.isFree()) {
                slidingWindowController.push((byte[])midBuffer.getFirst());
                System.out.println("send");
            }
        }
    }

    public void addLast(byte[] bytes) {
        midBuffer.put(bytes);
    }
}

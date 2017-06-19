package sender;

import common.Channel;

import java.util.ArrayList;


class SlidingWindowController {
    private Channel channel;
    private final Object lock = new Object();
    private final Object lockTimer = new Object();
    private ArrayList<byte[]> window;
    private ArrayList<Integer> timer;
    private volatile boolean isFreeToPull = false;
    private final int timeout;
    private volatile int leftWindowIndex;
    private volatile int rightWindowIndex;
    private volatile int maxSize = 0;
    private volatile boolean isMaxSizeFixed = false;

    public boolean isFree() {
        return isFreeToPull;
    }

    //TODO промежуточный буффер
    SlidingWindowController(int capacity, Channel channel, int timeout) {
        this.channel = channel;
        window = new ArrayList<>();
        timer = new ArrayList<>();
        this.timeout = timeout;

        leftWindowIndex = 0;
        rightWindowIndex = capacity - 1;
        isFreeToPull = true;
    }

    void push(byte[] buffer) {
        while (!isFreeToPull) ;
        window.add(buffer);
        if (window.size() == rightWindowIndex + 1) {
            isFreeToPull = false;
        }
        timer.add(timeout);
        channel.put(buffer);
    }

    byte[] getBytes() {
        return (byte[]) channel.get();
    }
    //TODO Callback добавить
    //TODO
    private void doResend(int index) {
        byte[] buffer;
        buffer = window.get(index);
        if (buffer != null)
            channel.put(buffer);
    }

    void setReceived(int index) {
        synchronized (lock) {
            window.set(index, null);
        }

        synchronized (lockTimer) {
            timer.set(index, -1);
            int a = leftWindowIndex;
            int c = timer.size();
            for (int i = a; i < c; ++i) {
                if (timer.get(i) == -1) {
                    leftWindowIndex++;
                    if (!isMaxSizeFixed)
                        rightWindowIndex++;
                    else
                        rightWindowIndex = maxSize;
                    isFreeToPull = true;
                } else break;
            }
        }

        if (leftWindowIndex > rightWindowIndex) {
            System.exit(0);
        }

        synchronized (lock) {
            if (isFreeToPull)
                lock.notify();
        }
    }

    void setMaxSize(int size) {
        this.maxSize = size;
        this.isMaxSizeFixed = true;
    }

    void doTimerIteration() {
        synchronized (lockTimer) {
            int a = leftWindowIndex;
            int b = timer.size();
            for (int i = a; i < b; ++i) {
                int t = timer.get(i);
                if (t == -1)
                    continue;
                if (t == 1) {
                    timer.set(i, timeout);
                    doResend(i);
                } else {
                    timer.set(i, t - 1);
                }
            }
        }
    }
}

// TODO добавить callback в буфер и окно
// TODO сделать плавную остановку вместо System.exit()
// TODO сделать циклическую очередь и заменить в буфере и окне
// TODO вернуть синхронизацию в метод push
// TODO сделать норм таймер (дополнительно)

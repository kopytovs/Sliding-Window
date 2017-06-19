package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Sender implements Runnable {

    private DatagramSocket datagramSocket;
    private SlidingWindowController slidingWindowController;
    private final InetAddress inetAddress;
    private final int port;
    private boolean isRunning;

    Sender(SlidingWindowController slidingWindowController, DatagramSocket datagramSocket,
           InetAddress inetAddress, int port) {
        this.slidingWindowController = slidingWindowController;
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(new byte[1], 1, inetAddress, port);

        isRunning = true;
        while (isRunning) {
            byte[] buffer = slidingWindowController.getBytes();
            packet.setData(buffer);
            packet.setLength(buffer.length);
            try {
                datagramSocket.send(packet);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        datagramSocket.close();
    }

    void stop() {
        isRunning = false;
    }
}

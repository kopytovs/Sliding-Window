package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class Receiver implements Runnable{

    private final SlidingWindowController slidingWindowController;
    private volatile boolean isRunning = true;
    private DatagramSocket datagramSocket;

    Receiver(SlidingWindowController slidingWindowController, DatagramSocket datagramSocket){
        this.slidingWindowController = slidingWindowController;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4];
        DatagramPacket packet;
        while(isRunning){
            packet = new DatagramPacket(buffer, 4);
            try {
                datagramSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int index = java.nio.ByteBuffer.wrap(packet.getData()).getInt();
            slidingWindowController.setReceived(index);
            System.out.println(index);
        }
    }

    void stop(){
        isRunning = false;
        datagramSocket.close();
    }
}

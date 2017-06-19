package receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class ClientReceiver implements Runnable {

    private DatagramSocket datagramSocket;
    private int bufferSize;
    private boolean isRunning = false;
    private final ReceiverSlidingWindow receiverSlidingWindow;
    private final AcknowledgeSender acknowledgeSender;
    private boolean isAcknInitialized = false;

    ClientReceiver(ReceiverSlidingWindow receiverSlidingWindow, AcknowledgeSender acknowledgeSender,
                   DatagramSocket datagramSocket) {
        this.receiverSlidingWindow = receiverSlidingWindow;
        this.acknowledgeSender = acknowledgeSender;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        int count = 0;
        try {
            bufferSize = datagramSocket.getReceiveBufferSize();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        isRunning = true;
        byte[] byteIndex = new byte[4];
        byte[] datagramBuffer = new byte[bufferSize];
        DatagramPacket packet = new DatagramPacket(datagramBuffer, datagramBuffer.length);
        while (isRunning) {
            try {
                datagramSocket.receive(packet);
                count++;
                System.out.println(count);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!isAcknInitialized) {
                acknowledgeSender.init(packet.getAddress());
                isAcknInitialized = true;
            }

            System.arraycopy(datagramBuffer, 0, byteIndex, 0, 4);

            int index = java.nio.ByteBuffer.wrap(byteIndex).getInt();
            acknowledgeSender.push(index);

            if (!receiverSlidingWindow.isPacketReceived(index)) {
                int length = packet.getLength();
                byte[] buffer = new byte[length - 4];
                System.arraycopy(datagramBuffer, 4, buffer, 0, length - 4);
                receiverSlidingWindow.push(buffer, index);
            }
        }
        datagramSocket.close();
    }

    void stop() {
        isRunning = false;
    }
}

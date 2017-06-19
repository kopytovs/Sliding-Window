package receiver;

import java.net.DatagramSocket;
import java.net.SocketException;


public class UDPReceiver {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String filePath = args[1];
        int acknPort = Integer.parseInt(args[2]);

        DatagramSocket receiveSocket = null;
        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        DatagramSocket acknowledgeSocket = null;
        try {
            acknowledgeSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        ReceiverSlidingWindow receiverSlidingWindow = new ReceiverSlidingWindow();

        AcknowledgeSender acknowledgeSender = new AcknowledgeSender(acknowledgeSocket, acknPort);
        ClientReceiver clientReceiver = new ClientReceiver(receiverSlidingWindow, acknowledgeSender, receiveSocket);
        FileWriter fileWriter = new FileWriter(filePath, receiverSlidingWindow);

        //Thread acknowledgeSenderThread = new Thread(acknowledgeSender);
        Thread receiverThread = new Thread(clientReceiver);
        Thread fileWriterThread = new Thread(fileWriter);

        receiverThread.start();
        fileWriterThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Done");
            acknowledgeSender.stop();
            clientReceiver.stop();
        }));
    }
}


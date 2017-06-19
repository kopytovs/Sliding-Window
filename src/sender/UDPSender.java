package sender;

import common.Channel;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class UDPSender {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String address = args[1];
        int blockSize = Integer.parseInt(args[2]);
        int capacity = Integer.parseInt(args[3]);
        int timeout = Integer.parseInt(args[4]);
        String filePath = args[5];
        int receiverPort = Integer.parseInt(args[6]);

        InetAddress receiverAddress = null;
        try {
            receiverAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Channel channel = new Channel();

        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        DatagramSocket receiverSocket = null;
        try {
            receiverSocket = new DatagramSocket(receiverPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }


        SlidingWindowController slidingWindowController =
                new SlidingWindowController(capacity, channel, timeout);

        FileReader fileReader = new FileReader(filePath, blockSize, slidingWindowController);
        Sender sender = new Sender(slidingWindowController, datagramSocket, receiverAddress, port);
        Timer timer = new Timer(slidingWindowController);
        Receiver receiver = new Receiver(slidingWindowController, receiverSocket);

        Thread fileReaderThread = new Thread(fileReader);
        Thread senderThread = new Thread(sender);
        Thread timerThread = new Thread(timer);
        Thread receiverThread = new Thread(receiver);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sender.stop();
            timer.stop();
            receiver.stop();
        }));

        fileReaderThread.start();
        senderThread.start();
        timerThread.start();
        receiverThread.start();


    }
}


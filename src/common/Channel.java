package common;

import java.util.LinkedList;


public class Channel {

    private final LinkedList queue = new LinkedList();
    private final Object lock = new Object();
    private int maxQueueSize = 20000;

    public Channel(){
    }

    /**
     *
     * @param object cannot be null
     */
    public void put(Object object){
        synchronized (lock) {
            if(object == null)
                throw new IllegalArgumentException("Null in channel");
            while(queue.size() > maxQueueSize){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(object);
            lock.notify();
        }
    }

    public Object get(){
        synchronized (lock){
            while(queue.isEmpty()){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }lock.notify();
            return queue.removeLast();
        }
    }
    public Object getFirst(){
        synchronized (lock){
            while(queue.isEmpty()){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }lock.notify();
            return queue.removeFirst();
        }
    }
}


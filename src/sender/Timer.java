package sender;


public class Timer implements Runnable{

    private SlidingWindowController slidingWindowController;
    private boolean isRunning = true;

    Timer(SlidingWindowController slidingWindowController){
        this.slidingWindowController = slidingWindowController;
    }
    @Override
    public void run() {
        while(isRunning){
            slidingWindowController.doTimerIteration();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void stop(){
        isRunning = false;
    }
}


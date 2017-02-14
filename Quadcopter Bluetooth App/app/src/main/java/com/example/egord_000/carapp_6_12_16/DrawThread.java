package com.example.egord_000.carapp_6_12_16;
import android.graphics.Canvas;

/**
 * Created by Ethan on 9/24/2016.
 */
public class DrawThread extends Thread {
    CustomSurfaceView v;
    public boolean running = false;

    public DrawThread(CustomSurfaceView v) {
        this.v = v;
    }
    public void setRunning(boolean run) {
        running = run;
    }

    @Override
    public void run() {
        while(running){
            Canvas canvas = v.getHolder().lockCanvas();
            if(canvas != null) {
                synchronized (v.getHolder()) {
                    v.paint(canvas);
                }
            }
            v.getHolder().unlockCanvasAndPost(canvas);
        }

        try {
            sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

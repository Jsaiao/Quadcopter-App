package com.example.egord_000.carapp_6_12_16;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.text.DecimalFormat;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private DrawThread drawThread;
    private SurfaceHolder holder;
    boolean isItOK = true;

    private float x1, y1, x2, y2, centerX1, centerX2, centerX, centerY;
    private double trigA = 0;
    Circle joy1, joy2;
    private boolean noTouchYet = true;

    Paint blue = new Paint();
    Paint darkBlue = new Paint();
    Paint gray = new Paint();
    Paint black = new Paint();
    Paint white = new Paint();

    private BluetoothSocket btSocket = null;

    public float getY1(){return y1;}



    public CustomSurfaceView(Context context){
        super(context);
        init();
    }
    public CustomSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }
    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public void init() {
        setOnTouchListener(this);

        drawThread = new DrawThread(this);
        holder = getHolder();
        blue.setARGB(255, 0, 0, 255);
        darkBlue.setARGB(255, 0, 0, 175);
        darkBlue.setStyle(Paint.Style.FILL);
        darkBlue.setTextSize(80);
        gray.setColor(Color.GRAY);
        black.setColor(Color.BLACK);
        black.setTextSize(500);
        white.setColor(Color.WHITE);
        white.setTextSize(500);

        holder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                drawThread.setRunning(true);
                drawThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                drawThread.setRunning(false);
                while (retry) {
                    try {
                        drawThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }});
    }
    private int ctr = 0;
    private final int loadT = 50;
    public void paint(Canvas c){
        //holder.lockCanvas();
        if(isItOK) {
            if (!holder.getSurface().isValid()) {
                holder.unlockCanvasAndPost(c);
                return;
            }
            c.drawARGB(255, 255, 255, 255);

            centerX1 = getWidth() / 4 - 1;
            centerX2 = (3 * getWidth()) / 4 - 1;
            centerX = getHeight() - 1;
            centerY = getHeight() / 2 - 1 + 100;

            if (noTouchYet) {
                x1 = centerX1;
                y1 = centerY;

                x2 = centerX2;
                y2 = centerY;

                joy1 = new Circle(centerX1, centerY, 410);
                joy2 = new Circle(centerX2, centerY, 410);
            }
            c.drawCircle(centerX1, centerY, 410, black);
            c.drawCircle(centerX1, centerY, 400, gray);

            c.drawCircle(centerX2, centerY, 410, black);
            c.drawCircle(centerX2, centerY, 400, gray);

            c.drawCircle(x1, y1, 210, darkBlue);
            c.drawCircle(x1, y1, 200, blue);

            c.drawCircle(x2, y2, 210, darkBlue);
            c.drawCircle(x2, y2, 200, blue);

            DecimalFormat df = new DecimalFormat("#.00");
            c.drawText("angle:    " + df.format(trigA), 1120, 400, darkBlue);
            c.drawText("x1:    " + df.format(x1), 150, 300, darkBlue);
            c.drawText("y1:    " + df.format(y1), 150, 400, darkBlue);
            c.drawText("x2:    " + df.format(x2), 2000, 300, darkBlue);
            c.drawText("y2:    " + df.format(y2), 2000, 400, darkBlue);

            if(ctr < loadT)
            {
                c.drawText("LOADING...", 100, 1010, black);
                c.drawText("LOADING...", 100, 990, black);
                c.drawText("LOADING...", 110, 1000, black);
                c.drawText("LOADING...", 90, 1000, black);
                c.drawText("LOADING...", 100, 1000, white);
            }
        }

        btSocket = QuadControl.getBTSocket();
        String pitch = "";
        String roll = "";
        String yaw = "";
        String throttle = "";
        //pitch
        pitch += "p";
        pitch += Integer.toString((int)map((y2), (centerY+joy2.getR()), (centerY-joy2.getR()), -150, 150));
        pitch += "P";
        //roll
        roll += "r";
        roll += Integer.toString((int)map((x2), (centerX2-joy2.getR()), (centerX2+joy2.getR()), -150, 150));
        roll += "R";
        //yaw
        yaw += "y";
        yaw += Integer.toString((int)map((x1), (centerX1-joy1.getR()), (centerX1+joy1.getR()), -150, 150));
        yaw += "Y";
        //throttle
        throttle += "t";
        throttle += Integer.toString((int)map((y1), (centerY+joy1.getR()), (centerY-joy1.getR()), 0, 100)); //1000, 2000
        throttle += "T";
/*
        int t = (int)map((y1), (centerY+joy1.getR()), (centerY-joy1.getR()), 1000, 2000);
        if(t > 2000) t = 2000;
        if(t < 700) t = 700;
        ByteBuffer b = ByteBuffer.allocate(11);
        b.putInt(t);*/
        if (btSocket!=null && ctr > loadT)
        {
            try
            {
                //btSocket.getOutputStream().write(pitch.getBytes());
                //btSocket.getOutputStream().write(roll.getBytes());
                //btSocket.getOutputStream().write(yaw.getBytes());
                btSocket.getOutputStream().write("t70T".getBytes());
            }
            catch (IOException e)
            {
            }
        }
        ctr++;
        holder.unlockCanvasAndPost(c);
    }/*
    public void pause(){
        isItOK = false;
        while(true){
            try{
                drawThread.join();
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
            break;
        }
        drawThread = null;
    }
    public void resume(){
        isItOK = true;
        drawThread = new DrawThread(this);
        drawThread.start();
    }*/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }







    @Override
    public boolean onTouch(View view, MotionEvent me){
        try {
            Thread.sleep(5);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        if(noTouchYet) noTouchYet = false;

        double trigX;
        double trigY;

        switch(me.getAction() & me.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                trigY = me.getY() - centerY;
                if (me.getX() < centerX) {
                    trigX = me.getX() - centerX1;
                    trigA = Math.atan(trigY / trigX);
                    if(trigX < 0 && trigY > 0) trigA += Math.PI;
                    else if(trigX < 0 && trigY < 0) trigA -= Math.PI;

                    if(checkCollision(joy1, new Point(me.getX(), me.getY()))) {
                        x1 = me.getX();
                        y1 = me.getY();
                    } else {
                        x1 = centerX1 + (float)(Math.cos(trigA) * joy1.getR());
                        y1 = centerY + (float) (Math.sin(trigA) * joy1.getR());
                    }
                } else if(me.getX() >= centerX) {
                    trigX = me.getX() - centerX2;
                    trigA = Math.atan(trigY / trigX);
                    if(trigX < 0 && trigY > 0) trigA += Math.PI;
                    else if(trigX < 0 && trigY < 0) trigA -= Math.PI;

                    if(checkCollision(joy2, new Point(me.getX(), me.getY()))) {
                        x2 = me.getX();
                        y2 = me.getY();
                    } else {
                        x2 = centerX2 + (float) (Math.cos(trigA) * joy1.getR());
                        y2 = centerY + (float)(Math.sin(trigA) * joy1.getR());
                    }
                }
                if (me.getPointerCount() > 1) {
                    trigY = me.getY(1) - centerY;
                    if (me.getX(1) < centerX) {
                        trigX = me.getX(1) - centerX1;
                        trigA = Math.atan(trigY / trigX);
                        if(trigX < 0 && trigY > 0) trigA += Math.PI;
                        else if(trigX < 0 && trigY < 0) trigA -= Math.PI;

                        if (checkCollision(joy1, new Point(me.getX(1), me.getY(1)))) {
                            x1 = me.getX(1);
                            y1 = me.getY(1);
                        } else {
                            x1 = centerX1 + (float)(Math.cos(trigA) * joy1.getR());
                            y1 = centerY + (float)(Math.sin(trigA) * joy1.getR());
                        }
                    } else if (me.getX(1) >= centerX){
                        trigX = me.getX(1) - centerX2;
                        trigA = Math.atan(trigY / trigX);
                        if(trigX < 0 && trigY > 0) trigA += Math.PI;
                        else if(trigX < 0 && trigY < 0) trigA -= Math.PI;

                        if(checkCollision(joy2, new Point(me.getX(1), me.getY(1)))) {
                            x2 = me.getX(1);
                            y2 = me.getY(1);
                        } else {
                            x2 = centerX2 + (float)(Math.cos(trigA) * joy1.getR());
                            y2 = centerY + (float)(Math.sin(trigA) * joy1.getR());
                        }
                    }
                }
                break;
        }


        return true;
    }
    private float map(float x, float in_min, float in_max, float out_min, float out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
    public boolean checkCollision(Circle circ, Point point)
    {
        float d = (float)Math.sqrt(Math.pow((point.getX() - circ.getX()), 2) + Math.pow((point.getY() - circ.getY()), 2));
        return d <= circ.getR();
    }
}
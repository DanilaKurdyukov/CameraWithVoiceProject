package com.example.camerawithvoiceapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

import java.util.LinkedList;

public class WaveSoundView extends SurfaceView {

    private static final int HISTORY_SIZE = 6;

    private static final float MAX_AMPLITUDE_TO_DRAW = 16384.0f;

    private LinkedList<short[]> mAudioData = new LinkedList<>();

    private Paint mPaint = new Paint();


    public WaveSoundView(Context context) {
        super(context);
    }

    public WaveSoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaveSoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAudioData = new LinkedList<>();
        mPaint = new Paint();
    }

    public synchronized void updateAudioData(short buffer[]){
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(0);
        mPaint.setAntiAlias(true);
        short newBuffer[];
        if (mAudioData.size()==HISTORY_SIZE){
            newBuffer = mAudioData.removeFirst();
            System.arraycopy(buffer,0,newBuffer,0,buffer.length);
        }
        else{
            newBuffer = buffer.clone();
        }
        mAudioData.addLast(newBuffer);

        Canvas canvas = getHolder().lockCanvas();
        if (canvas!=null){
            drawWaveForm(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void drawWaveForm(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        float width = getWidth();
        float height = getHeight();
        float centerY = height/2;

        int colorDelta = 255/(HISTORY_SIZE+1);
        int brightness = colorDelta;

        for (short[] buffer:mAudioData){
            mPaint.setColor(Color.argb(brightness,128,255,192));

            float lastX = -1;
            float lastY = -1;
            for (int i = 0;i<width;i++){
                int index = (int)((i/width)*buffer.length);
                short sample = buffer[index];
                float y = (sample/MAX_AMPLITUDE_TO_DRAW)*centerY+centerY;
                if (lastX!=-1){
                    canvas.drawLine(lastX,lastY,i,y,mPaint);
                }

                lastX=i;
                lastY=y;
            }
            brightness+=colorDelta;
        }
    }

}

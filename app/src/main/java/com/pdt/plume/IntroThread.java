package com.pdt.plume;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class IntroThread extends Thread {

    private SurfaceHolder _surfaceHolder;
    private IntroSurface _panel;
    private boolean _run = false;


    public IntroThread(SurfaceHolder surfaceHolder, IntroSurface panel) {
        _surfaceHolder = surfaceHolder;
        _panel = panel;
    }


    public void setRunning(boolean run) { //Allow us to stop the thread
        _run = run;
    }


    @Override
    public void run() {
        Canvas c;
        while (_run) {     //When setRunning(false) occurs, _run is
            c = null;      //set to false and loop ends, stopping thread


            try {
                c = _surfaceHolder.lockCanvas(null);
                synchronized (_surfaceHolder) {

                    //Insert methods to modify positions of items in onDraw()
                    _panel.postInvalidate();
                }
            } finally {
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }

        }
    }
}

package com.gps.capstone.traceroute;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.Utils.SensorUtil;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by saryana on 5/28/15.
 */
public class CustomView extends SurfaceView {
    Paint p;
    Paint p2;
    Context c;
    float f;
    public CustomView(Context context) {
        super(context);
        p = new Paint();
        p2 = new Paint();
        c = context;
        p.setColor(Color.BLUE);
        p.setStyle(Style.FILL);
        p2.setColor(Color.YELLOW);
        p.setStyle(Style.STROKE);
        setWillNotDraw(false);
        BusProvider.getInstance().register(this);
        f = 0f;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Toast.makeText(c, "Closing custom view", Toast.LENGTH_SHORT).show();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onData(NewDataEvent d) {
        if (d.type == EventType.DIRECTION_CHANGE) {
            f = d.values[0];
            if (f < 0) {
                f = (float) (Math.PI +(Math.PI + f));
            }
//            f = Math.round(SensorUtil.radianToDegree(f));
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2;
        float cy = getHeight() / 2;
        float radius = getHeight() / 2;

        float x = (float) (cx + radius * Math.cos(f));
        float y = (float) (cy + radius * Math.sin(f));
        canvas.drawCircle(cx, cy, radius, p2);
        canvas.drawLine(cx, cy, x, y, p);
    }
}

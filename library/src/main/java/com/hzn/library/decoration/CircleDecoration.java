package com.hzn.library.decoration;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.hzn.library.table.CellInfo;

import java.util.List;

/**
 * Show some circles
 * <br/>
 * Created by huzn on 2017/9/27.
 */

public class CircleDecoration implements EasyDecoration {

    private int bgColor;
    private int strokeColor;
    private int strokeWidth;
    private List<CircleDecorationInfo> decorationInfoList;

    private Paint bgPaint;
    private Paint strokePaint;

    public CircleDecoration(int bgColor, int strokeColor, int strokeWidth, List<CircleDecorationInfo> decorationInfoList) {
        this.bgColor = bgColor;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.decorationInfoList = decorationInfoList;
        init();
    }

    public CircleDecoration(List<CircleDecorationInfo> decorationInfoList) {
        this(Color.LTGRAY, Color.GRAY, 20, decorationInfoList);
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bgPaint.setColor(bgColor);

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokePaint.setColor(strokeColor);
        strokePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void draw(Canvas canvas) {
        if (null == decorationInfoList || decorationInfoList.size() < 0)
            return;
        for (CircleDecorationInfo info : decorationInfoList) {
            if (info.radius <= 0 || null == info.cellInfo)
                continue;
            float cx = info.cellInfo.getStartX() + info.cellInfo.width / 2.0f;
            float cy = info.cellInfo.getStartY() + info.cellInfo.height / 2.0f;
            canvas.drawCircle(cx, cy, info.radius, bgPaint);
            canvas.drawCircle(cx, cy, info.radius, strokePaint);
        }
    }

    public static class CircleDecorationInfo {
        public float radius;
        public CellInfo cellInfo;
    }
}

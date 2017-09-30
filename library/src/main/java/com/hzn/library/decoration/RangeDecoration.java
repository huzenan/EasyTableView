package com.hzn.library.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.hzn.library.table.CellInfo;
import com.hzn.library.table.EasyTableView;

import java.util.ArrayList;

/**
 * Show range with half circle on start and end
 * <br/>
 * Created by huzn on 2017/9/28.
 */

public class RangeDecoration implements EasyDecoration {

    private int start = 0;
    private int end = 0;
    private float radius = 0;
    private int bgColor = 0;
    private ArrayList<CellInfo> cellInfos;

    private Paint bgPaint;
    private Path startPath;
    private Path endPath;

    public RangeDecoration(int start, int end, float radius, int bgColor, ArrayList<CellInfo> cellInfos) {
        this.start = start;
        this.end = end;
        this.radius = radius;
        this.bgColor = bgColor;
        this.cellInfos = cellInfos;
        init();
    }

    public RangeDecoration(float radius, int bgColor, ArrayList<CellInfo> cellInfos) {
        this(-1, -1, radius, bgColor, cellInfos);
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        bgPaint.setAntiAlias(true);
        startPath = new Path();
        endPath = new Path();
    }

    private void initStartPath() {
        if (isIllegal())
            return;

        CellInfo cellInfo = cellInfos.get(start);
        float cx = cellInfo.getStartX() + cellInfo.width / 2.0f;
        float cy = cellInfo.getStartY() + cellInfo.height / 2.0f;
        RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        startPath.reset();
        startPath.moveTo(cx, cy - radius);
        startPath.arcTo(rectF, -90.0f, -180.0f);
        startPath.lineTo(cellInfo.getStartX() + cellInfo.width, cy + radius);
        startPath.lineTo(cellInfo.getStartX() + cellInfo.width, cy - radius);
        startPath.close();
    }

    private void initEndPath() {
        if (isIllegal())
            return;

        CellInfo cellInfo = cellInfos.get(end - 1);
        float cx = cellInfo.getStartX() + cellInfo.width / 2.0f;
        float cy = cellInfo.getStartY() + cellInfo.height / 2.0f;
        RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        endPath.reset();
        endPath.moveTo(cx, cy - radius);
        endPath.arcTo(rectF, -90.0f, 180.0f);
        endPath.lineTo(cellInfo.getStartX(), cy + radius);
        endPath.lineTo(cellInfo.getStartX(), cy - radius);
        endPath.close();
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public void setRangeTextColor(EasyTableView table, int textColor) {
        if (isIllegal() || null == table)
            return;

        CellInfo cellInfo = null;
        for (int i = start; i < end; i++) {
            cellInfo = cellInfos.get(i);
            cellInfo.textColor = textColor;
        }
        table.updateData(cellInfos);
    }

    @Override
    public void draw(Canvas canvas) {
        if (isIllegal())
            return;

        if (start == end - 1) {
            // circle
            CellInfo cellInfo = cellInfos.get(start);
            if (null == cellInfo)
                return;
            float cx = cellInfo.getStartX() + cellInfo.width / 2.0f;
            float cy = cellInfo.getStartY() + cellInfo.height / 2.0f;
            canvas.drawCircle(cx, cy, radius, bgPaint);
        } else {
            CellInfo cellInfo = null;
            float cy = 0.0f;
            for (int i = start; i < end; i++) {
                cellInfo = cellInfos.get(i);
                if (i == start) {
                    initStartPath();
                    canvas.drawPath(startPath, bgPaint);
                } else if (i == end - 1) {
                    initEndPath();
                    canvas.drawPath(endPath, bgPaint);
                } else {
                    cy = cellInfo.getStartY() + cellInfo.height / 2.0f;
                    canvas.drawRect(cellInfo.getStartX(),
                            cy - radius,
                            cellInfo.getStartX() + cellInfo.width,
                            cy + radius,
                            bgPaint);
                }
            }
        }
    }

    private boolean isIllegal() {
        return start < 0 || end < 0 || start >= end || null == cellInfos || end > cellInfos.size();
    }
}

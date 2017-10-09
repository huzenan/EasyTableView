package com.hzn.easytableview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.hzn.library.decoration.EasyDecoration;
import com.hzn.library.table.CellInfo;

/**
 * Check mark decoration
 * <br/>
 * Created by huzn on 2017/10/9.
 */

public class CalendarCheckDecoration implements EasyDecoration {

    private CellInfo curCheckedCell;

    private Paint paint;
    private Path path;

    public CalendarCheckDecoration() {
        this(10, Color.GREEN);
    }

    public CalendarCheckDecoration(float strokeWidth, int color) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        path = new Path();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!path.isEmpty())
            canvas.drawPath(path, paint);
    }

    public void setCheckedCell(CellInfo checkedCell) {
        path.reset();

        if (null == checkedCell)
            return;

        if (isCellEqualed(checkedCell))
            return;

        curCheckedCell = checkedCell;

        path.moveTo(checkedCell.getStartX() + checkedCell.width / 4.0f,
                checkedCell.getStartY() + 2.0f * checkedCell.height / 3.0f);
        path.lineTo(checkedCell.getStartX() + checkedCell.width / 2.0f,
                checkedCell.getStartY() + 4.0f * checkedCell.height / 5.0f);
        path.lineTo(checkedCell.getStartX() + 3.0f * checkedCell.width / 4.0f,
                checkedCell.getStartY() + checkedCell.height / 5.0f);
    }

    private boolean isCellEqualed(CellInfo checkedCell) {
        return null != curCheckedCell && null != checkedCell &&
                curCheckedCell.row == checkedCell.row &&
                curCheckedCell.line == checkedCell.line;
    }

}

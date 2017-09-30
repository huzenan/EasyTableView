package com.hzn.library.decoration;

import android.graphics.Canvas;

/**
 * Decoration interface of {@link com.hzn.library.table.EasyTableView}，
 * implement method {@link EasyDecoration#draw(Canvas)} and draw your own decorations.
 * For now we have several default decorations, see {@link CircleDecoration}, {@link RangeDecoration}
 * <br/>
 * Created by huzn on 2017/9/27.
 */

public interface EasyDecoration {

    /**
     * draw decorations，will be invoke during the drawing process of
     * {@link com.hzn.library.table.EasyTableView}
     *
     * @param canvas Canvas
     */
    void draw(Canvas canvas);

}

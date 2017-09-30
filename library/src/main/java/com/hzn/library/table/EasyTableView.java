package com.hzn.library.table;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.hzn.library.R;
import com.hzn.library.decoration.EasyDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Table View, with Horizontal and Vertical HEADER, each cell supports multiline text,
 * each text line supports different colors and sizes. Cells can be merged or unmerged.
 * <br/>
 * Created by huzn on 2017/1/12.
 */
public class EasyTableView extends View {

    // total rows, default 0
    private int rows;
    // total lines, default 0
    private int lines;
    // background color, default Color.WHITE
    private int bgColor;
    // VH Header color, will be drawn at least 2x2, default Color.LTGRAY
    private int headerHVColor;
    // Horizontal Header color, will be drawn at least 2x2, default Color.LTGRAY
    private int headerHColor;
    // Vertical Header color, will be drawn at least 2x2, default Color.LTGRAY
    private int headerVColor;
    // stroke color, default Color.GRAY
    private int strokeColor;
    // stroke size, default 1dp
    private int strokeSize;
    // outer stroke color, default Color.GRAY
    private int outStrokeColor;
    // outer stroke size, default 1dp
    private int outStrokeSize;
    // table corner radius, default 5dp
    private float outStrokeCorner;
    // table mode，having MODE_NORMAL, MODE_FIX_WIDTH, MODE_FIX_HEIGHT
    // and MODE_FIX_WIDTH_HEIGHT, default MODE_NORMAL
    private int mode;

    /**
     * normal mode, WIDTH and HEIGHT of table are both auto fit
     */
    public static final int MODE_NORMAL = 0;
    /**
     * fix width, width of each cell is averaged, HEIGHT of table is auto fit
     */
    public static final int MODE_FIX_WIDTH = 1;
    /**
     * fix height, height of each cell is averaged, WIDTH of table is auto fit
     */
    public static final int MODE_FIX_HEIGHT = 2;
    /**
     * fix width and height, both width and height of each cell is averaged
     */
    public static final int MODE_FIX_WIDTH_HEIGHT = 3;

    /**
     * add some rows above a row
     */
    public static final int ADD_ROWS_TOP = 0;
    /**
     * add some rows below a row
     */
    public static final int ADD_ROWS_BOTTOM = 1;
    /**
     * add some lines on left of a line
     */
    public static final int ADD_LINES_LEFT = 2;
    /**
     * add some lines on right of a line
     */
    public static final int ADD_LINES_RIGHT = 3;
    /**
     * default height of new rows
     */
    public static final float ADD_ROWS_DEFAULT_HEIGHT = 20.0f;
    /**
     * default width of new lines
     */
    public static final float ADD_LINES_DEFAULT_WIDTH = 20.0f;

    private Paint paint;
    private Paint strokePaint;
    private TextPaint textPaint;
    private RectF bgRectF;
    private Path tPath;
    private RectF tRectF;
    private RectF tCornerRectF;

    // data list of merged cells
    private ArrayList<MergeInfo> mergeInfoList;
    // width list of cells
    private float[] widthArr;
    // height list of cells
    private float[] heightArr;
    // data list of cells
    private CellInfo[][] cellArr;
    // bottom decoration, drawing above cells background, and below cells texts
    private EasyDecoration bottomDecoration;
    // top decoration, drawing above everything
    private EasyDecoration topDecoration;

    private Object curTouchCell;
    private float downX;
    private float downY;
    private int touchSlop;


    public EasyTableView(Context context) {
        this(context, null);
    }

    public EasyTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasyTableView, defStyleAttr, 0);
        rows = a.getInteger(R.styleable.EasyTableView_etvRows, 0);
        lines = a.getInteger(R.styleable.EasyTableView_etvLines, 0);
        bgColor = a.getColor(R.styleable.EasyTableView_etvBgColor, Color.WHITE);
        headerHVColor = a.getColor(R.styleable.EasyTableView_etvHeaderHVColor, Color.LTGRAY);
        headerHColor = a.getColor(R.styleable.EasyTableView_etvHeaderHColor, Color.LTGRAY);
        headerVColor = a.getColor(R.styleable.EasyTableView_etvHeaderVColor, Color.LTGRAY);
        strokeColor = a.getColor(R.styleable.EasyTableView_etvStrokeColor, Color.GRAY);
        strokeSize = a.getDimensionPixelSize(R.styleable.EasyTableView_etvStrokeSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        outStrokeColor = a.getColor(R.styleable.EasyTableView_etvOutStrokeColor, Color.GRAY);
        outStrokeSize = a.getDimensionPixelSize(R.styleable.EasyTableView_etvOutStrokeSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        outStrokeCorner = a.getDimensionPixelSize(R.styleable.EasyTableView_etvOutStrokeCorner, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        mode = a.getInteger(R.styleable.EasyTableView_etvMode, MODE_NORMAL);
        a.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);

        bgRectF = new RectF();
        tPath = new Path();
        tRectF = new RectF();
        tCornerRectF = new RectF();

        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        touchSlop = viewConfiguration.getScaledTouchSlop();

        resetTableData();
    }

    // reset data list of the table
    private void resetTableData() {
        mergeInfoList = new ArrayList<>();
        widthArr = new float[lines];
        heightArr = new float[rows];

        // 初始化单元格
        if (rows != 0 && lines != 0) {
            cellArr = new CellInfo[rows][lines];
            for (int r = 0; r < rows; r++)
                for (int l = 0; l < lines; l++)
                    cellArr[r][l] = new CellInfo();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (measureMode != MeasureSpec.EXACTLY) { // wrap_content
            // sum of each line's width
            width = 0;
            for (int l = 0; l < lines; l++)
                width = (int) (width + widthArr[l]);
            width = width + outStrokeSize + getPaddingLeft() + getPaddingRight();
        }

        measureMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (measureMode != MeasureSpec.EXACTLY) { // wrap_content
            // sum of each line's height
            height = 0;
            for (int r = 0; r < rows; r++)
                height = (int) (height + heightArr[r]);
            height = height + outStrokeSize + getPaddingTop() + getPaddingBottom();
        }

        bgRectF.left = 0.0f + getPaddingLeft() + outStrokeSize / 2.0f;
        bgRectF.top = 0.0f + getPaddingTop() + outStrokeSize / 2.0f;
        bgRectF.right = width - getPaddingRight() - outStrokeSize / 2.0f;
        bgRectF.bottom = height - getPaddingBottom() - outStrokeSize / 2.0f;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // init data such as coordinate
        initData();
        // draw background
        drawBg(canvas);
        // draw VH header
        drawHeaderVH(canvas);
        // draw horizontal header
        drawHeaderH(canvas);
        // draw vertical header
        drawHeaderV(canvas);
        // draw cells background
        drawCellsInfoBg(canvas);
        // draw bottom decorations
        drawBottomDecorations(canvas);
        // draw cells info(texts for now)
        drawCellsInfo(canvas);
        // draw strokes while strokeSize>0
        drawStrokes(canvas);
        // draw merged cells
        drawMergedCells(canvas);
        // draw outer stroke while outStrokeSize>0
        drawOutStroke(canvas);
        // draw top decorations, above everything
        drawTopDecorations(canvas);
    }

    // init data such as coordinate
    private void initData() {
        // calculate width and height of each line and row in normal mode
        if (mode == MODE_FIX_WIDTH || mode == MODE_FIX_WIDTH_HEIGHT) {
            float fixWidth = 1.0f * (bgRectF.right - bgRectF.left) / lines;
            fixWidth = fixWidth >= outStrokeCorner ? fixWidth : outStrokeCorner;
            for (int l = 0; l < lines; l++)
                widthArr[l] = fixWidth;
        }
        if (mode == MODE_FIX_HEIGHT || mode == MODE_FIX_WIDTH_HEIGHT) {
            float fixHeight = 1.0f * (bgRectF.bottom - bgRectF.top) / rows;
            fixHeight = fixHeight >= outStrokeCorner ? fixHeight : outStrokeCorner;
            for (int r = 0; r < rows; r++)
                heightArr[r] = fixHeight;
        }

        // init cells x, y coordinate, set row and line,
        // set width and height of each cell to max value
        float startY = bgRectF.top;
        for (int r = 0; r < rows; r++) {
            float startX = bgRectF.left;
            for (int l = 0; l < lines; l++) {
                cellArr[r][l].row = r;
                cellArr[r][l].line = l;
                cellArr[r][l].startX = startX;
                cellArr[r][l].startY = startY;
                cellArr[r][l].width = widthArr[l];
                cellArr[r][l].height = heightArr[r];
                startX += widthArr[l];
            }
            startY += heightArr[r];
        }

        // init merged cells startX, startY, width and height
        int mergeInfoSize = mergeInfoList.size();
        for (int i = 0; i < mergeInfoSize; i++) {
            MergeInfo mergeInfo = mergeInfoList.get(i);
            mergeInfo.startX = cellArr[mergeInfo.startRow][mergeInfo.startLine].startX;
            mergeInfo.startY = cellArr[mergeInfo.startRow][mergeInfo.startLine].startY;
            float width = 0.0f;
            float height = 0.0f;
            for (int r = mergeInfo.startRow; r <= mergeInfo.endRow; r++) {
                width = 0.0f;
                for (int l = mergeInfo.startLine; l <= mergeInfo.endLine; l++)
                    width += cellArr[r][l].width; // just for convenience

                height += cellArr[r][0].height;
            }
            mergeInfo.width = width;
            mergeInfo.height = height;
        }
    }

    // draw background
    private void drawBg(Canvas canvas) {
        paint.setColor(bgColor);
        canvas.drawRoundRect(bgRectF, outStrokeCorner, outStrokeCorner, paint);
    }

    // draw VH header
    private void drawHeaderVH(Canvas canvas) {
        // draw at least 2x2
        if (cellArr.length > 1 && cellArr[0].length > 1) {
            float twiceCorner = outStrokeCorner * 2;
            paint.setColor(headerHVColor);
            tRectF.left = bgRectF.left;
            tRectF.top = bgRectF.top;
            tRectF.right = bgRectF.left + twiceCorner;
            tRectF.bottom = bgRectF.top + twiceCorner;
            tPath.reset();
            tPath.moveTo(bgRectF.left + widthArr[0], bgRectF.top);
            tPath.lineTo(bgRectF.left + outStrokeCorner, bgRectF.top);
            tPath.arcTo(tRectF, -90.0f, -90.0f);
            tPath.lineTo(bgRectF.left, bgRectF.top + heightArr[0]);
            tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top + heightArr[0]);
            tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top);
            tPath.close();
            canvas.drawPath(tPath, paint);
        }
    }

    // draw horizontal header
    private void drawHeaderH(Canvas canvas) {
        // draw at least 2x2
        if (cellArr.length > 1 && cellArr[0].length > 1) {
            float twiceCorner = outStrokeCorner * 2;
            paint.setColor(headerHColor);
            tRectF.left = bgRectF.right - twiceCorner;
            tRectF.top = bgRectF.top;
            tRectF.right = bgRectF.right;
            tRectF.bottom = bgRectF.top + twiceCorner;
            tPath.reset();
            tPath.moveTo(bgRectF.left + widthArr[0], bgRectF.top);
            tPath.lineTo(bgRectF.right - outStrokeCorner, bgRectF.top);
            tPath.arcTo(tRectF, -90.0f, 90.0f);
            tPath.lineTo(bgRectF.right, bgRectF.top + heightArr[0]);
            tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top + heightArr[0]);
            tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top);
            tPath.close();
            canvas.drawPath(tPath, paint);
        }
    }

    // draw vertical header
    private void drawHeaderV(Canvas canvas) {
        // draw at least 2x2
        if (cellArr.length > 1 && cellArr[0].length > 1) {
            float twiceCorner = outStrokeCorner * 2;
            paint.setColor(headerVColor);
            tRectF.left = bgRectF.left;
            tRectF.top = bgRectF.bottom - twiceCorner;
            tRectF.right = bgRectF.left + twiceCorner;
            tRectF.bottom = bgRectF.bottom;
            tPath.reset();
            tPath.moveTo(bgRectF.left, bgRectF.top + heightArr[0]);
            tPath.lineTo(bgRectF.left, bgRectF.bottom - outStrokeCorner);
            tPath.arcTo(tRectF, 180.0f, -90.0f);
            tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.bottom);
            tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top + heightArr[0]);
            tPath.lineTo(bgRectF.left, bgRectF.top + heightArr[0]);
            tPath.close();
            canvas.drawPath(tPath, paint);
        }
    }

    // draw background of cells
    private void drawCellsInfoBg(Canvas canvas) {
        for (int r = 0; r < rows; r++) {
            for (int l = 0; l < lines; l++) {
                if (cellArr[r][l].type != CellInfo.TYPE_NONE) {
                    if (cellArr[r][l].bgColor != 0 && cellArr[r][l].width > 0 && cellArr[r][l].height > 0) {
                        tRectF.left = cellArr[r][l].startX;
                        tRectF.top = cellArr[r][l].startY;
                        tRectF.right = cellArr[r][l].startX + cellArr[r][l].width;
                        tRectF.bottom = cellArr[r][l].startY + cellArr[r][l].height;
                        this.paint.setColor(cellArr[r][l].bgColor);

                        tPath.reset();

                        tPath.moveTo(tRectF.left - outStrokeCorner, tRectF.top);

                        if (r == 0 && l == 0) {
                            addLeftTopCornerPath();
                        } else {
                            tPath.lineTo(tRectF.left, tRectF.top);
                            tPath.lineTo(tRectF.left, tRectF.top + outStrokeCorner);
                        }

                        tPath.lineTo(tRectF.left, tRectF.bottom - outStrokeCorner);

                        if (r == rows - 1 && l == 0) {
                            addLeftBottomCornerPath();
                        } else {
                            tPath.lineTo(tRectF.left, tRectF.bottom);
                            tPath.lineTo(tRectF.left + outStrokeCorner, tRectF.bottom);
                        }

                        tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.bottom);

                        if (r == rows - 1 && l == lines - 1) {
                            addRightBottomCornerPath();
                        } else {
                            tPath.lineTo(tRectF.right, tRectF.bottom);
                            tPath.lineTo(tRectF.right, tRectF.bottom - outStrokeCorner);
                        }

                        tPath.lineTo(tRectF.right, tRectF.top + outStrokeCorner);

                        if (r == 0 && l == lines - 1) {
                            addRightTopCornerPath();
                        } else {
                            tPath.lineTo(tRectF.right, tRectF.top);
                            tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.top);
                        }

                        tPath.lineTo(tRectF.left - outStrokeCorner, tRectF.top);

                        tPath.close();

                        canvas.drawPath(tPath, paint);
                    }
                }
            }
        }
    }

    // draw bottom decorations
    private void drawBottomDecorations(Canvas canvas) {
        if (null == bottomDecoration)
            return;
        bottomDecoration.draw(canvas);
    }

    // draw info of cells(texts for now)
    private void drawCellsInfo(Canvas canvas) {
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                if (cellArr[r][l].type != CellInfo.TYPE_NONE)
                    drawTexts(canvas, cellArr[r][l]);
    }

    // draw strokes while strokeSize>0
    private void drawStrokes(Canvas canvas) {
        if (strokeSize > 0) {
            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(strokeSize);
            for (int r = 1; r < rows; r++)
                if (cellArr[r - 1][0].height > 0)
                    canvas.drawLine(cellArr[r][0].startX, cellArr[r][0].startY, bgRectF.right, cellArr[r][0].startY, strokePaint);
            for (int l = 1; l < lines; l++)
                if (cellArr[0][l - 1].width > 0)
                    canvas.drawLine(cellArr[0][l].startX, cellArr[0][l].startY, cellArr[0][l].startX, bgRectF.bottom, strokePaint);
        }
    }

    // draw merged cells, this actually is covering normal cells
    // but will not cover the strokes
    private void drawMergedCells(Canvas canvas) {
        float halfStrokeSize = strokeSize / 2.0f;

        for (MergeInfo mergeInfo : mergeInfoList) {
            if (mergeInfo.startLine != 0)
                tRectF.left = mergeInfo.startX + halfStrokeSize;
            else
                tRectF.left = bgRectF.left;

            if (mergeInfo.startRow != 0)
                tRectF.top = mergeInfo.startY + halfStrokeSize;
            else
                tRectF.top = bgRectF.top;

            if (mergeInfo.endLine != lines - 1)
                tRectF.right = mergeInfo.startX + mergeInfo.width - halfStrokeSize;
            else
                tRectF.right = bgRectF.right;

            if (mergeInfo.endRow != rows - 1)
                tRectF.bottom = mergeInfo.startY + mergeInfo.height - halfStrokeSize;
            else
                tRectF.bottom = bgRectF.bottom;

            paint.setColor(mergeInfo.bgColor);

            tPath.reset();

            tPath.moveTo(tRectF.left + outStrokeCorner, tRectF.top);

            if (mergeInfo.startRow == 0 && mergeInfo.startLine == 0) {
                addLeftTopCornerPath();
            } else {
                tPath.lineTo(tRectF.left, tRectF.top);
                tPath.lineTo(tRectF.left, tRectF.top + outStrokeCorner);
            }

            tPath.lineTo(tRectF.left, tRectF.bottom - outStrokeCorner);

            if (mergeInfo.endRow == rows - 1 && mergeInfo.startLine == 0) {
                addLeftBottomCornerPath();
            } else {
                tPath.lineTo(tRectF.left, tRectF.bottom);
                tPath.lineTo(tRectF.left + outStrokeCorner, tRectF.bottom);
            }

            tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.bottom);

            if (mergeInfo.endRow == rows - 1 && mergeInfo.endLine == lines - 1) {
                addRightBottomCornerPath();
            } else {
                tPath.lineTo(tRectF.right, tRectF.bottom);
                tPath.lineTo(tRectF.right, tRectF.bottom - outStrokeCorner);
            }

            tPath.lineTo(tRectF.right, tRectF.top + outStrokeCorner);

            if (mergeInfo.startRow == 0 && mergeInfo.endLine == lines - 1) {
                addRightTopCornerPath();
            } else {
                tPath.lineTo(tRectF.right, tRectF.top);
                tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.top);
            }

            tPath.lineTo(tRectF.left + outStrokeCorner, tRectF.top);

            tPath.close();

            canvas.drawPath(tPath, paint);


            // start drawing merged cells texts
            canvas.save();
            canvas.clipRect(tRectF);

            if (null != mergeInfo.texts && mergeInfo.texts.length > 0) {
                int textRows = mergeInfo.texts.length;
                float h = mergeInfo.height;
                float w = mergeInfo.width;
                float[] textHeights = new float[textRows];
                float originX;
                float baseLine;
                Paint.FontMetrics fm;
                float textsTotalHeight = 0.0f;
                for (int t = 0; t < textRows; t++) {
                    textPaint.setTextSize(mergeInfo.textSizes[t]);
                    fm = textPaint.getFontMetrics();
                    textHeights[t] = fm.bottom - fm.top;
                    textsTotalHeight += textHeights[t];
                }
                float top = (h - textsTotalHeight) / 2.0f;
                for (int t = 0; t < textRows; t++) {
                    String text = mergeInfo.texts[t];
                    if (null != text && text.length() > 0) {
                        textPaint.setTextSize(mergeInfo.textSizes[t]);
                        textPaint.setColor(mergeInfo.textColors[t]);
                        fm = textPaint.getFontMetrics();
                        originX = mergeInfo.startX + w / 2.0f - textPaint.measureText(text) / 2.0f;
                        baseLine = mergeInfo.startY + top + textHeights[t] / 2.0f - (fm.ascent + fm.descent) / 2.0f;
                        canvas.drawText(text, originX, baseLine, textPaint);
                    }
                    top += textHeights[t];
                }
            }
            canvas.restore();
        }
    }

    // draw outer stroke while outStrokeSize>0
    private void drawOutStroke(Canvas canvas) {
        if (outStrokeSize > 0) {
            strokePaint.setColor(outStrokeColor);
            strokePaint.setStrokeWidth(outStrokeSize);

            float startPathX;
            if (outStrokeCorner > 0)
                startPathX = bgRectF.left + outStrokeCorner;
            else
                startPathX = bgRectF.left + outStrokeCorner - outStrokeSize / 2;

            tPath.reset();
            tPath.moveTo(startPathX, bgRectF.top);
            addLeftTopCornerPath();
            tPath.lineTo(bgRectF.left, bgRectF.bottom - outStrokeCorner);
            addLeftBottomCornerPath();
            tPath.lineTo(bgRectF.right - outStrokeCorner, bgRectF.bottom);
            addRightBottomCornerPath();
            tPath.lineTo(bgRectF.right, bgRectF.top + outStrokeCorner);
            addRightTopCornerPath();
            tPath.lineTo(bgRectF.left + outStrokeCorner, bgRectF.top);

            canvas.drawPath(tPath, strokePaint);
        }
    }

    // draw top decorations
    private void drawTopDecorations(Canvas canvas) {
        if (null == topDecoration)
            return;
        topDecoration.draw(canvas);
    }

    // draw texts in a cell, can be multiple lines
    // with different text size and color
    private final void drawTexts(Canvas canvas, CellInfo cellInfo) {
        if (null != cellInfo.texts && cellInfo.texts.length > 0) {
            canvas.save();
            canvas.clipRect(
                    cellInfo.getStartX(),
                    cellInfo.getStartY(),
                    cellInfo.getStartX() + cellInfo.width,
                    cellInfo.getStartY() + cellInfo.height);

            int textRows = cellInfo.texts.length;
            float h = cellInfo.height;
            float w = cellInfo.width;
            float[] textHeights = new float[textRows];
            float originX;
            float baseLine;
            Paint.FontMetrics fm;
            float textsTotalHeight = 0.0f;
            for (int t = 0; t < textRows; t++) {
                textPaint.setTextSize(cellInfo.textSizes[t]);
                fm = textPaint.getFontMetrics();
                textHeights[t] = fm.bottom - fm.top;
                textsTotalHeight += textHeights[t];
            }
            float top = (h - textsTotalHeight) / 2.0f;
            for (int t = 0; t < textRows; t++) {
                String text = cellInfo.texts[t];
                if (null != text && text.length() > 0) {
                    textPaint.setTextSize(cellInfo.textSizes[t]);
                    textPaint.setColor(cellInfo.textColors[t]);
                    fm = textPaint.getFontMetrics();
                    originX = cellInfo.startX + w / 2.0f - textPaint.measureText(text) / 2.0f;
                    baseLine = cellInfo.startY + top + textHeights[t] / 2.0f - (fm.ascent + fm.descent) / 2.0f;
                    canvas.drawText(text, originX, baseLine, textPaint);
                }
                top += textHeights[t];
            }
            canvas.restore();
        }
    }

    // add a left-top circular corner to tPath
    private void addLeftTopCornerPath() {
        tCornerRectF.left = bgRectF.left;
        tCornerRectF.top = bgRectF.top;
        tCornerRectF.right = bgRectF.left + outStrokeCorner * 2;
        tCornerRectF.bottom = bgRectF.top + outStrokeCorner * 2;
        tPath.arcTo(tCornerRectF, -90.0f, -90.0f);
    }

    // add a left-bottom circular corner to tPath
    private void addLeftBottomCornerPath() {
        tCornerRectF.left = bgRectF.left;
        tCornerRectF.top = bgRectF.bottom - outStrokeCorner * 2;
        tCornerRectF.right = bgRectF.left + outStrokeCorner * 2;
        tCornerRectF.bottom = bgRectF.bottom;
        tPath.arcTo(tCornerRectF, 180.0f, -90.0f);
    }

    // add a right-bottom circular corner to tPath
    private void addRightBottomCornerPath() {
        tCornerRectF.left = bgRectF.right - outStrokeCorner * 2;
        tCornerRectF.top = bgRectF.bottom - outStrokeCorner * 2;
        tCornerRectF.right = bgRectF.right;
        tCornerRectF.bottom = bgRectF.bottom;
        tPath.arcTo(tCornerRectF, 90.0f, -90.0f);
    }

    // add a right-top circular corner to tPath
    private void addRightTopCornerPath() {
        tCornerRectF.left = bgRectF.right - outStrokeCorner * 2;
        tCornerRectF.top = bgRectF.top;
        tCornerRectF.right = bgRectF.right;
        tCornerRectF.bottom = bgRectF.top + outStrokeCorner * 2;
        tPath.arcTo(tCornerRectF, 0.0f, -90.0f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != onCellClickListener) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();
                    curTouchCell = getCellByXY(downX, downY);
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    float moveX = event.getX();
                    float moveY = event.getY();
                    if (Math.abs(moveX - downX) > touchSlop && Math.abs(moveY - downY) > touchSlop) {
                        curTouchCell = null;
                        return false;
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    float downX = event.getX();
                    float downY = event.getY();
                    Object cell = getCellByXY(downX, downY);
                    // the same cell while down and up
                    if (cell instanceof CellInfo && curTouchCell instanceof CellInfo &&
                            ((CellInfo) cell).row == ((CellInfo) curTouchCell).row &&
                            ((CellInfo) cell).line == ((CellInfo) curTouchCell).line) {
                        onCellClickListener.onCellClick((CellInfo) cell);
                    } else if (cell instanceof MergeInfo && curTouchCell instanceof MergeInfo &&
                            ((MergeInfo) cell).startRow == ((MergeInfo) curTouchCell).startRow &&
                            ((MergeInfo) cell).endRow == ((MergeInfo) curTouchCell).endRow &&
                            ((MergeInfo) cell).startLine == ((MergeInfo) curTouchCell).startLine &&
                            ((MergeInfo) cell).endLine == ((MergeInfo) curTouchCell).endLine) {
                        onCellClickListener.onMergedCellClick((MergeInfo) cell);
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    // get (merged)cell by x, y coordinate
    private Object getCellByXY(float x, float y) {
        // traverse merged cells
        if (null != mergeInfoList && mergeInfoList.size() > 0) {
            int size = mergeInfoList.size();
            for (int i = 0; i < size; i++) {
                MergeInfo mergeInfo = mergeInfoList.get(i);
                if (mergeInfo.startX <= x && x <= mergeInfo.startX + mergeInfo.width &&
                        mergeInfo.startY <= y && y <= mergeInfo.startY + mergeInfo.height)
                    return mergeInfo;
            }
        }

        // traverse cells
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                if (cellArr[r][l].startX <= x && x <= cellArr[r][l].startX + cellArr[r][l].width &&
                        cellArr[r][l].startY <= y && y <= cellArr[r][l].startY + cellArr[r][l].height)
                    return cellArr[r][l];

        return null;
    }

    /**
     * set data list, this will clear the original data
     * including data of merged cells
     *
     * @param cellInfoList data list
     */
    public void setData(ArrayList<CellInfo> cellInfoList) {
        if (null == cellInfoList || cellInfoList.size() <= 0)
            return;

        // clear original data
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                cellArr[r][l] = new CellInfo();
        if (null != mergeInfoList && mergeInfoList.size() > 0)
            mergeInfoList.clear();

        // set new data
        int size = cellInfoList.size();
        for (int i = 0; i < size; i++) {
            CellInfo cellInfo = cellInfoList.get(i);

            // 超出表格范围的数据不处理
            if (cellInfo.row < rows && cellInfo.line < lines) {
                cellArr[cellInfo.row][cellInfo.line] = cellInfo;
                fillTextAttrs(cellInfo);
            }
        }

        // calculate max width of each line
        int textRows;
        if (mode != MODE_FIX_WIDTH && mode != MODE_FIX_WIDTH_HEIGHT) {
            float txtWidth;
            float maxWidth;
            float fixMaxWidth;
            for (int l = 0; l < lines; l++) {
                maxWidth = 0.0f;
                fixMaxWidth = -1;
                for (int r = 0; r < rows; r++) {
                    if (null != cellArr[r][l].texts) {
                        if (cellArr[r][l].width < 0) { // calculate by width of texts
                            textRows = cellArr[r][l].texts.length;
                            for (int t = 0; t < textRows; t++) {
                                textPaint.setTextSize(cellArr[r][l].textSizes[t]);
                                txtWidth = textPaint.measureText(cellArr[r][l].texts[t]);
                                if (maxWidth < txtWidth)
                                    maxWidth = txtWidth;
                            }
                        } else { // set by width value
                            if (fixMaxWidth < cellArr[r][l].width)
                                fixMaxWidth = cellArr[r][l].width;
                        }
                    } else if (fixMaxWidth < cellArr[r][l].width) { // set by width value while no texts found
                        fixMaxWidth = cellArr[r][l].width;
                    }
                }
                if (fixMaxWidth != -1)
                    maxWidth = fixMaxWidth;

                if (l == 0 || l == lines - 1)
                    maxWidth = maxWidth >= outStrokeCorner ? maxWidth : outStrokeCorner;

                widthArr[l] = maxWidth;
            }
        }

        // calculate max height of each row
        if (mode != MODE_FIX_HEIGHT && mode != MODE_FIX_WIDTH_HEIGHT) {
            Paint.FontMetrics fm;
            float txtHeight;
            float tempHeight;
            float maxHeight;
            float fixMaxHeight;
            for (int r = 0; r < rows; r++) {
                maxHeight = 0.0f;
                fixMaxHeight = -1;
                for (int l = 0; l < lines; l++) {
                    if (null != cellArr[r][l].texts) {
                        if (cellArr[r][l].height < 0) { // calculate by height of texts
                            tempHeight = 0.0f;
                            textRows = cellArr[r][l].texts.length;
                            for (int t = 0; t < textRows; t++) {
                                textPaint.setTextSize(cellArr[r][l].textSizes[t]);
                                fm = textPaint.getFontMetrics();
                                txtHeight = fm.bottom - fm.top;
                                tempHeight += txtHeight;
                            }

                            if (maxHeight < tempHeight)
                                maxHeight = tempHeight;
                        } else { // set by height value
                            if (fixMaxHeight < cellArr[r][l].height)
                                fixMaxHeight = cellArr[r][l].height;
                        }
                    } else if (fixMaxHeight < cellArr[r][l].height) { // set by height value while no texts found
                        fixMaxHeight = cellArr[r][l].height;
                    }
                }
                if (fixMaxHeight != -1)
                    maxHeight = fixMaxHeight;

                if (r == 0 || r == rows - 1)
                    maxHeight = maxHeight >= outStrokeCorner ? maxHeight : outStrokeCorner;

                heightArr[r] = maxHeight;
            }
        }

        requestLayout();
        invalidate();
    }

    /**
     * set data list, this will clear the original data
     * including data of merged cells
     *
     * @param rows         rows
     * @param lines        lines
     * @param cellInfoList data list
     */
    public void setData(int rows, int lines, ArrayList<CellInfo> cellInfoList) {
        this.rows = rows;
        this.lines = lines;
        resetTableData();
        setData(cellInfoList);
    }

    /**
     * update data, for now, only set the width and height of each line and row
     * to the last CellInfo of the data list
     *
     * @param cellInfos data list that need to update
     */
    public void updateData(CellInfo... cellInfos) {
        updateData(Arrays.asList(cellInfos));
    }

    /**
     * update data, for now, only set the width and height of each line and row
     * to the last CellInfo of the data list
     *
     * @param cellInfoList data list that need to update
     */
    public void updateData(List<CellInfo> cellInfoList) {
        float w;
        float h;
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo.row < rows && cellInfo.line < lines) {
                cellArr[cellInfo.row][cellInfo.line] = cellInfo;

                w = cellInfo.width;
                if (cellArr[0].length == 1 && cellInfo.width < 2.0f * outStrokeCorner)
                    w = 2.0f * outStrokeCorner;
                else if ((cellInfo.line == 0 || cellInfo.line == lines - 1) && cellInfo.width < outStrokeCorner)
                    w = outStrokeCorner;

                h = cellInfo.height;
                if (cellArr.length == 1 && cellInfo.height < 2.0f * outStrokeCorner)
                    h = 2.0f * outStrokeCorner;
                else if ((cellInfo.row == 0 || cellInfo.row == rows - 1) && cellInfo.height < outStrokeCorner)
                    h = outStrokeCorner;

                widthArr[cellInfo.line] = w;
                heightArr[cellInfo.row] = h;
                fillTextAttrs(cellInfo);
            }
        }

        requestLayout();
        invalidate();
    }

    /**
     * update merged cells, only add as new MergeInfo
     *
     * @param mergeInfos merged data list that need to update
     */
    public void updateMergeData(MergeInfo... mergeInfos) {
        updateMergeData(Arrays.asList(mergeInfos));
    }

    /**
     * update merged cells, only add as new MergeInfo
     *
     * @param mergeInfoList merged data list that need to update
     */
    public void updateMergeData(List<MergeInfo> mergeInfoList) {
        for (MergeInfo mergeInfo : mergeInfoList) {
            fillMergeTextAttrs(mergeInfo);
            this.mergeInfoList.add(mergeInfo);
        }

        requestLayout();
        invalidate();
    }

    /**
     * add several new rows
     *
     * @param curRow    add new rows by curRow, between 0 and rows-1
     * @param newRows   rows num added
     * @param height    height of new rows(px), using default ADD_ROWS_DEFAULT_HEIGHT while less than 0
     * @param direction ADD_ROWS_TOP or ADD_ROWS_BOTTOM
     * @return true if success, false if failed
     */
    public boolean addNewRows(int curRow, int newRows, float height, int direction) {
        if (newRows <= 0)
            return false;

        if (curRow < 0)
            curRow = 0;
        if (curRow >= rows)
            curRow = rows - 1;

        if (direction == ADD_ROWS_TOP)
            --curRow;

        if (height < 0)
            height = ADD_ROWS_DEFAULT_HEIGHT;

        // copy original data to new list with growing rows
        CellInfo[][] tCellArr = new CellInfo[rows + newRows][lines];
        float[] tHeightArr = new float[rows + newRows];
        for (int r = 0; r <= curRow; r++) {
            tCellArr[r] = Arrays.copyOf(cellArr[r], lines);
            tHeightArr[r] = heightArr[r];
        }
        for (int r = curRow + 1; r <= curRow + newRows; r++) {
            tCellArr[r] = new CellInfo[lines];
            tHeightArr[r] = height;

            for (int l = 0; l < lines; l++)
                tCellArr[r][l] = new CellInfo();
        }
        for (int r = curRow + newRows + 1; r < rows + newRows; r++) {
            tCellArr[r] = Arrays.copyOf(cellArr[r - newRows], lines);
            tHeightArr[r] = heightArr[r - newRows];
        }

        // release the original data
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                cellArr[r][l] = null;
        heightArr = null;

        rows += newRows;

        cellArr = tCellArr;
        heightArr = tHeightArr;

        requestLayout();
        invalidate();

        return true;
    }

    /**
     * remove several rows and the corresponding data
     *
     * @param start starting row, between 0 to rows-1
     * @param end   ending row, between 0 to rows-1
     * @return true if success, false if failed
     */
    public boolean removeRows(int start, int end) {
        int rowsToDel = end - start + 1;
        if (rowsToDel <= 0 || rowsToDel >= rows || start < 0 || end >= rows)
            return false;

        // copy original data to new list with reducing rows
        // and remove the corresponding data
        int newRows = rows - rowsToDel;
        CellInfo[][] tCellArr = new CellInfo[newRows][lines];
        float[] tHeightArr = new float[newRows];
        for (int r = 0; r < start; r++) {
            tCellArr[r] = Arrays.copyOf(cellArr[r], lines);
            tHeightArr[r] = heightArr[r];
        }
        for (int r = end + 1; r < rows; r++) {
            tCellArr[r - rowsToDel] = Arrays.copyOf(cellArr[r], lines);
            tHeightArr[r - rowsToDel] = heightArr[r];
        }

        // cell's height can not be less than 2*outStrokeCorner
        // while having only 1 row
        if (newRows == 1 && tHeightArr[0] < 2.0f * outStrokeCorner)
            tHeightArr[0] = 2.0f * outStrokeCorner;

        // release the original data
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                cellArr[r][l] = null;
        heightArr = null;

        rows = newRows;

        cellArr = tCellArr;
        heightArr = tHeightArr;

        requestLayout();
        invalidate();

        return true;
    }

    /**
     * add several new lines
     *
     * @param curLine   add new lines by curLine, between 0 and lines-1
     * @param newLines  lines num added
     * @param width     width of new lines(px), using default ADD_LINES_DEFAULT_WIDTH while less than 0
     * @param direction ADD_LINES_LEFT or ADD_LINES_RIGHT
     * @return true if success, false if failed
     */
    public boolean addNewLines(int curLine, int newLines, float width, int direction) {
        if (newLines <= 0)
            return false;

        if (curLine < 0)
            curLine = 0;
        if (curLine >= lines)
            curLine = lines - 1;

        if (direction == ADD_LINES_LEFT)
            --curLine;

        if (width < 0)
            width = ADD_LINES_DEFAULT_WIDTH;

        // copy original data to new list with growing lines
        CellInfo[][] tCellArr = new CellInfo[rows][lines + newLines];
        float[] tWidthArr = new float[lines + newLines];
        for (int r = 0; r < rows; r++) {
            tCellArr[r] = new CellInfo[lines + newLines];
            for (int l = 0; l <= curLine; l++)
                tCellArr[r][l] = cellArr[r][l];
            for (int l = curLine + 1; l <= curLine + newLines; l++)
                tCellArr[r][l] = new CellInfo();
            for (int l = curLine + newLines + 1; l < lines + newLines; l++)
                tCellArr[r][l] = cellArr[r][l - newLines];
        }

        // width of each line should be set once
        for (int l = 0; l <= curLine; l++)
            tWidthArr[l] = widthArr[l];
        for (int l = curLine + 1; l <= curLine + newLines; l++)
            tWidthArr[l] = width;
        for (int l = curLine + newLines + 1; l < lines + newLines; l++)
            tWidthArr[l] = widthArr[l - newLines];

        // release the original data
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                cellArr[r][l] = null;
        widthArr = null;

        lines += newLines;

        cellArr = tCellArr;
        widthArr = tWidthArr;

        requestLayout();
        invalidate();

        return true;
    }

    /**
     * remove several lines and the corresponding data
     *
     * @param start starting line, between 0 to lines-1
     * @param end   ending line, between 0 to lines-1
     * @return true if success, false if failed
     */
    public boolean removeLines(int start, int end) {
        int linesToDel = end - start + 1;
        if (linesToDel <= 0 || linesToDel >= lines || start < 0 || end >= lines)
            return false;

        // copy original data to new list with reducing lines
        // and remove the corresponding data
        int newLines = lines - linesToDel;
        CellInfo[][] tCellArr = new CellInfo[rows][newLines];
        float[] tWidthArr = new float[newLines];
        for (int r = 0; r < rows; r++) {
            tCellArr[r] = new CellInfo[newLines];
            for (int l = 0; l < start; l++)
                tCellArr[r][l] = cellArr[r][l];
            for (int l = end + 1; l < lines; l++)
                tCellArr[r][l - linesToDel] = cellArr[r][l];
        }

        // width of each line should be set once
        for (int l = 0; l < start; l++)
            tWidthArr[l] = widthArr[l];
        for (int l = end + 1; l < lines; l++)
            tWidthArr[l - linesToDel] = widthArr[l];

        // cell's width can not be less than 2*outStrokeCorner
        // while having only 1 line
        if (newLines == 1 && tWidthArr[0] < 2.0f * outStrokeCorner)
            tWidthArr[0] = 2.0f * outStrokeCorner;

        // release the original data
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                cellArr[r][l] = null;
        widthArr = null;

        lines = newLines;

        cellArr = tCellArr;
        widthArr = tWidthArr;

        requestLayout();
        invalidate();

        return true;
    }

    // fill the attributes of cells, including textColors and textSizes,
    // note that if textColor is set, textColors will be covered,
    // the same with textSize and textSizes
    private void fillTextAttrs(CellInfo cellInfo) {
        if (null != cellInfo.texts && cellInfo.texts.length > 0) {
            if (cellInfo.textColor == 0 && null == cellInfo.textColors) // both textColor and textColors are not set
                cellInfo.textColor = Color.BLACK;
            if (cellInfo.textColor != 0) { // cover textColors if textColor is set
                cellInfo.textColors = new int[cellInfo.texts.length];
                for (int t = 0; t < cellInfo.texts.length; t++)
                    cellInfo.textColors[t] = cellInfo.textColor;
            }

            if (cellInfo.textSize == -1 && null == cellInfo.textSizes) // both textSize and textSizes are not set
                cellInfo.textSize = spToPx(14);
            if (cellInfo.textSize != -1) { // cover textSizes if textSize is set
                cellInfo.textSizes = new int[cellInfo.texts.length];
                for (int t = 0; t < cellInfo.texts.length; t++)
                    cellInfo.textSizes[t] = cellInfo.textSize;
            }
        }
    }

    // fill the attributes of merged cells, including textColors and textSizes,
    // note that if textColor is set, textColors will be covered,
    // the same with textSize and textSizes
    private void fillMergeTextAttrs(MergeInfo mergeInfo) {
        if (null != mergeInfo.texts && mergeInfo.texts.length > 0) {
            if (mergeInfo.textColor == 0 && null == mergeInfo.textColors) // both textColor and textColors are not set
                mergeInfo.textColor = Color.BLACK;
            if (mergeInfo.textColor != 0) { // cover textColors if textColor is set
                mergeInfo.textColors = new int[mergeInfo.texts.length];
                for (int t = 0; t < mergeInfo.texts.length; t++)
                    mergeInfo.textColors[t] = mergeInfo.textColor;
            }

            if (mergeInfo.textSize == -1 && null == mergeInfo.textSizes) // both textSize and textSizes are not set
                mergeInfo.textSize = spToPx(14);
            if (mergeInfo.textSize != -1) { // cover textSizes if textSize is set
                mergeInfo.textSizes = new int[mergeInfo.texts.length];
                for (int t = 0; t < mergeInfo.texts.length; t++)
                    mergeInfo.textSizes[t] = mergeInfo.textSize;
            }
        }
    }

    /**
     * merge cells, data with illegal row or line will be ignored
     *
     * @param mergeInfos merged cells info list
     */
    public void mergeCells(MergeInfo... mergeInfos) {
        mergeCells(Arrays.asList(mergeInfos));
    }

    /**
     * merge cells, data with illegal row or line will be ignored
     *
     * @param mergeInfoList merged cells info list
     */
    public void mergeCells(List<MergeInfo> mergeInfoList) {
        if (null == mergeInfoList || mergeInfoList.size() == 0)
            return;

        boolean merged = false;
        int size = mergeInfoList.size();
        for (int i = 0; i < size; i++) {
            MergeInfo mergeInfo = mergeInfoList.get(i);

            // ignore the illegal data
            if (mergeInfo.startRow > mergeInfo.endRow ||
                    mergeInfo.startLine > mergeInfo.endLine ||
                    mergeInfo.startRow < 0 || mergeInfo.startLine < 0 ||
                    mergeInfo.endRow >= rows || mergeInfo.endLine >= lines)
                continue;

            if (mergeInfo.bgColor == 0) {
                if (cellArr[mergeInfo.startRow][mergeInfo.startLine].bgColor == 0)
                    mergeInfo.bgColor = bgColor;
                else
                    mergeInfo.bgColor = cellArr[mergeInfo.startRow][mergeInfo.startLine].bgColor;
            }
            fillMergeTextAttrs(mergeInfo);

            merged = true;
            this.mergeInfoList.add(mergeInfo);
        }

        if (merged) {
            requestLayout();
            invalidate();
        }
    }

    /**
     * unmerge the merged cells
     *
     * @param mergeInfos merged cells info list that need to be unmerged
     */
    public void unmergeCells(MergeInfo... mergeInfos) {
        unmergeCells(Arrays.asList(mergeInfos));
    }

    /**
     * unmerge the merged cells
     *
     * @param mergeInfoList merged cells info list that need to be unmerged
     */
    public void unmergeCells(List<MergeInfo> mergeInfoList) {
        if (null == mergeInfoList || mergeInfoList.size() == 0)
            return;

        this.mergeInfoList.removeAll(mergeInfoList);

        requestLayout();
        invalidate();
    }

    /**
     * set the bottom decorations, see {@link EasyDecoration}
     *
     * @param decoration your own decoration
     */
    public void setBottomDecorations(EasyDecoration decoration) {
        this.bottomDecoration = decoration;
    }

    /**
     * set the top decorations, see {@link EasyDecoration}
     *
     * @param decoration your own decoration
     */
    public void setTopDecorations(EasyDecoration decoration) {
        this.topDecoration = decoration;
    }


    // getters & setters
    public int getRows() {
        return this.rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getLines() {
        return this.lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getHeaderHVColor() {
        return headerHVColor;
    }

    public void setHeaderHVColor(int headerHVColor) {
        this.headerHVColor = headerHVColor;
    }

    public int getHeaderHColor() {
        return headerHColor;
    }

    public void setHeaderHColor(int headerHColor) {
        this.headerHColor = headerHColor;
    }

    public int getHeaderVColor() {
        return headerVColor;
    }

    public void setHeaderVColor(int headerVColor) {
        this.headerVColor = headerVColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeSize() {
        return strokeSize;
    }

    public void setStrokeSize(int strokeSize) {
        this.strokeSize = strokeSize;
    }

    public int getOutStrokeColor() {
        return outStrokeColor;
    }

    public void setOutStrokeColor(int outStrokeColor) {
        this.outStrokeColor = outStrokeColor;
    }

    public int getOutStrokeSize() {
        return outStrokeSize;
    }

    public void setOutStrokeSize(int outStrokeSize) {
        this.outStrokeSize = outStrokeSize;
    }

    public float getOutStrokeCorner() {
        return outStrokeCorner;
    }

    /**
     * set the radius of the table's corner, if outStrokeCorner is greater than
     * width or height of corner cells, outStrokeCorner will be set to the
     * minimum width or height of the corner cells
     *
     * @param outStrokeCorner radius of corner(px)
     */
    public void setOutStrokeCorner(float outStrokeCorner) {
        float min = cellArr[0][0].width;
        if (min > cellArr[0][0].height)
            min = cellArr[0][0].height;

        if (min > cellArr[0][lines - 1].width)
            min = cellArr[0][lines - 1].width;
        if (min > cellArr[0][lines - 1].height)
            min = cellArr[0][lines - 1].height;

        if (min > cellArr[rows - 1][0].width)
            min = cellArr[rows - 1][0].width;
        if (min > cellArr[rows - 1][0].height)
            min = cellArr[rows - 1][0].height;

        if (min > cellArr[rows - 1][lines - 1].width)
            min = cellArr[rows - 1][lines - 1].width;
        if (min > cellArr[rows - 1][lines - 1].height)
            min = cellArr[rows - 1][lines - 1].height;

        if (outStrokeCorner < 0 || outStrokeCorner > min)
            this.outStrokeCorner = min;
        else
            this.outStrokeCorner = outStrokeCorner;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * redraw the table, note that if any attributes is reset,
     * this should be call to redraw the table view
     */
    public void reset() {
        requestLayout();
        invalidate();
    }

    /**
     * listener of cells and merged cells
     */
    public interface OnCellClickListener {
        /**
         * invoke while a cell is click
         *
         * @param cellInfo CellInfo
         */
        public void onCellClick(CellInfo cellInfo);

        /**
         * invoke while a merged cell is click
         *
         * @param mergeInfo MergeInfo
         */
        public void onMergedCellClick(MergeInfo mergeInfo);
    }

    private OnCellClickListener onCellClickListener;

    /**
     * set the click listener
     *
     * @param onCellClickListener OnCellClickListener
     */
    public void setOnCellClickListener(OnCellClickListener onCellClickListener) {
        this.onCellClickListener = onCellClickListener;
    }

    private int spToPx(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}

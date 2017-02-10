package com.hzn.easytableview;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 表格视图，带有横竖向表头，表格体类型可扩展，单个单元格支持多行显示不同(颜色、大小)字符，支持合并、取消合并单元格
 * Created by huzn on 2017/1/12.
 */
public class EasyTableView extends View {

    // 总的行数，默认为0
    private int rows;
    // 总的列数，默认位0
    private int lines;
    // 背景颜色，默认为Color.WHITE
    private int bgColor;
    // 双向表头颜色，至少2x2的表格才绘制，默认为Color.LTGRAY
    private int headerHVColor;
    // 横向表头颜色，便于统一设置；至少2行2列才绘制，默认为Color.LTGRAY
    private int headerHColor;
    // 竖向表头颜色，便于统一设置；至少2行2列才绘制，默认为Color.LTGRAY
    private int headerVColor;
    // 线段颜色，默认为Color.GRAY
    private int strokeColor;
    // 线段粗细，默认为1dp
    private int strokeSize;
    // 边框颜色，默认为Color.GRAY
    private int outStrokeColor;
    // 边框粗细，默认为1dp
    private int outStrokeSize;
    // 边框圆角的宽度，默认为5dp
    private float outStrokeCorner;
    // 模式，分为MODE_NORMAL、MODE_FIX_WIDTH、MODE_FIX_HEIGHT和MODE_FIX_WIDTH_HEIGHT，默认为MODE_NORMAL
    private int mode;

    /**
     * 正常模式，表格的宽和高都由表格内容决定
     */
    public static final int MODE_NORMAL = 0;
    /**
     * 固定宽模式，表格的宽由width属性决定，每一列的宽度平均分，高由表格内容决定
     */
    public static final int MODE_FIX_WIDTH = 1;
    /**
     * 固定高模式，表格的高由height属性决定，每一行的高度平均分，宽由表格内容决定
     */
    public static final int MODE_FIX_HEIGHT = 2;
    /**
     * 固定宽高模式，表格的宽、高分别由width、height属性决定，每一行、每一列都平均分
     */
    public static final int MODE_FIX_WIDTH_HEIGHT = 3;

    /**
     * 在某行顶部添加若干行
     */
    public static final int ADD_ROWS_TOP = 0;
    /**
     * 在某行底部添加若干行
     */
    public static final int ADD_ROWS_BOTTOM = 1;
    /**
     * 在某列左侧添加若干列
     */
    public static final int ADD_LINES_LEFT = 2;
    /**
     * 在某列右侧添加若干列
     */
    public static final int ADD_LINES_RIGHT = 3;
    /**
     * 添加新行的默认高度
     */
    public static final float ADD_ROWS_DEFAULT_HEIGHT = 20.0f;
    /**
     * 添加新列的默认宽度
     */
    public static final float ADD_LINES_DEFAULT_WIDTH = 20.0f;

    private Paint paint;
    private Paint strokePaint;
    private TextPaint textPaint;
    private RectF bgRectF;
    private Path tPath;
    private RectF tRectF;
    private RectF tCornerRectF;

    // 合并单元格的数据集合
    private ArrayList<MergeInfo> mergeInfoList;
    // 单元格宽度集合
    private float[] widthArr;
    // 单元格高度集合
    private float[] heightArr;
    // 单元格数据集合
    private CellInfo[][] cellArr;

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

    // 重置表格数据
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
            // 每一列占用的宽度和
            width = 0;
            for (int l = 0; l < lines; l++)
                width = (int) (width + widthArr[l]);
            width = width + outStrokeSize + getPaddingLeft() + getPaddingRight();
        }

        measureMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (measureMode != MeasureSpec.EXACTLY) { // wrap_content
            // 每一行占用的高度和
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
        // 初始化坐标等数据
        initData();
        // 绘制背景
        drawBg(canvas);
        // 绘制双向表头
        drawHeaderVH(canvas);
        // 绘制横向表头
        drawHeaderH(canvas);
        // 绘制竖向表头
        drawHeaderV(canvas);
        // 绘制单元格内容
        drawCellsInfo(canvas);
        // 绘制线段
        drawStrokes(canvas);
        // 绘制合并的单元格
        drawMergedCells(canvas);
        // 绘制边框
        drawOutStroke(canvas);
    }

    // 初始化坐标等数据
    private void initData() {
        // 固定模式中（此时宽高不能为wrap_content），确定每一行、每一列的宽高
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

        // 初始化每个单元格的起始x、y坐标，并统一设置row、line，统一设置width、height为最大值（需要以最大值为标准）
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

        // 初始化合并后的单元格的startX、startY、width和height
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
                    width += cellArr[r][l].width; // 为了方便，不另外循环计算了

                height += cellArr[r][0].height;
            }
            mergeInfo.width = width;
            mergeInfo.height = height;
        }
    }

    // 绘制背景
    private void drawBg(Canvas canvas) {
        paint.setColor(bgColor);
        canvas.drawRoundRect(bgRectF, outStrokeCorner, outStrokeCorner, paint);
    }

    // 绘制双向表头
    private void drawHeaderVH(Canvas canvas) {
        // 至少2x2的表格才绘制双向表头
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

    // 绘制横向表头
    private void drawHeaderH(Canvas canvas) {
        // 至少2行2列才绘制
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

    // 绘制竖向表头
    private void drawHeaderV(Canvas canvas) {
        // 至少2行2列才绘制
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

    // 绘制单元格内容，包括表头
    private void drawCellsInfo(Canvas canvas) {
        for (int r = 0; r < rows; r++) {
            for (int l = 0; l < lines; l++) {
                if (cellArr[r][l].type != CellInfo.TYPE_NONE) {
                    // 绘制该单元格背景
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

                    // 绘制单元格内的字符
                    drawTexts(canvas, cellArr[r][l]);
                }
            }
        }
    }

    // 绘制线段
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

    // 绘制合并的单元格
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


            // 绘制合并后的单元格内的字符
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

    // 绘制边框
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

    // 绘制单个单元格中的字符
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

    // 在tPath中添加一段左上角的圆角
    private void addLeftTopCornerPath() {
        tCornerRectF.left = bgRectF.left;
        tCornerRectF.top = bgRectF.top;
        tCornerRectF.right = bgRectF.left + outStrokeCorner * 2;
        tCornerRectF.bottom = bgRectF.top + outStrokeCorner * 2;
        tPath.arcTo(tCornerRectF, -90.0f, -90.0f);
    }

    // 在tPath中添加一段左下角的圆角
    private void addLeftBottomCornerPath() {
        tCornerRectF.left = bgRectF.left;
        tCornerRectF.top = bgRectF.bottom - outStrokeCorner * 2;
        tCornerRectF.right = bgRectF.left + outStrokeCorner * 2;
        tCornerRectF.bottom = bgRectF.bottom;
        tPath.arcTo(tCornerRectF, 180.0f, -90.0f);
    }

    // 在tPath中添加一段右下角的圆角
    private void addRightBottomCornerPath() {
        tCornerRectF.left = bgRectF.right - outStrokeCorner * 2;
        tCornerRectF.top = bgRectF.bottom - outStrokeCorner * 2;
        tCornerRectF.right = bgRectF.right;
        tCornerRectF.bottom = bgRectF.bottom;
        tPath.arcTo(tCornerRectF, 90.0f, -90.0f);
    }

    // 在tPath中添加一段右上角的圆角
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
                    // 按下和释放的是同一个cell
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

    // 获取坐标x，y对应的cell
    private Object getCellByXY(float x, float y) {
        // 合并后的单元格
        if (null != mergeInfoList && mergeInfoList.size() > 0) {
            int size = mergeInfoList.size();
            for (int i = 0; i < size; i++) {
                MergeInfo mergeInfo = mergeInfoList.get(i);
                if (mergeInfo.startX <= x && x <= mergeInfo.startX + mergeInfo.width &&
                        mergeInfo.startY <= y && y <= mergeInfo.startY + mergeInfo.height)
                    return mergeInfo;
            }
        }

        // 单元格
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                if (cellArr[r][l].startX <= x && x <= cellArr[r][l].startX + cellArr[r][l].width &&
                        cellArr[r][l].startY <= y && y <= cellArr[r][l].startY + cellArr[r][l].height)
                    return cellArr[r][l];

        return null;
    }

    /**
     * 设置数据项，包含表头的内容，将清除表格原有数据，包括合并单元格的数据
     *
     * @param cellInfoList 数据项
     */
    public void setData(ArrayList<CellInfo> cellInfoList) {
        if (null == cellInfoList || cellInfoList.size() <= 0)
            return;

        // 清除原有数据
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                cellArr[r][l] = new CellInfo();
        if (null != mergeInfoList && mergeInfoList.size() > 0)
            mergeInfoList.clear();

        // 赋值
        int size = cellInfoList.size();
        for (int i = 0; i < size; i++) {
            CellInfo cellInfo = cellInfoList.get(i);

            // 超出表格范围的数据不处理
            if (cellInfo.row < rows && cellInfo.line < lines) {
                cellArr[cellInfo.row][cellInfo.line] = cellInfo;
                fillTextAttrs(cellInfo);
            }
        }

        // 计算出每一列的最大宽度
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
                        if (cellArr[r][l].width < 0) { // 根据测量的字符宽度来计算
                            textRows = cellArr[r][l].texts.length;
                            for (int t = 0; t < textRows; t++) {
                                textPaint.setTextSize(cellArr[r][l].textSizes[t]);
                                txtWidth = textPaint.measureText(cellArr[r][l].texts[t]);
                                if (maxWidth < txtWidth)
                                    maxWidth = txtWidth;
                            }
                        } else { // 根据设置的值来计算
                            if (fixMaxWidth < cellArr[r][l].width)
                                fixMaxWidth = cellArr[r][l].width;
                        }
                    } else if (fixMaxWidth < cellArr[r][l].width) { // 没有字符的时候,直接根据设置的值来计算
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

        // 计算出每一行的最大高度
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
                        if (cellArr[r][l].height < 0) { // 根据测量的字符高度来计算
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
                        } else { // 根据设置的值来计算
                            if (fixMaxHeight < cellArr[r][l].height)
                                fixMaxHeight = cellArr[r][l].height;
                        }
                    } else if (fixMaxHeight < cellArr[r][l].height) { // 没有字符的时候，直接根据设置的值来计算
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
     * 设置数据项，包含表头的内容，将清除表格原有数据，包括合并单元格的数据
     *
     * @param rows         行数
     * @param lines        列数
     * @param cellInfoList 数据项
     */
    public void setData(int rows, int lines, ArrayList<CellInfo> cellInfoList) {
        this.rows = rows;
        this.lines = lines;
        resetTableData();
        setData(cellInfoList);
    }

    /**
     * 更新表格数据，若设置了width或height，将强制更新单元格大小，大小以某行某列最后一个输入数据为准
     *
     * @param cellInfos 需要更新的数据项
     */
    public void updateData(CellInfo... cellInfos) {
        updateData(Arrays.asList(cellInfos));
    }

    /**
     * 更新表格数据，若设置了width或height，将强制更新单元格大小，大小以某行某列最后一个输入数据为准
     *
     * @param cellInfoList 需要更新的数据项集合
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
     * 更新合并的单元格数据
     *
     * @param mergeInfos 需要更新的合并数据项集合
     */
    public void updateMergeData(MergeInfo... mergeInfos) {
        updateMergeData(Arrays.asList(mergeInfos));
    }

    /**
     * 更新合并的单元格数据
     *
     * @param mergeInfoList 需要更新的合并数据项集合
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
     * 添加若干新行
     *
     * @param curRow    在此行的顶部或底部添加，小于0则为0，大于等于rows则为rows-1
     * @param newRows   新添加的行数
     * @param height    新行的高度，单位px，小于0时使用默认高度ADD_ROWS_DEFAULT_HEIGHT
     * @param direction 方向，为ADD_ROWS_TOP或ADD_ROWS_BOTTOM
     * @return 是否添加成功
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

        // 复制数据并添加新行
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

        // 释放原数据
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
     * 删除若干行，将同时删除数据
     *
     * @param start 起始行，范围0~(rows-1)
     * @param end   结束行，范围0~(rows-1)
     * @return 是否删除成功
     */
    public boolean removeRows(int start, int end) {
        int rowsToDel = end - start + 1;
        if (rowsToDel <= 0 || rowsToDel >= rows || start < 0 || end >= rows)
            return false;

        // 复制数据并删除旧行
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

        // 只有一行的情况下，行高不能小于2*outStrokeCorner
        if (newRows == 1 && tHeightArr[0] < 2.0f * outStrokeCorner)
            tHeightArr[0] = 2.0f * outStrokeCorner;

        // 释放原数据
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
     * 添加若干新列
     *
     * @param curLine   在此列的左侧或右侧添加，小于0则为0，大于等于lines则为lines-1
     * @param newLines  新添加的行数
     * @param width     新行的高度，单位px，小于0时使用默认高度ADD_ROWS_DEFAULT_HEIGHT
     * @param direction 方向，为ADD_LINES_LEFT或ADD_LINES_RIGHT
     * @return 是否添加成功
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

        // 复制数据并添加新列
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

        // 各列宽度只需要赋值一次
        for (int l = 0; l <= curLine; l++)
            tWidthArr[l] = widthArr[l];
        for (int l = curLine + 1; l <= curLine + newLines; l++)
            tWidthArr[l] = width;
        for (int l = curLine + newLines + 1; l < lines + newLines; l++)
            tWidthArr[l] = widthArr[l - newLines];

        // 释放原数据
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
     * 删除若干列，将删除数据
     *
     * @param start 起始列，范围0~(lines-1)
     * @param end   结束列，范围0~(lines-1)
     * @return 是否删除成功
     */
    public boolean removeLines(int start, int end) {
        int linesToDel = end - start + 1;
        if (linesToDel <= 0 || linesToDel >= lines || start < 0 || end >= lines)
            return false;

        // 复制数据并删除旧列
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

        // 各列宽度只需赋值一次
        for (int l = 0; l < start; l++)
            tWidthArr[l] = widthArr[l];
        for (int l = end + 1; l < lines; l++)
            tWidthArr[l - linesToDel] = widthArr[l];

        // 只有一列的情况下，列宽不能小于2*outStrokeCorner
        if (newLines == 1 && tWidthArr[0] < 2.0f * outStrokeCorner)
            tWidthArr[0] = 2.0f * outStrokeCorner;

        // 释放原数据
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

    // 填充单元格texts的属性，包括textColors和textSizes
    private void fillTextAttrs(CellInfo cellInfo) {
        if (null != cellInfo.texts && cellInfo.texts.length > 0) {
            if (cellInfo.textColor == 0 && null == cellInfo.textColors) // textColor和textColors都没有设置
                cellInfo.textColor = Color.BLACK;
            if (cellInfo.textColor != 0) { // 设置了textColor，则覆盖textColors
                cellInfo.textColors = new int[cellInfo.texts.length];
                for (int t = 0; t < cellInfo.texts.length; t++)
                    cellInfo.textColors[t] = cellInfo.textColor;
            }

            if (cellInfo.textSize == -1 && null == cellInfo.textSizes) // textSize和textSizes都没有设置
                cellInfo.textSize = spToPx(14);
            if (cellInfo.textSize != -1) { // 设置了textSize，则覆盖textSizes
                cellInfo.textSizes = new int[cellInfo.texts.length];
                for (int t = 0; t < cellInfo.texts.length; t++)
                    cellInfo.textSizes[t] = cellInfo.textSize;
            }
        }
    }

    // 填充合并单元格texts的属性，包括textColors和textSizes
    private void fillMergeTextAttrs(MergeInfo mergeInfo) {
        if (null != mergeInfo.texts && mergeInfo.texts.length > 0) {
            if (mergeInfo.textColor == 0 && null == mergeInfo.textColors) // textColor和textColors都没有设置
                mergeInfo.textColor = Color.BLACK;
            if (mergeInfo.textColor != 0) { // 设置了textColor，则覆盖textColors
                mergeInfo.textColors = new int[mergeInfo.texts.length];
                for (int t = 0; t < mergeInfo.texts.length; t++)
                    mergeInfo.textColors[t] = mergeInfo.textColor;
            }

            if (mergeInfo.textSize == -1 && null == mergeInfo.textSizes) // textSize和textSizes都没有设置
                mergeInfo.textSize = spToPx(14);
            if (mergeInfo.textSize != -1) { // 设置了textSize，则覆盖textSizes
                mergeInfo.textSizes = new int[mergeInfo.texts.length];
                for (int t = 0; t < mergeInfo.texts.length; t++)
                    mergeInfo.textSizes[t] = mergeInfo.textSize;
            }
        }
    }

    /**
     * 合并单元格，将自动过滤行列超出范围的数据
     *
     * @param mergeInfos 合并单元格数据集
     */
    public void mergeCells(MergeInfo... mergeInfos) {
        mergeCells(Arrays.asList(mergeInfos));
    }

    /**
     * 合并单元格，将自动过滤行列超出范围的数据
     *
     * @param mergeInfoList 合并单元格数据集
     */
    public void mergeCells(List<MergeInfo> mergeInfoList) {
        if (null == mergeInfoList || mergeInfoList.size() == 0)
            return;

        boolean merged = false;
        int size = mergeInfoList.size();
        for (int i = 0; i < size; i++) {
            MergeInfo mergeInfo = mergeInfoList.get(i);

            // 过滤超出范围的数据
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
     * 取消合并单元格
     *
     * @param mergeInfos 要取消合并的单元格数据集
     */
    public void unmergeCells(MergeInfo... mergeInfos) {
        unmergeCells(Arrays.asList(mergeInfos));
    }

    /**
     * 取消合并单元格
     *
     * @param mergeInfoList 要取消合并的单元格数据集
     */
    public void unmergeCells(List<MergeInfo> mergeInfoList) {
        if (null == mergeInfoList || mergeInfoList.size() == 0)
            return;

        this.mergeInfoList.removeAll(mergeInfoList);

        requestLayout();
        invalidate();
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

    /**
     * 获取线段粗细
     *
     * @return 线段粗细，单位px
     */
    public int getStrokeSize() {
        return strokeSize;
    }

    /**
     * 设置线段粗细
     *
     * @param strokeSize 线段粗细，单位px
     */
    public void setStrokeSize(int strokeSize) {
        this.strokeSize = strokeSize;
    }

    public int getOutStrokeColor() {
        return outStrokeColor;
    }

    public void setOutStrokeColor(int outStrokeColor) {
        this.outStrokeColor = outStrokeColor;
    }

    /**
     * 获取边框线段粗细
     *
     * @return 边框线段粗细，单位px
     */
    public int getOutStrokeSize() {
        return outStrokeSize;
    }

    /**
     * 设置边框线段粗细
     *
     * @param outStrokeSize 边框线段粗细，单位px
     */
    public void setOutStrokeSize(int outStrokeSize) {
        this.outStrokeSize = outStrokeSize;
    }

    /**
     * 获取边框四周圆角半径
     *
     * @return 圆角半径，单位px
     */
    public float getOutStrokeCorner() {
        return outStrokeCorner;
    }

    /**
     * 设置边框四周圆角半径，半径大于四周单元格的长或宽时，将取四周单元格的长或宽的较小值
     *
     * @param outStrokeCorner 圆角半径，小于0时，默认设置为四周单元格的长或宽的较小值，单位px
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
     * 重置，在更新了表格参数后，需要调用此方法重绘表格
     */
    public void reset() {
        requestLayout();
        invalidate();
    }

    /**
     * 一个单元格的信息
     */
    public static class CellInfo {
        /**
         * 不绘制内容，可用于隐藏单元格内容，单元格数据不会清除
         */
        public static final int TYPE_NONE = 0;
        /**
         * 正常类型
         */
        public static final int TYPE_NORMAL = 1;
        /**
         * 按钮类型
         */
        public static final int TYPE_BUTTON = 2;
        /**
         * 单元格的类型
         */
        public int type = TYPE_NORMAL;
        /**
         * Object类型的tag标记
         */
        public Object tag = null;
        /**
         * 所在行
         */
        public int row = -1;
        /**
         * 所在列
         */
        public int line = -1;
        /**
         * 起始x坐标，不提供外部设置，只能获取
         */
        private float startX = 0.0f;
        /**
         * 起始y坐标，不提供外部设置，只能获取
         */
        private float startY = 0.0f;
        /**
         * 宽度，小于0时，将在绘制单元格时，根据模式来自动设置宽
         */
        public float width = -1.0f;
        /**
         * 高度，小于0时，将在绘制单元格时，根据模式来自动设置高
         */
        public float height = -1.0f;
        /**
         * 背景颜色，只有设置时才特殊填充，否则用该单元格默认的颜色填充
         */
        public int bgColor = 0;
        /**
         * 字体颜色，不设置时默认为Color.BLACK，设置后将覆盖textColors的值
         */
        public int textColor = 0;
        /**
         * 字体颜色集，设置时必须与texts长度一致
         */
        public int[] textColors = null;
        /**
         * 字体大小，单位px，不设置时默认为14sp对应的px值，设置后将覆盖textSizes的值
         */
        public int textSize = -1;
        /**
         * 字体大小集，单位px，设置时必须与texts长度一致
         */
        public int[] textSizes = null;
        /**
         * 显示的字符
         */
        public String[] texts = null;

        public CellInfo() {
        }

        public CellInfo(
                int type,
                Object tag,
                int row,
                int line,
                float width,
                float height,
                int bgColor,
                int textColor,
                int[] textColors,
                int textSize,
                int[] textSizes,
                String[] texts) {
            this.type = type;
            this.tag = tag;
            this.row = row;
            this.line = line;
            this.width = width;
            this.height = height;
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.textColors = textColors;
            this.textSize = textSize;
            this.textSizes = textSizes;
            this.texts = texts;
        }

        public float getStartX() {
            return startX;
        }

        public float getStartY() {
            return startY;
        }
    }

    /**
     * 一个合并单元格的信息
     */
    public static class MergeInfo {
        public static final int TYPE_NORMAL = 1;
        public static final int TYPE_BUTTON = 2;
        /**
         * 合并后的单元格的类型
         */
        public int type = TYPE_NORMAL;
        /**
         * Object类型的tag标记
         */
        public Object tag = null;
        /**
         * 合并起始行下标
         */
        public int startRow;
        /**
         * 合并起始列下标
         */
        public int startLine;
        /**
         * 合并结束行下标
         */
        public int endRow;
        /**
         * 合并结束列下标
         */
        public int endLine;
        /**
         * 起始x坐标，不提供外部设置，只能获取
         */
        private float startX = 0.0f;
        /**
         * 起始y坐标，不提供外部设置，只能获取
         */
        private float startY = 0.0f;
        /**
         * 宽度，不提供外部设置，只能获取
         */
        private float width = -1.0f;
        /**
         * 高度，不提供外部设置，只能获取
         */
        private float height = -1.0f;
        /**
         * 背景颜色，默认时自动使用左上角的单元格颜色来填充
         */
        public int bgColor = 0;
        /**
         * 字体颜色，不设置时默认为Color.BLACK，设置后将覆盖textColors的值
         */
        public int textColor = 0;
        /**
         * 字体颜色集，设置时必须与texts长度一致
         */
        public int[] textColors = null;
        /**
         * 字体大小，单位px，不设置时默认为14sp对应的px值，设置后将覆盖textSizes的值
         */
        public int textSize = -1;
        /**
         * 字体大小集，单位px，设置时必须与texts长度一致
         */
        public int[] textSizes = null;
        /**
         * 合并后的单元格显示的字符，单元格合并后，不设置时，只显示左上角的单元格的字符
         */
        public String[] texts;

        public MergeInfo() {
        }

        public MergeInfo(
                int type,
                Object tag,
                int startRow,
                int startLine,
                int endRow,
                int endLine,
                int bgColor,
                int textColor,
                int[] textColors,
                int textSize,
                int[] textSizes,
                String[] texts) {
            this.type = type;
            this.tag = tag;
            this.startRow = startRow;
            this.startLine = startLine;
            this.endRow = endRow;
            this.endLine = endLine;
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.textColors = textColors;
            this.textSize = textSize;
            this.textSizes = textSizes;
            this.texts = texts;
        }

        public float getStartX() {
            return startX;
        }

        public float getStartY() {
            return startY;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }
    }

    /**
     * 监听单元格、合并后的单元格的点击事件
     */
    public interface OnCellClickListener {
        /**
         * 单元格点击时回调
         *
         * @param cellInfo 单元格信息
         */
        public void onCellClick(CellInfo cellInfo);

        /**
         * 合并后的单元格点击时回调
         *
         * @param mergeInfo 合并后的单元格信息
         */
        public void onMergedCellClick(MergeInfo mergeInfo);
    }

    private OnCellClickListener onCellClickListener;

    /**
     * 设置监听点击的事件
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

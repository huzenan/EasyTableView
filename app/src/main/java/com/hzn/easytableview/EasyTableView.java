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
    // 横向表头颜色，便于统一设置；至少2行才绘制，默认为Color.LTGRAY
    private int headerHColor;
    // 竖向表头颜色，便于统一设置；至少2列才绘制，默认为Color.LTGRAY
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
    private int outStrokeCorner;
    // 模式，分为MODE_NORMAL、MODE_FIX_WIDTH、MODE_FIX_HEIGHT和MODE_FIX_WIDTH_HEIGHT，默认为MODE_NORMAL
    private int mode;

    // 正常模式，表格的宽和高都由表格内容决定
    public static final int MODE_NORMAL = 0;
    // 固定宽模式，表格的宽由width属性决定，每一列的宽度平均分，高由表格内容决定
    public static final int MODE_FIX_WIDTH = 1;
    // 固定高模式，表格的高由height属性决定，每一行的高度平均分，宽由表格内容决定
    public static final int MODE_FIX_HEIGHT = 2;
    // 固定宽高模式，表格的宽、高分别由width、height属性决定，每一行、每一列都平均分
    public static final int MODE_FIX_WIDTH_HEIGHT = 3;

    private Paint paint;
    private Paint strokePaint;
    private TextPaint textPaint;
    private RectF bgRectF;
    private Path tPath;
    private RectF tRectF;
    private RectF tCornerRectF;

    // 单元格数据集合
    private ArrayList<CellInfo> cellInfoList;
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

        cellInfoList = new ArrayList<>();
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

        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        touchSlop = viewConfiguration.getScaledTouchSlop();
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
            for (int l = 0; l < lines; l++)
                widthArr[l] = fixWidth;
        }
        if (mode == MODE_FIX_HEIGHT || mode == MODE_FIX_WIDTH_HEIGHT) {
            float fixHeight = 1.0f * (bgRectF.bottom - bgRectF.top) / rows;
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
                for (int l = mergeInfo.startLine; l <= mergeInfo.endLine; l++) {
                    cellArr[r][l].mergeInfo = mergeInfo;
                    width += cellArr[r][l].width; // 为了方便，不另外循环计算了
                }
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
            int twiceCorner = outStrokeCorner * 2;
            paint.setColor(headerHVColor);
            tRectF.left = bgRectF.left;
            tRectF.top = bgRectF.top;
            tRectF.right = bgRectF.left + twiceCorner;
            tRectF.bottom = bgRectF.top + twiceCorner;
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
        // 至少2行才绘制
        if (cellArr.length > 1) {
            int twiceCorner = outStrokeCorner * 2;
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
        // 至少2列才绘制
        if (cellArr[0].length > 1) {
            int twiceCorner = outStrokeCorner * 2;
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
                        if (rows - 1 == 0 && lines - 1 == 0) {
                            // 只有一格
                            tPath.moveTo(tRectF.left - outStrokeCorner, tRectF.top);
                            addLeftTopCornerPath();
                            tPath.lineTo(tRectF.left, tRectF.bottom - outStrokeCorner);
                            addLeftBottomCornerPath();
                            tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.bottom);
                            addRightBottomCornerPath();
                            tPath.lineTo(tRectF.right, tRectF.top + outStrokeCorner);
                            addRightTopCornerPath();
                            tPath.close();
                            canvas.drawPath(tPath, paint);
                        } else if (rows - 1 == 0 && lines > 1) {
                            // 只有一行
                            if (l == 0) {
                                // 最左侧
                                tPath.moveTo(tRectF.left + outStrokeCorner, tRectF.top);
                                addLeftTopCornerPath();
                                tPath.lineTo(tRectF.left, tRectF.bottom - outStrokeCorner);
                                addLeftBottomCornerPath();
                                tPath.lineTo(tRectF.right, tRectF.bottom);
                                tPath.lineTo(tRectF.right, tRectF.top);
                                tPath.close();
                                canvas.drawPath(tPath, paint);
                            } else if (l == lines - 1) {
                                // 最右侧
                                tPath.moveTo(tRectF.left, tRectF.top);
                                tPath.lineTo(tRectF.left, tRectF.bottom);
                                tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.bottom);
                                addRightBottomCornerPath();
                                tPath.lineTo(tRectF.right, tRectF.top + outStrokeCorner);
                                addRightTopCornerPath();
                                tPath.close();
                                canvas.drawPath(tPath, paint);
                            } else {
                                // 中间
                                canvas.drawRect(tRectF.left, tRectF.top, tRectF.right, tRectF.bottom, paint);
                            }
                        } else if (rows > 1 && lines - 1 == 0) {
                            // 只有一列
                            if (r == 0) {
                                // 最上侧
                                tPath.moveTo(tRectF.left + outStrokeCorner, tRectF.top);
                                addLeftTopCornerPath();
                                tPath.lineTo(tRectF.left, tRectF.bottom);
                                tPath.lineTo(tRectF.right, tRectF.bottom);
                                tPath.lineTo(tRectF.right, tRectF.top + outStrokeCorner);
                                addRightTopCornerPath();
                                tPath.close();
                                canvas.drawPath(tPath, paint);
                            } else if (r == rows - 1) {
                                // 最下侧
                                tPath.moveTo(tRectF.left, tRectF.top);
                                tPath.lineTo(tRectF.left, tRectF.bottom - outStrokeCorner);
                                addLeftBottomCornerPath();
                                tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.bottom);
                                addRightBottomCornerPath();
                                tPath.lineTo(tRectF.right, tRectF.top);
                                tPath.close();
                                canvas.drawPath(tPath, paint);
                            } else {
                                // 中间
                                canvas.drawRect(tRectF.left, tRectF.top, tRectF.right, tRectF.bottom, paint);
                            }
                        } else if (r == 0 && l == 0) {
                            // 左上角
                            tPath.moveTo(tRectF.left + outStrokeCorner, tRectF.top);
                            addLeftTopCornerPath();
                            tPath.lineTo(tRectF.left, tRectF.bottom);
                            tPath.lineTo(tRectF.right, tRectF.bottom);
                            tPath.lineTo(tRectF.right, tRectF.top);
                            tPath.close();
                            canvas.drawPath(tPath, paint);
                        } else if (r == 0 && l == lines - 1) {
                            // 右上角
                            tPath.moveTo(tRectF.left, tRectF.top);
                            tPath.lineTo(tRectF.left, tRectF.bottom);
                            tPath.lineTo(tRectF.right, tRectF.bottom);
                            tPath.lineTo(tRectF.right, tRectF.top + outStrokeCorner);
                            addRightTopCornerPath();
                            tPath.close();
                            canvas.drawPath(tPath, paint);
                        } else if (r == rows - 1 && l == 0) {
                            // 左下角
                            tPath.moveTo(tRectF.left, tRectF.top);
                            tPath.lineTo(tRectF.left, tRectF.bottom - outStrokeCorner);
                            addLeftBottomCornerPath();
                            tPath.lineTo(tRectF.right, tRectF.bottom);
                            tPath.lineTo(tRectF.right, tRectF.top);
                            tPath.close();
                            canvas.drawPath(tPath, paint);
                        } else if (r == rows - 1 && l == lines - 1) {
                            // 右下角
                            tPath.moveTo(tRectF.left, tRectF.top);
                            tPath.lineTo(tRectF.left, tRectF.bottom);
                            tPath.lineTo(tRectF.right - outStrokeCorner, tRectF.bottom);
                            addRightBottomCornerPath();
                            tPath.lineTo(tRectF.right, tRectF.top);
                            tPath.close();
                            canvas.drawPath(tPath, paint);
                        } else {
                            // 中间
                            canvas.drawRect(tRectF.left, tRectF.top, tRectF.right, tRectF.bottom, paint);
                        }
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
        float halfStrokeWidth = strokeSize / 2.0f;

        for (MergeInfo mergeInfo : mergeInfoList) {
            paint.setColor(mergeInfo.bgColor);
            tRectF.left = mergeInfo.startX + halfStrokeWidth;
            tRectF.top = mergeInfo.startY + halfStrokeWidth;
            tRectF.right = mergeInfo.startX + mergeInfo.width - halfStrokeWidth;
            tRectF.bottom = mergeInfo.startY + mergeInfo.height - halfStrokeWidth;
            canvas.drawRect(tRectF, paint);

            // 绘制合并后的单元格内的字符
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
        }
    }

    // 绘制边框
    private void drawOutStroke(Canvas canvas) {
        if (outStrokeSize > 0) {
            strokePaint.setColor(outStrokeColor);
            strokePaint.setStrokeWidth(outStrokeSize);

            tPath.reset();
            tPath.moveTo(bgRectF.left + outStrokeCorner, bgRectF.top);
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

        this.cellInfoList.addAll(cellInfoList);
        int size = cellInfoList.size();

        // 清除原有数据
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                cellArr[r][l] = new CellInfo();
        if (null != mergeInfoList && mergeInfoList.size() > 0)
            mergeInfoList.clear();

        // 赋值
        for (int i = 0; i < size; i++) {
            CellInfo cellInfo = this.cellInfoList.get(i);
            cellArr[cellInfo.row][cellInfo.line] = cellInfo;

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
                heightArr[r] = maxHeight;
            }
        }

        // 刷新
        requestLayout();
        invalidate();
    }

    /**
     * 合并单元格
     *
     * @param mergeInfoList 合并单元格数据集
     */
    public void mergeCells(ArrayList<MergeInfo> mergeInfoList) {
        if (null == mergeInfoList || mergeInfoList.size() == 0)
            return;

        this.mergeInfoList.addAll(mergeInfoList);
        int size = mergeInfoList.size();
        for (int i = 0; i < size; i++) {
            MergeInfo mergeInfo = mergeInfoList.get(i);

            if (mergeInfo.bgColor == 0)
                mergeInfo.bgColor = cellArr[mergeInfo.startRow][mergeInfo.startLine].bgColor;

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

        // 刷新
        requestLayout();
        invalidate();
    }

    /**
     * 设置行数
     *
     * @param rows 行数
     */
    public void setRows(int rows) {
        this.rows = rows;
        requestLayout();
        invalidate();
    }

    /**
     * 设置列数
     *
     * @param lines 列数
     */
    public void setLines(int lines) {
        this.lines = lines;
        requestLayout();
        invalidate();
    }

    /**
     * 一个单元格的信息
     */
    public static class CellInfo {
        public static final int TYPE_NONE = 0;
        public static final int TYPE_NORMAL = 1;
        public static final int TYPE_BUTTON = 2;
        /**
         * 单元格的类型
         */
        public int type = TYPE_NONE;
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
         * 起始x坐标，不提供外部设置
         */
        private float startX = 0.0f;
        /**
         * 起始y坐标，不提供外部设置
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
        /**
         * 在进行合并单元格操作时，会将此单元格与相应的MergeInfo绑定，取消合并时置为空；
         * 同一单元格被多次合并时，将绑定为最后一次合并的MergeInfo，不建议这样做；
         * 当mergeInfo不为空时，该单元格的信息将使用mergeInfo的信息
         */
        public MergeInfo mergeInfo = null;

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
         * 起始x坐标，不提供外部设置
         */
        private float startX = 0.0f;
        /**
         * 起始y坐标，不提供外部设置
         */
        private float startY = 0.0f;
        /**
         * 宽度，不提供外部设置
         */
        private float width = -1.0f;
        /**
         * 高度，不提供外部设置
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
         * 字体大小集，单位px，，设置时必须与texts长度一致
         */
        public int[] textSizes = null;
        /**
         * 合并后的单元格显示的字符，单元格合并后，不设置时，只显示左上角的单元格的字符
         */
        public String[] texts;
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

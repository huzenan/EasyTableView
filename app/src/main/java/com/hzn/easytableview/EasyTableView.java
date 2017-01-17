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
    // 双向表头，默认为Color.LTGRAY
    private int headerHVColor;
    // 横向表头颜色，默认为Color.LTGRAY
    private int headerHColor;
    // 竖向表头颜色，默认为Color.LTGRAY
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

    // 表格宽
    private int width;
    // 表格高
    private int height;

    private Paint paint;
    private Paint strokePaint;
    private TextPaint textPaint;
    private RectF bgRectF;
    private Path tPath;
    private RectF tRectF;

    // 合并单元格的数据集合
    private ArrayList<MergeInfo> mergeInfoList;
    // 单元格宽度集合
    private float[] widthArr;
    // 单元格高度集合
    private float[] heightArr;
    // 单元格数据集合
    private GridInfo[][] gridArr;

    private Object curTouchCell;


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

        mergeInfoList = new ArrayList<>();
        widthArr = new float[lines];
        heightArr = new float[rows];

        // 初始化单元格
        if (rows != 0 && lines != 0) {
            gridArr = new GridInfo[rows][lines];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < lines; j++)
                    gridArr[i][j] = new GridInfo();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        if (measureMode != MeasureSpec.EXACTLY) { // wrap_content
            // 每一列占用的宽度和
            width = 0;
            for (int i = 0; i < lines; i++)
                width = (int) (width + widthArr[i] + strokeSize);
            width = width - strokeSize + outStrokeSize + getPaddingLeft() + getPaddingRight();
        }

        measureMode = MeasureSpec.getMode(heightMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        if (measureMode != MeasureSpec.EXACTLY) { // wrap_content
            // 每一行占用的高度和
            height = 0;
            for (int i = 0; i < rows; i++)
                height = (int) (height + heightArr[i] + strokeSize);
            height = height - strokeSize + outStrokeSize + getPaddingTop() + getPaddingBottom();
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
            float fixWidth = 1.0f * (width - outStrokeSize) / lines;
            for (int i = 0; i < lines; i++)
                widthArr[i] = fixWidth;
        }
        if (mode == MODE_FIX_HEIGHT || mode == MODE_FIX_WIDTH_HEIGHT) {
            float fixHeight = 1.0f * (height - outStrokeSize) / rows;
            for (int i = 0; i < rows; i++)
                heightArr[i] = fixHeight;
        }

        // 初始化每个单元格的起始x、y坐标，并统一设置row、line，统一设置width、height为最大值（需要以最大值为标准）
        float startY = 0.0f + outStrokeSize / 2.0f;
        for (int i = 0; i < rows; i++) {
            float startX = 0.0f + outStrokeSize / 2.0f;
            for (int j = 0; j < lines; j++) {
                gridArr[i][j].row = i;
                gridArr[i][j].height = j;
                gridArr[i][j].startX = startX;
                gridArr[i][j].startY = startY;
                gridArr[i][j].width = widthArr[j];
                gridArr[i][j].height = heightArr[i];
                startX += widthArr[j];
            }
            startY += heightArr[i];
        }

        // 初始化合并后的单元格的startX、startY、width和height
        int mergeInfoSize = mergeInfoList.size();
        for (int i = 0; i < mergeInfoSize; i++) {
            MergeInfo mergeInfo = mergeInfoList.get(i);
            mergeInfo.startX = gridArr[mergeInfo.startRow][mergeInfo.startLine].startX;
            mergeInfo.startY = gridArr[mergeInfo.startRow][mergeInfo.startLine].startY;
            float width = 0.0f;
            float height = 0.0f;
            for (int r = mergeInfo.startRow; r <= mergeInfo.endRow; r++) {
                width = 0.0f;
                for (int l = mergeInfo.startLine; l <= mergeInfo.endLine; l++) {
                    gridArr[r][l].mergeInfo = mergeInfo;
                    width += gridArr[r][l].width; // 为了方便，不另外循环计算了
                }
                height += gridArr[r][0].height;
            }
            mergeInfo.width = width;
            mergeInfo.height = height;
        }
    }

    // 绘制背景
    private void drawBg(Canvas canvas) {
        paint.setColor(bgColor);
        canvas.drawRoundRect(bgRectF, outStrokeCorner / 2.0f, outStrokeCorner / 2.0f, paint);
    }

    // 绘制双向表头
    private void drawHeaderVH(Canvas canvas) {
        paint.setColor(headerHVColor);
        tRectF.left = bgRectF.left;
        tRectF.top = bgRectF.top;
        tRectF.right = bgRectF.left + outStrokeCorner;
        tRectF.bottom = bgRectF.top + outStrokeCorner;
        tPath.moveTo(bgRectF.left + widthArr[0], bgRectF.top);
        tPath.lineTo(bgRectF.left + outStrokeCorner, bgRectF.top);
        tPath.addArc(tRectF, -90.0f, -90.0f);
        tPath.lineTo(bgRectF.left, bgRectF.top + heightArr[0]);
        tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top + heightArr[0]);
        tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top);
        tPath.close();
        canvas.drawPath(tPath, paint);
    }

    private void drawHeaderH(Canvas canvas) {
        // 绘制横向表头
        paint.setColor(headerHColor);
        tRectF.left = bgRectF.right - outStrokeCorner;
        tRectF.top = bgRectF.top;
        tRectF.right = bgRectF.right;
        tRectF.bottom = bgRectF.top + outStrokeCorner;
        tPath.reset();
        tPath.moveTo(bgRectF.left + widthArr[0], bgRectF.top);
        tPath.lineTo(bgRectF.right - outStrokeCorner, bgRectF.top);
        tPath.addArc(tRectF, -90.0f, 90.0f);
        tPath.lineTo(bgRectF.right, bgRectF.top + heightArr[0]);
        tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top + heightArr[0]);
        tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top);
        tPath.close();
        canvas.drawPath(tPath, paint);

        // 绘制横向表头的字符
        for (int l = 1; l < gridArr[0].length; l++) {
            if (null != gridArr[0][l].texts && gridArr[0][l].texts.length > 0) {
                int textRows = gridArr[0][l].texts.length;
                float h = gridArr[0][l].height;
                float w = gridArr[0][l].width;
                float[] textHeights = new float[textRows];
                float originX;
                float baseLine;
                Paint.FontMetrics fm;
                float textsTotalHeight = 0.0f;
                for (int t = 0; t < textRows; t++) {
                    textPaint.setTextSize(gridArr[0][l].textSizes[t]);
                    fm = textPaint.getFontMetrics();
                    textHeights[t] = fm.bottom - fm.top;
                    textsTotalHeight += textHeights[t];
                }
                float top = (h - textsTotalHeight) / 2.0f;
                for (int t = 0; t < textRows; t++) {
                    String text = gridArr[0][l].texts[t];
                    if (null != text && text.length() > 0) {
                        textPaint.setTextSize(gridArr[0][l].textSizes[t]);
                        textPaint.setColor(gridArr[0][l].textColors[t]);
                        fm = textPaint.getFontMetrics();
                        originX = gridArr[0][l].startX + w / 2.0f - textPaint.measureText(text) / 2.0f;
                        baseLine = gridArr[0][l].startY + top + textHeights[t] / 2.0f - (fm.ascent + fm.descent) / 2.0f;
                        canvas.drawText(text, originX, baseLine, textPaint);
                    }
                    top += textHeights[t];
                }
            }
        }
    }

    private void drawHeaderV(Canvas canvas) {
        // 绘制竖向表头
        paint.setColor(headerVColor);
        tRectF.left = bgRectF.left;
        tRectF.top = bgRectF.bottom - outStrokeCorner;
        tRectF.right = bgRectF.left + outStrokeCorner;
        tRectF.bottom = bgRectF.bottom;
        tPath.reset();
        tPath.moveTo(bgRectF.left, bgRectF.top + heightArr[0]);
        tPath.lineTo(bgRectF.left, bgRectF.bottom - outStrokeCorner);
        tPath.addArc(tRectF, 180.0f, -90.0f);
        tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.bottom);
        tPath.lineTo(bgRectF.left + widthArr[0], bgRectF.top + heightArr[0]);
        tPath.lineTo(bgRectF.left, bgRectF.top + heightArr[0]);
        tPath.close();
        canvas.drawPath(tPath, paint);

        // 绘制竖向表头的字符
        for (int r = 1; r < gridArr.length; r++) {
            if (null != gridArr[r][0].texts && gridArr[r][0].texts.length > 0) {
                int textRows = gridArr[r][0].texts.length;
                float h = gridArr[r][0].height;
                float w = gridArr[r][0].width;
                float[] textHeights = new float[textRows];
                float originX;
                float baseLine;
                Paint.FontMetrics fm;
                float textsTotalHeight = 0.0f;
                for (int t = 0; t < textRows; t++) {
                    textPaint.setTextSize(gridArr[r][0].textSizes[t]);
                    fm = textPaint.getFontMetrics();
                    textHeights[t] = fm.bottom - fm.top;
                    textsTotalHeight += textHeights[t];
                }
                float top = (h - textsTotalHeight) / 2.0f;
                for (int t = 0; t < textRows; t++) {
                    String text = gridArr[r][0].texts[t];
                    if (null != text && text.length() > 0) {
                        textPaint.setTextSize(gridArr[r][0].textSizes[t]);
                        textPaint.setColor(gridArr[r][0].textColors[t]);
                        fm = textPaint.getFontMetrics();
                        originX = gridArr[r][0].startX + w / 2.0f - textPaint.measureText(text) / 2.0f;
                        baseLine = gridArr[r][0].startY + top + textHeights[t] / 2.0f - (fm.ascent + fm.descent) / 2.0f;
                        canvas.drawText(text, originX, baseLine, textPaint);
                    }
                    top += textHeights[t];
                }
            }
        }
    }

    // 绘制单元格内容 TODO 右下角的圆角
    private void drawCellsInfo(Canvas canvas) {
        float halfStrokeWidth = strokeSize / 2.0f;

        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < lines; j++) {
                if (gridArr[i][j].type != GridInfo.TYPE_NONE) {
                    // 绘制该单元格背景
                    if (gridArr[i][j].bgColor != 0) {
                        this.paint.setColor(gridArr[i][j].bgColor);
                        canvas.drawRect(
                                gridArr[i][j].startX + halfStrokeWidth,
                                gridArr[i][j].startY + halfStrokeWidth,
                                gridArr[i][j].startX + gridArr[i][j].width - halfStrokeWidth,
                                gridArr[i][j].startY + gridArr[i][j].height - halfStrokeWidth,
                                paint);
                    }

                    // 绘制单元格内的字符
                    if (null != gridArr[i][j].texts && gridArr[i][j].texts.length > 0) {
                        int textRows = gridArr[i][j].texts.length;
                        float h = gridArr[i][j].height;
                        float w = gridArr[i][j].width;
                        float[] textHeights = new float[textRows];
                        float originX;
                        float baseLine;
                        Paint.FontMetrics fm;
                        float textsTotalHeight = 0.0f;
                        for (int t = 0; t < textRows; t++) {
                            textPaint.setTextSize(gridArr[i][j].textSizes[t]);
                            fm = textPaint.getFontMetrics();
                            textHeights[t] = fm.bottom - fm.top;
                            textsTotalHeight += textHeights[t];
                        }
                        float top = (h - textsTotalHeight) / 2.0f;
                        for (int t = 0; t < textRows; t++) {
                            String text = gridArr[i][j].texts[t];
                            if (null != text && text.length() > 0) {
                                textPaint.setTextSize(gridArr[i][j].textSizes[t]);
                                textPaint.setColor(gridArr[i][j].textColors[t]);
                                fm = textPaint.getFontMetrics();
                                originX = gridArr[i][j].startX + w / 2.0f - textPaint.measureText(text) / 2.0f;
                                baseLine = gridArr[i][j].startY + top + textHeights[t] / 2.0f - (fm.ascent + fm.descent) / 2.0f;
                                canvas.drawText(text, originX, baseLine, textPaint);
                            }
                            top += textHeights[t];
                        }
                    }
                }
            }
        }
    }

    // 绘制线段
    private void drawStrokes(Canvas canvas) {
        strokePaint.setColor(strokeColor);
        strokePaint.setStrokeWidth(strokeSize);
        for (int i = 1; i < rows; i++)
            canvas.drawLine(gridArr[i][0].startX, gridArr[i][0].startY, bgRectF.right, gridArr[i][0].startY, strokePaint);
        for (int i = 1; i < lines; i++)
            canvas.drawLine(gridArr[0][i].startX, gridArr[0][i].startY, gridArr[0][i].startX, bgRectF.bottom, strokePaint);
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
        strokePaint.setColor(outStrokeColor);
        strokePaint.setStrokeWidth(outStrokeSize);
        tPath.reset();

        tPath.moveTo(bgRectF.left + outStrokeCorner / 2.0f, bgRectF.top);

        tRectF.left = bgRectF.left;
        tRectF.top = bgRectF.top;
        tRectF.right = bgRectF.left + outStrokeCorner;
        tRectF.bottom = bgRectF.top + outStrokeCorner;
        tPath.addArc(tRectF, -90.0f, -90.0f);

        tPath.lineTo(bgRectF.left, bgRectF.bottom - outStrokeCorner / 2.0f);

        tRectF.left = bgRectF.left;
        tRectF.top = bgRectF.bottom - outStrokeCorner;
        tRectF.right = bgRectF.left + outStrokeCorner;
        tRectF.bottom = bgRectF.bottom;
        tPath.addArc(tRectF, 180.0f, -90.0f);

        tPath.lineTo(bgRectF.right - outStrokeCorner / 2.0f, bgRectF.bottom);

        tRectF.left = bgRectF.right - outStrokeCorner;
        tRectF.top = bgRectF.bottom - outStrokeCorner;
        tRectF.right = bgRectF.right;
        tRectF.bottom = bgRectF.bottom;
        tPath.addArc(tRectF, 90.0f, -90.0f);

        tPath.lineTo(bgRectF.right, bgRectF.top + outStrokeCorner / 2.0f);

        tRectF.left = bgRectF.right - outStrokeCorner;
        tRectF.top = bgRectF.top;
        tRectF.right = bgRectF.right;
        tRectF.bottom = bgRectF.top + outStrokeCorner;
        tPath.addArc(tRectF, 0.0f, -90.0f);

        tPath.lineTo(bgRectF.left + outStrokeCorner / 2.0f, bgRectF.top);

        canvas.drawPath(tPath, strokePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != onCellClickListener) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    float downX = event.getX();
                    float downY = event.getY();
                    curTouchCell = getCellByXY(downX, downY);
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    float downX = event.getX();
                    float downY = event.getY();
                    Object cell = getCellByXY(downX, downY);
                    // 按下和释放的是同一个cell
                    if (cell instanceof GridInfo && curTouchCell instanceof GridInfo &&
                            ((GridInfo) cell).row == ((GridInfo) curTouchCell).row &&
                            ((GridInfo) cell).line == ((GridInfo) curTouchCell).line) {
                        onCellClickListener.onCellClick((GridInfo) cell);
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
                if (gridArr[r][l].startX <= x && x <= gridArr[r][l].startX + gridArr[r][l].width &&
                        gridArr[r][l].startY <= y && y <= gridArr[r][l].startY + gridArr[r][l].height)
                    return gridArr[r][l];

        return null;
    }

    /**
     * 设置数据项，包含表头的内容，将清除表格原有数据，包括合并单元格的数据
     *
     * @param gridInfoList 数据项
     */
    public void setData(ArrayList<GridInfo> gridInfoList) {
        int size = gridInfoList.size();

        // 清除原有数据
        for (int r = 0; r < rows; r++)
            for (int l = 0; l < lines; l++)
                gridArr[r][l] = new GridInfo();
        if (null != mergeInfoList && mergeInfoList.size() > 0)
            mergeInfoList.clear();

        // 赋值
        for (int i = 0; i < size; i++) {
            GridInfo gridInfo = gridInfoList.get(i);
            gridArr[gridInfo.row][gridInfo.line] = gridInfo;

            if (null != gridInfo.texts && gridInfo.texts.length > 0) {
                if (gridInfo.textColor == 0 && null == gridInfo.textColors) // textColor和textColors都没有设置
                    gridInfo.textColor = Color.BLACK;
                if (gridInfo.textColor != 0) { // 设置了textColor，则覆盖textColors
                    gridInfo.textColors = new int[gridInfo.texts.length];
                    for (int t = 0; t < gridInfo.texts.length; t++)
                        gridInfo.textColors[t] = gridInfo.textColor;
                }

                if (gridInfo.textSize == -1 && null == gridInfo.textSizes) // textSize和textSizes都没有设置
                    gridInfo.textSize = spToPx(14);
                if (gridInfo.textSize != -1) { // 设置了textSize，则覆盖textSizes
                    gridInfo.textSizes = new int[gridInfo.texts.length];
                    for (int t = 0; t < gridInfo.texts.length; t++)
                        gridInfo.textSizes[t] = gridInfo.textSize;
                }
            }
        }

        // 计算出每一列的最大宽度
        int textRows;
        if (mode != MODE_FIX_WIDTH && mode != MODE_FIX_WIDTH_HEIGHT) {
            float txtWidth;
            float maxWidth;
            for (int i = 0; i < lines; i++) {
                maxWidth = 0.0f;
                for (int j = 0; j < rows; j++) {
                    if (null != gridArr[j][i].texts) {
                        if (gridArr[j][i].width < 0) { // 根据测量的字符宽度来计算
                            textRows = gridArr[j][i].texts.length;
                            for (int t = 0; t < textRows; t++) {
                                textPaint.setTextSize(gridArr[j][i].textSizes[t]);
                                txtWidth = textPaint.measureText(gridArr[j][i].texts[t]);
                                if (maxWidth < txtWidth)
                                    maxWidth = txtWidth;
                            }
                        } else { // 根据设置的值来计算
                            if (maxWidth < gridArr[j][i].width)
                                maxWidth = gridArr[j][i].width;
                        }
                    }
                }
                widthArr[i] = maxWidth + strokeSize;
            }
            widthArr[0] = widthArr[0] - strokeSize / 2.0f + outStrokeSize / 2.0f;
            widthArr[lines - 1] = widthArr[lines - 1] - strokeSize / 2.0f + outStrokeSize / 2.0f;
        }

        // 计算出每一行的最大高度
        if (mode != MODE_FIX_HEIGHT && mode != MODE_FIX_WIDTH_HEIGHT) {
            Paint.FontMetrics fm;
            float txtHeight;
            float tempHeight;
            float maxHeight;
            for (int i = 0; i < rows; i++) {
                maxHeight = 0.0f;
                for (int j = 0; j < lines; j++) {
                    if (null != gridArr[i][j].texts) {
                        if (gridArr[i][j].height < 0) { // 根据测量的字符高度来计算
                            tempHeight = 0.0f;
                            textRows = gridArr[i][j].texts.length;
                            for (int t = 0; t < textRows; t++) {
                                textPaint.setTextSize(gridArr[i][j].textSizes[t]);
                                fm = textPaint.getFontMetrics();
                                txtHeight = fm.bottom - fm.top;
                                tempHeight += txtHeight;
                            }

                            if (maxHeight < tempHeight)
                                maxHeight = tempHeight;
                        } else { // 根据设置的值来计算
                            if (maxHeight < gridArr[i][j].height)
                                maxHeight = gridArr[i][j].height;
                        }
                    }
                }
                heightArr[i] = maxHeight + strokeSize;
            }
            heightArr[0] = heightArr[0] - strokeSize / 2.0f + outStrokeSize / 2.0f;
            heightArr[rows - 1] = heightArr[rows - 1] - strokeSize / 2.0f + outStrokeSize / 2.0f;
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
                mergeInfo.bgColor = gridArr[mergeInfo.startRow][mergeInfo.startLine].bgColor;

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
     * 一个单元格的信息
     */
    public static class GridInfo {
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

    private int spToPx(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 监听单元格、合并后的单元格的点击事件
     */
    public interface OnCellClickListener {
        /**
         * 单元格点击时回调
         *
         * @param gridInfo 单元格信息
         */
        public void onCellClick(GridInfo gridInfo);

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
}

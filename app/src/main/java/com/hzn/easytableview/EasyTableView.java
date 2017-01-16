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
import android.view.View;

import java.util.ArrayList;

/**
 * 表格视图，带有横竖向表头，表格体类型可扩展，单个单元格支持多行显示，支持合并、取消合并单元格
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
    // 横向表头字体颜色，默认为Color.BLACK
    private int headerHTextColor;
    // 竖向表头字体颜色，默认为Color.BLACK
    private int headerVTextColor;
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
        headerHTextColor = a.getColor(R.styleable.EasyTableView_etvHeaderHTextColor, Color.BLACK);
        headerVTextColor = a.getColor(R.styleable.EasyTableView_etvHeaderVTextColor, Color.BLACK);
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

    /////////////////////////////////////////////////////////////////////
    private void testData() {
        ArrayList<GridInfo> testData = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            GridInfo gridInfo = new GridInfo();
            gridInfo.type = GridInfo.TYPE_NORMAL;
            gridInfo.row = 0;
            gridInfo.line = i;
            gridInfo.texts = new String[2];
            gridInfo.texts[0] = "周" + i;
            gridInfo.texts[1] = "1/" + (15 + i);
            gridInfo.textSizes = new int[2];
            gridInfo.textSizes[0] = spToPx(getContext(), 12);
            gridInfo.textSizes[1] = spToPx(getContext(), 10);
            gridInfo.textColors = new int[2];
            gridInfo.textColors[0] = Color.BLACK;
            gridInfo.textColors[1] = Color.BLACK;
            testData.add(gridInfo);
        }

        GridInfo gridInfo1 = new GridInfo();
        gridInfo1.type = GridInfo.TYPE_NORMAL;
        gridInfo1.row = 1;
        gridInfo1.line = 0;
        gridInfo1.texts = new String[1];
        gridInfo1.texts[0] = "上午";
        gridInfo1.textSizes = new int[1];
        gridInfo1.textSizes[0] = spToPx(getContext(), 14);
        gridInfo1.textColors = new int[1];
        gridInfo1.textColors[0] = Color.LTGRAY;
        testData.add(gridInfo1);

        GridInfo gridInfo2 = new GridInfo();
        gridInfo2.type = GridInfo.TYPE_NORMAL;
        gridInfo2.row = 2;
        gridInfo2.line = 0;
        gridInfo2.texts = new String[1];
        gridInfo2.texts[0] = "下午";
        gridInfo2.textSizes = new int[1];
        gridInfo2.textSizes[0] = spToPx(getContext(), 14);
        gridInfo2.textColors = new int[1];
        gridInfo2.textColors[0] = Color.LTGRAY;
        testData.add(gridInfo2);

        GridInfo gridInfo3 = new GridInfo();
        gridInfo3.type = GridInfo.TYPE_BUTTON;
        gridInfo3.row = 1;
        gridInfo3.line = 6;
        gridInfo3.texts = new String[1];
        gridInfo3.texts[0] = "约满";
        gridInfo3.textSizes = new int[1];
        gridInfo3.textSizes[0] = spToPx(getContext(), 14);
        gridInfo3.textColors = new int[1];
        gridInfo3.textColors[0] = Color.BLACK;
        gridInfo3.bgColor = Color.LTGRAY;
        testData.add(gridInfo3);

        setData(testData);
    }

    public int spToPx(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    /////////////////////////////////////////////////////////////////////////


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

        // TODO 测试数据
        testData();

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO 绘制背景 用arc绘制
        paint.setColor(bgColor);
        canvas.drawRoundRect(bgRectF, outStrokeCorner, outStrokeCorner, paint);

        // 绘制双向表头
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

        // 绘制线段
        strokePaint.setColor(strokeColor);
        strokePaint.setStrokeWidth(strokeSize);
        for (int i = 1; i < rows; i++)
            canvas.drawLine(gridArr[i][0].startX, gridArr[i][0].startY, bgRectF.right, gridArr[i][0].startY, strokePaint);
        for (int i = 1; i < lines; i++)
            canvas.drawLine(gridArr[0][i].startX, gridArr[0][i].startY, gridArr[0][i].startX, bgRectF.bottom, strokePaint);


        // 绘制边框
        float halfStrokeWidth = strokeSize / 2.0f;
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


        // 绘制单元格内容
        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < lines; j++) {
                if (gridArr[i][j].type != GridInfo.TYPE_NONE) {
                    // 绘制该单元格背景
                    if (gridArr[i][j].bgColor != -1) {
                        this.paint.setColor(gridArr[i][j].bgColor);
                        canvas.drawRect(
                                gridArr[i][j].startX + halfStrokeWidth,
                                gridArr[i][j].startY + halfStrokeWidth,
                                gridArr[i][j].startX + gridArr[i][j].width - halfStrokeWidth,
                                gridArr[i][j].startY + gridArr[i][j].height - halfStrokeWidth,
                                paint);
                    }
                }
            }
        }
    }

    /**
     * 获取某一列占用的最大宽度
     *
     * @param line 指定的列
     * @return line列占用的最大宽度
     */
//    private float getMaxWidthInLine(int line) {
//        float max = 0;
//        for (int i = 0; i < rows; i++) {
//            if (max < gridArr[i][line].maxWidth)
//                max = gridArr[i][line].maxWidth;
//        }
//        return max;
//    }

    /**
     * 获取某一行占用的最大高度
     *
     * @param row 指定的行
     * @return row行占用的最大高度
     */
//    private float getMaxHeightInRow(int row) {
//        float max = 0;
//        for (int i = 0; i < lines; i++) {
//            if (max < gridArr[i][row].maxHeight)
//                max = gridArr[i][row].maxHeight;
//        }
//        return max;
//    }

    /**
     * 设置数据项，包含表头的内容
     *
     * @param gridInfoList 数据项
     */
    public void setData(ArrayList<GridInfo> gridInfoList) {
        int size = gridInfoList.size();

        // 赋值
        for (int i = 0; i < size; i++) {
            GridInfo gridInfo = gridInfoList.get(i);
            gridArr[gridInfo.row][gridInfo.line] = gridInfo;
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

        // 刷新 TODO
//        requestLayout();
//        invalidate();
    }

    /**
     * 一个单元格的信息
     */
    public class GridInfo {
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
         * 起始x坐标
         */
        public float startX = 0.0f;
        /**
         * 起始y坐标
         */
        public float startY = 0.0f;
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
        public int bgColor = -1;
        /**
         * 字体颜色，以text[]的长度为准，TODO 提供的数组长度不够时自动使用最后一个元素向后扩展
         */
        public int[] textColors = null;
        /**
         * 字体大小，以text[]的长度为准，TODO 提供的数组长度不够时自动使用最后一个元素向后扩展
         */
        public int[] textSizes = null;
        /**
         * 显示的字符，设置后必须设置相应的textColors和textSizes
         */
        public String[] texts = null;
        /**
         * 在进行合并单元格操作时会指向相应的MergeInfo，取消合并时置为空
         */
        public MergeInfo mergeInfo = null;
    }

    /**
     * 一个合并单元格的信息
     */
    public class MergeInfo {
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
         * 颜色，默认时自动使用左上角的单元格颜色来填充
         */
        public int color;
        /**
         * 合并后的单元格显示的字符，单元格合并后，只显示左上角的单元格的字符 TODO size color
         */
        public String[] texts;
    }
}

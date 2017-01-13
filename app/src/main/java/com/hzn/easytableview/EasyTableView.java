package com.hzn.easytableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
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
    private int lineColor;
    // 线段粗细，默认为1dp
    private int lineSize;
    // 边框颜色，默认为Color.GRAY
    private int outLineColor;
    // 边框粗细，默认为1dp
    private int outLineSize;
    // 横向表头字体颜色，默认为Color.BLACK
    private int headerHTextColor;
    // 竖向表头字体颜色，默认为Color.BLACK
    private int headerVTextColor;


    private TextPaint textPaint;


    // 横向表头数据集合
    private ArrayList<GridInfo> headerHList;
    // 竖向表头数据集合
    private ArrayList<GridInfo> headerVList;
    // 合并单元格的数据集合
    private ArrayList<MergeInfo> mergeInfoList;
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
        lineColor = a.getColor(R.styleable.EasyTableView_etvLineColor, Color.GRAY);
        lineSize = a.getDimensionPixelSize(R.styleable.EasyTableView_etvLineSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        outLineColor = a.getColor(R.styleable.EasyTableView_etvOutLineColor, Color.GRAY);
        outLineSize = a.getDimensionPixelSize(R.styleable.EasyTableView_etvOutLineSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        headerHTextColor = a.getColor(R.styleable.EasyTableView_etvHeaderHTextColor, Color.BLACK);
        headerVTextColor = a.getColor(R.styleable.EasyTableView_etvHeaderVTextColor, Color.BLACK);
        a.recycle();

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);

        headerHList = new ArrayList<>();
        headerVList = new ArrayList<>();
        mergeInfoList = new ArrayList<>();

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
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (mode != MeasureSpec.EXACTLY) { // wrap_content
            // 每一列占用的宽度和
            for (int i = 0; i < lines; i++)
                width += getMaxWidthInLine(i);

            // TODO
        }
    }

    /**
     * 获取某一列占用的最大宽度
     *
     * @param line 指定的列
     * @return line列占用的最大宽度
     */
    private float getMaxWidthInLine(int line) {
        float max = 0;
        for (int i = 0; i < rows; i++) {
            if (max < gridArr[i][line].maxWidth)
                max = gridArr[i][line].maxWidth;
        }
        return max;
    }

    /**
     * 一个单元格的信息
     */
    public class GridInfo {
        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_BUTTON = 1;
        /**
         * 单元格的类型
         */
        public int type;
        /**
         * Object类型的tag标记
         */
        public Object tag;
        /**
         * 所在行
         */
        public int row;
        /**
         * 所在列
         */
        public int line;
        /**
         * 最大宽度，不设置时，将在设置单元格时，按照字符来计算
         */
        public float maxWidth;
        /**
         * 最大高度，不设置时，将在设置单元格时，按照字符来计算
         */
        public float maxHeight;
        /**
         * 背景颜色，只有设置时才特殊填充，否则用该单元格默认的颜色填充
         */
        public int bgColor;
        /**
         * 字体颜色，以text[]的长度为准，提供的数组长度不够时自动使用最后一个元素向后扩展
         */
        public int[] textColors;
        /**
         * 字体大小，以text[]的长度为准，提供的数组长度不够时自动使用最后一个元素向后扩展
         */
        public int[] textSizes;
        /**
         * 显示的字符
         */
        public String[] texts;
        /**
         * 在进行合并单元格操作时会指向相应的MergeInfo，取消合并时置为空
         */
        public MergeInfo mergeInfo;
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

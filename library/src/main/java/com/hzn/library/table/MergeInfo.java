package com.hzn.library.table;

/**
 * 一个合并单元格的信息
 * <br/>
 * Created by huzn on 2017/9/27.
 */
public class MergeInfo {
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
    float startX = 0.0f;
    /**
     * 起始y坐标，不提供外部设置，只能获取
     */
    float startY = 0.0f;
    /**
     * 宽度，不提供外部设置，只能获取
     */
    float width = -1.0f;
    /**
     * 高度，不提供外部设置，只能获取
     */
    float height = -1.0f;
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

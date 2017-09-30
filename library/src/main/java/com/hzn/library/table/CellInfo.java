package com.hzn.library.table;

/**
 * 一个单元格的信息
 * <br/>
 * Created by huzn on 2017/9/27.
 */
public class CellInfo {
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
    float startX = 0.0f;
    /**
     * 起始y坐标，不提供外部设置，只能获取
     */
    float startY = 0.0f;
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

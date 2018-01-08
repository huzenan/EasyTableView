package com.hzn.library.table;

/**
 * Information of a merged cell.
 * <br/>
 * Created by huzn on 2017/9/27.
 */
public class MergeInfo {
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_BUTTON = 2;
    /**
     * Type of cell after merged.
     */
    public int type = TYPE_NORMAL;
    /**
     * Tag of merged cell.
     */
    public Object tag = null;
    /**
     * Starting row of merged cell.
     */
    public int startRow;
    /**
     * Starting line of merged cell.
     */
    public int startLine;
    /**
     * ending row of merged cell.
     */
    public int endRow;
    /**
     * ending line of merged cell.
     */
    public int endLine;
    /**
     * Starting x-coordinate of cell,
     * access with {@link #getStartX()}.
     */
    float startX = 0.0f;
    /**
     * Starting y-coordinate of cell,
     * access with {@link #getStartY()}.
     */
    float startY = 0.0f;
    /**
     * Width of cell, access with {@link #getWidth()}.
     */
    float width = -1.0f;
    /**
     * Height of cell, access with {@link #getHeight()}.
     */
    float height = -1.0f;
    /**
     * Background color of merged cell, draw with color
     * of left-top cell by default.
     */
    public int bgColor = 0;
    /**
     * Text color of merged cell, this will override the {@link #textColors},
     * text will be drew with Color.BLACK by default.
     */
    public int textColor = 0;
    /**
     * Text colors, denotes color of each text line, should be set
     * to the same size of {@link #texts}.
     */
    public int[] textColors = null;
    /**
     * Size of text, this will override {@link #textSizes},
     * text will be drew with 14sp by default.
     */
    public int textSize = -1;
    /**
     * Text sizes, denotes size of each text line, should be set
     * to the same size of {@link #texts}.
     */
    public int[] textSizes = null;
    /**
     * Texts, multiple lines, if not setting, text of left-top cell
     * will be showed.
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

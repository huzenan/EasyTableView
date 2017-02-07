package com.hzn.easytableview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.text.ParseException;

/**
 * 带动画的路径视图，将通过设置的路径pathString，自动按顺序生成路径动画，包括绘制动画与清除动画（反方向）；
 * 其中fixedWidth和fixedHeight属性必选，用于设置相对于所提供的pathString的固定宽高，以保证在任何实际测量出的宽高中，路径位置正确；
 * strokeFixedWidth属性可选，用于设置相对于所提供的pathString的固定线段宽度，以保证在任何实际测量的宽高中，绘制路径的线段宽度显示比例一致；
 * Created by huzn on 2016/12/21.
 */
public class EasyPathView extends View {

    // 相对于所提供路径的固定的宽，默认为0
    private int fixedWidth;
    // 相对于所提供路径的固定的高，默认为0
    private int fixedHeight;
    // 需要解析的路径
    private String pathString;
    // 线段颜色，默认Color.BLACK
    private int strokeColor;
    // 线段宽度，默认1dp
    private int strokeWidth;
    // 相对于所提供路径的固定的线段宽度，提供该参数时，无视strokeWidth参数，默认为-1
    private float strokeFixedWidth;
    // 线段是否平滑，默认false
    private boolean strokeIsRound;
    // 动画时间长度集，单位ms，默认为全部500
    private String animDurations;
    // 动画播放模式，为ANIM_MODE_TOGETHER或ANIM_MODE_SEPARATE，默认为ANIM_MODE_TOGETHER
    private int animMode;
    // 状态，为STATE_SHOW或STATE_HIDE，默认为STATE_SHOW
    private int state;
    // 是否为动态的，设置为动态时，将只显示绘制过的路径，不显示路径本身，默认为false
    private boolean dynamic;

    public static final int STATE_NONE = -1;
    public static final int STATE_SHOW = 0;
    public static final int STATE_HIDE = 1;
    public static final int STATE_ANIM_SHOW = 2;
    public static final int STATE_ANIM_HIDE = 3;

    public static final int ANIM_MODE_TOGETHER = 0;
    public static final int ANIM_MODE_SEPARATE = 1;

    private int pathCount;
    private static final int MAX_PATH_COUNT = 10;
    private Path pathDst;
    private Path[] pathDstList;
    private PathMeasure pm;
    private PathMeasure[] pmList;
    private Paint paint;
    private float factor;

    // 当animMode为ANIM_MODE_SEPARATE时，记录当前播放的动画
    private int curAnimIndex;
    // 动画时间长度集
    private long animDurationArr[];
    // 动画时间占比集
    private float animDurationRatioArr[];
    // 动画默认播放时长
    private static final long ANIM_DURATION_DEFAULT = 500;
    // 动画是否循环
    private boolean isAnimRepeat;

    private ValueAnimator.AnimatorUpdateListener updateListener;
    private float animatorValue;

    public EasyPathView(Context context) {
        this(context, null);
    }

    public EasyPathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyPathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasyPathView, defStyleAttr, 0);
        fixedWidth = a.getInteger(R.styleable.EasyPathView_epvFixedWidth, 0);
        fixedHeight = a.getInteger(R.styleable.EasyPathView_epvFixedHeight, 0);
        pathString = a.getString(R.styleable.EasyPathView_epvPathString);
        strokeColor = a.getColor(R.styleable.EasyPathView_epvStrokeColor, Color.BLACK);
        strokeWidth = a.getDimensionPixelOffset(R.styleable.EasyPathView_epvStrokeWidth, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        strokeFixedWidth = a.getFloat(R.styleable.EasyPathView_epvStrokeFixedWidth, -1.0f);
        strokeIsRound = a.getBoolean(R.styleable.EasyPathView_epvStrokeIsRound, false);
        animDurations = a.getString(R.styleable.EasyPathView_epvAnimDurations);
        animMode = a.getInteger(R.styleable.EasyPathView_epvAnimMode, ANIM_MODE_TOGETHER);
        state = a.getInteger(R.styleable.EasyPathView_epvState, STATE_SHOW);
        dynamic = a.getBoolean(R.styleable.EasyPathView_epvDynamic, false);
        a.recycle();

        initPath();
        initPaint();
        initListener();
        initDuration();
    }

    private void initPath() {
        pathCount = 0;

        pathDst = new Path();
        pathDstList = new Path[MAX_PATH_COUNT];
        for (int i = 0; i < MAX_PATH_COUNT; i++)
            pathDstList[i] = new Path();

        pm = new PathMeasure();
        pmList = new PathMeasure[MAX_PATH_COUNT];
        for (int i = 0; i < MAX_PATH_COUNT; i++)
            pmList[i] = new PathMeasure();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        if (strokeIsRound) {
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }
    }

    private void initListener() {
        updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        };
    }

    private void initDuration() {
        if (TextUtils.isEmpty(animDurations))
            animDurations = String.valueOf(ANIM_DURATION_DEFAULT);

        // 获取动画时间长度集
        String[] splitDur = animDurations.split(",");
        animDurationArr = new long[MAX_PATH_COUNT];
        int splitLen = Math.min(splitDur.length, MAX_PATH_COUNT);
        for (int i = 0; i < splitLen; i++)
            animDurationArr[i] = Long.parseLong(splitDur[i]);
        for (int i = splitLen; i < MAX_PATH_COUNT; i++)
            animDurationArr[i] = ANIM_DURATION_DEFAULT;

        animDurationRatioArr = new float[MAX_PATH_COUNT];

        isAnimRepeat = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        rebuildPathData();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        width = width + getPaddingLeft() + getPaddingRight();
        if (mode != MeasureSpec.EXACTLY) { // wrap_content
            width = fixedWidth;
        }

        int height = MeasureSpec.getSize(heightMeasureSpec);
        mode = MeasureSpec.getMode(heightMeasureSpec);
        height = height + getPaddingTop() + getPaddingBottom();
        if (mode != MeasureSpec.EXACTLY) { // wrap_content
            height = fixedHeight;
        }

        // 得到实际测量大小与指定大小的比例factor，当宽、高计算出的比例不同时，使用较小值
        float f1 = 1.0f * width / fixedWidth;
        float f2 = 1.0f * height / fixedHeight;
        factor = Math.min(f1, f2);

        if (strokeFixedWidth != -1)
            paint.setStrokeWidth(strokeFixedWidth * factor);

        rebuildPathData();

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (state == STATE_NONE)
            return;


        float startRatio = 0.27f * animatorValue * animatorValue + 0.73f;

        if (animMode == ANIM_MODE_TOGETHER) {
            float startD = 0.0f;
            for (int i = 0; i < pathCount; i++) {
                if (dynamic)
                    startD = pmList[i].getLength() * animatorValue * startRatio;

                pathDst.reset();
                pathDst.rLineTo(0, 0); // 兼容KITKAT的硬件加速，详见PathMeasure.getSegment()的注释
                pmList[i].getSegment(startD, pmList[i].getLength() * animatorValue, pathDst, true);
                canvas.drawPath(pathDst, paint);
            }
        } else if (animMode == ANIM_MODE_SEPARATE) {
            if (!dynamic) {
                if (state == STATE_SHOW || state == STATE_ANIM_SHOW) {
                    // 绘制已经完成动画的路径
                    for (int i = 0; i < curAnimIndex; i++)
                        canvas.drawPath(pathDstList[i], paint);
                } else if (state == STATE_HIDE || state == STATE_ANIM_HIDE) {
                    // 绘制未播放动画的路径
                    for (int i = curAnimIndex - 1; i >= 0; i--)
                        canvas.drawPath(pathDstList[i], paint);
                }
            }

            // 绘制正在播放动画的路径
            if (0 <= curAnimIndex && curAnimIndex < pathCount) {
                float startD = 0.0f;
                if (dynamic)
                    startD = pmList[curAnimIndex].getLength() * animatorValue * startRatio;

                pathDst.reset();
                pathDst.rLineTo(0, 0); // 兼容KITKAT的硬件加速，详见PathMeasure.getSegment()的注释
                pmList[curAnimIndex].getSegment(startD, pmList[curAnimIndex].getLength() * animatorValue, pathDst, true);
                canvas.drawPath(pathDst, paint);
            }
        }
    }

    private void rebuildPathData() {
        try {
            pathCount = 0;

            Path path = EasyPathParser.getInstance().parsePath(pathString, this, factor);
            pm.setPath(path, false);
            while (pm.nextContour()) {
                ++pathCount;
                if (pathCount > MAX_PATH_COUNT) {
                    pathCount = MAX_PATH_COUNT;
                    break;
                }

                // 存储所有轮廓
                pathDstList[pathCount - 1].reset();
                pathDstList[pathCount - 1].rLineTo(0, 0); // 兼容KITKAT
                pm.getSegment(0, pm.getLength(), pathDstList[pathCount - 1], true);
                pmList[pathCount - 1].setPath(pathDstList[pathCount - 1], false);
            }

            if (state == STATE_SHOW)
                animatorValue = 1.0f;

            if (animMode == ANIM_MODE_SEPARATE)
                curAnimIndex = pathCount;

            // 计算动画时间占比集
            long totalDuration = 0L;
            for (int i = 0; i < pathCount; i++)
                totalDuration += animDurationArr[i];
            for (int i = 0; i < pathCount; i++)
                animDurationRatioArr[i] = 1.0f * animDurationArr[i] / totalDuration;

        } catch (ParseException e) {
            e.printStackTrace();

            pathCount = 0;
            pm = null;
            pathDstList = null;
            pmList = null;

            state = STATE_NONE;
            animatorValue = 0.0f;
            curAnimIndex = 0;
        }
    }

    private ValueAnimator getDrawAnimator() {
        ValueAnimator drawAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        drawAnimator.addUpdateListener(updateListener);
        drawAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (null != onAnimatorListener)
                    onAnimatorListener.onAnimStart(state);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animMode == ANIM_MODE_SEPARATE) {
                    if (curAnimIndex < pathCount) {
                        ++curAnimIndex;
                        getDrawAnimator().setDuration(animDurationArr[curAnimIndex]).start();
                    } else {
                        if (isAnimRepeat) {
                            if (null != onAnimatorListener)
                                onAnimatorListener.onAnimRepeat(state);
                            startDraw();
                        } else {
                            state = STATE_SHOW;
                        }
                    }
                } else if (animMode == ANIM_MODE_TOGETHER) {
                    if (isAnimRepeat) {
                        if (null != onAnimatorListener)
                            onAnimatorListener.onAnimRepeat(state);
                        startDraw();
                    } else {
                        state = STATE_SHOW;
                    }
                }

                if (null != onAnimatorListener)
                    onAnimatorListener.onAnimEnd(state);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return drawAnimator;
    }

    private ValueAnimator getEraseAnimator() {
        ValueAnimator eraseAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        eraseAnimator.addUpdateListener(updateListener);
        eraseAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (null != onAnimatorListener)
                    onAnimatorListener.onAnimStart(state);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animMode == ANIM_MODE_SEPARATE) {
                    if (curAnimIndex > 0) {
                        --curAnimIndex;
                        getEraseAnimator().setDuration(animDurationArr[curAnimIndex]).start();
                    } else {
                        if (isAnimRepeat) {
                            if (null != onAnimatorListener)
                                onAnimatorListener.onAnimRepeat(state);
                            startErase();
                        } else {
                            state = STATE_HIDE;
                        }
                    }
                } else if (animMode == ANIM_MODE_TOGETHER) {
                    if (isAnimRepeat) {
                        if (null != onAnimatorListener)
                            onAnimatorListener.onAnimRepeat(state);
                        startErase();
                    } else {
                        state = STATE_HIDE;
                    }
                }

                if (null != onAnimatorListener)
                    onAnimatorListener.onAnimEnd(state);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return eraseAnimator;
    }

    /**
     * 开始绘制路径
     */
    public void startDraw() {
        state = STATE_ANIM_SHOW;

        if (animMode == ANIM_MODE_SEPARATE)
            curAnimIndex = 0;

        getDrawAnimator().setDuration(animDurationArr[curAnimIndex]).start();
    }

    /**
     * 开始绘制路径
     *
     * @param repeat 动画是否循环绘制
     */
    public void startDraw(boolean repeat) {
        isAnimRepeat = repeat;
        startDraw();
    }

    /**
     * 开始清除路径
     */
    public void startErase() {
        state = STATE_ANIM_HIDE;

        if (animMode == ANIM_MODE_SEPARATE)
            curAnimIndex = pathCount - 1;

        getEraseAnimator().setDuration(animDurationArr[curAnimIndex]).start();
    }

    public void startErase(boolean repeat) {
        isAnimRepeat = true;
        startErase();
    }

    /**
     * 停止循环绘制
     */
    public void stopRepeat() {
        isAnimRepeat = false;
    }

    /**
     * 设置路径动画当前的进度
     *
     * @param progress 路径动画当前的进度，范围0.0f~1.0f
     */
    public void setAnimProgress(float progress) {
        if (progress < 0.0f)
            progress = 0.0f;
        else if (progress > 1.0f)
            progress = 1.0f;

        if (animMode == ANIM_MODE_TOGETHER) {
            animatorValue = progress;
            invalidate();
        } else if (animMode == ANIM_MODE_SEPARATE) {
            float curTotal = 0.0f;
            for (int i = 0; i < pathCount; i++) {
                curTotal += animDurationRatioArr[i];
                if (progress <= curTotal) {
                    curAnimIndex = i;
                    animatorValue = 1.0f - (curTotal - progress) / animDurationRatioArr[i];
                    invalidate();
                    break;
                }
            }
        }
    }

    /**
     * 重置，在修改参数后，startDraw之前需要调用此方法
     */
    public void reset() {
        initPath();
        initPaint();
        initListener();
        initDuration();

        requestLayout();
        invalidate();
    }

    // getters and setters
    public int getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(int fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(int fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    public String getPathString() {
        return pathString;
    }

    public void setPathString(String pathString) {
        this.pathString = pathString;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public float getStrokeFixedWidth() {
        return strokeFixedWidth;
    }

    public void setStrokeFixedWidth(float strokeFixedWidth) {
        this.strokeFixedWidth = strokeFixedWidth;
    }

    public boolean getStrokeIsRound() {
        return strokeIsRound;
    }

    public void setStrokeIsRound(boolean strokeIsRound) {
        this.strokeIsRound = strokeIsRound;
    }

    public String getAnimDurations() {
        return animDurations;
    }

    public void setAnimDurations(String animDurations) {
        this.animDurations = animDurations;
    }

    public int getAnimMode() {
        return animMode;
    }

    public void setAnimMode(int animMode) {
        this.animMode = animMode;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * 获取当前状态
     *
     * @return 返回当前状态，包括STATE_NONE，STATE_SHOW，STATE_HIDE，STATE_ANIM_SHOW和STATE_ANIM_HIDE
     */
    public int getState() {
        return state;
    }

    /**
     * 用于监听路径动画，可选择监听哪些动画状态
     */
    public static abstract class OnAnimatorListener {
        /**
         * 动画开始时回调
         *
         * @param state 当前动画状态，包括STATE_SHOW，STATE_HIDE，STATE_ANIM_SHOW和STATE_ANIM_HIDE
         */
        protected void onAnimStart(int state) {
        }

        /**
         * 动画结束时回调
         *
         * @param state 当前动画状态，包括STATE_SHOW，STATE_HIDE，STATE_ANIM_SHOW和STATE_ANIM_HIDE
         */
        protected void onAnimEnd(int state) {
        }

        /**
         * 动画重复时回调，紧接着会回调onAnimStart
         *
         * @param state 当前动画状态，包括STATE_SHOW，STATE_HIDE，STATE_ANIM_SHOW和STATE_ANIM_HIDE
         */
        protected void onAnimRepeat(int state) {
        }
    }

    private OnAnimatorListener onAnimatorListener;

    /**
     * 添加监听路径动画执行的接口
     *
     * @param onAnimatorListener 用于监听路径动画
     */
    public void addOnAnimatorListener(OnAnimatorListener onAnimatorListener) {
        this.onAnimatorListener = onAnimatorListener;
    }
}

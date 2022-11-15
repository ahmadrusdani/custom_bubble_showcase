package com.kanggo.bubbleshowcase.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.kanggo.bubbleshowcase.OnBubbleMessageViewListener
import com.kanggo.bubbleshowcase.R
import com.kanggo.bubbleshowcase.ScreenUtils

/**
 * Created by jcampos on 05/09/2018.
 */

class CustomBubbleMessageView : LinearLayout {

    private val WIDTH_ARROW = 20

    private var itemView: View? = null
    private var targetViewScreenLocation: RectF? = null
    private var mBackgroundColor: Int = ContextCompat.getColor(context, R.color.white)
    private var arrowPositionList = ArrayList<CustomBubbleShowCase.ArrowPosition>()

    private var paint: Paint? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, builder: Builder) : super(context) {
        initView()
        setAttributes(builder)
        setBubbleListener(builder)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        setWillNotDraw(false)
    }

    private fun setAttributes(builder: Builder) {
        itemView = builder.mView
        itemView?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(itemView!!)
        builder.mBackgroundColor?.let { mBackgroundColor = builder.mBackgroundColor!! }
        arrowPositionList = builder.mArrowPosition
        targetViewScreenLocation = builder.mTargetViewScreenLocation
    }

    private fun setBubbleListener(builder: Builder) {
        itemView?.setOnClickListener { builder.mListener?.onBubbleClick() }
    }


    //END REGION

    //REGION AUX FUNCTIONS

    private fun getViewWidth(): Int = width

    private fun getMargin(): Int = ScreenUtils.dpToPx(20)

    private fun getSecurityArrowMargin(): Int =
        getMargin() + ScreenUtils.dpToPx(2 * WIDTH_ARROW / 3)

    //END REGION

    //REGION SHOW ITEM

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        prepareToDraw()
        drawRectangle(canvas)

        for (arrowPosition in arrowPositionList) {
            drawArrow(canvas, arrowPosition, targetViewScreenLocation)
        }
    }

    private fun prepareToDraw() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = mBackgroundColor
        paint!!.style = Paint.Style.FILL
        paint!!.strokeWidth = 4.0f
    }

    private fun drawRectangle(canvas: Canvas) {
        val rect = RectF(
            getMargin().toFloat(),
            getMargin().toFloat(),
            getViewWidth() - getMargin().toFloat(),
            height - getMargin().toFloat()
        )
        canvas.drawRoundRect(rect, 24f, 24f, paint!!)
    }

    private fun drawArrow(
        canvas: Canvas,
        arrowPosition: CustomBubbleShowCase.ArrowPosition,
        targetViewLocationOnScreen: RectF?
    ) {
        val xPosition: Int
        val yPosition: Int

        when (arrowPosition) {
            CustomBubbleShowCase.ArrowPosition.LEFT -> {
                xPosition = getMargin()
                yPosition =
                    if (targetViewLocationOnScreen != null) getArrowVerticalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else height / 2
            }
            CustomBubbleShowCase.ArrowPosition.RIGHT -> {
                xPosition = getViewWidth() - getMargin()
                yPosition =
                    if (targetViewLocationOnScreen != null) getArrowVerticalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else height / 2
            }
            CustomBubbleShowCase.ArrowPosition.TOP -> {
                xPosition =
                    if (targetViewLocationOnScreen != null) getArrowHorizontalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else width / 2
                yPosition = getMargin()
            }
            CustomBubbleShowCase.ArrowPosition.BOTTOM -> {
                xPosition =
                    if (targetViewLocationOnScreen != null) getArrowHorizontalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else width / 2
                yPosition = height - getMargin()
            }
        }

        drawRhombus(canvas, paint, xPosition, yPosition, ScreenUtils.dpToPx(WIDTH_ARROW))
    }

    private fun getArrowHorizontalPositionDependingOnTarget(targetViewLocationOnScreen: RectF?): Int {
        val xPosition: Int = when {
            isOutOfRightBound(targetViewLocationOnScreen) -> width - getSecurityArrowMargin()
            isOutOfLeftBound(targetViewLocationOnScreen) -> getSecurityArrowMargin()
            else -> Math.round(
                targetViewLocationOnScreen!!.centerX() - ScreenUtils.getAxisXpositionOfViewOnScreen(
                    this
                )
            )
        }
        return xPosition
    }

    private fun getArrowVerticalPositionDependingOnTarget(targetViewLocationOnScreen: RectF?): Int {
        val yPosition: Int
        when {
            isOutOfBottomBound(targetViewLocationOnScreen) -> yPosition =
                height - getSecurityArrowMargin()
            isOutOfTopBound(targetViewLocationOnScreen) -> yPosition = getSecurityArrowMargin()
            else -> yPosition = Math.round(
                targetViewLocationOnScreen!!.centerY() + ScreenUtils.getStatusBarHeight(
                    context
                ) - ScreenUtils.getAxisYpositionOfViewOnScreen(this)
            )
        }
        return yPosition
    }

    private fun isOutOfRightBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerX() > ScreenUtils.getAxisXpositionOfViewOnScreen(
            this
        ) + width - getSecurityArrowMargin()
    }

    private fun isOutOfLeftBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerX() < ScreenUtils.getAxisXpositionOfViewOnScreen(
            this
        ) + getSecurityArrowMargin()
    }

    private fun isOutOfBottomBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerY() > ScreenUtils.getAxisYpositionOfViewOnScreen(
            this
        ) + height - getSecurityArrowMargin() - ScreenUtils.getStatusBarHeight(context)
    }

    private fun isOutOfTopBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerY() < ScreenUtils.getAxisYpositionOfViewOnScreen(
            this
        ) + getSecurityArrowMargin() - ScreenUtils.getStatusBarHeight(context)
    }


    private fun drawRhombus(canvas: Canvas, paint: Paint?, x: Int, y: Int, width: Int) {
        val halfRhombusWidth = width / 2

        val path = Path()
        path.moveTo(x.toFloat(), (y + halfRhombusWidth).toFloat()) // Top
        path.lineTo((x - halfRhombusWidth).toFloat(), y.toFloat()) // Left
        path.lineTo(x.toFloat(), (y - halfRhombusWidth).toFloat()) // Bottom
        path.lineTo((x + halfRhombusWidth).toFloat(), y.toFloat()) // Right
        path.lineTo(x.toFloat(), (y + halfRhombusWidth).toFloat()) // Back to Top
        path.close()

        canvas.drawPath(path, paint!!)
    }


    //END REGION

    /**
     * Builder for BubbleMessageView class
     */
    class Builder {
        lateinit var mContext: Context
        var mTargetViewScreenLocation: RectF? = null
        var mBackgroundColor: Int? = null
        var mArrowPosition = ArrayList<CustomBubbleShowCase.ArrowPosition>()
        var mListener: OnBubbleMessageViewListener? = null
        var mView: View? = null
        fun from(context: Context): Builder {
            mContext = context
            return this
        }

        fun setContentView(view: View?): Builder {
            mView = view
            return this
        }

        fun targetViewScreenLocation(targetViewLocationOnScreen: RectF): Builder {
            mTargetViewScreenLocation = targetViewLocationOnScreen
            return this
        }

        fun backgroundColor(backgroundColor: Int?): Builder {
            mBackgroundColor = backgroundColor
            return this
        }

        fun arrowPosition(arrowPosition: List<CustomBubbleShowCase.ArrowPosition>): Builder {
            mArrowPosition.clear()
            mArrowPosition.addAll(arrowPosition)
            return this
        }

        fun listener(listener: OnBubbleMessageViewListener?): Builder {
            mListener = listener
            return this
        }

        fun build(): CustomBubbleMessageView {
            return CustomBubbleMessageView(mContext, this)
        }
    }
}
package com.kanggo.bubbleshowcase.kanggo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kanggo.bubbleshowcase.Extension.convertHtml
import com.kanggo.bubbleshowcase.OnBubbleMessageViewListener
import com.kanggo.bubbleshowcase.R
import com.kanggo.bubbleshowcase.ScreenUtils
import com.kanggo.bubbleshowcase.databinding.ViewBubbleMessageNewBinding

/**
 * Created by jcampos on 05/09/2018.
 */

class KanggoBubbleMessageView : LinearLayout {

    private val WIDTH_ARROW = 20

    private var itemView: View? = null

    private var textViewTitle: TextView? = null
    private var textViewSubtitle: TextView? = null
    private var buttonView: Button? = null
    private var showCaseMessageViewLayout: LinearLayout? = null

    private var targetViewScreenLocation: RectF? = null
    private var mBackgroundColor: Int = ContextCompat.getColor(context, R.color.white)
    private var arrowPositionList = ArrayList<KanggoBubbleShowCase.ArrowPosition>()

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

        inflateXML()
        bindViews()
    }

    private fun inflateXML() {
        itemView =
            ViewBubbleMessageNewBinding.inflate(LayoutInflater.from(context), this, false).root
        addView(itemView)
//        itemView = inflate(context, R.layout.view_bubble_message_new, this)
    }

    private fun bindViews() {
        buttonView = findViewById(R.id.buttonViewShowCase)
        textViewTitle = findViewById(R.id.textViewShowCaseTitle)
        textViewSubtitle = findViewById(R.id.textViewShowCaseText)
        showCaseMessageViewLayout = findViewById(R.id.showCaseMessageViewLayout)
    }

    private fun setAttributes(builder: Builder) {
        if (builder.mButtonVisibility) {
            buttonView?.visibility = View.VISIBLE
        }
        buttonView?.text = builder.mButtonTitle
        builder.mTitle?.let {
            textViewTitle?.visibility = View.VISIBLE
            textViewTitle?.text = builder.mTitle?.convertHtml()
        }
        builder.mSubtitle?.let {
            textViewSubtitle?.visibility = View.VISIBLE
            textViewSubtitle?.text = builder.mSubtitle?.convertHtml()
        }
        builder.mTextColor?.let {
            textViewTitle?.setTextColor(builder.mTextColor!!)
            textViewSubtitle?.setTextColor(builder.mTextColor!!)
        }
        builder.mTitleTextSize?.let {
            textViewTitle?.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                builder.mTitleTextSize!!.toFloat()
            )
        }
        builder.mSubtitleTextSize?.let {
            textViewSubtitle?.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                builder.mSubtitleTextSize!!.toFloat()
            )
        }
        builder.mBackgroundColor?.let { mBackgroundColor = builder.mBackgroundColor!! }
        arrowPositionList = builder.mArrowPosition
        targetViewScreenLocation = builder.mTargetViewScreenLocation
    }

    private fun setBubbleListener(builder: Builder) {
        buttonView?.setOnClickListener { builder.mListener?.onCloseActionImageClick() }
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
        canvas.drawRoundRect(rect, 18f, 18f, paint!!)
    }

    private fun drawArrow(
        canvas: Canvas,
        arrowPosition: KanggoBubbleShowCase.ArrowPosition,
        targetViewLocationOnScreen: RectF?
    ) {
        val xPosition: Int
        val yPosition: Int

        when (arrowPosition) {
            KanggoBubbleShowCase.ArrowPosition.LEFT -> {
                xPosition = getMargin()
                yPosition =
                    if (targetViewLocationOnScreen != null) getArrowVerticalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else height / 2
            }
            KanggoBubbleShowCase.ArrowPosition.RIGHT -> {
                xPosition = getViewWidth() - getMargin()
                yPosition =
                    if (targetViewLocationOnScreen != null) getArrowVerticalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else height / 2
            }
            KanggoBubbleShowCase.ArrowPosition.TOP -> {
                xPosition =
                    if (targetViewLocationOnScreen != null) getArrowHorizontalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else width / 2
                yPosition = getMargin()
            }
            KanggoBubbleShowCase.ArrowPosition.BOTTOM -> {
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
        val xPosition: Int
        when {
            isOutOfRightBound(targetViewLocationOnScreen) -> xPosition =
                width - getSecurityArrowMargin()
            isOutOfLeftBound(targetViewLocationOnScreen) -> xPosition = getSecurityArrowMargin()
            else -> xPosition = Math.round(
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
        var mTitle: String? = null
        var mSubtitle: String? = null
        var mBackgroundColor: Int? = null
        var mTextColor: Int? = R.color.textPrimaryColor
        var mTitleTextSize: Int? = null
        var mSubtitleTextSize: Int? = null
        var mButtonVisibility: Boolean = false
        var mButtonTitle: String = "OK"
        var mArrowPosition = ArrayList<KanggoBubbleShowCase.ArrowPosition>()
        var mListener: OnBubbleMessageViewListener? = null

        fun from(context: Context): Builder {
            mContext = context
            return this
        }

        fun title(title: String?): Builder {
            mTitle = title
            return this
        }

        fun subtitle(subtitle: String?): Builder {
            mSubtitle = subtitle
            return this
        }

        fun buttonVisibility(params: Boolean = false): Builder {
            mButtonVisibility = params
            return this
        }

        fun buttonTitle(params: String = "OK"): Builder {
            mButtonTitle = params
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

        fun textColor(textColor: Int?): Builder {
            mTextColor = textColor
            return this
        }

        fun titleTextSize(textSize: Int?): Builder {
            mTitleTextSize = textSize
            return this
        }

        fun subtitleTextSize(textSize: Int?): Builder {
            mSubtitleTextSize = textSize
            return this
        }

        fun arrowPosition(arrowPosition: List<KanggoBubbleShowCase.ArrowPosition>): Builder {
            mArrowPosition.clear()
            mArrowPosition.addAll(arrowPosition)
            return this
        }

        fun listener(listener: OnBubbleMessageViewListener?): Builder {
            mListener = listener
            return this
        }

        fun build(): KanggoBubbleMessageView {
            return KanggoBubbleMessageView(mContext, this)
        }
    }
}
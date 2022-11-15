package com.kanggo.bubbleshowcase.custom


import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewbinding.ViewBinding
import com.kanggo.bubbleshowcase.*


/**
 * Created by jcampos on 04/09/2018.
 */

class CustomBubbleShowCase(builder: CustomBubbleShowCaseBuilder) {
    private val SHARED_PREFS_NAME = "BubbleShowCasePrefs"

    private val FOREGROUND_LAYOUT_ID = 731

    private val DURATION_SHOW_CASE_ANIMATION = 200 //ms
    private val DURATION_BACKGROUND_ANIMATION = 700 //ms
    private val DURATION_BEATING_ANIMATION = 700 //ms

    private val MAX_WIDTH_MESSAGE_VIEW_TABLET = 420 //dp

    /**
     * Enum class which corresponds to each valid position for the BubbleMessageView arrow
     */
    enum class ArrowPosition {
        TOP, BOTTOM, LEFT, RIGHT
    }

    /**
     * Highlight mode. It represents the way that the target view will be highlighted
     * - VIEW_LAYOUT: Default value. All the view box is highlighted (the rectangle where the view is contained). Example: For a TextView, all the element is highlighted (characters and background)
     * - VIEW_SURFACE: Only the view surface is highlighted, but not the background. Example: For a TextView, only the characters will be highlighted
     */
    enum class HighlightMode {
        VIEW_LAYOUT, VIEW_SURFACE
    }


    private val mActivity: Activity = builder.mActivity!!

    //BubbleMessageView params
    private val mBackgroundColor: Int? = builder.mBackgroundColor
    private val mShowOnce: String? = builder.mShowOnce
    private val mHighlightMode: HighlightMode? = builder.mHighlightMode
    private val mArrowPositionList: MutableList<ArrowPosition> = builder.mArrowPositionList
    private val mTargetView: View? = builder.mTargetView
    private val mBubbleShowCaseListener: KanggoBubbleShowCaseListener? =
        builder.mBubbleShowCaseListener

    //Sequence params
    private val mSequenceListener: SequenceShowCaseListener? = builder.mSequenceShowCaseListener
    private val isFirstOfSequence: Boolean = builder.mIsFirstOfSequence!!
    private val isLastOfSequence: Boolean = builder.mIsLastOfSequence!!

    //References
    private var backgroundDimLayout: RelativeLayout? = null
    private var bubbleMessageViewBuilder: CustomBubbleMessageView.Builder? = null
    private var rootView: View = builder.mView!!

    fun show() {
        if (mShowOnce != null) {
            if (isBubbleShowCaseHasBeenShowedPreviously(mShowOnce)) {
                notifyDismissToSequenceListener()
                return
            } else {
                registerBubbleShowCaseInPreferences(mShowOnce)
            }
        }

        val rootView = getViewRoot(mActivity)
        backgroundDimLayout = getBackgroundDimLayout()
        setBackgroundDimListener(backgroundDimLayout)
        bubbleMessageViewBuilder = getBubbleMessageViewBuilder()

        if (mTargetView != null && mArrowPositionList.size <= 1) {
            //Wait until the end of the layout animation, to avoid problems with pending scrolls or view movements
            val handler = Handler()
            handler.postDelayed({
                val target = mTargetView
                //If the arrow list is empty, the arrow position is set by default depending on the targetView position on the screen
                if (mArrowPositionList.isEmpty()) {
                    if (ScreenUtils.isViewLocatedAtHalfTopOfTheScreen(
                            mActivity,
                            target
                        )
                    ) mArrowPositionList.add(ArrowPosition.TOP) else mArrowPositionList.add(
                        ArrowPosition.BOTTOM
                    )
                    bubbleMessageViewBuilder = getBubbleMessageViewBuilder()
                }

                if (isVisibleOnScreen(target)) {
                    addTargetViewAtBackgroundDimLayout(target, backgroundDimLayout)
                    addBubbleMessageViewDependingOnTargetView(
                        target,
                        bubbleMessageViewBuilder!!,
                        backgroundDimLayout
                    )
                } else {
                    dismiss()
                }
            }, DURATION_BACKGROUND_ANIMATION.toLong())
        } else {
            addBubbleMessageViewOnScreenCenter(bubbleMessageViewBuilder!!, backgroundDimLayout)
        }
        if (isFirstOfSequence) {
            //Add the background dim layout above the root view
            val animation = AnimationUtils.getFadeInAnimation(0, DURATION_BACKGROUND_ANIMATION)
            backgroundDimLayout?.let {
                rootView.addView(
                    AnimationUtils.setAnimationToView(
                        backgroundDimLayout!!,
                        animation
                    )
                )
            }
        }
    }

    fun dismiss() {
        if (backgroundDimLayout != null && isLastOfSequence) {
            //Remove background dim layout if the BubbleShowCase is the last of the sequence
            finishSequence()
        } else {
            //Remove all the views created over the background dim layout waiting for the next BubbleShowCsse in the sequence
            backgroundDimLayout?.removeAllViews()
        }
        notifyDismissToSequenceListener()
    }

    fun finishSequence() {
        val rootView = getViewRoot(mActivity)
        rootView.removeView(backgroundDimLayout)
        backgroundDimLayout = null
    }

    private fun notifyDismissToSequenceListener() {
        mSequenceListener?.let { mSequenceListener.onDismiss() }
    }

    private fun getViewRoot(activity: Activity): ViewGroup {
        val androidContent = activity.findViewById<ViewGroup>(android.R.id.content)
        return androidContent.parent.parent as ViewGroup
    }

    private fun getBackgroundDimLayout(): RelativeLayout {
        if (mActivity.findViewById<RelativeLayout>(FOREGROUND_LAYOUT_ID) != null)
            return mActivity.findViewById(FOREGROUND_LAYOUT_ID)
        val backgroundLayout = RelativeLayout(mActivity)
        backgroundLayout.id = FOREGROUND_LAYOUT_ID
        backgroundLayout.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        backgroundLayout.setBackgroundColor(
            ContextCompat.getColor(
                mActivity,
                R.color.transparent_grey
            )
        )
        backgroundLayout.isClickable = true
        return backgroundLayout
    }

    private fun setBackgroundDimListener(backgroundDimLayout: RelativeLayout?) {
        backgroundDimLayout?.setOnClickListener { dismiss() }
    }

    private fun getBubbleMessageViewBuilder(): CustomBubbleMessageView.Builder {
        return CustomBubbleMessageView.Builder()
            .from(mActivity)
            .setContentView(rootView)
            .arrowPosition(mArrowPositionList)
            .backgroundColor(mBackgroundColor)
    }

    private fun isBubbleShowCaseHasBeenShowedPreviously(id: String): Boolean {
        val mPrefs = mActivity.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        return getString(mPrefs, id) != null
    }

    private fun registerBubbleShowCaseInPreferences(id: String) {
        val mPrefs = mActivity.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        setString(mPrefs, id, id)
    }

    private fun getString(mPrefs: SharedPreferences, key: String): String? {
        return mPrefs.getString(key, null)
    }

    private fun setString(mPrefs: SharedPreferences, key: String, value: String) {
        val editor = mPrefs.edit()
        editor.putString(key, value)
        editor.apply()
    }


    /**
     * This function takes a screenshot of the targetView, creating an ImageView from it. This new ImageView is also set on the layout passed by param
     */
    private fun addTargetViewAtBackgroundDimLayout(
        targetView: View?,
        backgroundDimLayout: RelativeLayout?
    ) {
        if (targetView == null) return

        val targetScreenshot = takeScreenshot(targetView, mHighlightMode)
        val targetScreenshotView = ImageView(mActivity)
        targetScreenshotView.setImageBitmap(targetScreenshot)
//        targetScreenshotView.setOnClickListener {
//            if (!mDisableTargetClick)
//                dismiss()
//            mBubbleShowCaseListener?.onTargetClick(this)
//        }

        val targetViewParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        targetViewParams.setMargins(
            getXposition(targetView),
            getYposition(targetView),
            getScreenWidth(mActivity) - (getXposition(targetView) + targetView.width),
            0
        )
        backgroundDimLayout?.addView(
            AnimationUtils.setBouncingAnimation(
                targetScreenshotView,
                0,
                DURATION_BEATING_ANIMATION
            ), targetViewParams
        )
    }

    /**
     * This function creates the BubbleMessageView depending the position of the target and the desired arrow position. This new view is also set on the layout passed by param
     */
    private fun addBubbleMessageViewDependingOnTargetView(
        targetView: View?,
        bubbleMessageViewBuilder: CustomBubbleMessageView.Builder,
        backgroundDimLayout: RelativeLayout?
    ) {
        if (targetView == null) return
        val showCaseParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        when (bubbleMessageViewBuilder.mArrowPosition[0]) {
            ArrowPosition.LEFT -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                if (ScreenUtils.isViewLocatedAtHalfTopOfTheScreen(mActivity, targetView)) {
                    showCaseParams.setMargins(
                        getXposition(targetView) + targetView.width,
                        getYposition(targetView),
                        if (isTablet()) getScreenWidth(mActivity) - (getXposition(targetView) + targetView.width) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity) - (getXposition(targetView) + targetView.width)
                        ) else 0,
                        0
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                } else {
                    showCaseParams.setMargins(
                        getXposition(targetView) + targetView.width,
                        0,
                        if (isTablet()) getScreenWidth(mActivity) - (getXposition(targetView) + targetView.width) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity) - (getXposition(targetView) + targetView.width)
                        ) else 0,
                        getScreenHeight(mActivity) - getYposition(targetView) - targetView.height
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }
            ArrowPosition.RIGHT -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                if (ScreenUtils.isViewLocatedAtHalfTopOfTheScreen(mActivity, targetView)) {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        getYposition(targetView),
                        getScreenWidth(mActivity) - getXposition(targetView),
                        0
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                } else {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        0,
                        getScreenWidth(mActivity) - getXposition(targetView),
                        getScreenHeight(mActivity) - getYposition(targetView) - targetView.height
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }
            ArrowPosition.TOP -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                if (ScreenUtils.isViewLocatedAtHalfLeftOfTheScreen(mActivity, targetView)) {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) else 0,
                        getYposition(targetView) + targetView.height,
                        if (isTablet()) getScreenWidth(mActivity) - getXposition(targetView) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity) - getXposition(targetView)
                        ) else 0,
                        0
                    )
                } else {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) + targetView.width - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        getYposition(targetView) + targetView.height,
                        if (isTablet()) getScreenWidth(mActivity) - getXposition(targetView) - targetView.width else 0,
                        0
                    )
                }
            }
            ArrowPosition.BOTTOM -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                if (ScreenUtils.isViewLocatedAtHalfLeftOfTheScreen(mActivity, targetView)) {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) else 0,
                        0,
                        if (isTablet()) getScreenWidth(mActivity) - getXposition(targetView) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity) - getXposition(targetView)
                        ) else 0,
                        getScreenHeight(mActivity) - getYposition(targetView)
                    )
                } else {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) + targetView.width - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        0,
                        if (isTablet()) getScreenWidth(mActivity) - getXposition(targetView) - targetView.width else 0,
                        getScreenHeight(mActivity) - getYposition(targetView)
                    )
                }
            }
        }

        val bubbleMessageView = bubbleMessageViewBuilder.targetViewScreenLocation(
            RectF(
                getXposition(targetView).toFloat(),
                getYposition(targetView).toFloat(),
                getXposition(targetView).toFloat() + targetView.width,
                getYposition(targetView).toFloat() + targetView.height
            )
        )
            .build()

        bubbleMessageView.id = createViewId()
        val animation = AnimationUtils.getScaleAnimation(0, DURATION_SHOW_CASE_ANIMATION)
        backgroundDimLayout?.addView(
            AnimationUtils.setAnimationToView(
                bubbleMessageView,
                animation
            ), showCaseParams
        )
    }

    /**
     * This function creates a BubbleMessageView and it is set on the center of the layout passed by param
     */
    private fun addBubbleMessageViewOnScreenCenter(
        bubbleMessageViewBuilder: CustomBubbleMessageView.Builder,
        backgroundDimLayout: RelativeLayout?
    ) {
        val showCaseParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        showCaseParams.addRule(RelativeLayout.CENTER_VERTICAL)
        val bubbleMessageView: CustomBubbleMessageView = bubbleMessageViewBuilder.build()
        bubbleMessageView.id = createViewId()
        if (isTablet()) showCaseParams.setMargins(
            if (isTablet()) getScreenWidth(mActivity) / 2 - ScreenUtils.dpToPx(
                MAX_WIDTH_MESSAGE_VIEW_TABLET
            ) / 2 else 0,
            0,
            if (isTablet()) getScreenWidth(mActivity) / 2 - ScreenUtils.dpToPx(
                MAX_WIDTH_MESSAGE_VIEW_TABLET
            ) / 2 else 0,
            0
        )
        val animation = AnimationUtils.getScaleAnimation(0, DURATION_SHOW_CASE_ANIMATION)
        backgroundDimLayout?.addView(
            AnimationUtils.setAnimationToView(
                bubbleMessageView,
                animation
            ), showCaseParams
        )
    }

    private fun createViewId(): Int {
        return View.generateViewId()
    }

    private fun takeScreenshot(targetView: View, highlightMode: HighlightMode?): Bitmap? {
        if (highlightMode == null || highlightMode == HighlightMode.VIEW_LAYOUT)
            return takeScreenshotOfLayoutView(targetView)
        return takeScreenshotOfSurfaceView(targetView)
    }

    private fun takeScreenshotOfLayoutView(targetView: View): Bitmap? {
        if (targetView.width == 0 || targetView.height == 0) {
            return null
        }

        val rootView = getViewRoot(mActivity)
        val currentScreenView = rootView.getChildAt(0)
        currentScreenView.buildDrawingCache()
        val bitmap: Bitmap = Bitmap.createBitmap(
            currentScreenView.drawingCache,
            getXposition(targetView),
            getYposition(targetView),
            targetView.width,
            targetView.height
        )
        currentScreenView.isDrawingCacheEnabled = false
        currentScreenView.destroyDrawingCache()
        return bitmap
    }

    private fun takeScreenshotOfSurfaceView(targetView: View): Bitmap? {
        if (targetView.width == 0 || targetView.height == 0) {
            return null
        }

        targetView.isDrawingCacheEnabled = true
        val bitmap: Bitmap = Bitmap.createBitmap(targetView.drawingCache)
        targetView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun isVisibleOnScreen(targetView: View?): Boolean {
        if (targetView != null) {
            if (getXposition(targetView) >= 0 && getYposition(targetView) >= 0) {
                return getXposition(targetView) != 0 || getYposition(targetView) != 0
            }
        }
        return false
    }

    private fun getXposition(targetView: View): Int {
        return ScreenUtils.getAxisXpositionOfViewOnScreen(targetView) - getScreenHorizontalOffset()
    }

    private fun getYposition(targetView: View): Int {
        return ScreenUtils.getAxisYpositionOfViewOnScreen(targetView) - getScreenVerticalOffset()
    }

    private fun getScreenHeight(context: Context): Int {
        return ScreenUtils.getScreenHeight(context) - getScreenVerticalOffset()
    }

    private fun getScreenWidth(context: Context): Int {
        return ScreenUtils.getScreenWidth(context) - getScreenHorizontalOffset()
    }

    private fun getScreenVerticalOffset(): Int {
        return if (backgroundDimLayout != null) ScreenUtils.getAxisYpositionOfViewOnScreen(
            backgroundDimLayout!!
        ) else 0
    }

    private fun getScreenHorizontalOffset(): Int {
        return if (backgroundDimLayout != null) ScreenUtils.getAxisXpositionOfViewOnScreen(
            backgroundDimLayout!!
        ) else 0
    }

    private fun getMessageViewWidthOnTablet(availableSpace: Int): Int {
        return if (availableSpace > ScreenUtils.dpToPx(MAX_WIDTH_MESSAGE_VIEW_TABLET)) ScreenUtils.dpToPx(
            MAX_WIDTH_MESSAGE_VIEW_TABLET
        ) else availableSpace
    }

    private fun isTablet(): Boolean = mActivity.resources.getBoolean(R.bool.isTablet)


}
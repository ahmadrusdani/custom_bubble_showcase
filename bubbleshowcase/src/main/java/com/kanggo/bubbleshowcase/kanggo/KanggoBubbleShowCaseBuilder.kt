package com.kanggo.bubbleshowcase.kanggo

import android.app.Activity
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewTreeObserver
import com.kanggo.bubbleshowcase.KanggoBubbleShowCaseListener
import com.kanggo.bubbleshowcase.SequenceShowCaseListener
import com.kanggo.bubbleshowcase.base.BaseBubbleShowCaseBuilder
import java.util.ArrayList

/**
 * Created by jcampos on 04/09/2018.
 */
class KanggoBubbleShowCaseBuilder : BaseBubbleShowCaseBuilder {

    internal var mActivity: Activity? = null
    internal var mTitle: String? = null
    internal var mSubtitle: String? = null
    internal var mBackgroundColor: Int? = null
    internal var mTextColor: Int? = null
    internal var mTitleTextSize: Int? = null
    internal var mButtonTitle: String? = null
    internal var mButtonVisibility: Boolean? = null
    internal var mSubtitleTextSize: Int? = null
    internal var mHighlightMode: KanggoBubbleShowCase.HighlightMode? = null
    internal var mDisableTargetClick: Boolean = false
    internal var mShowOnce: String? = null
    internal var mIsFirstOfSequence: Boolean? = null
    internal var mIsLastOfSequence: Boolean? = null
    internal val mArrowPositionList = ArrayList<KanggoBubbleShowCase.ArrowPosition>()
    internal var mTargetView: View? = null
    internal var mBubbleShowCaseListener: KanggoBubbleShowCaseListener? = null
    internal var mSequenceShowCaseListener: SequenceShowCaseListener? = null

    private var onGlobalLayoutListenerTargetView: ViewTreeObserver.OnGlobalLayoutListener? = null

    /**
     * Builder constructor. It needs an instance of the current activity to convert it to a weak reference in order to avoid memory leaks
     */
    constructor(activity: Activity) {
        mActivity = activity
    }

    /**
     * Title of the BubbleShowCase. This text is bolded in the view.
     */
    fun title(title: String): KanggoBubbleShowCaseBuilder {
        mTitle = title
        return this
    }

    /**
     * Additional description of the BubbleShowCase. This text has a regular format
     */
    fun description(subtitle: String): KanggoBubbleShowCaseBuilder {
        mSubtitle = subtitle
        return this
    }


    /**
     * Background color of the BubbleShowCase.
     *  - #3F51B5 color will be set if this param is not defined
     */
    fun backgroundColor(color: Int): KanggoBubbleShowCaseBuilder {
        mBackgroundColor = color
        return this
    }

    /**
     * Background color of the BubbleShowCase.
     *  - #3F51B5 color will be set if this param is not defined
     */
    fun backgroundColorResourceId(colorResId: Int): KanggoBubbleShowCaseBuilder {
        mBackgroundColor = ContextCompat.getColor(mActivity!!, colorResId)
        return this
    }

    /**
     * Text color of the BubbleShowCase.
     *  - White color will be set if this param is not defined
     */
    fun textColor(color: Int): KanggoBubbleShowCaseBuilder {
        mTextColor = color
        return this
    }

    /**
     * Text color of the BubbleShowCase.
     *  - White color will be set if this param is not defined
     */
    fun textColorResourceId(colorResId: Int): KanggoBubbleShowCaseBuilder {
        mTextColor = ContextCompat.getColor(mActivity!!, colorResId)
        return this
    }

    /**
     * Title text size in SP.
     * - Default value -> 16 sp
     */
    fun titleTextSize(textSize: Int): KanggoBubbleShowCaseBuilder {
        mTitleTextSize = textSize
        return this
    }

    /**
     * Description text size in SP.
     * - Default value -> 14 sp
     */
    fun descriptionTextSize(textSize: Int): KanggoBubbleShowCaseBuilder {
        mSubtitleTextSize = textSize
        return this
    }

    /**
     * Showing close button
     * - Default value false -
     * */
    fun showButton(visible: Boolean): KanggoBubbleShowCaseBuilder {
        mButtonVisibility = visible
        return this
    }

    /**
     * Change button title text
     * - Default value false -
     * */
    fun textButton(textButton: String): KanggoBubbleShowCaseBuilder {
        mButtonTitle = textButton
        return this
    }

    /**
     * If an unique id is passed in this function, this BubbleShowCase will only be showed once
     * - ID to identify the BubbleShowCase
     */
    fun showOnce(id: String): KanggoBubbleShowCaseBuilder {
        mShowOnce = id
        return this
    }

    /**
     * Target view to be highlighted. Set a TargetView is essential to figure out BubbleShowCase position
     * - If a target view is not defined, the BubbleShowCase final position will be the center of the screen
     */
    fun targetView(targetView: View): KanggoBubbleShowCaseBuilder {
        mTargetView = targetView
        return this
    }

    /**
     * If this variable is true, when user clicks on the target, the showcase will not be dismissed
     *  Default value -> false
     */
    fun disableTargetClick(isDisabled: Boolean): KanggoBubbleShowCaseBuilder {
        mDisableTargetClick = isDisabled
        return this
    }

    /**
     * Insert an arrowPosition to force the position of the BubbleShowCase.
     * - ArrowPosition enum values: LEFT, RIGHT, TOP and DOWN
     * - If an arrow position is not defined, the BubbleShowCase will be set in a default position depending on the targetView
     */
    fun arrowPosition(arrowPosition: KanggoBubbleShowCase.ArrowPosition): KanggoBubbleShowCaseBuilder {
        mArrowPositionList.clear()
        mArrowPositionList.add(arrowPosition)
        return this
    }

    /**
     * Insert a set of arrowPosition to force the position of the BubbleShowCase.
     * - ArrowPosition enum values: LEFT, RIGHT, TOP and DOWN
     * - If the number of arrow positions is 0 or more than 1, BubbleShowCase will be set on the center of the screen
     */
    fun arrowPosition(arrowPosition: List<KanggoBubbleShowCase.ArrowPosition>): KanggoBubbleShowCaseBuilder {
        mArrowPositionList.clear()
        mArrowPositionList.addAll(arrowPosition)
        return this
    }

    /**
     * Highlight mode. It represents the way that the target view will be highlighted
     * - VIEW_LAYOUT: Default value. All the view box is highlighted (the rectangle where the view is contained). Example: For a TextView, all the element is highlighted (characters and background)
     * - VIEW_SURFACE: Only the view surface is highlighted, but not the background. Example: For a TextView, only the characters will be highlighted
     */
    fun highlightMode(highlightMode: KanggoBubbleShowCase.HighlightMode): KanggoBubbleShowCaseBuilder {
        mHighlightMode = highlightMode
        return this
    }

    /**
     * Add a BubbleShowCaseListener in order to listen the user actions:
     * - onTargetClick -> It is triggered when the user clicks on the target view
     * - onCloseClick -> It is triggered when the user clicks on the close icon
     */
    fun listener(bubbleShowCaseListener: KanggoBubbleShowCaseListener): KanggoBubbleShowCaseBuilder {
        mBubbleShowCaseListener = bubbleShowCaseListener
        return this
    }

    /**
     * Add a sequence listener in order to know when a BubbleShowCase has been dismissed to show another one
     */
    internal fun sequenceListener(sequenceShowCaseListener: SequenceShowCaseListener): KanggoBubbleShowCaseBuilder {
        mSequenceShowCaseListener = sequenceShowCaseListener
        return this
    }

    internal fun isFirstOfSequence(isFirst: Boolean): KanggoBubbleShowCaseBuilder {
        mIsFirstOfSequence = isFirst
        return this
    }

    internal fun isLastOfSequence(isLast: Boolean): KanggoBubbleShowCaseBuilder {
        mIsLastOfSequence = isLast
        return this
    }

    /**
     * Build the BubbleShowCase object from the builder one
     */
    private fun build(): KanggoBubbleShowCase {
        if (mIsFirstOfSequence == null)
            mIsFirstOfSequence = true
        if (mIsLastOfSequence == null)
            mIsLastOfSequence = true

        return KanggoBubbleShowCase(this)
    }

    /**
     * Show the BubbleShowCase using the params added previously
     */
    fun show(): KanggoBubbleShowCase {
        val bubbleShowCase = build()
        if (mTargetView != null) {
            val targetView = mTargetView!!
            if (targetView.height == 0 || targetView.width == 0) {
                //If the view is not already painted, we wait for it waiting for view changes using OnGlobalLayoutListener
                onGlobalLayoutListenerTargetView = ViewTreeObserver.OnGlobalLayoutListener {
                    bubbleShowCase.show()
                    targetView.viewTreeObserver.removeOnGlobalLayoutListener(
                        onGlobalLayoutListenerTargetView
                    )
                }
                targetView.viewTreeObserver.addOnGlobalLayoutListener(
                    onGlobalLayoutListenerTargetView
                )
            } else {
                bubbleShowCase.show()
            }
        } else {
            bubbleShowCase.show()
        }
        return bubbleShowCase
    }

}
package com.kanggo.bubbleshowcase.custom

import android.app.Activity
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewTreeObserver
import androidx.viewbinding.ViewBinding
import com.kanggo.bubbleshowcase.KanggoBubbleShowCaseListener
import com.kanggo.bubbleshowcase.SequenceShowCaseListener
import com.kanggo.bubbleshowcase.base.BaseBubbleShowCaseBuilder
import java.util.ArrayList

/**
 * Created by jcampos on 04/09/2018.
 */
class CustomBubbleShowCaseBuilder : BaseBubbleShowCaseBuilder {

    internal var mActivity: Activity? = null
    internal var mView: View? = null
    internal var mBackgroundColor: Int? = null
    internal var mHighlightMode: CustomBubbleShowCase.HighlightMode? = null
    internal var mShowOnce: String? = null
    internal var mIsFirstOfSequence: Boolean? = null
    internal var mIsLastOfSequence: Boolean? = null
    internal val mArrowPositionList = ArrayList<CustomBubbleShowCase.ArrowPosition>()
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

    fun setContentView(view: View): CustomBubbleShowCaseBuilder {
        mView = view
        return this
    }

    /**
     * Background color of the BubbleShowCase.
     *  - #3F51B5 color will be set if this param is not defined
     */
    fun backgroundColor(color: Int): CustomBubbleShowCaseBuilder {
        mBackgroundColor = color
        return this
    }

    /**
     * Background color of the BubbleShowCase.
     *  - #3F51B5 color will be set if this param is not defined
     */
    fun backgroundColorResourceId(colorResId: Int): CustomBubbleShowCaseBuilder {
        mBackgroundColor = ContextCompat.getColor(mActivity!!, colorResId)
        return this
    }

    /**
     * If an unique id is passed in this function, this BubbleShowCase will only be showed once
     * - ID to identify the BubbleShowCase
     */
    fun showOnce(id: String): CustomBubbleShowCaseBuilder {
        mShowOnce = id
        return this
    }

    /**
     * Target view to be highlighted. Set a TargetView is essential to figure out BubbleShowCase position
     * - If a target view is not defined, the BubbleShowCase final position will be the center of the screen
     */
    fun targetView(targetView: View): CustomBubbleShowCaseBuilder {
        mTargetView = targetView
        return this
    }

    /**
     * Insert an arrowPosition to force the position of the BubbleShowCase.
     * - ArrowPosition enum values: LEFT, RIGHT, TOP and DOWN
     * - If an arrow position is not defined, the BubbleShowCase will be set in a default position depending on the targetView
     */
    fun arrowPosition(arrowPosition: CustomBubbleShowCase.ArrowPosition): CustomBubbleShowCaseBuilder {
        mArrowPositionList.clear()
        mArrowPositionList.add(arrowPosition)
        return this
    }

    /**
     * Insert a set of arrowPosition to force the position of the BubbleShowCase.
     * - ArrowPosition enum values: LEFT, RIGHT, TOP and DOWN
     * - If the number of arrow positions is 0 or more than 1, BubbleShowCase will be set on the center of the screen
     */
    fun arrowPosition(arrowPosition: List<CustomBubbleShowCase.ArrowPosition>): CustomBubbleShowCaseBuilder {
        mArrowPositionList.clear()
        mArrowPositionList.addAll(arrowPosition)
        return this
    }

    /**
     * Highlight mode. It represents the way that the target view will be highlighted
     * - VIEW_LAYOUT: Default value. All the view box is highlighted (the rectangle where the view is contained). Example: For a TextView, all the element is highlighted (characters and background)
     * - VIEW_SURFACE: Only the view surface is highlighted, but not the background. Example: For a TextView, only the characters will be highlighted
     */
    fun highlightMode(highlightMode: CustomBubbleShowCase.HighlightMode): CustomBubbleShowCaseBuilder {
        mHighlightMode = highlightMode
        return this
    }

    /**
     * Add a BubbleShowCaseListener in order to listen the user actions:
     * - onTargetClick -> It is triggered when the user clicks on the target view
     * - onCloseClick -> It is triggered when the user clicks on the close icon
     */
    fun listener(bubbleShowCaseListener: KanggoBubbleShowCaseListener): CustomBubbleShowCaseBuilder {
        mBubbleShowCaseListener = bubbleShowCaseListener
        return this
    }

    /**
     * Add a sequence listener in order to know when a BubbleShowCase has been dismissed to show another one
     */
    internal fun sequenceListener(sequenceShowCaseListener: SequenceShowCaseListener): CustomBubbleShowCaseBuilder {
        mSequenceShowCaseListener = sequenceShowCaseListener
        return this
    }

    internal fun isFirstOfSequence(isFirst: Boolean): CustomBubbleShowCaseBuilder {
        mIsFirstOfSequence = isFirst
        return this
    }

    internal fun isLastOfSequence(isLast: Boolean): CustomBubbleShowCaseBuilder {
        mIsLastOfSequence = isLast
        return this
    }

    /**
     * Build the BubbleShowCase object from the builder one
     */
    private fun build(): CustomBubbleShowCase {
        if (mIsFirstOfSequence == null)
            mIsFirstOfSequence = true
        if (mIsLastOfSequence == null)
            mIsLastOfSequence = true

        return CustomBubbleShowCase(this)
    }

    /**
     * Show the BubbleShowCase using the params added previously
     */
    fun show(): CustomBubbleShowCase {
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
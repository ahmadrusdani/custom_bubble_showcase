package com.kanggo.bubbleshowcase.newBase

import com.kanggo.bubbleshowcase.SequenceShowCaseListener
import com.kanggo.bubbleshowcase.kanggo.KanggoBubbleShowCaseBuilder

abstract class BaseBubbleShowCaseBuilder : BaseBubbleShowCase {

    protected abstract var mIsFirstOfSequence: Boolean?
    protected abstract var mIsLastOfSequence: Boolean?
    protected abstract var mSequenceShowCaseListener: SequenceShowCaseListener?

    override fun sequenceListener(sequenceShowCaseListener: SequenceShowCaseListener): BaseBubbleShowCaseBuilder {
        mSequenceShowCaseListener = sequenceShowCaseListener
        return this
    }

    override fun isFirstOfSequence(isFirst: Boolean): BaseBubbleShowCaseBuilder {
        this.mIsFirstOfSequence = isFirst
        return this
    }

    override fun isLastOfSequence(isLast: Boolean): BaseBubbleShowCaseBuilder {
        this.mIsLastOfSequence = isLast
        return this
    }
}
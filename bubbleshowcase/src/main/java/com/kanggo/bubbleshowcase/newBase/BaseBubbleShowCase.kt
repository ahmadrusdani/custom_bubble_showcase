package com.kanggo.bubbleshowcase.newBase

import com.kanggo.bubbleshowcase.SequenceShowCaseListener
import com.kanggo.bubbleshowcase.kanggo.KanggoBubbleShowCaseBuilder

interface BaseBubbleShowCase {
    fun sequenceListener(sequenceShowCaseListener: SequenceShowCaseListener): BaseBubbleShowCaseBuilder
    fun isFirstOfSequence(isFirst: Boolean): BaseBubbleShowCaseBuilder
    fun isLastOfSequence(isLast: Boolean): BaseBubbleShowCaseBuilder
}
package com.bobble.dexter.models

import java.nio.ByteBuffer

data class FieldIdItem(
        var classIdx: Short,
        var typeIdx: Short,
        var nameIdx: Int
) {
    companion object {
        val size: Int = 8
    }

    constructor(byteBuffer: ByteBuffer) : this(
            classIdx = byteBuffer.short,
            typeIdx = byteBuffer.short,
            nameIdx = byteBuffer.int
    )

    constructor():this(classIdx =0,
            typeIdx = 0,
            nameIdx = 0)
}

package com.bobble.dexter.models

import java.nio.ByteBuffer


data class TypeIdItem(
        var descriptorIdx: Int
) {
    companion object {
        val size: Int = 4
    }

    constructor(byteBuffer: ByteBuffer) : this(
            descriptorIdx = byteBuffer.int
    )

    constructor():this(descriptorIdx = 0)
}
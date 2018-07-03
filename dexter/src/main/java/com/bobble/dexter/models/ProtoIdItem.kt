package com.bobble.dexter.models

import java.nio.ByteBuffer

data class ProtoIdItem(
        var shortyIdx: Int,
        var returnTypeIdx: Int,
        var parametersOff: Int
) {
    companion object {
        val size: Int = 12
    }

    constructor(byteBuffer: ByteBuffer) : this(
            shortyIdx = byteBuffer.int,
            returnTypeIdx = byteBuffer.int,
            parametersOff = byteBuffer.int
    )

    constructor():this(shortyIdx = 0,
            returnTypeIdx = 0,
            parametersOff = 0)
}
package com.bobble.dexter.models

import java.nio.ByteBuffer

data class ClassDefItem(
        var classIdx: Int,
        var accessFlags: Int,
        var superclassIdx: Int,
        var interfacesOff: Int,
        var sourceFileIdx: Int,
        var annotationsOff: Int,
        var classDataOff: Int,
        var staticValuesOff: Int
) {
    companion object {
        val size: Int = 0x20
    }

    constructor(byteBuffer: ByteBuffer) : this(
            classIdx = byteBuffer.int,
            accessFlags = byteBuffer.int,
            superclassIdx = byteBuffer.int,
            interfacesOff = byteBuffer.int,
            sourceFileIdx = byteBuffer.int,
            annotationsOff = byteBuffer.int,
            classDataOff = byteBuffer.int,
            staticValuesOff = byteBuffer.int
    )

    constructor() : this(
            classIdx = 0,
            accessFlags = 0,
            superclassIdx = 0,
            interfacesOff = 0,
            sourceFileIdx = 0,
            annotationsOff = 0,
            classDataOff = 0,
            staticValuesOff = 0
    )

}
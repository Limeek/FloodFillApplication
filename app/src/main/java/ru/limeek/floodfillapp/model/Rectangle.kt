package ru.limeek.floodfillapp.model

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable

data class Rectangle (
    var colorFill: Int = Color.WHITE,
    var x: Int = 0,
    var y: Int = 0,
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(colorFill)
        parcel.writeInt(x)
        parcel.writeInt(y)
        parcel.writeFloat(left)
        parcel.writeFloat(top)
        parcel.writeFloat(right)
        parcel.writeFloat(bottom)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Rectangle> {
        override fun createFromParcel(parcel: Parcel): Rectangle {
            return Rectangle(parcel)
        }

        override fun newArray(size: Int): Array<Rectangle?> {
            return arrayOfNulls(size)
        }
    }
}
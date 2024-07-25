package com.example.hotspotconnect.utils

import android.content.Context
import android.view.View
import android.widget.Toast

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

fun View.invisible():Boolean{
    return this.visibility == View.VISIBLE
}

fun Context.toast(msg:String){
  Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}

fun Context.toastLong(msg:String){
    Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
}


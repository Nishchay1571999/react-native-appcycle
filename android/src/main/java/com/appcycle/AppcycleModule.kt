package com.appcycle

import com.facebook.react.bridge.ReactApplicationContext

class AppcycleModule(reactContext: ReactApplicationContext) :
  NativeAppcycleSpec(reactContext) {

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = NativeAppcycleSpec.NAME
  }
}

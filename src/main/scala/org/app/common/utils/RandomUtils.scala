package org.app.common.utils

object RandomUtils {
  def ID(length: Int = 16): String = {
    java.util.UUID.randomUUID().toString.replace("-", "").take(length)
  }
}

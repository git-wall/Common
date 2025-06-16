package org.app.common.support

import com.fasterxml.jackson.core.`type`.TypeReference

object TypeRef {
  def refer[T](): TypeReference[T] = new TypeReference[T] { }
}

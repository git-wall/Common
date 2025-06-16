package org.app.common.utils

object SFluentUtils {

  val STREAM_MAX_SIZE = 10000

  def filter[T](list: List[T], f: T => Boolean): List[T] = {
    list.filter(f)
  }

  def map[T, R](list: List[T], m: T => R): List[R] = {
    list.map(m)
  }

  def pipeline[T, R](list: List[T], f: T => Boolean, m: T => R): List[R] = {
    list.filter(f).map(m)
  }

  def pipeline[T, R](list: List[T], f: T => Boolean, m: T => R, limit: Int): List[R] = {
    list.filter(f).map(m).take(limit)
  }
}

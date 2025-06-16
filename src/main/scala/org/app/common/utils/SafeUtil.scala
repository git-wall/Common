package org.app.common.utils

import scala.util.{Try, Success, Failure}

object SafeUtil {
  def getTSF[T](t: T): T = {
    Try(t) match {
      case Success(value) => value
      case Failure(exception) => throw new IllegalArgumentException("Error when safeTry", exception)
    }
  }

  def parse[T](any: Any): Either[Any, T] = {
    try {
      Right(any.asInstanceOf[T])
    } catch {
      case _: ClassCastException => Left("Not a valid type")
    }
  }

  def mapper[T, R](t: T, mapper: Function[T, R]): R = {
    Option.apply(t).map(mapper).getOrElse(throw new IllegalArgumentException("Error when mapper"))
  }

  def filterThrow[T](t: T, filter: T => Boolean, errorMessage: String): T = {
    Option.apply(t).filter(filter).getOrElse(throw new IllegalArgumentException(errorMessage))
  }

  def filterOrElse[T](t: T, filter: T => Boolean, default: T): T = {
    Option.apply(t).filter(filter).getOrElse(default)
  }
}

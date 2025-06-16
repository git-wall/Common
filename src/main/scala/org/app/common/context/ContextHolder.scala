package org.app.common.context

object ContextHolder {
  private val contextThreadLocal = new ThreadLocal[scala.collection.mutable.Map[String, Any]] {
    override def initialValue(): scala.collection.mutable.Map[String, Any] = scala.collection.mutable.Map.empty
  }

  def set(key: String, value: Any): Unit = contextThreadLocal.get().update(key, value)

  def get[T](key: String): Option[T] =
    contextThreadLocal.get().get(key).asInstanceOf[Option[T]]

  def remove(key: String): Unit = contextThreadLocal.get().remove(key)

  def clear(): Unit = contextThreadLocal.remove()
}
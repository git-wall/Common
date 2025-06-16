package org.app.common.design

import org.app.common.thread.Worker

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

class PoisonPill[T](val name: String, poison: T, consumer: T => Unit) extends Worker[T] {

  val queue: BlockingQueue[T] = new LinkedBlockingQueue[T]()
  
  def put(data: T): Unit = {
    queue.put(data)
  }

  override def before(): Unit = {
    println(s"[$name] Ready to run") 
  }

  override def now(): Unit = {
    val item = queue.poll()
    if (item == null) return

    if (item == poison) {
      println(s"[$name] Received poison pill. Shutting down.")
      stop()
    } else {
      consumer(item)
    }
  }

  override def after(): Unit = {
    println(s"[$name] Worker stopped")
  }
}


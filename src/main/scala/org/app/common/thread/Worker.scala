package org.app.common.thread

import org.app.common.design.TemplateMethod

abstract class Worker[T] extends Runnable with TemplateMethod {
  @volatile private var running = true

  def stop(): Unit = {
    running = false
  }

  def start(): Unit = {
    val thread = new Thread(this)
    thread.setDaemon(true)
    thread.start()
  }

  override def run(): Unit = {
    before()
    while (running) {
      now()
    }
    after()
  }
}

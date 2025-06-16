package org.app.common.thread

import org.app.common.design.TemplateMethodAction

abstract class ThreadAction : Runnable, TemplateMethodAction {

    private var running = true

    fun start() {
        running = true
        val thread = Thread(this)
        thread.isDaemon = true
        thread.start()
    }

    fun stop() {
        running = false
    }

    abstract override fun before()

    abstract override fun now()

    abstract override fun after()

    override fun run() {
        before()
        while (running) {
            now()
        }
        after()
    }
}
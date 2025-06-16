package org.app.common.design

import mu.KotlinLogging
import org.app.common.thread.ThreadAction
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue

class Poison<T>(
    private val name: String,
    private val poison: T,
    private val consumer: (T) -> Unit
) : ThreadAction() {

    private val logger = KotlinLogging.logger {}

    val queue: Queue<T> = LinkedBlockingQueue<T>()

    fun put(item: T) {
        queue.offer(item)
    }

    override fun before() {
        logger.info("$name ready to run")
    }

    override fun now() {
        val item = queue.poll() ?: return
        if (item == poison) {
            logger.error("$name received poison, shutting down")
            stop()
        } else {
            consumer(item)
        }
    }

    override fun after() {
        logger.info("$name is shutting down")
    }
}
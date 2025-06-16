import threading
import logging
from typing import TypeVar, Generic, Callable
from queue import Queue, Empty

T = TypeVar('T')
log = logging.getLogger(__name__)

class PoisonPill(Generic[T], threading.Thread):
    def __init__(self, name: str, poison: T, queue: Queue, consumer: Callable[[T], None]):
        super().__init__()
        self.name = name.upper()
        self.queue = queue
        self.consumer = consumer
        self.poison = poison
        self._shutdown = threading.Event()

    def offer(self, item: T):
        self.queue.put(item)

    def before(self):
        logger.info(f"{self.name} ready to run")

    def after(self):
        logger.info(f"{self.name} close")

    def run(self):
        self.before()
        while not self._shutdown.is_set():
            try:
                item = self.queue.get(timeout=1)  # Avoid infinite block
                if item == self.poison:
                    logger.info(f"{self.name} received poison pill, exiting")
                    self._shutdown.set()
                    break
                self.consumer(item)
            except Empty:
                continue
        self.after()

    def shutdown(self):
        self._shutdown.set()
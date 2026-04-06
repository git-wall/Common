```java
class BinaryHeapPriorityQueueExample {
    public static void main(String[] args) {
        // Create a priority queue with natural ordering
        PriorityQueue<Integer> pq = new BinaryHeapPriorityQueue<>();

        // Add elements
        pq.add(10);
        pq.add(5);
        pq.add(15);
        pq.add(3);
        pq.add(7);

        System.out.println("Priority queue size: " + pq.size());  // Output: 5

        // Peek at the head element (smallest element)
        System.out.println("Head element: " + pq.peek());  // Output: 3

        // Poll elements (retrieve and remove the head)
        System.out.println("\nPolling elements in priority order:");
        while (!pq.isEmpty()) {
            System.out.println(pq.poll());  // Output: 3, 5, 7, 10, 15
        }

        // Create a priority queue with a custom comparator (reverse order)
        PriorityQueue<Integer> reversePq = new BinaryHeapPriorityQueue<>(
            (a, b) -> b.compareTo(a)  // Reverse natural ordering
        );

        // Add elements
        reversePq.add(10);
        reversePq.add(5);
        reversePq.add(15);
        reversePq.add(3);
        reversePq.add(7);

        System.out.println("\nReverse priority queue size: " + reversePq.size());  // Output: 5

        // Peek at the head element (largest element due to reverse ordering)
        System.out.println("Head element: " + reversePq.peek());  // Output: 15

        // Poll elements (retrieve and remove the head)
        System.out.println("\nPolling elements in reverse priority order:");
        while (!reversePq.isEmpty()) {
            System.out.println(reversePq.poll());  // Output: 15, 10, 7, 5, 3
        }

        // Example with custom objects
        PriorityQueue<Task> taskQueue = new BinaryHeapPriorityQueue<>();

        // Add tasks with priorities
        taskQueue.add(new Task("Write report", 3));
        taskQueue.add(new Task("Fix bug", 1));
        taskQueue.add(new Task("Prepare presentation", 2));
        taskQueue.add(new Task("Attend meeting", 4));
        taskQueue.add(new Task("Code review", 2));

        System.out.println("\nTask queue size: " + taskQueue.size());  // Output: 5

        // Process tasks in priority order (lower number = higher priority)
        System.out.println("\nProcessing tasks in priority order:");
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            System.out.println("Processing: " + task);
        }

        // Example of using offer, remove, and element methods
        PriorityQueue<Integer> methodsPq = new BinaryHeapPriorityQueue<>();

        // Using offer (same as add)
        methodsPq.offer(10);
        methodsPq.offer(5);
        methodsPq.offer(15);

        // Using element (same as peek but throws exception if empty)
        System.out.println("\nHead element: " + methodsPq.element());  // Output: 5

        // Using remove (same as poll but throws exception if empty)
        System.out.println("Removed element: " + methodsPq.remove());  // Output: 5

        // Check if contains an element
        System.out.println("Contains 10: " + methodsPq.contains(10));  // Output: true
        System.out.println("Contains 5: " + methodsPq.contains(5));    // Output: false (already removed)

        // Convert to array
        Object[] array = methodsPq.toArray();
        System.out.println("\nArray contents:");
        for (Object obj : array) {
            System.out.println(obj);  // Output: 10, 15
        }

        // Clear the queue
        methodsPq.clear();
        System.out.println("\nAfter clearing, size: " + methodsPq.size());  // Output: 0
    }

    // Example class for demonstrating priority queue with custom objects
    static class Task implements Comparable<Task> {
        private final String name;
        private final int priority;  // Lower number = higher priority

        public Task(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public int compareTo(Task other) {
            return Integer.compare(this.priority, other.priority);
        }

        @Override
        public String toString() {
            return name + " (Priority: " + priority + ")";
        }
    }
}
```

package org.app.common.crdt;

import com.netopyr.wurmloch.crdt.*;
import com.netopyr.wurmloch.store.CrdtStore;
import com.netopyr.wurmloch.store.LocalCrdtStore;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Easy-to-use CRDT wrapper library for common distributed data operations
 * Provides simple interfaces for counters, sets, registers, and maps
 */
public class EasyCRDT {

    private final CrdtStore store;
    /**
     * -- GETTER --
     * Get the current node ID
     */
    @Getter
    private final String nodeId;
    private final Map<String, Object> crdtInstances = new ConcurrentHashMap<>();

    /**
     * Create a new CRDT instance with a unique node ID
     *
     * @param nodeId Unique identifier for this node
     */
    public EasyCRDT(String nodeId) {
        this.nodeId = nodeId;
        this.store = new LocalCrdtStore();
    }

    /**
     * Create a new CRDT instance with auto-generated node ID
     */
    public EasyCRDT() {
        this(UUID.randomUUID().toString());
    }

    // ========== COUNTER OPERATIONS ==========

    /**
     * Create or get a grow-only counter
     * Use case: Page views, likes, downloads - things that only increase
     */
    public CounterWrapper createGrowCounter(String counterId) {
        return new CounterWrapper(counterId, CounterType.GROW_ONLY);
    }

    /**
     * Create or get a plus-minus counter
     * Use case: Votes (up/down), inventory levels, account balances
     */
    public CounterWrapper createPlusMinusCounter(String counterId) {
        return new CounterWrapper(counterId, CounterType.PLUS_MINUS);
    }

    // ========== SET OPERATIONS ==========

    /**
     * Create or get a grow-only set
     * Use case: Tags, categories, user permissions - things that are only added
     */
    public SetWrapper<String> createGrowSet(String setId) {
        return new SetWrapper<>(setId, SetType.GROW_ONLY);
    }

    /**
     * Create or get an add-remove set
     * Use case: Shopping cart, friend lists, group members
     */
    public SetWrapper<String> createAddRemoveSet(String setId) {
        return new SetWrapper<>(setId, SetType.ADD_REMOVE);
    }

    // ========== REGISTER OPERATIONS ==========

    /**
     * Create or get a last-writer-wins register
     * Use case: User profile data, document titles, configuration values
     */
    public RegisterWrapper<String> createRegister(String registerId) {
        return new RegisterWrapper<>(registerId);
    }

    // ========== SYNCHRONIZATION ==========

    /**
     * Sync with another CRDT instance
     * Use case: Periodic sync between nodes, conflict resolution
     */
    public void syncWith(EasyCRDT other) {
        // In a real implementation, you'd sync the underlying CRDTs
        // This is a placeholder for the sync mechanism
        System.out.println("Syncing " + this.nodeId + " with " + other.nodeId);
    }

    // ========== INNER CLASSES ==========

    public class CounterWrapper {
        private final String id;
        private final CounterType type;

        private CounterWrapper(String id, CounterType type) {
            this.id = id;
            this.type = type;
            initializeCounter();
        }

        private void initializeCounter() {
            String key = "counter_" + id;
            if (!crdtInstances.containsKey(key)) {
                if (type == CounterType.GROW_ONLY) {
                    crdtInstances.put(key, store.createGCounter(key));
                } else {
                    crdtInstances.put(key, store.createPNCounter(key));
                }
            }
        }

        /**
         * Increment the counter by 1
         */
        public void increment() {
            increment(1);
        }

        /**
         * Increment the counter by a specified amount
         */
        public void increment(long amount) {
            if (type == CounterType.GROW_ONLY) {
                GCounter counter = (GCounter) crdtInstances.get("counter_" + id);
                counter.increment(amount);
            } else {
                PNCounter counter = (PNCounter) crdtInstances.get("counter_" + id);
                counter.increment(amount);
            }
        }

        /**
         * Decrement the counter (only for plus-minus counters)
         */
        public void decrement() {
            decrement(1);
        }

        /**
         * Decrement the counter by a specified amount (only for plus-minus counters)
         */
        public void decrement(long amount) {
            if (type == CounterType.PLUS_MINUS) {
                PNCounter counter = (PNCounter) crdtInstances.get("counter_" + id);
                counter.decrement(amount);
            } else {
                throw new UnsupportedOperationException("Cannot decrement a grow-only counter");
            }
        }

        /**
         * Get current counter value
         */
        public long getValue() {
            if (type == CounterType.GROW_ONLY) {
                GCounter counter = (GCounter) crdtInstances.get("counter_" + id);
                return counter.get();
            } else {
                PNCounter counter = (PNCounter) crdtInstances.get("counter_" + id);
                return counter.get();
            }
        }
    }

    public class SetWrapper<T> {
        private final String id;
        private final SetType type;

        private SetWrapper(String id, SetType type) {
            this.id = id;
            this.type = type;
            initializeSet();
        }

        private void initializeSet() {
            String key = "set_" + id;
            if (!crdtInstances.containsKey(key)) {
                if (type == SetType.GROW_ONLY) {
                    crdtInstances.put(key, store.createGSet(key));
                } else {
                    crdtInstances.put(key, store.createORSet(key));
                }
            }
        }

        /**
         * Add an element to set
         */
        @SuppressWarnings("unchecked")
        public void add(T element) {
            if (type == SetType.GROW_ONLY) {
                GSet<T> set = (GSet<T>) crdtInstances.get("set_" + id);
                set.add(element);
            } else {
                ORSet<T> set = (ORSet<T>) crdtInstances.get("set_" + id);
                set.add(element);
            }
        }

        /**
         * Remove an element from a set (only for add-remove sets)
         */
        @SuppressWarnings("unchecked")
        public void remove(T element) {
            if (type == SetType.ADD_REMOVE) {
                ORSet<T> set = (ORSet<T>) crdtInstances.get("set_" + id);
                set.remove(element);
            } else {
                throw new UnsupportedOperationException("Cannot remove from grow-only set");
            }
        }

        /**
         * Check if an element exists in a set
         */
        @SuppressWarnings("unchecked")
        public boolean contains(T element) {
            if (type == SetType.GROW_ONLY) {
                GSet<T> set = (GSet<T>) crdtInstances.get("set_" + id);
                return set.contains(element);
            } else {
                ORSet<T> set = (ORSet<T>) crdtInstances.get("set_" + id);
                return set.contains(element);
            }
        }

        /**
         * Get all elements in a set
         */
        @SuppressWarnings("unchecked")
        public Set<T> getAll() {
            String key = "set_" + id;
            Object setInstance = crdtInstances.get(key);

            if (setInstance == null) {
                return new HashSet<>(); // or throw an exception
            }

            try {
                if (type == SetType.GROW_ONLY) {
                    GSet<T> set = (GSet<T>) setInstance;
                    return new HashSet<>(set);
                } else {
                    ORSet<T> set = (ORSet<T>) setInstance;
                    return new HashSet<>(set);
                }
            } catch (ClassCastException e) {
                throw new IllegalStateException("Invalid CRDT type in storage for key: " + key, e);
            }
        }

        /**
         * Get size of set
         */
        public int size() {
            return getAll().size();
        }
    }

    public class RegisterWrapper<T> {
        private final String id;

        private RegisterWrapper(String id) {
            this.id = id;
            initializeRegister();
        }

        private void initializeRegister() {
            String key = "register_" + id;
            if (!crdtInstances.containsKey(key)) {
                crdtInstances.put(key, store.createLWWRegister(key));
            }
        }

        /**
         * Set value in register
         */
        @SuppressWarnings("unchecked")
        public void set(T value) {
            LWWRegister<T> register = (LWWRegister<T>) crdtInstances.get("register_" + id);
            register.set(value);
        }

        /**
         * Get current value from register
         */
        @SuppressWarnings("unchecked")
        public Optional<T> get() {
            LWWRegister<T> register = (LWWRegister<T>) crdtInstances.get("register_" + id);
            return Optional.ofNullable(register.get());
        }
    }

    // ========== ENUMS ==========

    private enum CounterType {
        GROW_ONLY, PLUS_MINUS
    }

    private enum SetType {
        GROW_ONLY, ADD_REMOVE
    }
}
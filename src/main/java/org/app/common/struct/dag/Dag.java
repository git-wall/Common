package org.app.common.struct.dag;

import java.io.Serializable;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public interface Dag<E> extends Collection<E>, Cloneable, Serializable {

    boolean put(E key, E value);

    boolean putAll(E key, Collection<E> value);

    Deque<E> getRoots();

    int rootSize();

    Deque<E> getNodes();

    Set<E> getEdges(E key);

    Deque<Edge<E>> getRootTrees();

    List<E> topologicalSort();

    boolean putIfAcyclic(E key, E value);

    interface Edge<E> {
        E getData();
        void setData(E data);
        Set<Edge<E>> getChildren();
        void setChildren(Set<Edge<E>> children);
    }
}

package org.app.common.algorithms;

public class FenwickTree {
    private final int[] tree;

    public FenwickTree(int size) {
        tree = new int[size + 1]; // 1-based indexing
    }

    // Adds value to the element at index
    public void update(int index, int value) {
        for (; index < tree.length; index += index & -index)
            tree[index] += value;
    }

    // Computes prefix sum from 1 to index
    public int query(int index) {
        int sum = 0;
        for (; index > 0; index -= index & -index)
            sum += tree[index];
        return sum;
    }
}


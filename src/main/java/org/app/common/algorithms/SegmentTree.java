package org.app.common.algorithms;

public class SegmentTree {
    private final int n;
    private final long[] tree;

    public SegmentTree(long[] arr) {
        this.n = arr.length;
        // allocate 4*N to be safe
        this.tree = new long[4 * n];
        build(arr, 1, 0, n - 1);
    }

    // Build the tree
    private void build(long[] arr, int node, int start, int end) {
        if (start == end) {
            tree[node] = arr[start];
        } else {
            int mid = (start + end) >>> 1;
            build(arr, 2 * node, start, mid);
            build(arr, 2 * node + 1, mid + 1, end);
            tree[node] = tree[2 * node] + tree[2 * node + 1];
        }
    }

    // Range sum query [l, r]
    public long query(int l, int r) {
        return query(1, 0, n - 1, l, r);
    }

    private long query(int node, int start, int end, int l, int r) {
        if (r < start || end < l) {
            return 0; // identity for sum
        }
        if (l <= start && end <= r) {
            return tree[node];
        }
        int mid = (start + end) >>> 1;
        long leftSum = query(2 * node, start, mid, l, r);
        long rightSum = query(2 * node + 1, mid + 1, end, l, r);
        return leftSum + rightSum;
    }

    // Point update: set arr[idx] = val
    public void update(int idx, long val) {
        update(1, 0, n - 1, idx, val);
    }

    private void update(int node, int start, int end, int idx, long val) {
        if (start == end) {
            tree[node] = val;
        } else {
            int mid = (start + end) >>> 1;
            if (idx <= mid) {
                update(2 * node, start, mid, idx, val);
            } else {
                update(2 * node + 1, mid + 1, end, idx, val);
            }
            tree[node] = tree[2 * node] + tree[2 * node + 1];
        }
    }
}


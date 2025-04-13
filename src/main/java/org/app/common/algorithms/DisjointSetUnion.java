package org.app.common.algorithms;

import lombok.Getter;

import java.util.*;

public class DisjointSetUnion {

    /**
     * Weighted Union-Find with Path Compression
     */
    public static class WeightedUnion {
        private final int[] parent;
        private final int[] size;

        public WeightedUnion(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX == rootY) return;

            // Weighted union
            if (size[rootX] < size[rootY]) {
                parent[rootX] = rootY;
                size[rootY] += size[rootX];
            } else {
                parent[rootY] = rootX;
                size[rootX] += size[rootY];
            }
        }
    }

    /**
     * Persistent DSU with rollback support.
     */
    public static class PersistentDSU {
        private final int[] parent;
        private final int[] size;
        private final Deque<Change> history;

        public static class Change {
            int x, parentX, sizeX, y, parentY, sizeY;

            public Change(int x, int parentX, int sizeX, int y, int parentY, int sizeY) {
                this.x = x;
                this.parentX = parentX;
                this.sizeX = sizeX;
                this.y = y;
                this.parentY = parentY;
                this.sizeY = sizeY;
            }
        }

        public PersistentDSU(int n) {
            parent = new int[n];
            size = new int[n];
            history = new ArrayDeque<>();
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        public int find(int x) {
            return parent[x] == x ? x : find(parent[x]);
        }

        public void union(int x, int y) {
            int fx = find(x);
            int fy = find(y);
            if (fx == fy) return;

            if (size[fx] < size[fy]) {
                int tmp = fx;
                fx = fy;
                fy = tmp;
            }

            history.push(new Change(fx, parent[fx], size[fx], fy, parent[fy], size[fy]));
            parent[fy] = fx;
            size[fx] += size[fy];
        }

        public void rollback() {
            if (history.isEmpty()) return;
            Change c = history.pop();
            parent[c.x] = c.parentX;
            size[c.x] = c.sizeX;
            parent[c.y] = c.parentY;
            size[c.y] = c.sizeY;
        }
    }

    /**
     * DSU on Tree (also called Small-to-Large or Heavy-Light Merging)
     */
    public static class DSUOnTree {
        private final List<Integer>[] tree;
        @Getter
        private final int[] size, color, result;
        private final Map<Integer, Integer> colorCount;

        public DSUOnTree(int n, int[] color) {
            this.tree = new ArrayList[n];
            this.size = new int[n];
            this.result = new int[n];
            this.colorCount = new HashMap<>();
            this.color = color;
            for (int i = 0; i < n; i++) tree[i] = new ArrayList<>();
        }

        public void addEdge(int u, int v) {
            tree[u].add(v);
            tree[v].add(u);
        }

        public void dfsSize(int u, int p) {
            size[u] = 1;
            for (int v : tree[u]) {
                if (v != p) {
                    dfsSize(v, u);
                    size[u] += size[v];
                }
            }
        }

        public void dfs(int u, int p, boolean keep) {
            int maxSize = -1, bigChild = -1;
            for (int v : tree[u]) {
                if (v != p && size[v] > maxSize) {
                    maxSize = size[v];
                    bigChild = v;
                }
            }

            for (int v : tree[u]) {
                if (v != p && v != bigChild) dfs(v, u, false);
            }

            if (bigChild != -1) dfs(bigChild, u, true);

            colorCount.put(color[u], colorCount.getOrDefault(color[u], 0) + 1);

            for (int v : tree[u]) {
                if (v != p && v != bigChild) {
                    // Example merge: assuming we store count per color
                    addSubtree(v, u);
                }
            }

            result[u] = colorCount.size(); // Example: number of distinct colors in subtree

            if (!keep) {
                removeSubtree(u, p);
            }
        }

        private void addSubtree(int u, int p) {
            colorCount.put(color[u], colorCount.getOrDefault(color[u], 0) + 1);
            for (int v : tree[u]) {
                if (v != p) addSubtree(v, u);
            }
        }

        private void removeSubtree(int u, int p) {
            colorCount.put(color[u], colorCount.get(color[u]) - 1);
            if (colorCount.get(color[u]) == 0) {
                colorCount.remove(color[u]);
            }
            for (int v : tree[u]) {
                if (v != p) removeSubtree(v, u);
            }
        }
    }
}


package org.app.common.stream;

import lombok.Getter;

@Getter
class JoinedData<L, R> {
    private final L left;
    private final R right;

    public JoinedData(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return String.format("Joined{left=%s, right=%s}", left, right);
    }
}

package org.app.common.entities;

import lombok.Getter;
import lombok.Setter;

import java.sql.Statement;

@Setter
@Getter
public class Action {
    private int success;
    public int failed;
    private int total;
    private int unknown;

    public Action(int total) {
        this.total = total;
    }

    public void incrementSuccess(int count) {
        this.success += count;
    }

    public void incrementFailed(int count) {
        this.failed += count;
    }

    public void incrementUnknownSuccess(int count) {
        this.unknown += count;
    }

    public void collectDataInfo(int[] rs) {
        for (int r : rs) {
            if (r >= 0 || r == Statement.SUCCESS_NO_INFO) {
                incrementSuccess(1);
            } else if (r == Statement.EXECUTE_FAILED) {
                incrementFailed(1);
            } else {
                incrementUnknownSuccess(1);
            }
        }
    }
}

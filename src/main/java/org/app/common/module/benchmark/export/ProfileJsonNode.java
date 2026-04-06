package org.app.common.module.benchmark.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ProfileJsonNode {

    public String method;

    public long timeMs;
    public long memoryKb;

    public long count;

    public Integer callCount;

    public List<ProfileJsonNode> children = new ArrayList<>();
}

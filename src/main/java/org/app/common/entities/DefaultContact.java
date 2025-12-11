package org.app.common.entities;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class DefaultContact implements ContactService {
    @Override
    public void init() {
        log.info("Initializing DefaultContact");
    }
}

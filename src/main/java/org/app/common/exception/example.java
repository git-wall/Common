package org.app.common.exception;

import lombok.NoArgsConstructor;
import org.app.common.entities.ApiResponse;
import org.app.common.exception.base.AppException;
import org.app.common.exception.base.HttpSupport;

public class example {
    private final Core repo;
    private final Core2 repo2;

    public example(Core repo, Core2 repo2) {
        this.repo = repo;
        this.repo2 = repo2;
    }

    public Object get(Long id) {
        try {
            var a = HttpSupport.runWithRetry(repo::callApiCore, "example.Core.callApiCore");
            return HttpSupport.run(() -> repo2.callApiCore2(a), "example.Core2.callApiCore2");
        } catch (AppException e) {
            return ErrorHandler.handle(e, UserDto::empty);
        }
    }

    public static class Core {
        public <T> ApiResponse<T> callApiCore() {
            return null;
        }
    }

    public static class Core2 {
        public <T> ApiResponse<T> callApiCore2(Object a) {
            return null;
        }
    }

    @NoArgsConstructor
    public static class UserDto {
        private static final UserDto empty = new UserDto();
        private int a;

        public static UserDto empty() {
            return empty;
        }
    }
}

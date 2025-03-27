package org.app.common.validation;

/**
 * <pre>
 * {@code
 * // api define
 * @PostMapping(value = TagURL.Auth.REGISTER)
 * public void a(@Validated(GroupValidation.CREATE.class) @RequestBody dto request) {}
 *
 * // request
 * @NotNull(groups = {GroupValidation.UPDATE.class})
 * private Integer id;
 * }
 * </pre>
 */
public interface GroupValidation {
    interface CREATE {
    }

    interface UPDATE {
    }
}

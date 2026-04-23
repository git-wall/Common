<<<<<<<< HEAD:core/src/main/java/org/app/core/exception/TooManyRequestsException.java
package org.app.core.exception;
========
package org.app.exception;
>>>>>>>> dev:exception/src/main/java/org/app/exception/TooManyRequestsException.java

import lombok.experimental.StandardException;

@StandardException
public class TooManyRequestsException extends RuntimeException {
    private static final long serialVersionUID = -1595757622964685690L;
}

package org.app.web;

import javax.servlet.http.HttpServletRequest;

public interface RequestProvider {
    /**
     * If spring create class and Override function and add this shit
     * <pre>{@code
     * RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
     * if (requestAttributes != null) {
     *    if (requestAttributes instanceof ServletRequestAttributes) {
     *       return ((ServletRequestAttributes) requestAttributes).getRequest();}}
     * }
     * */
    HttpServletRequest getCurrentRequest();
}

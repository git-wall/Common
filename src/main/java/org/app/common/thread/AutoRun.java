package org.app.common.thread;

import org.app.common.annotation.Description;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @apiNote
 * <pre>{@code
 *     @AutoRun // here is import + extends RunnableProvider
 *     public class ScheduledReport extends RunnableProvider {
 *         @Override
 *         public void before() {
 *             logg(this.getClass().getSimpleName(), " ready to run");
 *         }
 *
 *         @Override
 *         public void now() {
 *         }
 *
 *         @Override
 *         public void after() {
 *             logg(this.getClass().getSimpleName(), " close");
 *         }
 *     }
 * }</pre>
 * */

@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Description(detail = "This class for collect class have injection to register thread to class RunnableProvider")
public @interface AutoRun {
    String detail() default "This class is auto-registered for execution.";
}

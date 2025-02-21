package org.app.common.pattern.legacy;

/**
 * Fluent APIs - Example
 * <pre>{@code
 * public class Letter extends FluentApi<Letter> {
 *
 *     private String from;
 *     private String to;
 *     private String body;
 *
 *     public Letter() {
 *         super(this);
 *     }
 *
 *     public Letter from(String from) {
 *         this.from = from;
 *         return self;
 *     }
 *
 *     public Letter to(String to) {
 *         this.to = to;
 *         return self;
 *     }
 *
 *     public Letter body(String body) {
 *         this.body = body;
 *         return self;
 *     }
 * }
 * }</pre>
 * */
public abstract class FluentApi<T> {

    protected T self;

    protected FluentApi(T self) {
        this.self = self;
    }

    @SuppressWarnings("unchecked")
    public T getSelf() {
        return (T) this;
    }
}

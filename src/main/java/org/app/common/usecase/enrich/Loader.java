package org.app.common.usecase.enrich;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Loader<P, ID, C> {

    public final Function<List<ID>, List<C>> loader;
    public Function<C, ID> childKey;
    public BiConsumer<P, List<C>> attacher;

    Loader(
        Function<List<ID>, List<C>> loader,
        Function<C, ID> childKey,
        BiConsumer<P, List<C>> attacher
    ) {
        this.loader = loader;
        this.childKey = childKey;
        this.attacher = attacher;
    }

    public static <P, ID, C> Loader<P, ID, C> loader(Function<List<ID>, List<C>> loader) {
        return new Loader<>(loader, null, null);
    }

    public Loader<P, ID, C> group(Function<C, ID> childKey) {
        this.childKey = childKey;
        return this;
    }

    public Loader<P, ID, C> attacher(BiConsumer<P, List<C>> attacher) {
        this.attacher = attacher;
        return this;
    }
}

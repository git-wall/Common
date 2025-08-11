class Pipeline<T> {
    private data: T[] | T;

    constructor(data: T[] | T) {
        this.data = data;
    }

    static from<T>(data: T[] | T): Pipeline<T> {
        return new Pipeline(data);
    }

    where(predicate: (data: T) => boolean): Pipeline<T> {
        if (this.data instanceof Array) {
            const rs = this.data.filter(e => predicate(e));
            return new Pipeline<any>(rs);
        }

        if (predicate(this.data))
            return this
        else
            return new Pipeline<any>(null);
    }

    map<U>(fn: (data: T[] | T) => U): Pipeline<U> {
        if (this.data === null) {
            // If data is null, return a Pipeline with null as data
            return new Pipeline<U>(null as any);
        }
        const result = fn(this.data);
        return new Pipeline<U>(result);
    }

    first(): Pipeline<T> {
        if (this.data instanceof Array) {
            this.data = this.data[0];
        }
        return this;
    }

    get(): T[] | T {
        return this.data;
    }
}

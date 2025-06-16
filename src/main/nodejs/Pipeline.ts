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
        const rs: U[] | U = this.data === null ? null : fn(this.data);
        return new Pipeline(rs);
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

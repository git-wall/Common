class Stream<T> {
    private readonly data: T[];
    private readonly pipeline: ((data: any[]) => any[])[];

    constructor(data: T[] | T, pipeline: ((data: any[]) => any[])[] = []) {
        this.data = Array.isArray(data) ? data : [data];
        this.pipeline = pipeline;
    }

    static from<T>(data: T[] | T): Stream<T> {
        return new Stream<T>(data);
    }

    where(predicate: (item: T) => boolean): Stream<T> {
        return new Stream<T>(this.data, [
            ...this.pipeline,
            (data: T[]) => data.filter(predicate),
        ]);
    }

    map<U>(mapper: (item: T) => U): Stream<U> {
        // @ts-ignore
        return new Stream<U>(this.data, [
            ...this.pipeline,
            (data: T[]) => data.map(mapper),
        ]);
    }

    first(): T | null {
        const result = this.execute();
        return result.length > 0 ? result[0] : null;
    }

    get(): T[] {
        return this.execute();
    }

    private execute(): T[] {
        return this.pipeline.reduce((acc, fn) => fn(acc), this.data);
    }
}

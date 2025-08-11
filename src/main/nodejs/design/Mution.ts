type Constructor<T> = new (...args: any[]) => T;

/**
 * Utility class to mimic the MultionType behavior in TypeScript.
 */
export class MultionType<K extends string, V> {
    // @ts-ignore
  private readonly multion: Map<K, V> = new Map();

    constructor(
        private readonly enumType: Record<K, K>,
        implementations: Constructor<V>[]
    ) {
        const enumKeys = Object.keys(this.enumType) as K[];

        for (const enumKey of enumKeys) {
            const enumKeyLower = enumKey.toLowerCase();

            for (const implClass of implementations) {
                const classNameLower = implClass.name.toLowerCase();

                if (classNameLower.includes(enumKeyLower)) {
                    try {
                        const instance = new implClass();
                        this.multion.set(enumKey, instance);
                        break; // Found a match
                    } catch (e) {
                        // Continue to next implementation if instantiation fails
                        continue;
                    }
                }
            }
        }
    }

    public get(key: K): V | undefined {
        return this.multion.get(key);
    }

    public getMultion(): Readonly<K, V> {
        return this.multion;
    }

    public static of<K extends string, V>(
        enumType: Record<K, K>,
        implementations: Constructor<V>[]
    ): MultionType<K, V> {
        return new MultionType(enumType, implementations);
    }
}

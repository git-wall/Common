public static class ProxyFactory
{
    public static T Create<T>(T target, ILogger logger) where T : class
    {
        var proxy = DispatchProxy.Create<T, LoggingProxy<T>>() as LoggingProxy<T>;
        proxy.Target = target;
        proxy.Logger = logger;
        return proxy as T;
    }
}
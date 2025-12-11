public class LoggingProxy<T> : DispatchProxy
{
    public T Target { get; set; }
    public ILogger Logger { get; set; }

    protected override object Invoke(MethodInfo targetMethod, object[] args)
    {
        // check attribute
        var hasLog = targetMethod.GetCustomAttribute<LogAttribute>() != null;
        if (!hasLog)
            return targetMethod.Invoke(Target, args);

        // get class name
        var className = targetMethod.DeclaringType?.FullName;
        var packageName = targetMethod.DeclaringType?.Namespace;

        // log input
        var input = string.Join(", ", args.Select(a => a?.ToString() ?? "null"));

        LogEntities log = new LogEntities();
        log.Method = targetMethod.Name;
        log.Class = className;
        log.Package = packageName;
        log.Request = input;

        // log execute time
        var stopwatch = Stopwatch.StartNew();

        try
        {
            var result = targetMethod.Invoke(Target, args);

            log.Response = result;
            log.ExecuteTime = stopwatch.ElapsedMilliseconds;
            // log
            Logger.LogInformation("Log: {Log}", log);

            return result;
        }
        catch (TargetInvocationException ex)
        {
            Logger.LogError(ex, "Log: {Log}", log);
            log.ExecuteTime = stopwatch.ElapsedMilliseconds;
            throw ex.InnerException ?? ex;
        }
        finally
        {
            stopwatch.Stop();
        }
    }
}

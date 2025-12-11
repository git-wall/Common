```csharp
#region dùng thử trên service
public interface IService {
    [Log]
    int Add(int a, int b);
}

public class Service : IService {
    public readonly ILogger<Service> Logger;
    public Service() {
        Logger = LoggerFactory.Create(builder => builder.AddConsole()).CreateLogger(Service.GetType().FullName);
    }

    public int Add(int a, int b) {
        return a + b;
    }
}

public class Controller {
    public readonly IService _service;
    public Controller() {
        _service = ProxyFactory.Create(new Service(), LoggerFactory.Create(builder => builder.AddConsole()).CreateLogger(Program.GetType().FullName));
    }

    public int Add(int a, int b) {
        return _service.Add(a, b);
    }
}
```
Thay vi sử dụng reflection để truy cập vào các thành phần của lớp
Chúng ta có thể sử dụng MethodHandle để truy cập nhanh hơn và an toàn hơn. 
MethodHandle là một đối tượng đại diện cho một phương thức hoặc một trường trong Java, 
và nó cung cấp một cách tiếp cận hiệu quả để gọi các phương thức hoặc truy cập các trường mà không cần phải sử dụng reflection.

### MethodHandle in Invoker and MethodAccessorCache
- MethodAccessorCache cung cấp cơ chế lưu cache getter và setter method handles, giúp tăng hiệu suất khi truy cập các trường của lớp

```java
import org.app.core.utils.ClassUtils;

public static void main(String[] args) {
  ClassUtils.set(object, "fieldName", value);
}
```

- Invoker sử dụng MethodHandle để gọi các phương thức một cách nhanh chóng và hiệu quả, thay vì sử dụng reflection truyền thống.

```java
import org.app.core.utils.ClassUtils;
public static void main(String[] args) {
  ClassUtils.invokeMethod(object, "methodName", args);
}
```
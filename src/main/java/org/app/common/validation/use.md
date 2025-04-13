```java
public void valid() {
    var user = new User("user", 24, Sex.FEMALE, "foobar.com");
    Valid.of(user)
            .valid(User::name, Objects::nonNull, "name is null")
            .valid(User::name, name -> !name.isEmpty(), "name is empty")
            .valid(User::email, email -> !email.contains("@"), "email not contains '@'")
            .valid(User::age, age -> age > 20 && age < 30, "age isn't between 20 and 30")
            .get();
}
```
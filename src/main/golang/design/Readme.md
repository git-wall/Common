### pipeline.go
```go
func use() {
	var rs = With("1", "2", "abc", "def").
		Filter(func(item string) bool { return len(item) > 1 }).
		Map(func(item string) any { return len(item) }).
		FindFirst()

	var rs1 = From("1", "2", "abc", "def").
		Filter(func(i interface{}) bool {
			return len(i.(string)) == 1
		}).
		Map(func(i interface{}) interface{} {
			return int(i.(string)[0])
		}).
		Get()

	println(rs)
	println(rs1)
}
```
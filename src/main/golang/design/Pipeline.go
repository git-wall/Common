package design

type Pipeline[T any] struct {
	value []T
}

func With[T any](values ...T) *Pipeline[T] {
	return &Pipeline[T]{value: values}
}

func (p *Pipeline[T]) Filter(f func(T) bool) *Pipeline[T] {
	var result []T
	for _, v := range p.value {
		if f(v) {
			result = append(result, v)
		}
	}
	p.value = result
	return p
}

func (p Pipeline[T]) Map(f func(T) any) *Pipeline[any] { // Changed return type to pointer
	var result []any
	for _, v := range p.value {
		result = append(result, f(v))
	}
	return &Pipeline[any]{value: result} // Return pointer
}

func (p *Pipeline[T]) ToSlice() []T {
	return p.value
}

func (p *Pipeline[T]) ToAny() any {
	return p.value
}

func (p *Pipeline[T]) Get() []T {
	return p.value
}

func (p *Pipeline[T]) FindFirst() *T {
	if len(p.value) == 0 {
		return nil
	}
	return &p.value[0]
}

func (p *Pipeline[T]) FindAny() (T, bool) {
	if len(p.value) == 0 {
		var zero T
		return zero, false
	}
	return p.value[0], true
}

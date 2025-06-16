package design

import "reflect"

type Stream struct {
	value  interface{}
	filter func(interface{}) bool
	mapper func(interface{}) interface{}
}

func From(value ...interface{}) *Stream {
	return &Stream{value: value}
}

func (p *Stream) Where(filter func(interface{}) bool) *Stream {
	p.filter = filter
	return p
}

func (p *Stream) Map(mapper func(interface{}) interface{}) *Stream {
	p.mapper = mapper
	return p
}

func (p *Stream) Get() interface{} {
	val := reflect.ValueOf(p.value)

	if val.Kind() == reflect.Slice {
		var result []interface{}
		for i := 0; i < val.Len(); i++ {
			elem := val.Index(i).Interface()
			if p.filter == nil || p.filter(elem) {
				if p.mapper != nil {
					result = append(result, p.mapper(elem))
				} else {
					result = append(result, elem)
				}
			}
		}
		return result
	}

	if p.filter != nil && !p.filter(p.value) {
		return nil
	}
	if p.mapper != nil {
		return p.mapper(p.value)
	}
	return p.value
}

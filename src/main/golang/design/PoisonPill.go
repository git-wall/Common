package design

import (
	"context"
	"log"
)

type PoisonPill[T comparable] struct {
	Name     string
	Queue    chan T
	Consumer func(T)
	Poison   T
	ctx      context.Context
	cancel   context.CancelFunc
}

func NewPoisonPill[T comparable](name string, poison T, queueSize int, consumer func(T)) *PoisonPill[T] {
	ctx, cancel := context.WithCancel(context.Background())
	return &PoisonPill[T]{
		Name:     name,
		Queue:    make(chan T, queueSize),
		Consumer: consumer,
		Poison:   poison,
		ctx:      ctx,
		cancel:   cancel,
	}
}

func (p *PoisonPill[T]) Offer(item T) {
	p.Queue <- item
}

func (p *PoisonPill[T]) Start() {
	log.Printf("%s ready to run", p.Name)
	go func() {
		defer log.Printf("%s close", p.Name)
		for {
			select {
			case <-p.ctx.Done():
				return
			case item := <-p.Queue:
				if item == p.Poison {
					log.Printf("%s received poison pill, exiting", p.Name)
					p.cancel()
					return
				}
				p.Consumer(item)
			}
		}
	}()
}

func (p *PoisonPill[T]) Shutdown() {
	p.cancel()
}

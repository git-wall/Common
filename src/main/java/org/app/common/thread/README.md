### Threads ≈ CPU Cores × (1 + Wait Time IO/Handling Time)

If you have 4 CPU cores:
- the average wait time for IO is 2 times the handling time
- the wait time for IO usually is above 2 the handling time
- 4 x (1 + 2) = 12 threads
- you can have around 12 threads running concurrently.
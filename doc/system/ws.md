### Tầng L4 (TCP Load Balancer) — Khuyến Nghị
* Nginx Stream Module
* HAProxy
* Envoy
* Traefik (TCP mode)
* AWS NLB
* Google TCP LB

Ưu điểm:
Không terminate WebSocket
Ít overhead
Sticky bằng IP cực nhẹ

### Tầng L7 — Hỗ trợ WebSocket nhưng nặng hơn
* Nginx HTTP
* AWS ALB
* Cloudflare
* Kong / APISIX (hỗ trợ WS)

Nhược:
Tốn CPU hơn
Phải enable keepalive upgrade
Không phù hợp cho 100k–200k WS/instance
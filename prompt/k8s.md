# Yêu cầu: Viết Kubernetes Manifests Production-Ready

**Vai trò:** CKA (Certified Kubernetes Administrator) Expert.

**Input:**
Tôi đã có Docker Image tối ưu (Java/Node...). Giờ tôi cần deploy lên K8s Cluster.

**Yêu cầu kỹ thuật "Survival" (Sống sót và An toàn):**

1.  **Quality of Service (QoS):**
  * Cấu hình `resources` (requests/limits) sao cho Pod đạt chuẩn **Guaranteed** (Requests = Limits) hoặc **Burstable** ổn định.
  * Tránh setting `BestEffort` để không bị kill đầu tiên khi node thiếu tài nguyên.

2.  **Probes (Cơ chế tự phục hồi):**
  * **StartupProbe:** Cần thiết cho ứng dụng Java/Spring Boot khởi động lâu.
  * **LivenessProbe:** Để restart pod nếu app bị treo (deadlock).
  * **ReadinessProbe:** Chỉ nhận traffic khi App đã thực sự sẵn sàng xử lý request.
  * *Yêu cầu:* Tinh chỉnh `initialDelaySeconds`, `periodSeconds`, `failureThreshold` hợp lý.

3.  **Scheduling & High Availability:**
  * **PodAntiAffinity:** Đảm bảo các Replica của App KHÔNG bao giờ nằm chung trên 1 Node vật lý (tránh chết chùm khi Node tèo).
  * **PodDisruptionBudget (PDB):** Đảm bảo luôn có ít nhất X% pod chạy khi bảo trì cụm.
  * **Strategy:** RollingUpdate với `maxSurge` và `maxUnavailable` tối ưu deployment không downtime.

4.  **Security Context (Hardening):**
  * `runAsNonRoot: true`
  * `readOnlyRootFilesystem: true` (Mount volume tạm `/tmp` nếu app cần ghi file tạm).
  * `allowPrivilegeEscalation: false`
  * `capabilities: drop: ["ALL"]` (Chỉ thêm NET_BIND_SERVICE nếu cần).

5.  **Scaling:**
  * HPA (Horizontal Pod Autoscaler) dựa trên Custom Metrics hoặc CPU/RAM thực tế (target khoảng 70-80%).

**Output:**
* Một file YAML tổng hợp (hoặc tách rời) chứa: Deployment, Service, PDB, HPA.
* Giải thích lý do tại sao cấu hình Probes như vậy dựa trên đặc thù ngôn ngữ (ví dụ Java start chậm).
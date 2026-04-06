+------------+        +------------+        +------------+        +------------+        +------------+
|  VM1       |        |  VM2       |        |  VM3       |        |  VM4       |        |  VM5       |
| GitLab CI  |        | Docker     |        | Kubernetes |        | K8s Worker |        | K8s Worker |
| Server     |        | Registry   |        | Admin      |        | Node       |        | Node       |
+------------+        +------------+        +------------+        +------------+        +------------+
      |                     |                    |                     |                    |
      | Push code           |                    |                     |                    |
      |-------------------> |                    |                     |                    |
      | CI/CD pipeline      |                    |                     |                    |
      | triggers Docker     |                    |                     |                    |
      | build & push        |------------------> |                     |                    |
      |-------------------> | Push Image         |                     |                    |
      | to Docker Registry  |------------------> | kubectl apply       |                    |
      | (VM2)               |                    | deployment.yaml     |                    |
      |                     |                    |-------------------->| Pull image from    |
      |                     |                    |                     | Docker registry    |
      |                     |                    |                     | (VM2)              |
      |                     |                    |                     |                    |
      |                     |                    |                     | Run containers     |
      |                     |                    |                     | (Pods)             |
      |                     |                    |                     |                    |
+------------+        +------------+        +------------+        +------------+        +------------+

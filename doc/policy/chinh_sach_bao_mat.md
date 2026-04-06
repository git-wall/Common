```less
[ Client ]
  |
[ Spring Boot ]
(non-root)
  |
[ Docker Sandbox ]
  |
[ Private Network ]
  |
[ PostgreSQL | Oracle ]
```
👉 Không dùng Java Security Manager <br>
👉 Dùng Docker + OS sandbox + DB policy

Mục tiêu bảo mật

| Area      | Mục tiêu                            |
|-----------|-------------------------------------|
| JVM / App | Không bị RCE, hạn chế quyền OS      |
| Docker    | App bị hack **không phá được host** |
| DB        | Không leak credential, không SQLi   |
| Network   | App chỉ nói chuyện với DB           |
| Runtime   | Không đọc file / exec bừa           |

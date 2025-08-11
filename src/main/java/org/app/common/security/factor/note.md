Go [Google Developer Console](https://console.cloud.google.com/project) 
and get the Client ID & Client Secret

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope: profile, email
```
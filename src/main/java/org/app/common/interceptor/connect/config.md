```properties
graylog.host=localhost                     # Graylog server host (e.g., IP or hostname)
graylog.port=12201                         # Graylog server port (default is 12201 for UDP)
graylog.maxChunkSize=508                   # Maximum chunk size for GELF messages (in bytes)
graylog.useCompression=true                # Enable or disable compression for GELF messages
graylog.originHost=localhost               # The origin host name to include in GELF messages
graylog.includeRawMessage=false            # Include raw log message in GELF payload
graylog.includeMarker=true                 # Include markers in GELF payload
graylog.includeMdcData=true                # Include MDC (Mapped Diagnostic Context) data in GELF payload
graylog.includeCallerData=false            # Include caller data (class/method info) in GELF payload
graylog.includeRootCauseData=false         # Include root cause data in GELF payload
graylog.includeLevelName=true              # Include log level name in GELF payload
```
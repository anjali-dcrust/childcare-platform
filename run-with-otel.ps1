$env:JAVA_TOOL_OPTIONS="-javaagent:./opentelemetry-javaagent.jar"
$env:OTEL_SERVICE_NAME="childcare-platform"
$env:OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4318"
$env:OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
$env:OTEL_TRACES_EXPORTER="otlp"
$env:OTEL_METRICS_EXPORTER="none"
$env:OTEL_LOGS_EXPORTER="none"

mvn spring-boot:run

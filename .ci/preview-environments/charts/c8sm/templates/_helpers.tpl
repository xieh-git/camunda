{{ define "commonLabels" -}}
{{- toYaml .Values.global.labels -}}
{{ end }}

{{ define "commonAnnotations" -}}
camunda.cloud/created-by: "https://github.com/camunda/camunda/blob/main/.ci/{{ .Template.Name }}"
{{ end }}

{{- define "ingress.domain" -}}
{{- printf "%s.%s" .Release.Name .Values.ingress.domain | trimPrefix "optimize-" -}}
{{- end -}}

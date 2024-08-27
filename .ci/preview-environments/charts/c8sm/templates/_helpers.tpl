{{ define "commonLabels" -}}
{{- toYaml .Values.global.labels -}}
{{ end }}

{{- define "ingress.domain" -}}
{{- printf "%s.%s" .Release.Name .Values.ingress.domain | trimPrefix "camunda-" -}}
{{- end -}}

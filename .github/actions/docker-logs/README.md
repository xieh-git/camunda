# Docker Logs

An action intended to be used together with the compose composite action but can also be used for manually started docker containers.

The aim is to collect docker logs and upload them as artifacts of the CI build. The action will dump the logs of all running docker containers.

Composite actions don't support pre and post steps yet, see [#1478](https://github.com/actions/runner/issues/1478).

## Usage

### Inputs

|    Input     |            Description             | Required | Default |
|--------------|------------------------------------|----------|---------|
| archive_name | The name provided for the archive. | true     |         |

## Example of using the action

```yaml
steps:
- uses: actions/checkout@v3
- name: Start Elasticsearch
  uses: ./.github/actions/compose
  with:
    compose_file: .github/actions/compose/docker-compose.elasticsearch.yml
    project_name: elasticsearch
  env:
    ELASTIC_VERSION: 8.12.0
    ELASTIC_JVM_MEMORY: 1
...
- name: Docker log dump
    uses: ./.github/actions/docker-logs
    if: always() # always dump the docker logs in case of success and failure
    with:
      archive_name: migration-docker
```


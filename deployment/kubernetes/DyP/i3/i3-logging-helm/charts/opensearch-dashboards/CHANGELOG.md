# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---
## [Unreleased]
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.24.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.17.1
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.23.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.17.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.22.0]
### Added
- Ability to add additional `labels` to `serviceMonitor`
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.21.2]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- Bug `protocol` missing for metrics in `Service`
### Security
---
## [2.21.1]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- Fixed `ServiceMonitor` bug for `port` value
### Security
---
## [2.21.0]
### Added
- Added `ServiceMonitor` support for Prometheus monitoring
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.20.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.16.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.19.1]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- Fixed pod topology spread constraints in Dashboards
### Security
---
## [2.19.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.15.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.18.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.14.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.17.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.13.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.16.1]
### Added
- Support for newer HorizontalPodAutoscaler api in Dashboards
- Ability to configure HorizontalPodAutoscaler to use memory as a metric source type
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.16.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.12.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.15.1]
### Added
- Added support for pod topology spread constraints in Dashboards
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.15.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.11.1
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.14.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.11.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.13.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.10.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.12.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.9.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.11.1]
### Added
- Support string type for extraObjects
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.11.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.8.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.10.0]
### Added
- Updated OpenSearch Dashboards appVersion to 2.7.0
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.9.3]
### Added
- Added custom opensearch and dashboard annotations through values.yaml
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.9.2]
### Added
- Support setting ipFamilyPolicy on Service
- Support setting ipFamilies on Service
---
## [2.9.1]
### Added
### Changed
- Allow user-defined labels on ingress resource
### Deprecated
### Removed
### Fixed
### Security
---
## [2.9.0]
### Added
### Changed
- Updated OpenSearch Dashboards appVersion to 2.6.0
### Deprecated
### Removed
### Fixed
### Security
---
## [2.8.0]
### Added
### Changed
- Updated OpenSearch Dashboards appVersion to 2.5.0
### Deprecated
### Removed
### Fixed
### Security
---
## [2.7.0]
### Added
### Changed
- Updated OpenSearch Dashboards appVersion to 2.4.1
---
## [2.6.1]
### Added
- Added the ability to define plugins on node startup via plugins.enabled option for opensearch-dashboards chart.
- Added the ability to define defaultMode for the opensearch_dashboards.yml file which is mounted as configMap
- Added documentation for new configurations
### Changed
- Updated version to 2.6.1
---
## [2.6.0]
### Added
### Changed
- Updated OpenSearch Dashboards appVersion to 2.4.0
### Deprecated
### Removed
### Fixed
### Security
---
## [2.5.3]
### Added
- Startup, readiness and liveness probes for deployment
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.5.2]
### Added
- Template configmap content by tpl function
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.5.1]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- ingress helm validation error fix by removing additional hyphen
### Security
---
## [2.5.0]
### Added
### Changed
- Updated version to 2.5.0 and appVersion to "2.3.0".
### Deprecated
### Removed
### Fixed
### Security
---
## [2.4.2]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- OpenSearch Dashboards fixed failure when ingress service port is a string (named port)
### Security
---
## [2.4.1]
### Added
- Helm chart-releaser parallel release issue, updated version to 2.4.1.
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.4.0]
### Added
- Updated version to 2.4.0 and appVersion to "2.2.1".
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.3.0]
### Added
- Updated version to 2.3.0 and appVersion to "2.2.0".
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.2.4]
### Added
- Add lifecycle hooks for opensearch-dashboards charts
### Changed
### Deprecated
### Removed
### Fixed
### Security
---
## [2.2.3]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- Opensearch dashboards ingress template to use values from provided config
### Security
---
## [2.2.2]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- Opensearch dashboard fix issue #295.
- Opensearch dashboard config map support both string and map format.
### Security
---
## [2.2.1]
### Added
### Changed
### Deprecated
### Removed
### Fixed
- Opensearch dashboard config map format is restored with backward support of string format
### Security
---
## [2.2.0]
### Added
### Changed
- Updated version to 2.2.0 and appVersion to "2.1.0".
### Deprecated
### Removed
### Fixed
### Security
---
## [2.1.0]
### Added
### Changed
- Updated version to 2.1.0 and appVersion to "2.0.1".
### Deprecated
### Removed
### Fixed
### Security
---
## [2.0.1]
### Added
### Changed
- Updated version to 2.0.1 and appVersion to "2.0.0".
### Deprecated
### Removed
### Fixed
### Security
---
## [2.0.0]
### Added
### Changed
- Updated version to 2.0.0 and appVersion to "2.0.0-rc1".
### Deprecated
### Removed
### Fixed
### Security

[Unreleased]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.24.0...HEAD
[2.24.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.23.0...opensearch-dashboards-2.24.0
[2.23.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.22.0...opensearch-dashboards-2.23.0
[2.22.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.21.1...opensearch-dashboards-2.22.0
[2.21.2]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.21.1...opensearch-dashboards-2.21.2
[2.21.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.21.0...opensearch-dashboards-2.21.1
[2.21.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.20.0...opensearch-dashboards-2.21.0
[2.20.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.19.1...opensearch-dashboards-2.20.0
[2.19.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.19.0...opensearch-dashboards-2.19.1
[2.19.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.18.0...opensearch-dashboards-2.19.0
[2.18.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.17.0...opensearch-dashboards-2.18.0
[2.17.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.16.0...opensearch-dashboards-2.17.0
[2.16.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.15.1...opensearch-dashboards-2.16.0
[2.15.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.15.0...opensearch-dashboards-2.15.1
[2.15.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.14.0...opensearch-dashboards-2.15.0
[2.14.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.13.0...opensearch-dashboards-2.14.0
[2.13.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.12.0...opensearch-dashboards-2.13.0
[2.12.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.11.1...opensearch-dashboards-2.12.0
[2.11.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.11.0...opensearch-dashboards-2.11.1
[2.11.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.10.0...opensearch-dashboards-2.11.0
[2.10.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.9.0...opensearch-dashboards-2.10.0
[2.9.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.8.0...opensearch-dashboards-2.9.0
[2.8.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.7.0...opensearch-dashboards-2.8.0
[2.7.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.6.1...opensearch-dashboards-2.7.0
[2.6.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.6.0...opensearch-dashboards-2.6.1
[2.6.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.5.3...opensearch-dashboards-2.6.0
[2.5.3]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.5.2...opensearch-dashboards-2.5.3
[2.5.2]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.5.1...opensearch-dashboards-2.5.2
[2.5.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.5.0...opensearch-dashboards-2.5.1
[2.5.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.4.1...opensearch-dashboards-2.5.0
[2.4.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.4.0...opensearch-dashboards-2.4.1
[2.4.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.3.0...opensearch-dashboards-2.4.0
[2.3.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.2.4...opensearch-dashboards-2.3.0
[2.2.4]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.2.3...opensearch-dashboards-2.2.4
[2.2.3]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.2.2...opensearch-dashboards-2.2.3
[2.2.2]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.2.1...opensearch-dashboards-2.2.2
[2.2.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.2.0...opensearch-dashboards-2.2.1
[2.2.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.1.0...opensearch-dashboards-2.2.0
[2.1.0]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.0.1...opensearch-dashboards-2.1.0
[2.0.1]: https://github.com/opensearch-project/helm-charts/compare/opensearch-dashboards-2.0.0...opensearch-dashboards-2.0.1

# fido2-demo-server

FIDO2에 대한 공부를 위한 데모 서버

## 프로젝트 구조

FIDO 기능은 변경 이유와 책임을 기준으로 다음과 같이 나눕니다.

```text
src/main/java/tf/demo/fido2/fido/
├── api/          # HTTP 요청 검증과 응답을 담당하는 컨트롤러
├── application/  # 등록·인증 세레모니 유스케이스와 트랜잭션
├── config/       # Relying Party, Clock, 설정 프로퍼티 구성
└── persistence/  # JPA 엔터티·저장소와 WebAuthn CredentialRepository 어댑터
```

요청은 `api`에서 받고, `application`이 등록 또는 인증 흐름을 실행하며, `persistence`가 사용자·자격증명·세레모니 데이터를 저장하고 조회합니다. `config`는 이 구성 요소들을 조립하고 환경별 설정을 제공합니다.

## FIDO API

모든 FIDO API의 기준 경로는 `/api/fido`입니다.

| 목적           | Method | 경로                      | 요청 본문                      |
| -------------- | ------ | ------------------------- | ------------------------------ |
| 등록 옵션 생성 | `POST` | `/registration/options`   | `username`, `displayName`      |
| 등록 응답 검증 | `POST` | `/registration/verify`    | `ceremonyId`, `credentialJson` |
| 인증 옵션 생성 | `POST` | `/authentication/options` | `username`                     |
| 인증 응답 검증 | `POST` | `/authentication/verify`  | `ceremonyId`, `credentialJson` |
| discoverable 인증 옵션 | `POST` | `/authentication/discoverable/options` | 없음 |
| discoverable 인증 검증 | `POST` | `/authentication/discoverable/verify` | `ceremonyId`, `credentialJson` |
| credential 목록 | `GET` | `/users/{username}/credentials` | 없음 |
| credential 삭제 | `DELETE` | `/users/{username}/credentials/{credentialId}` | 없음 |

옵션 생성 API는 `ceremonyId`와 브라우저의 WebAuthn API에 전달할 `publicKey`를 반환합니다. 검증 API에는 옵션 생성 시 받은 `ceremonyId`와 브라우저가 반환한 credential JSON을 전달합니다. 세레모니는 한 번만 사용할 수 있으며, 기본 유효 시간은 5분입니다. 동시에 같은 세레모니를 완료하려는 요청 중 하나만 처리됩니다. 오류 응답은 `code`, `message`, `path`, `correlationId`를 포함하며 내부 예외 정보는 노출하지 않습니다.

## 브라우저 데모

서버를 실행한 뒤 `http://localhost:8080/`을 열면 등록, 사용자명 인증, 사용자명 없는 (discoverable credential) 인증, credential 목록/삭제를 직접 시연할 수 있습니다. WebAuthn은 지원 브라우저의 HTTPS 또는 `localhost` 보안 컨텍스트에서만 동작합니다. 화면은 credential의 원시 응답을 표시하거나 저장하지 않습니다.

credential 관리 API는 인증을 포함하지 않는 데모 편의 기능입니다. 공개 또는 운영 배포에서는 애플리케이션 인증/인가 계층으로 이 경로를 반드시 보호하거나 비활성화해야 합니다.

## 설정

`application.yaml`의 `fido` 설정으로 Relying Party와 세레모니 유효 시간을 지정합니다.

```yaml
fido:
  rp-id: localhost
  rp-name: FIDO2 Demo
  origins:
    - http://localhost:8080
  challenge-ttl: PT5M
  ceremony-retention: PT24H
  ceremony-cleanup-interval: PT1M
  authenticator-attachment: PLATFORM # 생략하면 모든 인증기 허용
  resident-key: PREFERRED
  user-verification: REQUIRED
  attestation-policy: DEMO_UNTRUSTED
```

기본 `demo` 프로필은 H2와 `DEMO_UNTRUSTED` attestation 정책을 사용합니다. `prod` 프로필은 PostgreSQL과 Flyway migration을 사용하고 JPA 스키마 자동 갱신을 비활성화합니다. 다음 환경 변수를 제공하세요: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `FIDO_RP_ID`, `FIDO_ORIGINS`, 선택적으로 `FIDO_RP_NAME`, `FIDO_CORS_ORIGINS`.

운영 환경에서는 실제 도메인에 맞게 `rp-id`와 `origins`를 반드시 변경해야 하며, trusted attestation 정책을 명시적으로 유지해야 합니다. `/actuator/health`와 `/actuator/metrics`는 운영 경계에서 접근을 제한해야 합니다. 애플리케이션의 간단한 IP별 rate limit은 단일 인스턴스 데모 보호용이므로 다중 인스턴스 운영 환경에서는 게이트웨이 또는 분산 rate limit을 사용하세요.

## 코드 포맷

이 프로젝트는 `Spotless`를 사용해 Java와 Gradle 파일의 포맷을 정리합니다.

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
```

## 테스트

```bash
./gradlew test
```

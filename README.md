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

옵션 생성 API는 `ceremonyId`와 브라우저의 WebAuthn API에 전달할 `publicKey`를 반환합니다. 검증 API에는 옵션 생성 시 받은 `ceremonyId`와 브라우저가 반환한 credential JSON을 전달합니다. 세레모니는 한 번만 사용할 수 있으며, 기본 유효 시간은 5분입니다.

## 설정

`application.yaml`의 `fido` 설정으로 Relying Party와 세레모니 유효 시간을 지정합니다.

```yaml
fido:
  rp-id: localhost
  rp-name: FIDO2 Demo
  origins:
    - http://localhost:8080
  challenge-ttl: PT5M
```

운영 환경에서는 실제 도메인에 맞게 `rp-id`와 `origins`를 반드시 변경해야 합니다.

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

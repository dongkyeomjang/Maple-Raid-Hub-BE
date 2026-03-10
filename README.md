# 🍁 메-력소 Backend

**메이플스토리 월드통합 보스 파티 매칭 플랫폼**

> 서버가 달라도 함께 보스를 잡을 수 있도록, 크로스 월드 파티 매칭 서비스

🔗 **https://www.mapleraid.com**

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.2.2 |
| **Language** | Java 21 (LTS) |
| **Build** | Gradle 8.14+ |
| **Database** | MySQL (Production), H2 (Test) |
| **NoSQL** | MongoDB (채팅 메시지 저장) |
| **Cache** | Redis (Lettuce) |
| **Auth** | JWT + OAuth2 (Kakao) |
| **WebSocket** | STOMP + SockJS |
| **ORM** | Spring Data JPA (Hibernate) |
| **Mapping** | MapStruct 1.5.5 |
| **Test** | JUnit 5, TestContainers, Awaitility |

---

## 아키텍처

**Hexagonal Architecture (Ports & Adapters)** 패턴을 적용하여 도메인 로직을 프레임워크로부터 격리합니다.

```
src/main/java/com/maple/
├── character/           # 캐릭터 관리 및 인증
├── chat/                # 1:1 DM 채팅
├── party/               # 파티룸 관리
├── post/                # 모집 게시글
├── user/                # 사용자 계정
├── manner/              # 매너 평가 시스템
├── notification/        # 알림 & Discord 연동
├── security/            # 인증/인가
├── external/            # 외부 API 연동 (Nexon)
└── core/                # 공통 모듈
```

각 도메인 모듈은 다음 구조를 따릅니다:

```
domain-module/
├── domain/              # 순수 도메인 엔티티 (프레임워크 무관)
├── application/
│   ├── port/
│   │   ├── in/          # Input Port (UseCase 인터페이스)
│   │   │   ├── usecase/ # UseCase 정의
│   │   │   ├── input/   # Input DTO
│   │   │   └── output/  # Output DTO
│   │   └── out/         # Output Port (Repository 인터페이스)
│   └── service/         # UseCase 구현체
└── adapter/
    ├── in/
    │   ├── web/         # REST Controller
    │   └── websocket/   # WebSocket Handler
    └── out/             # Repository 구현체, 외부 연동
```

---

## 주요 기능

### 1. 파티 모집 시스템
- 보스 선택 (단일/묶음), 모집 인원(2~6명) 설정
- 지원 → 수락/거절 워크플로우
- 7일 자동 만료, 정원 충족 시 자동 마감
- 상태 흐름: `RECRUITING` → `CLOSED` / `CANCELED` / `EXPIRED`

### 2. 캐릭터 관리 & 인증
- Nexon Open API 연동으로 캐릭터 정보 동기화 (직업, 레벨, 전투력, 장비)
- 심볼 장착 챌린지 기반 소유권 인증
- 인증 상태: `UNVERIFIED_CLAIMED` → `VERIFIED_OWNER`

### 3. 실시간 채팅
- **파티 채팅**: 파티룸 생성 시 자동 개설되는 그룹 채팅
- **1:1 DM**: 모집글 맥락 기반 다이렉트 메시지
- STOMP over WebSocket, MongoDB 메시지 저장
- 읽지 않은 메시지 카운팅

### 4. 매너 온도 시스템
- 파티 완료 후 상호 평가 (태그 기반)
- 기본 36.5°, 범위 0~99
- 노쇼 시 -2.0 자동 차감
- 30일 내 동일 유저 중복 평가 방지

### 5. 파티룸
- 레디 체크 (60초 카운트다운)
- When2Meet 스타일 일정 조율
- 파티장 권한: 강퇴, 레디체크 시작, 완료 처리
- 멤버 soft delete (`leftAt` 타임스탬프)

### 6. Discord 연동
- OAuth2 기반 Discord 계정 연결
- 파티 알림 Discord DM 전송
- 알림 설정 커스터마이징

---

## API 엔드포인트

### 인증 `/api/auth`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/signup` | 회원가입 |
| POST | `/login` | 로그인 (JWT 쿠키 발급) |
| POST | `/logout` | 로그아웃 |
| POST | `/oauth/complete` | OAuth 회원가입 완료 |
| PATCH | `/nickname` | 닉네임 변경 |

### 모집글 `/api/posts`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 모집글 작성 |
| GET | `/` | 모집글 목록 조회 |
| GET | `/{postId}` | 모집글 상세 |
| PATCH | `/{postId}` | 모집글 수정 |
| DELETE | `/{postId}` | 모집글 삭제 |
| POST | `/{postId}/close` | 모집 마감 |
| POST | `/{postId}/applications` | 지원하기 |
| GET | `/{postId}/applications` | 지원자 목록 |
| POST | `/{postId}/applications/{appId}/accept` | 지원 수락 |
| POST | `/{postId}/applications/{appId}/reject` | 지원 거절 |
| DELETE | `/{postId}/applications/{appId}` | 지원 철회 |

### 파티룸 `/api/party-rooms`
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 내 파티룸 목록 |
| GET | `/{roomId}` | 파티룸 상세 |
| POST | `/{roomId}/leave` | 파티 탈퇴 |
| DELETE | `/{roomId}/members/{userId}` | 멤버 강퇴 |
| POST | `/{roomId}/ready-check` | 레디체크 시작 |
| POST | `/{roomId}/ready` | 레디 표시 |
| POST | `/{roomId}/complete` | 파티 완료 |
| GET | `/{roomId}/messages` | 채팅 메시지 조회 |
| PUT | `/{roomId}/availability` | 가능 시간 저장 |
| POST | `/{roomId}/schedule` | 일정 확정 |

### 캐릭터 `/api/characters`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 캐릭터 등록 |
| GET | `/` | 내 캐릭터 목록 |
| GET | `/{characterId}` | 캐릭터 상세 |
| DELETE | `/{characterId}` | 캐릭터 삭제 |
| GET | `/{characterId}/verify` | 인증 챌린지 조회 |
| POST | `/{characterId}/verify` | 인증 제출 |

### DM `/api/dm`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/rooms` | DM 방 생성/조회 |
| GET | `/rooms` | DM 방 목록 |
| GET | `/rooms/{roomId}/messages` | DM 메시지 조회 |
| POST | `/rooms/{roomId}/messages` | DM 전송 |
| POST | `/rooms/{roomId}/read` | 읽음 처리 |

### 매너 평가 `/api/manner`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/evaluations` | 평가 제출 |
| GET | `/evaluations` | 내 평가 내역 |
| GET | `/evaluations/{userId}` | 유저 평가 요약 |

### Discord & 알림
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/discord/link` | Discord 연결 |
| POST | `/api/discord/unlink` | Discord 해제 |
| GET | `/api/notifications/preferences` | 알림 설정 조회 |
| PUT | `/api/notifications/preferences` | 알림 설정 변경 |

---

## WebSocket

| 설정 | 값 |
|------|-----|
| **Endpoint** | `/ws` (SockJS fallback) |
| **Protocol** | STOMP |
| **Heartbeat** | 10초 |
| **메시지 크기 제한** | 64 KB |

### 구독 경로

```
/topic/party/{partyRoomId}       → 파티 채팅 메시지
/topic/dm/{dmRoomId}             → DM 메시지
/user/{userId}/queue/notifications → 개인 알림
/user/{userId}/queue/party-notifications → 파티 알림
```

### 전송 경로

```
/app/chat/{partyRoomId}          → 파티 채팅 메시지 전송
/app/dm/{dmRoomId}               → DM 메시지 전송
```

---

## 인증 & 보안

- **JWT 인증**: Access Token(1시간) + Refresh Token(7일), HttpOnly 쿠키 저장
- **OAuth2**: 카카오 로그인 지원
- **세션**: Stateless (서버 세션 없음)
- **CORS**: 환경별 허용 도메인 설정
- **역할**: `USER` (기본 역할)

---

## 도메인 모델

```
User ──────────── Character (1:N)
  │                    │
  │                    └── VerificationChallenge (1:N)
  │
  ├── Post (1:N) ───── Application (1:N)
  │                         │
  │                         └── PartyRoom (1:1)
  │                               └── PartyMember (1:N)
  │
  ├── DirectMessageRoom ── DirectMessage (1:N)
  │
  └── MannerEvaluation (evaluator/evaluatee)
```

---

## 환경 설정

### 프로필

| 프로필 | DB | 용도 |
|--------|-----|------|
| `local` | MySQL (localhost) | 로컬 개발 |
| `dev` | MySQL (Docker) | 개발 서버 |
| `prod` | MySQL (Docker) | 프로덕션 |
| `test` | H2 (in-memory) | 테스트 |

### 주요 환경변수

```yaml
# Database
spring.datasource.url
spring.datasource.username
spring.datasource.password

# MongoDB
spring.data.mongodb.uri

# Redis
spring.data.redis.host
spring.data.redis.port

# JWT
jwt.secret
jwt.access-token-validity
jwt.refresh-token-validity

# OAuth2 (Kakao)
spring.security.oauth2.client.registration.kakao.client-id
spring.security.oauth2.client.registration.kakao.client-secret

# Nexon API
nexon.openapi.key

# Discord
discord.bot-token
discord.guild-id
```

---

## 실행 방법

### 사전 요구사항
- Java 21+
- MySQL 8.0+
- MongoDB
- Redis

### 로컬 개발

```bash
# 빌드
./gradlew build

# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 프로덕션

```bash
# JAR 빌드
./gradlew build

# 실행
java -jar build/libs/app.jar --spring.profiles.active=prod
```

### Docker

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/app.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar", "--spring.profiles.active=prod"]
```

---

## 프로젝트 통계

| 항목 | 수치 |
|------|------|
| Java 소스 파일 | 416개 |
| 도메인 모듈 | 9개 |
| REST Controller | 18개 |
| UseCase 인터페이스 | 56개 |
| Service 구현체 | 57개 |
| API 엔드포인트 | 50개+ |
| 에러 코드 | 100개+ |

---

## 라이선스

이 프로젝트는 비공개 프로젝트입니다. 무단 복제 및 배포를 금합니다.

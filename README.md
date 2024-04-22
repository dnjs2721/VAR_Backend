<div align="center">

# 💎 VIP And Rendezvous

### 프리미엄 소셜 매칭 플랫폼

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3%20%7C%20CodeDeploy-FF9900?style=flat-square&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)

</div>

<br/>

## 📌 프로젝트 소개

VIP And Rendezvous는 **프리미엄 회원 간 소셜 매칭과 구독 기반 서비스를 제공하는 플랫폼**입니다.  
JWT 인증, OAuth2 소셜 로그인, 실시간 채팅, 결제 연동까지 실무 수준의 백엔드 기능을 end-to-end로 구현했습니다.

> 🏫 구름톤 트레이닝 풀스택 개발자 양성과정 3기 팀 프로젝트 (2023.10 – 2024.04)

<br/>

## 🏗️ 서비스 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        CI/CD 파이프라인                            │
│                                                                 │
│  Developer  →  GitHub  →  GitHub Actions  →  AWS CodeDeploy     │
│  (IntelliJ)    (push)      (빌드 / 테스트)       ↓     ↓           │
│                                             Amazon  Amazon      │
│                                               EC2    S3         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Amazon EC2 (Ubuntu 22.04)                      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Spring Boot 3.2                       │   │
│  │                                                          │   │
│  │   ┌─────────── ┐  ┌───────────┐  ┌───────────────────┐   │   │
│  │   │JWT + OAuth2│  │ WebSocket │  │  Quartz Scheduler │   │   │
│  │   │Spring Sec. │  │  STOMP    │  │  (Write-Back 플러시)│   │   │
│  │   └─────┬──────┘  └─────┬──── ┘  └────────┬──────────┘   │   │
│  │         │               │                 │              │   │
│  │   ┌─────▼───────────────▼─────────────────▼──────────┐   │   │
│  │   │            Spring Data JPA + QueryDSL            │   │   │
│  │   └──────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────┐   ┌────────────┐   ┌────────────┐               │
│  │   MySQL 8  │   │  Redis 7   │   │  Amazon S3 │               │
│  │    (RDB)   │   │Token/Cache │   │ 파일 스토리지 │               │
│  └────────────┘   └────────────┘   └────────────┘               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                     PortOne (결제 연동)
```

### ⚡ Write-Back 채팅 캐싱 전략

실시간 채팅의 DB 부하 문제를 해결하기 위해 Redis를 캐시 계층으로 도입한 아키텍처입니다.

```
[클라이언트]
    │  WebSocket 메시지 발행
    ▼
[ChatWebSocketHandler]
    ├─ ① Redis RPUSH (O(1), 즉시 응답)        → Redis List: chat:messages:{roomId}
    └─ ② STOMP 브로드캐스트 (/sub/chat/room/*)

             ↓ Quartz Scheduler (5분마다)

[ChatFlushJob]
    ├─ ① 활성 채팅방 목록 조회 (Redis Set)
    ├─ ② 방별 메시지 LRANGE → Redis 삭제
    └─ ③ MySQL Bulk INSERT (saveAll)
```

> **결과:** 메시지 발생 시마다 발생하던 디스크 I/O를 5분 주기 벌크 처리로 전환, RDBMS 부하 대폭 감소

<br/>

## 🛠️ 기술 스택

| 분류 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.3, Spring Data JPA, Spring Security |
| **Query** | QueryDSL 5.0, MyBatis |
| **Database** | MySQL 8.0, Redis 7.0 |
| **Auth** | JWT (jjwt 0.11.5), OAuth2 (카카오) |
| **실시간** | WebSocket, STOMP |
| **스케줄러** | Quartz Scheduler (Write-Back 플러시) |
| **결제** | PortOne (iamport-rest-client-java) |
| **인프라** | AWS EC2, S3, CodeDeploy |
| **CI/CD** | GitHub Actions |
| **문서화** | SpringDoc OpenAPI (Swagger) 2.4.0 |

<br/>

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 🔐 인증 / 인가 | JWT Access + Refresh Token, Spring Security 필터 체인 |
| 🌐 소셜 로그인 | OAuth2 카카오 연동 (신규/기존 회원 분기 처리) |
| 💬 실시간 채팅 | WebSocket + STOMP + **Redis Write-Back** 캐싱 전략 |
| 💳 결제 | PortOne API 연동, 서버 사이드 결제 검증 |
| 📁 파일 업로드 | AWS S3 Presigned URL 방식 (서버 부하 최소화) |
| ⏰ 자동화 | Quartz Scheduler — 채팅 플러시 / 구독 만료 처리 |
| 🔍 검색 | QueryDSL 동적 쿼리 + Pageable 커서 페이지네이션 |
| 📚 API 문서 | Swagger UI 자동 생성 |

<br/>

## 🚀 트러블슈팅

<details>
<summary><b>Redis Write-Back Race Condition — Refresh Token 동시 갱신 문제</b></summary>

**문제:** 동시에 여러 요청이 들어올 때 Refresh Token이 중복 발급되어 이전 토큰도 유효한 상태가 됨  
**해결:** Redis `SET NX + TTL` 원자적 연산으로 갱신 잠금 처리, 동시 요청 시 첫 번째 요청만 성공하도록 처리

</details>

<details>
<summary><b>WebSocket 세션 누수 — 비정상 종료 시 메모리 누수</b></summary>

**문제:** 클라이언트가 비정상 종료 시 서버 STOMP 세션이 해제되지 않아 메모리 누수 발생  
**해결:** `SessionDisconnectEvent` 리스너 등록으로 비정상 종료 감지 시 세션 자동 정리

</details>

<details>
<summary><b>QueryDSL N+1 문제 — 채팅 메시지 조회</b></summary>

**문제:** 채팅 메시지 조회 시 연관 엔티티(User)를 별도 쿼리로 N번 조회  
**해결:** `fetchJoin()` 적용으로 단일 쿼리 처리, 응답 시간 대폭 감소

</details>

<details>
<summary><b>AWS CodeDeploy 배포 실패 — IAM 권한 오류</b></summary>

**문제:** GitHub Actions 빌드 성공 후 CodeDeploy 단계에서 권한 오류로 배포 실패  
**해결:** EC2 IAM Role에 S3 접근 및 CodeDeploy 실행 권한 추가, `appspec.yml` 경로 설정 수정

</details>

<br/>

## 📁 프로젝트 구조

```
src/main/java/
└── com/vip/
    ├── auth/           # JWT, OAuth2, Spring Security
    ├── chat/           # WebSocket, Redis Write-Back, Quartz Job
    ├── member/         # 회원 도메인
    ├── payment/        # PortOne 결제 연동
    ├── file/           # AWS S3 업로드
    └── config/         # 전역 설정 (Redis, Quartz, Security, WebSocket)
```

<br/>

## 👤 팀 구성 및 기여

- **백엔드 팀원 4명** | 백엔드 집중 개발
- **본인 담당:**
  - JWT + OAuth2 인증 시스템 전담 설계 및 구현
  - WebSocket + Redis Write-Back 채팅 아키텍처 설계 및 구현
  - CI/CD 파이프라인 구축 (GitHub Actions → CodeDeploy → EC2)
  - QueryDSL 동적 검색 필터링 API 설계

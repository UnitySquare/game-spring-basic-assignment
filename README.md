# Crimson Citadel

Spring Boot + JPA 기반의 Crimson Citadel 게임 서버 프로젝트입니다.

## 실행 방법

### 1. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다.

```env
MYSQL_ROOT_PASSWORD=비밀번호
```

### 2. MySQL 실행

```bash
docker compose up -d
```

### 3. Spring Boot 실행

IntelliJ에서 `GameBasicApplication`을 실행합니다.

## API

| 메서드      | 경로                         | 설명           |
| -------- | -------------------------- | ------------ |
| `POST`   | `/games`                   | 게임과 시작 덱 생성  |
| `GET`    | `/games`                   | 게임 목록 조회     |
| `GET`    | `/games/{gameId}`          | 게임 상세 조회     |
| `PATCH`  | `/games/{gameId}`          | 플레이어 이름 변경   |
| `PUT`    | `/games/{gameId}/progress` | 게임 진행 상황 저장  |
| `DELETE` | `/games/{gameId}`          | 게임 삭제        |
| `GET`    | `/rankings`                | 시즌 클리어 랭킹 조회 |

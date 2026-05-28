<div align="center">

<img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/dragonfruit-market/%EC%9A%A9%EA%B3%BC%EB%A7%88%EC%BC%93_%EC%BA%90%EB%A6%AD%ED%84%B0_icon.png" width="80">

# 용과마켓 (Dragonfruit Market)

### 판매자가 직접 상점·상품을 등록하고 구매자는 AI 개인화 추천으로 원하는 상품을 탐색할 수 있는 커머스 플랫폼

<img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/dragonfruit-market/%EC%9A%A9%EA%B3%BC%EB%A7%88%EC%BC%93_%EB%AA%A9%EC%97%85.png" width="800">

</div>

<br>

## 프로젝트 소개

판매자가 직접 상점·상품을 등록하고 구매자는 AI 개인화 추천으로 원하는 상품을 탐색할 수 있는 커머스 플랫폼입니다.

- 개인 프로젝트로 시작해 프론트 · AI 개발자와 협업하며 완성도 있는 커머스 플랫폼으로 발전
- 전체 리팩토링 및 TossPayments · 소셜 로그인 · AI 상품 추천 기능 추가로 실무 수준의 커머스 백엔드 경험

<br>

## 개발 동기

학교 Spring Boot 수행평가 프로젝트로 시작한 백엔드 단독 프로젝트였습니다.
이후 프론트엔드 · AI 개발자가 합류하면서 실제 서비스에 가까운 형태로 발전시킬 수 있었습니다.

백엔드(본인)도 기존 코드의 오류 · 버그 수정과 전체적인 리팩토링을 진행하며 성능을 향상시키고,
소셜 로그인 · TossPayments 결제 · AI 개인화 상품 추천 기능을 추가로 구현하면서
**완성도 있는 커머스 플랫폼**을 만들고자 노력했습니다.

<br>

## 팀 구성

<div align="center">

| **[@yoonjeonggg](https://github.com/yoonjeonggg)** | **[@alvin081105](https://github.com/alvin081105)** |
| :------: | :------: |
| 백엔드 · 기획 | 프론트엔드 · AI |
| Spring Boot | React TypeScript · FastAPI |

</div>

<br>

## 1. 개발 기간 및 작업 관리

### 개발 기간

- 전체 개발 기간 : 2025.10.27 ~ 2025.12.10
- 리팩토링 · 기능 추가 : 2026.04.14 ~ 2026.05.20

<br>

### 작업 관리

- GitHub Projects와 Issues를 사용하여 진행 상황을 공유했습니다.
- Notion을 활용해 기능 명세 및 트러블슈팅 내용을 기록했습니다.

<br>

## 2. 개발 환경

- **Backend** : Java 21, Spring Boot 3.5.7, MySQL, Redis
- **Frontend** : React TypeScript
- **AI** : FastAPI, Python
- **배포** : AWS EC2, Docker, Nginx, GitHub Actions (CI/CD)
- **DB** : MySQL (Aiven), Redis (Docker)
- **버전 및 이슈관리** : Github, Github Issues
- **협업 툴** : Notion, Github
- **결제** : TossPayments

<br>

## 3. 기술 스택

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/dragonfruit-market/%EC%9A%A9%EA%B3%BC%EB%A7%88%EC%BC%93_%ED%94%84%EB%A0%88%EC%9E%84%EC%9B%8C%ED%81%AC.png" alt="프레임워크" width="800">
</div>

<br>

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/springBoot_icon.png" width="16"> Spring Boot 3.5.7 |
| ORM | Spring Data JPA, QueryDSL |
| Database | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/aiven-icon.png" width="16"> MySQL (Aiven), <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/redis_icon.png" width="16"> Redis |
| Security | Spring Security, JWT, OAuth2 (Kakao, Naver) |
| Storage | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/aws_icon.png" width="16"> AWS S3 |
| Mail | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/ses_icon.png" width="16"> AWS SES |
| Payment | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/toss_icon.png" width="16"> TossPayments |
| Real-time | Spring SSE (Server-Sent Events) |
| Frontend | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/react_icon.png" width="16"> React TypeScript |
| AI | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/fastAPI_icon.png" width="16"> FastAPI, Python |
| Infra | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/AWS_EC2_icon.png" width="16"> AWS EC2, Docker, Nginx |
| CI/CD | GitHub Actions |
| API Docs | Swagger (SpringDoc OpenAPI 3) |
| Collaboration | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/github_icon.png" width="16"> Github, Notion |

<br>

### 기술 선정 이유

#### Spring SSE (Server-Sent Events)
- 주문 접수 · 배송 상태 변경 · 문의 답변 등 **서버→클라이언트 단방향 알림**에 적합한 구조로 WebSocket 대비 구현 복잡도를 낮추면서 실시간 알림을 구현했습니다.

#### Redis
- 이메일 인증 코드의 **TTL(만료 시간) 관리**와 JWT 리프레시 토큰 저장에 활용해 인메모리 기반의 빠른 처리를 구현했습니다.

#### QueryDSL
- 가격순 · 인기순 · 최신순 정렬 및 카테고리 · 성별 **다중 조건 동적 필터링** 구현에 적합해 도입했습니다.

#### TossPayments
- 국내 커머스 환경에 최적화된 결제 SDK로, 결제 준비 · 승인 · 실패 · 취소 플로우를 안정적으로 구현할 수 있었습니다.

> 📎 [기술 스택을 선정한 이유](https://www.notion.so/36d0aa80477f8026a868d9d1043cee79?pvs=21)

<br>

## 4. ☁️ 서비스 아키텍처

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/dragonfruit-market/%EC%9A%A9%EA%B3%BC%EB%A7%88%EC%BC%93_%EC%84%9C%EB%B9%84%EC%8A%A4%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98.png" alt="서비스 아키텍처" width="800">
</div>

<br>

## 5. 🗄 ERD

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/dragonfruit-market/%EC%9A%A9%EA%B3%BC%EB%A7%88%EC%BC%93_ERD.png" alt="ERD" width="800">
</div>

<br>

## 6. 📁 프로젝트 구조

```
src/main/java/.../
├── controller/       # REST API 엔드포인트
├── service/          # 비즈니스 로직
├── repository/       # 데이터 접근 계층 (JPA, QueryDSL)
│   └── custom/       # QueryDSL 동적 쿼리
├── domain/           # JPA 엔티티
│   └── enums/        # 상태 열거형
├── dto/              # 요청/응답 DTO
├── auth/             # OAuth2, JWT
├── global/
│   ├── config/       # Security, Redis, S3 설정
│   └── common/       # ApiResponse, 공통 예외
└── exception/        # CustomException, GlobalExceptionHandler
```

<br>

## 7. 역할 분담

### 최윤정 · 백엔드 · 기획

**주문 · 결제 시스템 구현** `TossPayments` `주문 처리`
- TossPayments API 연동을 통한 결제 준비 · 승인 · 실패 · 취소 플로우 구현
- 결제 완료 시 재고 차감 · 주문 상태 관리 자동 처리
- 장바구니 · 주문 생성 · 주문 내역 조회 기능 구현

**실시간 알림 시스템 구현** `Spring SSE` `실시간 알림`
- SSE(Server-Sent Events) 기반 실시간 알림 기능 구현
- 주문 상태 변경 · 문의 답변 등 주요 이벤트 발생 시 사용자에게 실시간 전달
- WebSocket 대비 단방향 알림 구조에 적합하다고 판단하여 SSE 선택

**AI 개인화 상품 추천 연동** `AI 연동` `행동 로그` `QueryDSL`
- 상품 상세 조회 시 로그인 유저의 VIEW 행동 로그를 자동 저장하여 AI 추천 데이터로 활용
- 로그인 유저는 행동 로그 기반 AI 개인화 추천, 비로그인 유저는 인기순으로 분기 처리
- AI 서버에 상품 컨텍스트(상품명 · 가격 · 카테고리 · 이미지 등) 제공을 위한 전용 API 구현
- QueryDSL 동적 쿼리로 가격순 · 인기순 · 최신순 정렬 및 카테고리 · 성별 다중 조건 필터링 구현

<br>

## 8. ✨ 주요 기능

### 🔐 인증 · 회원
- JWT 기반 로그인 및 리프레시 토큰 자동 로그인
- 카카오 · 네이버 소셜 로그인 (OAuth2)
- AWS SES + Redis를 이용한 이메일 인증
- 회원가입, 프로필 조회 · 수정, 비밀번호 변경

### 🏪 판매자
- 판매자 등록 · 수정 · 삭제
- 판매자 팔로우 · 좋아요
- 판매자별 상품 목록 조회

### 📦 상품
- 상품 등록 · 수정 · 삭제
- 전체 조회 (가격순 · 인기순 · 최신순 정렬, 카테고리 · 성별 필터)
- 키워드 검색, 좋아요 · 찜
- AI 개인화 상품 추천 (로그인 유저 행동 로그 기반 · 비로그인 유저 인기순)

### 🛒 장바구니
- 상품 담기 · 수량 변경 · 삭제 · 전체 조회

### 💳 주문 · 결제
- 주문 생성 (재고 자동 차감, 판매자 알림 발송)
- 주문 목록 · 상세 조회, 주문 취소 (재고 복구)
- 배송 상태 변경 (판매자 전용, 구매자 알림 발송)
- TossPayments 결제 준비 · 승인 · 실패 · 취소

### ⭐ 리뷰
- 리뷰 작성 · 삭제, 상품별 리뷰 목록 조회, 리뷰 좋아요

### 🎟 쿠폰
- 쿠폰 생성 (관리자), 쿠폰 등록 · 내 쿠폰 조회
- 정률 · 정액 할인 타입 지원

### 💬 문의
- 문의 등록 · 목록 조회
- 판매자/관리자 답변 등록 (답변 시 문의자 알림 발송)

### 🔔 실시간 알림
- SSE(Server-Sent Events) 기반 실시간 알림
- 주문 접수 · 배송 상태 변경 · 문의 답변 이벤트 알림

<br>

## 9. ▶️ 시연 영상

### 판매자 — 로그인 · 상점 등록 · 상품 등록

<div align="center">
  <a href="https://youtu.be/Sh8F6ReK2to">
    <img src="https://img.youtube.com/vi/Sh8F6ReK2to/maxresdefault.jpg" alt="판매자 시연 영상" width="800">
  </a>
</div>

<br>

### 구매자 — 상품 탐색 · AI 문의 기능

<div align="center">
  <a href="https://youtu.be/3Ul7tTYIiEc">
    <img src="https://img.youtube.com/vi/3Ul7tTYIiEc/maxresdefault.jpg" alt="구매자 시연 영상" width="800">
  </a>
</div>

<br>

### 결제 — 토스페이먼츠 결제

<div align="center">
  <a href="https://youtu.be/pdt-F6A791U">
    <img src="https://img.youtube.com/vi/pdt-F6A791U/maxresdefault.jpg" alt="결제 시연 영상" width="800">
  </a>
</div>

<br>

## 10. 🚨 성능 개선 및 트러블슈팅

### ⚡ JPA N+1 문제 해결

**문제 원인**
- 상품 목록 조회 API에서 JPA 지연 로딩(Lazy Loading) 상태로 연관 엔티티에 반복 접근 시 상품 개수만큼 추가 쿼리가 실행되어 성능 저하 발생

**해결 과정**
- JPQL Fetch Join 적용 → 연관 엔티티를 한 번의 쿼리로 함께 조회하도록 개선

**배운 점**
- JPA 연관관계 조회 시 발생할 수 있는 N+1 문제와 해결 방법을 이해하게 됨

> 📎 [관련 글 보러가기](https://www.notion.so/JPA-N-1-36d0aa80477f80478e89cb82290092c3?pvs=21)

<br>

### <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/ses_icon.png" width="20"> AWS SES 샌드박스 제한 및 이메일 신뢰도 문제 해결

**문제 원인**
- AWS SES 샌드박스 환경에서는 자격 증명에 등록된 이메일로만 발송 가능해 실제 사용자에게 인증 메일 발송 불가
- 발신자 주소 도메인 불일치 및 SPF 레코드 미설정으로 Gmail에서 경고 문구 표시

**해결 과정**
- Route 53 도메인 DNS 레코드에 SPF 레코드 추가 → Gmail 경고 문구 제거
- 도메인 기반 자격 증명 생성 및 Easy DKIM · DKIM 서명 활성화로 이메일 위조 · 변조 방지
- AWS SES 프로덕션 액세스 요청 제출 → 승인 완료 → 모든 이메일 주소로 발송 가능

**배운 점**
- SPF · DKIM 등 이메일 인증 프로토콜과 이메일 신뢰도 · 보안 설정까지 고려해야 함을 체감

> 📎 [관련 글 보러가기](https://www.notion.so/AWS-SES-36d0aa80477f80df8920c62f933f5977?pvs=21)

<br>

### 🔄 JPA 양방향 연관관계 직렬화 무한 루프 문제 해결

**문제 원인**
- Product · Seller 엔티티 양방향 연관관계에서 엔티티를 DTO 없이 직접 반환하면서 Jackson 직렬화 과정에서 순환 참조(Circular Reference) 발생
- API 응답 JSON이 수십 단계 이상 중첩되며 응답 데이터가 비정상적으로 커지고 성능 저하 발생

**해결 과정**
- `@JsonIgnore` · `@JsonManagedReference / @JsonBackReference` 검토했으나 엔티티에 직렬화 로직이 결합되어 유지보수성이 떨어진다고 판단
- DTO 기반 응답 구조로 리팩터링 → 필요한 데이터만 DTO로 변환하여 반환

**배운 점**
- 도메인 모델과 API 응답 모델 분리로 유지보수성 · 확장성 향상
- 양방향 연관관계 엔티티를 직접 반환할 경우 직렬화 문제가 발생할 수 있음을 이해

> 📎 [관련 글 보러가기](https://www.notion.so/JPA-36d0aa80477f80d883c1d16067619075?pvs=21)

<br>

## 11. 🚀 Getting Started

```bash
# 설정 파일 복사
cp src/main/resources/example/application.yml.example src/main/resources/application.yml
cp src/main/resources/example/application-local.yml.example src/main/resources/application-local.yml

# 빌드
./gradlew clean build -x test

# 실행
java -jar build/libs/*.jar
```

- **Swagger UI:** `http://localhost:8282/api/swagger-ui.html`

<br>

### 환경 변수 설정

| 항목 | 설명 |
|------|------|
| `spring.datasource.password` | 로컬 MySQL 비밀번호 |
| `spring.jwt.secret` | 32자 이상 JWT 시크릿 키 |
| `aws.access-key` | AWS IAM Access Key |
| `aws.secret-key` | AWS IAM Secret Key |
| `aws.s3.bucket` | S3 버킷 이름 |
| `aws.send-mail-from` | SES 발신 이메일 |
| `NAVER_CLIENT_ID / NAVER_CLIENT_SECRET` | 네이버 OAuth 앱 키 |
| `KAKAO_CLIENT_ID / KAKAO_CLIENT_SECRET` | 카카오 OAuth 앱 키 |
| `APP_BASE_URL` | OAuth 리다이렉트 베이스 URL |

<br>

## 12. 🧪 테스트

서비스 레이어 단위 테스트 **125개** 작성 (Mockito 기반)

```bash
./gradlew test
```

대상: `AuthService` `CartService` `CouponService` `OrderService` `PaymentService` `ProductService` `ReviewService` `InquiryService` `SellerService` `UserService` `AdminService`

<br>

## 13. 🔁 CI/CD

`main` 브랜치에 push 시 자동 배포

```
GitHub Actions → Gradle 빌드 → Docker 이미지 빌드 → Docker Hub 푸시 → EC2 컨테이너 재시작
```

<br>

---

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/dragonfruit-market/%EC%9A%A9%EA%B3%BC.png" width="60">
  <br>
  <sub>2025 개인 프로젝트 · 용과마켓</sub>
</div>

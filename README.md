# samnammae-BE

## 🚀 Project Overview
삼남매(samnammae) 프로젝트의 백엔드 저장소입니다.
사용자 맞춤형 서비스를 제공하기 위한 RESTful API 서버 구축을 목표로 합니다.

## 🛠 Tech Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: MySQL 8.0, Redis
- **Infrastructure**: AWS EC2, S3
- **CI/CD**: GitHub Actions

## 📋 API Specification (Test Point)
| 기능 | 엔드포인트 | HTTP 메서드 | 상태 |
| :--- | :--- | :---: | :--- |
| 사용자 인증 | `/api/v1/auth/login` | `POST` | ✅ 완료 |
| 상품 목록 조회 | `/api/v1/products` | `GET` | 🏗 진행 중 |
| 장바구니 담기 | `/api/v1/cart` | `POST` | ❌ 대기 |

## ⚙️ Core Logic
1. **Security**: JWT 기반의 Stateless 인증 시스템 체택.
2. **Architecture**: 레이어드 아키텍처(Controller-Service-Repository) 적용.
3. **Validation**: @Valid 어노테이션을 이용한 요청 데이터 검증.
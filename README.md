# Auth Backend (회원/인증 템플릿)

`KAU-CHUNG-WOON-OT/festival_backend`에서 **회원/인증 + 공통 기능**만 떼어낸 Spring Boot 백엔드입니다.
배포 대상은 **AWS EC2 (Docker Compose + Nginx + Let's Encrypt)** 입니다.

- Spring Boot 3.3 / Java 21 / MySQL / JPA
- JWT(Access + Refresh, Refresh는 HttpOnly 쿠키 또는 `Refresh-Token` 헤더)
- 학번+비밀번호 회원가입/로그인, 카카오 소셜 로그인
- Swagger(Basic 인증 보호), 헬스체크, 공통 응답/예외 처리, CORS

## API

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/auth/signin` | 회원가입 (학번 기반) + 토큰 발급 | - |
| POST | `/auth/login` | 로그인 + 토큰 발급 | - |
| POST | `/auth/refresh` | 토큰 재발급 | - |
| POST | `/auth/logout` | 로그아웃 (refresh token 삭제) | ✅ |
| GET | `/oauth2/authorization/kakao` | 카카오 로그인 시작 | - |
| GET | `/api/users/me` | 내 정보 조회 | ✅ |
| POST | `/api/users/me/profile` | 카카오 로그인 후 추가 정보(학번/학과/이메일) 등록 | ✅ |
| GET | `/api/v1/health` | 헬스체크 | - |
| GET | `/swagger-ui.html` | API 문서 (Swagger 계정 필요) | Basic |

카카오 로그인 성공 시 `OAUTH2_FRONT_REDIRECT_URI` 기준으로 프론트의 `/info`(추가 정보 미등록) 또는 `/home`으로 리다이렉트됩니다.

## 프로젝트 구조

```
src/main/java/.../
├── config/        Cors, JpaAuditing, Swagger
├── controller/    Health, auth/, user/
├── domain/        auth(RefreshToken), user(User, Role, SocialType, StudentType), common
├── dto/           auth, oauth(Kakao), user
├── exception/     ErrorCode, GlobalExceptionHandler ...
├── repository/    auth, user
├── response/      ApiResponse, ErrorResponse
├── security/      SecurityConfig, jwt/, oauth/, auth/
└── service/       auth, user
deploy/
├── docker-compose.yml          EC2에서 실행 (app + nginx)
└── nginx/default.conf.template HTTPS 리버스 프록시
.github/workflows/deploy.yml    main 푸시 → GHCR → EC2 배포
.env.example                    EC2 서버 환경변수 예시
```

## 로컬 실행

```bash
export DB_USERNAME=root DB_PASSWORD=... JWT_SECRET=$(openssl rand -base64 48) \
       KAKAO_CLIENT_ID=... KAKAO_CLIENT_SECRET=... \
       SWAGGER_USERNAME=admin SWAGGER_PASSWORD=... JPA_DDL_AUTO=update
./gradlew bootRun
```

---

## EC2 배포

### 배포 흐름

```
main 푸시 → GitHub Actions
  1) Docker 이미지 빌드 → GHCR(ghcr.io) 푸시
  2) deploy/ 파일을 EC2 ~/app 으로 복사 (scp)
  3) EC2에서 docker compose pull && up -d → 헬스체크
```

### 0. Terraform으로 인프라 생성 (권장)

`terraform/` 이 아래 1번의 EC2 준비를 대신합니다.

- VPC (퍼블릭 서브넷 2, 프라이빗 서브넷 2, NAT 없음)
- EC2 Ubuntu 24.04 `t3.small`, 탄력적 IP, 보안 그룹(22 = 지정 IP, 80/443 = 전체)
- user_data로 Docker/Compose, 스왑 2GB, certbot, `~/app` 폴더 자동 준비
- RDS MySQL 8.0 `db.t3.micro` (프라이빗, EC2 보안 그룹에서만 3306 허용), 비밀번호 자동 생성
- SSM Session Manager 접속용 IAM 역할, (선택) Route53 A 레코드

```bash
cd terraform
ssh-keygen -t ed25519 -f ~/.ssh/auth-backend      # 키가 없으면
cp terraform.tfvars.example terraform.tfvars       # 값 채우기
terraform init
terraform plan
terraform apply

terraform output                    # ec2_public_ip, db_url, db_username ...
terraform output -raw db_password   # .env 의 DB_PASSWORD
```

- `ec2_public_ip` → GitHub Secret `EC2_HOST`, 도메인 A 레코드
- `db_url` / `db_username` / `db_password` → EC2의 `~/app/.env`
- `~/.ssh/auth-backend` 개인키 내용 → GitHub Secret `EC2_SSH_KEY`
- state 파일에 DB 비밀번호가 평문으로 들어가므로 state/tfvars는 커밋하지 마세요 (`.gitignore` 처리됨).
- apply 후 user_data 완료까지 2~3분 걸립니다 (`sudo tail -f /var/log/user-data.log`).

그다음은 1번의 `.env` 작성부터 진행하면 됩니다.

### 1. EC2 준비 (최초 1회, Terraform을 쓰지 않을 때)

- 인스턴스: Ubuntu 24.04, t3.small 이상 권장 (t3.micro는 메모리 부족 가능)
- 보안 그룹 인바운드: `22`(본인 IP), `80`, `443`
- 탄력적 IP 할당 후 도메인 A 레코드(예: `api.example.com`)를 연결
- DB: RDS MySQL 권장. RDS 보안 그룹에서 EC2 보안 그룹의 3306 접근 허용

```bash
# Docker 설치
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER && newgrp docker

# (t3.micro/small이면) 스왑 2GB 추가
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 작업 폴더와 환경변수
mkdir -p ~/app && cd ~/app
nano .env        # .env.example 내용을 붙여넣고 값 채우기
chmod 600 .env
```

### 2. HTTPS 인증서 발급 (최초 1회)

Refresh 토큰 쿠키가 `Secure` 이므로 **HTTPS가 반드시 필요**합니다.

```bash
sudo apt-get update && sudo apt-get install -y certbot
sudo mkdir -p /var/www/certbot
# 80 포트가 비어 있을 때(nginx 실행 전) 발급
sudo certbot certonly --standalone -d api.example.com --agree-tos -m you@example.com -n
```

자동 갱신 (nginx 실행 중에는 webroot 방식으로 갱신):

```bash
sudo crontab -e
# 아래 한 줄 추가
0 4 * * * certbot renew --webroot -w /var/www/certbot --quiet && docker exec nginx nginx -s reload
```

> 갱신 명령에 `--webroot`를 주면 nginx가 떠 있는 상태에서도 80 포트 충돌 없이 갱신됩니다.
> 미리 확인: `sudo certbot renew --webroot -w /var/www/certbot --dry-run`

### 3. GitHub Secrets 등록

저장소 → Settings → Secrets and variables → Actions

| 이름 | 값 |
|---|---|
| `EC2_HOST` | EC2 탄력적 IP 또는 도메인 |
| `EC2_USER` | `ubuntu` (Amazon Linux면 `ec2-user`) |
| `EC2_SSH_KEY` | EC2 SSH 개인키 전체 내용 (Terraform 사용 시 `pbcopy < ~/.ssh/auth-backend`) |

> 이미지는 GHCR(`ghcr.io/<owner>/<repo>`, 소문자)에 올라갑니다. 워크플로우의 `GITHUB_TOKEN`으로 푸시하고,
> 배포 때 EC2에서도 같은 토큰으로 잠깐 로그인해 pull 하므로 별도 레지스트리 계정/토큰이 필요 없습니다.
> EC2의 `~/app/.env` 에 `APP_IMAGE=ghcr.io/<owner>/<repo>` 를 넣어 두세요 (수동 `docker compose` 실행용).

### 4. 배포

`main` 브랜치에 푸시하거나 Actions 탭에서 **Deploy to EC2 → Run workflow** 를 누르면 됩니다.

- 첫 배포는 `.env`의 `JPA_DDL_AUTO=update`로 테이블을 생성하고, 이후 `validate`로 바꾼 뒤 `docker compose up -d` 하세요.
- 로그 확인: `cd ~/app && docker compose logs -f app`

### 5. 카카오 개발자 콘솔 설정

- Redirect URI: `https://api.example.com/login/oauth2/code/kakao`
- 플랫폼 > Web 사이트 도메인에 프론트/백엔드 도메인 등록

## 원본 대비 변경점

- 주점/메뉴/푸드트럭/동아리/투표 기능과 관련 ErrorCode, 보안 예외 경로 제거
- 배포: ECS(ECR + Task Definition) → EC2(GHCR + SSH + Docker Compose + Nginx)
- 설정 파일의 비밀값 기본값(DB 비밀번호, 카카오 시크릿, Swagger 비밀번호) 제거 → 전부 환경변수
- CORS 허용 도메인을 `*`에서 `CORS_ALLOWED_ORIGINS` 환경변수로 변경 (쿠키 사용 시 와일드카드는 위험)
- 원본 저장소에서 누락돼 있던 `gradle-wrapper.jar` 복구, `.gitignore`에 `build/`, `.gradle/`, `.idea/`, `.env` 추가

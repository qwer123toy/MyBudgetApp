#  MyBudgetApp - 가계부 앱

##  앱 소개
<div align="center">
  <img src="/screenshot/01.jpg" style="width : 400px; height : 900px;">
  <img src="/screenshot/02.jpg" style="width : 400px; height : 900px;">
  <br>
    <img src="/screenshot/03.jpg" style="width : 400px; height : 900px;">
      <img src="/screenshot/04.jpg" style="width : 400px; height : 900px;">
</div>

MyBudgetApp은 **귀여운 디자인**과 **기본적인 가계부 기능**을 결합한 Android 가계부 앱입니다. <br>
이모지와 파스텔 색상으로 디자인되어 사용하기 즐겁고, 다양한 통계와 필터 기능으로 가계 관리가 편리합니다.

##  주요 기능

###  거래 관리
- **수입/지출 기록** : 날짜, 항목명, 금액, 카테고리 등 상세 정보 입력
- **고정지출/필수항목 분류** : 월세, 생활비 등 정기적인 지출 관리
- **메모 기능** : 거래에 대한 추가 정보 기록
- **터치로 수정** : 간편한 터치로 거래 정보 수정
- **안전한 삭제** : 확인 다이얼로그로 실수 방지

###  고급 필터링
- **월별 조회** : 특정 월의 거래만 확인
- **카테고리별 분류** : 식비, 교통비, 쇼핑 등 카테고리별 검색
- **타입별 구분** : 수입과 지출 구분하여 조회
- **고정지출 필터** : 고정지출만 따로 확인
- **필수항목 필터** : 필수 지출과 선택 지출 구분

###  시각적 통계
- **파이차트** : 카테고리별 지출 비율을 한눈에
- **월별 통계** : 과거 모든 월의 가계부 기록 조회
- **수입 대비 지출 비율** : 건전한 가계 관리 지원
- **전월 대비 변화율** : 지출 패턴 분석
- **고정지출/변동지출 구분** : 상세한 지출 분석

###  예산 관리
- **월별 예산 설정** : 목표 예산 설정으로 계획적인 소비
- **남은 예산 표시** : 실시간 예산 잔액 확인
- **자동 적용 기능** : 다음 달 예산 자동 설정
- **예산 초과 경고** : 색상으로 예산 상태 표시

##  디자인 특징

- **귀여운 이모지** : 모든 UI 요소에 직관적인 이모지 사용
- **파스텔 색상** : 핑크, 오렌지, 크림 색상의 부드러운 조합
- **카드 UI** : 깔끔한 카드 기반 레이아웃
- **둥근 모서리** : 16dp 라운드로 부드러운 느낌
- **그라데이션 버튼** : 시각적으로 아름다운 버튼 디자인

## 📱 화면 구성

### 1.  거래추가
- 날짜 선택 (DatePicker)
- 수입/지출 타입 선택
- 항목명 및 금액 입력
- 카테고리 선택 (12가지 카테고리)
- 고정지출/필수항목 설정
- 메모 입력

### 2.  내역
- 최신순 거래 목록
- 고급 필터링 옵션
- 터치로 수정, 길게 눌러 삭제
- 고정지출/필수항목 태그 표시
- 빈 상태 안내

### 3.  통계
- 월별 선택 스피너
- 카테고리별 지출 파이차트
- 수입/지출/잔액 요약
- 고정지출/불필요지출 분석
- 예산 관리 현황
- 수입 대비 지출 비율
- 전월 대비 변화율

### 4.  설정
- 예산 설정 및 관리
- 자동 적용 설정
- 앱 정보 및 버전
- 사용법 안내

##  기술 스택

- **Language** : Kotlin
- **Platform** : Android (API 21+)
- **Database** : SQLite
- **Chart** : MPAndroidChart
- **Architecture** : Fragment-based Navigation
- **Storage** : SharedPreferences (예산 관리)
- **Build** : Gradle

##  설치 방법

**APK 다운로드** : 곧 플레이스토어 출시 예정

##  빌드 방법

```bash
# 프로젝트 클론
git clone https ://github.com/qwer123toy/MyBudgetApp.git
cd MyBudgetApp

# 디버그 빌드
./gradlew assembleDebug

# 릴리즈 빌드
./gradlew assembleRelease
```

## 사용법

### 첫 거래 등록
1. **거래추가** 탭 선택
2. 날짜를 터치하여 날짜 선택
3. 수입 또는 지출 선택
4. 항목명과 금액 입력
5. 적절한 카테고리 선택
6. 필요시 고정지출/필수항목 설정
7. ** 저장** 버튼 터치

### 거래 수정/삭제
- **수정** : 거래 항목을 터치
- **삭제** : 거래 항목을 길게 누르기

### 통계 확인
1. **통계** 탭 선택
2. 상단에서 조회할 월 선택
3. 파이차트와 상세 통계 확인

### 예산 설정
1. **설정** 탭 선택
2. ** 예산 설정하기** 버튼 터치
3. 월 예산 금액 입력
4. 자동 적용 여부 설정

##  카테고리

-  식비
-  교통비
-  쇼핑
-  의료/건강
-  주거/통신
-  교육
-  문화/여가
-  경조사/회비
-  급여
-  부업
-  용돈/기타
-  기타

##  데이터 관리

- **로컬 저장** : 모든 데이터는 기기에 안전하게 저장
- **자동 백업** : 앱 데이터는 Android 시스템 백업에 포함
- **데이터 보안** : 외부 접근이 불가능한 앱 전용 저장소 사용

##  향후 계획

-  데이터 내보내기/가져오기
-  위젯 지원
-  더 다양한 차트와 통계
-  예산 알림 기능
-  테마 선택 기능

##  라이선스

이 프로젝트는 개인 학습 목적으로 제작되었습니다.

##  개발자

**이지맨**
- GitHub : [qwer123toy](https://github.com/qwer123toy)

---

**💝 MyBudgetApp으로 더 쉽고 즐거운 가계 관리를 시작해보세요!** 

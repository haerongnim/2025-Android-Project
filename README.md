# 📱 Spendy - 영수증 기반 가계부 Android 앱

### 소개
**Spendy**는 영수증을 촬영하거나 이미지를 업로드하면 **OCR(문자인식) 및 AI 분류(TensorFlow Lite, KcELECTRA)** 를 통해 자동으로 거래 내역을 추출하고, 카테고리별 통계, 지도 기반 위치, 친구와의 정산 등 다양한 기능을 제공하는 스마트 가계부 앱입니다.

---

### 📄 프로젝트 상세 보고서 (PDF)
> **아래 배지나 이미지를 클릭**하면 상세 PDF 문서를 확인하실 수 있습니다.

[![View PDF](https://img.shields.io/badge/View-Project_PDF-red?style=for-the-badge&logo=adobeacrobatreader)](https://github.com/haerongnim/2025-Android-Project/SPENDY_final.pdf)

[<img src="./pdf_preview.png" width="80%" alt="PDF 미리보기">](https://github.com/haerongnim/2025-Android-Project/SPENDY_final.pdf)

---

### 주요 기능
- **영수증 OCR**: CLOVA OCR API, Tesseract 등 활용
- **AI 카테고리 분류**: TFLite + KcELECTRA 모델로 품목 자동 분류
- **지도 연동**: 네이버 지도 SDK, 카카오 Geocoding API로 위치 표시
- **거래 내역 관리**: Firestore 연동, 거래 추가/삭제/상세보기
- **통계/차트**: 카테고리별, 월별 소비 통계 시각화
- **친구 기능**: 친구 추가, 채팅, 정산 등

---

### 📂 폴더 구조
```text
app/
 ├── build.gradle.kts
 ├── google-services.json
 ├── proguard-rules.pro
 └── src/
     └── main/
           ├── AndroidManifest.xml
           ├── assets/
           │    ├── kcelectra_receipt_model_int8.tflite  # TFLite 모델
           │    ├── tokenizer.json                      # 토크나이저 설정
           │    └── tessdata/                           # Tesseract 한글 데이터
           ├── java/com/example/spendy_2/
           │    └── ui/
           │         ├── home/        # 홈(영수증, 거래, AI)
           │         ├── friends/     # 친구/채팅/정산
           │         ├── statistics/  # 통계/차트
           │         └── map/          # 지도
           └── res/
                ├── layout/           # 화면 레이아웃 XML
                ├── values/           # strings, colors, themes
                └── drawable/         # 아이콘, 배경 등
```
___
### 주요 라이브러리 및 API
- TensorFlow Lite: org.tensorflow:tensorflow-lite, -support
- 네이버 지도 SDK: implementation(libs.naver.map)
- 카카오 Geocoding API: OkHttp로 REST 호출
- Firebase: Firestore, Auth, Analytics, Storage
- CLOVA OCR: REST API 연동
- Glide: 이미지 로딩
- MPAndroidChart: 통계 차트
- CameraX: 카메라 기능
- Retrofit/OkHttp: 네트워크 통신
___
### 빌드 및 실행 방법
1. Firebase 프로젝트 등록
  - google-services.json 파일 필요
2. API 키 등록
  - 네이버 지도, 카카오 REST API 키 필요
  - (AndroidManifest.xml 및 코드 내 삽입)
3. 필수 의존성 설치
  - Android Studio에서 Gradle Sync
4. 앱 실행
  - Android Studio에서 실행(에뮬레이터/실기기)
___
### 참고
- TFLite 모델/토크나이저 파일은 assets/에 위치
- OCR/지도/AI 기능은 네트워크 연결 필요
- 자세한 기능별 코드는 app/src/main/java/com/example/spendy_2/ui/ 하위 참고

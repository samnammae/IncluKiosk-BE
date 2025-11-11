# Java 17 이미지 기반 (원하는 버전에 맞게 조정 가능)
FROM eclipse-temurin:17-jre-alpine

# JAR 파일 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 포트 오픈
EXPOSE 8085

# 실행 명령
ENTRYPOINT ["java","-jar","/app.jar"]
FROM eclipse-temurin:17-jre-alpine
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8
RUN apk add --no-cache tzdata wget && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone
WORKDIR /app
RUN mkdir -p /app/uploads
COPY uploads/ /app/uploads/
COPY skill-gateway/target/skill-gateway-1.0.2.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/category/list || exit 1
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Xmx256m", "-Xms256m", "-jar", "app.jar"]

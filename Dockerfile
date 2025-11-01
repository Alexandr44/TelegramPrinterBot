FROM eclipse-temurin:17-jdk-jammy

# Установим печать и конвертацию
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        cups-client \
        libreoffice \
        imagemagick \
        ghostscript \
        poppler-utils \
        curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копируем jar и скрипт
COPY build/libs/*.jar /app/app.jar
COPY build/resources/main/scripts/printer.sh /app/print.sh
RUN chmod +x /app/print.sh

#ENV TELEGRAM_TOKEN=""
#ENV TELEGRAM_USERNAME=""

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

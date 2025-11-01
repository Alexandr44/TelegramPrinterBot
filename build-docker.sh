./gradlew clean bootJar
docker build -t myprinterbot:1.0 .
docker save myprinterbot:1.0 -o myprinterbot.tar
scp ./myprinterbot.tar mediaserver@192.168.0.200:/tmp/

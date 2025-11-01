./gradlew shadowJar
./gradlew clean bootJar

docker build -t myprinterbot:1.0 .
docker save myprinterbot:1.0 -o myprinterbot.tar
scp ./myprinterbot.tar mediaserver@192.168.0.200:/tmp/



#  sudo docker images
#  sudo docker load -i ./myprinterbot.tar
#  sudo docker run -d \
#    --name printerbot \
#    --restart unless-stopped \
#    --network host \
#    -e PRINTER_NAME="HP-LaserJet-P2015-Series" \
#    -v /tmp:/tmp \
#    -v /etc/localtime:/etc/localtime:ro \
#    myprinterbot:1.0
#
#  sudo docker logs -f printerbot
#
#  sudo docker stop printerbot
#  sudo docker rm printerbot
#  sudo docker rmi myprinterbot:1.0
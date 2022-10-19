@REM restart with clean install
@REM may be need to try kafka in Docker: https://hub.docker.com/r/bitnami/kafka
@REM reference: https://kafka.apache.org/31/documentation.html#topicconfigs

cd \
cd kaka\bin\windows

@echo TFlow Topics = [project-data,project-read,project-write,project-build]

@REM echo Delete Topic
@REM kafka-topics --bootstrap-server DESKTOP-K1PAMA3:9092 --delete --topic project-data

@echo Create Topic
set OPTIONS=--config max.message.bytes=671088640 --config retention.ms=6048000000 --config segment.ms=6048000000 --config file.delete.delay.ms=0
kafka-topics --bootstrap-server DESKTOP-K1PAMA3:9092 --create --topic project-write --partitions 1 --replication-factor 1 %OPTIONS%
kafka-topics --bootstrap-server DESKTOP-K1PAMA3:9092 --create --topic project-read --partitions 1 --replication-factor 1 %OPTIONS%
kafka-topics --bootstrap-server DESKTOP-K1PAMA3:9092 --create --topic project-data --partitions 1 --replication-factor 1 %OPTIONS%
kafka-topics --bootstrap-server DESKTOP-K1PAMA3:9092 --create --topic project-build --partitions 1 --replication-factor 1 %OPTIONS%

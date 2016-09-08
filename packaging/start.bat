@echo off
set LC_ALL=C
java -Xmx1024m  -XX:+UseParallelGC  -XX:-UseGCOverheadLimit -jar dubliner.jar %*
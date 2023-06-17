mvn clean compile package install

Copy-Item .\misc\battles.battle C:\robocode\battles\

java -Xmx1024M -cp C:\robocode\libs\* -XX:+IgnoreUnrecognizedVMOptions "--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED" "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED" "--add-opens=java.desktop/sun.awt=ALL-UNNAMED" robocode.Robocode -battle C:\robocode\battles\battles.battle -results results.txt

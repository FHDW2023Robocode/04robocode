mvn compile package install

cp ./misc/battles.battle ~/robocode/battles/

java -Xmx1024M -cp ~/robocode/libs/* -XX:+IgnoreUnrecognizedVMOptions "--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED" "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED" "--add-opens=java.desktop/sun.awt=ALL-UNNAMED" robocode.Robocode -battle ~/robocode/battles/battles.battle -results results.txt

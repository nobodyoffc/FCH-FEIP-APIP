zip -d FchParser.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'
zip -d FeipParser.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'
zip -d ApipManager.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'
zip -d Tools.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

scp FchParser.jar armx@154.221.28.61:/home/armx/apip/
scp FeipParser.jar armx@154.221.28.61:/home/armx/apip/
scp ApipManager.jar armx@154.221.28.61:/home/armx/apip/
scp Tools.jar armx@154.221.28.61:/home/armx/

cp /Users/liuchangyong/Desktop/IdeaProjects/FCH-FEIP-APIP/APIP-Server/target/APIP-Server.war /Users/liuchangyong/Desktop/IdeaProjects//FCH-FEIP-APIP/out/APIP.war
scp APIP.war armx@154.221.28.61:/home/armx/apache-tomcat-9.0.76/webapps/

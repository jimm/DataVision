rem Please see the comments in translation_to_jar.sh.

cd ..\lib
java -classpath .:TranslateOMatic.jar:DataVision.jar \
	jimm.properties.TranslateOMatic %1 %2
jar uf DataVision.jar datavision_%1%_%2%.properties menu_%1%_%2%.properties \
	paper_%1%_%2%.properties 

rem Please see the comments in translate.sh.

cd ..\lib
java -classpath .:TranslateOMatic.jar:DataVision.jar \
	jimm.properties.TranslateOMatic %1 %2 %3

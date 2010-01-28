JAVAC = javac
JAR = jar

JAR_FILE = TranslateOMatic.jar
LIB_DIR = ../../lib

# As of version 1.0 of DataVision, this makefile is broken because I moved
# TranslateOMatic into the jimm.properties package. Nobody uses this, so
# I don't need to fix it right now.
all:
	$(JAVAC) TranslateOMatic.java
	$(JAR) cf $(JAR_FILE) *.class translate.properties
	/bin/rm -f *.class
	mv $(JAR_FILE) $(LIB_DIR)

clean:
	/bin/rm -f *.class

distclean:	clean
	/bin/rm -f $(LIB_DIR)/$(JAR_FILE)

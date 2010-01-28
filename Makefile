# Makefile for DataVision.
#
# Jim Menard, jimm@io.com
#
# The .PHONY directives are not necessary. If your make doesn't support
# them, delete them.

SHELL = /bin/sh
JAVAC = javac
JAVA = java
JAR = jar
# JOPTS = -deprecation

CLASSES_DIR = classes
LIB_DIR = lib
DOCS_DIR = docs
SRC_ROOT = jimm
D = $(SRC_ROOT)/datavision
PROPS_DIR = $(SRC_ROOT)/properties
TEST_DIR = $(D)/test
TESTDATA_DIR = $(D)/testdata

# DEBUG_LANG = -g fr -i FR
# DEBUG_LANG = -g pt -i BR

POSTGRESQL_JAR = /usr/local/lib/postgresql.jdbc.jar
MYSQL_JAR = /usr/local/lib/mysql-connector-java-3.0.8-stable-bin.jar
DB_JARS = $(POSTGRESQL_JAR):$(MYSQL_JAR)
JUNIT_JAR = /usr/local/lib/junit.jar


JRUBY_JARS = $(LIB_DIR)/jruby.jar

BUILD_CLASSPATH = -classpath .:$(LIB_DIR)/jcalendar.jar:$(LIB_DIR)/iText.jar:$(JRUBY_JARS):$(LIB_DIR)/bsf.jar
RUN_CLASSPATH = $(BUILD_CLASSPATH):$(DB_JARS):$(CLASSES_DIR)
TEST_BUILD_CLASSPATH = $(BUILD_CLASSPATH):$(JUNIT_JAR)
TEST_RUN_CLASSPATH = $(RUN_CLASSPATH):$(JUNIT_JAR)
MAIN_CLASS = jimm.datavision.DataVision

MANIFEST = $(SRC_ROOT)/Manifest

JARFILE = $(LIB_DIR)/DataVision.jar

IMAGES = images/*.gif images/*.png
CLASSES_IMAGE_DIR = $(CLASSES_DIR)/images

DEBUG_XML_FILE = postgresql.xml
# DEBUG_XML_FILE = mysql.xml

# Testing, testing...is this thing on?
TEST_MAIN_CLASS = jimm.datavision.test.AllTests

# ================================================================
# Make the application. Does not create the JAR file.

.PHONY:	all
all:	html images classes properties

.PHONY: classes-dir
classes-dir:
	test -d $(CLASSES_DIR) || mkdir $(CLASSES_DIR)

.PHONY: classes
classes:	classes-dir
	$(JAVAC) $(JOPTS) $(BUILD_CLASSPATH) -d $(CLASSES_DIR) \
	  `find $(SRC_ROOT) \( -path '$(D)/test*' -prune \) \
	  -o -name '*.java' -print`

# ' <- Emacs font-lock un-confuser

.PHONY: properties
properties:
	./bin/copyProperties.rb

.PHONY:jar
jar:	$(JARFILE)
$(JARFILE):	all
	cp $(MANIFEST) $(CLASSES_DIR)
	cd $(CLASSES_DIR) && \
	  $(JAR) cfm DataVision.jar Manifest *.properties $(IMAGES) \
	  `find $(SRC_ROOT) \( -path '$(D)/test*' -prune \) \
	  -o -name '*.class' -print`
	mv $(CLASSES_DIR)/DataVision.jar $(LIB_DIR)

.PHONY: images
images:		$(CLASSES_IMAGE_DIR)

$(CLASSES_IMAGE_DIR):	classes-dir
	test -d $(CLASSES_IMAGE_DIR) || mkdir $(CLASSES_IMAGE_DIR)
	cp $(IMAGES) $(CLASSES_IMAGE_DIR)

# ================================================================
#
# Documentation targets.
#

# Though the application relies on the HTML help, it is not generated
# automatically from the "all" target.
.PHONY:	html
html:
	$(MAKE) -C $(DOCS_DIR) html

.PHONY:	docs
docs:
	$(MAKE) -C $(DOCS_DIR)

.PHONY:	javadoc
javadoc:
	$(MAKE) -C $(DOCS_DIR) javadoc

# ================================================================
#
# Debug and development targets.
#

# Standard development target: make the app, copy examples/$(DEBUG_XML_FILE)
# to /tmp/$(DEBUG_XML_FILE), and run the app against the copy.
.PHONY:	debug rdebug
debug:	all rdebug
rdebug:
	cp examples/$(DEBUG_XML_FILE) /tmp/$(DEBUG_XML_FILE)
	$(JAVA) $(RUN_CLASSPATH) $(MAIN_CLASS) $(DEBUG_LANG) \
		/tmp/$(DEBUG_XML_FILE)

# Make the app and run it against the copy in /tmp/$(DEBUG_XML_FILE). This
# way, we can run the app against a possibly modified report.
.PHONY:	debug-nocopy rdebug-nocopy
debug-nocopy: all rdebug-nocopy
rdebug-nocopy:
	$(JAVA) $(RUN_CLASSPATH) $(MAIN_CLASS) $(DEBUG_LANG) \
		/tmp/$(DEBUG_XML_FILE)

# Make the app and run it with no XML file argument. This creates a new
# report.
.PHONY:	debug-new rdebug-new
debug-new: all rdebug-new
rdebug-new:
	$(JAVA) $(RUN_CLASSPATH) $(MAIN_CLASS) $(DEBUG_LANG)

# Make the app and run it against a copy of examples/old_format.xml.
.PHONY:	debug-convert rdebug-convert
debug-convert: all rdebug-convert
rdebug-convert:
	cp examples/old_format.xml /tmp/old_format.xml
	$(JAVA) $(RUN_CLASSPATH) $(MAIN_CLASS) $(DEBUG_LANG) \
		/tmp/old_format.xml

# Make the app and run it against examples/charsep.xml.
.PHONY: debug-charsep rdebug-charsep
debug-charsep: all rdebug-charsep
rdebug-charsep:
	cp examples/charsep.xml /tmp/charsep.xml
	$(JAVA) $(RUN_CLASSPATH) $(MAIN_CLASS) $(DEBUG_LANG) \
		-e examples/charsep_data.csv /tmp/charsep.xml

# Make the app and run it against examples/ncsql.xml.
.PHONY: debug-ncsql rdebug-ncsql
debug-ncsql: all rdebug-ncsql
rdebug-ncsql:
	cp examples/ncsql.xml /tmp/ncsql.xml
	$(JAVA) $(RUN_CLASSPATH) $(MAIN_CLASS) $(DEBUG_LANG) \
		/tmp/ncsql.xml

# ================================================================
#
# Builds tags file
#
.PHONY: tags
tags:
	etags `find . -name '*.java'`

# ================================================================
#
# Builds Translate-O-Matic
#
.PHONY: xlate
xlate:
	$(MAKE) -C $(PROPS_DIR)

# ================================================================
#
# Builds and runs JUnit tests. 
#

.PHONY:	test
test:	test-classes rtest

.PHONY:	test-g
test-g:	test-classes rtest-g

.PHONY: test-classes
test-classes: classes-dir
	$(JAVAC) $(JOPTS) -d $(CLASSES_DIR) $(TEST_BUILD_CLASSPATH) \
		`find $(TEST_DIR) -name '*.java'`

.PHONY: rtest
rtest:	properties
	$(JAVA) $(TEST_RUN_CLASSPATH) $(TEST_MAIN_CLASS) $*

# The -g command line option to AllTests uses the JUnit swingui instead of
# the textui.
.PHONY: rtest-g
rtest-g:	properties
	$(JAVA) $(TEST_RUN_CLASSPATH) $(TEST_MAIN_CLASS) -g

# ================================================================
#
# Builds connection info test app. To run the app, use
# java -classpath classes:path-to-driver.jar examples.ConnectionTest args...
#
.PHONY: conntest
conntest:	$(CLASSES_DIR)/examples/ConnectionTest.class

$(CLASSES_DIR)/examples/ConnectionTest.class:	classes-dir examples/ConnectionTest.java
	$(JAVAC) $(JOPTS) -classpath .:$(JARFILE) -d $(CLASSES_DIR) \
		examples/ConnectionTest.java

# ================================================================
#
# Targets for making database generation programs.
#

.PHONY:	postgres
postgres:
	$(MAKE) -C $(TESTDATA_DIR)/postgres all

.PHONY:	mysql
mysql:
	$(MAKE) -C $(TESTDATA_DIR)/mysql all

.PHONY:	oracle
oracle:
	$(MAKE) -C $(TESTDATA_DIR)/oracle all

.PHONY:	odbc
odbc:
	$(MAKE) -C $(TESTDATA_DIR)/odbc all

# ================================================================

# A release consists of two tarballs: the first contains the source, the
# Jar file and the HTML version of the docs. The second contains a fresh
# version of the Web site.
.PHONY:	release
release:	all $(JARFILE) clean
	$(MAKE) -C $(PROPS_DIR) distclean
	$(MAKE) -C $(DOCS_DIR) release
	./bin/release.sh
	$(MAKE) -C $(DOCS_DIR) web

.PHONY:	web
web:
	$(MAKE) -C $(DOCS_DIR) web

# ================================================================
#
# Kryten's favorites.
#

.PHONY:	clean
clean:
	/bin/rm -fr $(CLASSES_DIR) $(PROPS_DIR)/*.class $(LIB_DIR)/*.properties
	/bin/rm -fr out.ps build `find . -name core`
	$(MAKE) -C $(DOCS_DIR) clean
	$(MAKE) -C $(TESTDATA_DIR) clean
	$(MAKE) -C $(PROPS_DIR) clean

.PHONY:	distclean
distclean:	clean
	/bin/rm -fr $(JARFILE)
	$(MAKE) -C $(DOCS_DIR) distclean
	$(MAKE) -C $(TESTDATA_DIR) distclean
	$(MAKE) -C $(PROPS_DIR) distclean

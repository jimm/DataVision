JAVAC = javac
TOP = ../../..
CLASS_DIR = $(TOP)/classes
JCLASSPATH = -classpath .

SRC = Office.java Job.java SchemaGen.java AggregateTestData.java

TIMESTAMP_FILE = $(CLASS_DIR)/testdata_timestamp

all:
	@echo targets: postgres, mysql, oracle, odbc, clean, distclean

java_classes:	$(TIMESTAMP_FILE)

$(TIMESTAMP_FILE):	$(SRC)
	$(JAVAC) -d $(CLASS_DIR) $(JCLASSPATH) $?

postgres:	$(TIMESTAMP_FILE)
	$(MAKE) -C postgres all

mysql:		$(TIMESTAMP_FILE)
	$(MAKE) -C mysql all

oracle:		$(TIMESTAMP_FILE)
	$(MAKE) -C oracle all

odbc:		$(TIMESTAMP_FILE)
	$(MAKE) -C odbc all

clean:
	rm -fr $(TIMESTAMP_FILE) $(CLASS_DIR)/jimm/datavision/testdata
	$(MAKE) -C postgres clean
	$(MAKE) -C mysql clean
	$(MAKE) -C oracle clean
	$(MAKE) -C odbc clean

distclean:	clean

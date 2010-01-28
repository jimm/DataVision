NAME = dv_example
TOP = ../../../..

CLASSES_DIR = $(TOP)/classes
TIMESTAMP_FILE = $(CLASSES_DIR)/mysql_timestamp

JAVA = java
JAVAC = javac
JCLASSPATH = -classpath $(CLASSPATH):$(TOP)/classes

all:		load

# Load data into tables.
load:		superclasses schema.sql create_tables $(TIMESTAMP_FILE)
	$(JAVA) $(JCLASSPATH) jimm.datavision.testdata.mysql.CreateData \
		>load.sql
	mysql $(NAME) <load.sql
	rm -f load.sql

superclasses:
	$(MAKE) -C .. java_classes

schema.sql:	$(TIMESTAMP_FILE)
	$(JAVA) $(JCLASSPATH) jimm.datavision.testdata.mysql.MySQLSchemaGen \
		>schema.sql

$(TIMESTAMP_FILE):	CreateData.java MySQLSchemaGen.java
	$(JAVAC) -d $(TOP)/classes $(JCLASSPATH) $?
	touch $(TIMESTAMP_FILE)

# Create tables by loading schema.
create_tables:	create_database
	mysql $(NAME) <schema.sql

# Create database, if it does not already exist.
# Create user, if he does not already exist.
# The ".localdomain" bit is for RedHat.
create_database:
	mysql $(NAME) -e "select 'yes';" >/dev/null 2>&1 || \
		( mysqladmin create $(NAME) && \
		mysql $(NAME) -e "grant all privileges on $(NAME).* to ''@localhost;" && \
		mysql $(NAME) -e "grant all privileges on $(NAME).* to ''@localhost.localdomain;" )

drop_database:
	mysqladmin drop $(NAME)

clean:
	rm -fr schema.sql load.sql $(TIMESTAMP_FILE) \
		$(CLASSES)/jimm/datavision/testdata/mysql

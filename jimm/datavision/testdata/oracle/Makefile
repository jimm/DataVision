NAME = dv_example
TOP = ../../../..

CLASSES_DIR = $(TOP)/classes
TIMESTAMP_FILE = $(CLASSES_DIR)/oracle_timestamp

JAVA = java
JAVAC = javac
JCLASSPATH = -classpath $(CLASSPATH):$(CLASSES_DIR)

all:		load

# Load data into tables.
load:		schema create_tables $(TIMESTAMP_FILE)
	$(MAKE) -C .. java_classes
	$(JAVA) $(JCLASSPATH) jimm.datavision.testdata.oracle.CreateData \
		>load.sql
	sqlnet $(NAME) load.sql >/dev/null
	rm -f load.sql

schema.sql:	$(TIMESTAMP_FILE)
	$(JAVA) $(JCLASSPATH) jimm.datavision.testdata.oracle.OracleSchemaGen \
		>schema.sql

$(TIMESTAMP_FILE):	CreateData.java OracleSchemaGen.java
	$(JAVAC) -d $(TOP)/classes $(JCLASSPATH) $?
	touch $(TIMESTAMP_FILE)

# Create tables by loading schema.
create_tables:	create_database
	sqlnet $(NAME) schema.sql

# Create database, if it does not already exist.
# Create user, if he does not already exist.
#
# THIS IS VERY, VERY WRONG.
create_database:
	psql $(NAME) -c "select 'yes' from dual;" >/dev/null || \
		createdb $(NAME)
	psql -U $(USER) $(NAME) -c "select 'yes' from dual;" >/dev/null || \
		psql $(NAME) -c "create user $(USER);"

drop_database:
	dropdb $(NAME)

clean:
	rm -fr schema.sql load.sql $(TIMESTAMP_FILE) \
		$(CLASSES)/jimm/datavision/testdata/oracle

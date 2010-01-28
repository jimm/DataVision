NAME = dv_example
TOP = ../../../..

CLASSES_DIR = $(TOP)/classes
TIMESTAMP_FILE = $(CLASSES_DIR)/postgres_timestamp

JAVA = java
JAVAC = javac
JCLASSPATH = -classpath $(CLASSES_DIR)

all:		load

# Load data into tables.
load:		superclasses schema.sql create_tables $(TIMESTAMP_FILE)
	$(JAVA) $(JCLASSPATH) jimm.datavision.testdata.postgres.CreateData \
		>load.sql
	psql -f load.sql $(NAME) >/dev/null
	rm -f load.sql

superclasses:
	$(MAKE) -C .. java_classes

schema.sql:	$(TIMESTAMP_FILE)
	$(JAVA) $(JCLASSPATH) \
		jimm.datavision.testdata.postgres.PostgreSQLSchemaGen \
		>schema.sql

$(TIMESTAMP_FILE):	CreateData.java PostgreSQLSchemaGen.java
	$(JAVAC) -d $(TOP)/classes $(JCLASSPATH) $?
	touch $(TIMESTAMP_FILE)

# Create tables by loading schema.
create_tables:	create_database
	psql -f schema.sql $(NAME)

# Create database, if it does not already exist.
# Create user, if he does not already exist.
create_database:
	psql -c "select 'yes';" $(NAME) >/dev/null || \
		createdb $(NAME)
	psql -c "select 'yes';" $(NAME) $(USER) >/dev/null || \
		psql $(NAME) -c "create user $(USER);" 

drop_database:
	dropdb $(NAME)

clean:
	rm -fr schema.sql load.sql $(TIMESTAMP_FILE) \
		$(CLASSES)/jimm/datavision/testdata/postgres

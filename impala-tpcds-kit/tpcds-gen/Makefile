
all: target/lib/dsdgen.jar target/tpcds-gen-1.0-SNAPSHOT.jar

target/tpcds-gen-1.0-SNAPSHOT.jar: $(shell find -name *.java) 
	mvn package

target/tpcds_kit.zip: tpcds_kit.zip
	mkdir -p target/
	cp tpcds_kit.zip target/tpcds_kit.zip

tpcds_kit.zip:
	@if [ ! -f $@ ]; then \
		echo "Downloading tpcds_kit.zip..."; \
		curl --output $@ http://dev.hortonworks.com.s3.amazonaws.com/hive-testbench/tpcds/tpcds_kit.zip; \
	else \
		echo "tpcds_kit.zip already exists. Skipping download."; \
	fi

target/lib/dsdgen.jar: target/tools/dsdgen
	cd target/; mkdir -p lib/; ( jar cvf lib/dsdgen.jar tools/ || gjar cvf lib/dsdgen.jar tools/ )

target/tools/dsdgen: target/tpcds_kit.zip
	test -d target/tools/ || (cd target; unzip tpcds_kit.zip)
	test -d target/tools/ || (cd target; mv */tools tools)
	cd target/tools; cat ../../patches/all/*.patch | patch -p0
	cd target/tools; cat ../../patches/${MYOS}/*.patch | patch -p1
	cd target/tools; make clean; make dsdgen

clean:
	mvn clean

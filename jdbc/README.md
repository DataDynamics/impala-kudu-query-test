# Impala JDBC Driver

Impala JDBC Driver를 사용하기 위한 설정 방법을 설명합니다. 보다 상세한 사항은 `Cloudera-JDBC-Connector-for-Apache-Impala-Install-Guide.pdf` 문서를 확인하십시오.

## JDBC Connection

* JDBC Driver : `com.cloudera.impala.jdbc.Driver`
* JDBC DataSource : `com.cloudera.impala.jdbc.DataSource`
* JDBC URL Pattern : `jdbc:impala://[Host]:[Port]/[Schema];[Property1]=[Value];[Property2]=[Value];...`
  * Example : `jdbc:impala://node1.example.com:18000/default2;AuthMech=3;UID=cloudera;PWD=cloudera`
  * Example : `jdbc:impala://node1.example.com:21050;AuthMech=3;UID=impala;PWD=cloudera;`
  * Example : `jdbc:impala://localhost:18000/default2;AuthMech=3;UID=cloudera;PWD=cloudera;MEM_LIMIT=1000000000;REQUEST_POOL=myPool;`

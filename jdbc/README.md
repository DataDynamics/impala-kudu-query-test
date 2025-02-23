# Impala JDBC Driver

Impala JDBC Driver를 사용하기 위한 설정 방법을 설명합니다. 보다 상세한 사항은 `Cloudera-JDBC-Connector-for-Apache-Impala-Install-Guide.pdf` 문서를 확인하십시오.

## JDBC Connection

JDBC 연결을 위한 가장 기본적인 정보는 다음과 같습니다.

* JDBC Driver : `com.cloudera.impala.jdbc.Driver`
* JDBC DataSource : `com.cloudera.impala.jdbc.DataSource`
* JDBC URL Pattern : `jdbc:impala://[Host]:[Port]/[Schema];[Property1]=[Value];[Property2]=[Value];...`
  * Example : `jdbc:impala://node1.example.com:18000/default2;AuthMech=3;UID=cloudera;PWD=cloudera`
  * Example : `jdbc:impala://node1.example.com:21050;AuthMech=3;UID=impala;PWD=cloudera;`
  * Example : `jdbc:impala://localhost:18000/default2;AuthMech=3;UID=cloudera;PWD=cloudera;MEM_LIMIT=1000000000;REQUEST_POOL=myPool;`

주요 인증방식은 다음과 같으며 인증 방식에 따라서 JDBC URL에 전달해야하는 설정 파라미터도 상이합니다.

* `AuthMech`
  * No Auth = 0
    * `jdbc:impala://<COORDINATOR>:21050;AuthMech=0`
  * Kerberos = 1
    * `jdbc:impala://<COORDINATOR>:21050;AuthMech=1;KrbRealm=EXAMPLE.COM;KrbHostFQDN=node1.example.com;KrbServiceName=impala`
  * User Name = 2
    * `jdbc:impala://<COORDINATOR>:21050;AuthMech=2;UID=impala`
  * LDAP = 3
    * `jdbc:impala://<COORDINATOR>:21050;AuthMech=3;UID=impala;PWD=cloudera;`
  * JWT = 14

또한 Impala 2.0 이후 버전부터 JDBC URL에 환경설정 속성을 추가할 수 있습니다.

```
jdbc:impala://localhost:18000/default2;AuthMech=3;UID=cloudera;PWD=cloudera;MEM_LIMIT=1000000000;REQUEST_POOL=myPool
```

이것 이외에 JDBC Driver에서 주로 사용하는 커넥터 옵션은 다음과 같습니다.

* `DelegationUID`
   * 모든 요청을 특정 사용자에게 위임하는 기능
   * 인증은 A이되 실 사용자는 B인 경우 A는 `UID`, B는 `DelegationUID`로 설정
* `UseNativeQuery`
   * 애플리케이션에서 전달한 query를 수정하는지 여부
   * 기본값 0 (0인 경우 query를 변경, 1은 변형하지 않고 있는 그대로 쿼리를 전달)
* `SocketTime`
   * 서버의 응답을 대기하는 시간 (초)
   * 기본값은 0이며 timeout이 없는 상태
* `TransportMode`
   * Thrift로 전송하는 프로토콜
   * `binary` - Binary Transport Protocol을 사용
   * `sasl` - SASL Transport Protocol을 사용
   * `http` - HTTP Transport Protocol을 사용
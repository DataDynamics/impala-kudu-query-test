# Impala & Kudu 테스트 드라이버

이 레파지토리는 Impala, Kudu 설치후 동작 테스트를 위한 테스트 쿼리를 포함하고 있습니다.

* Movielens
* Impala TPC-DS Kit
* Impala Iceberg Table


## Impala JDBC Driver

Impala JDBC Driver를 사용하기 위해서는 CLASSPATH에 `ImpalaJDBC42.jar` 파일을 추가하고 다음의 JDBC URL을 사용합니다. AD LDAP 등을 통해 인증이 활성화되어 있지 않은 경우 `UID`, `PWD`는 아무값이나 사용하도록 합니다.

```
jdbc:impala://<COORDINATOR>:21050;AuthMech=0
```

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

이것 이외에 JDBC Driver에서 제공하는 추가 설정은

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

## Impala ODBC Driver


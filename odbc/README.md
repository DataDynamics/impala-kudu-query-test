# Impala ODBC Driver

Impala ODBC Driver를 사용하기 위한 설정 방법을 설명합니다.

## 필요 버전

다음의 패키지는 CentOS, RHEL 기준으로 설명합니다.

* iODBC 3.42.9 이후 버전
* unixODBC 2.2.14 이후 버전
  * RHEL 9.4 버전에는 2.3.9-4 버전을 OS에서 기본 제공
* libsasl 설치
  * cyrus-sasl-2.1.22-7 이후 버전
  * cyrus-sasl-gssapi-2.1.22-7 이후 버전
  * cyrus-sasl-plain-2.1.22-7 이후 버전
  * RHEL 9.4 버전에는 2.1.27-21 버전을 OS에서 기본 제공

본 문서에서는 iODBC가 아닌 unixODBC 기준으로 설명합니다.

## ODBC 설치

다음의 커맨드로 SASL 및 ODBC 관련 패키지를 설치합니다.

```
yum install -y unixODBC cyrus-sasl cyrus-sasl-gssapi cyrus-sasl-plain
```

## Impala ODBC Driver 설치

Cloudera에서 Impala ODBC Connector를 다운로드(https://www.cloudera.com/downloads/connectors/impala/odbc) 하고 다음의 커맨드로 설치합니다. 설치후 Impala ODBC Driver의 홈 디렉토리는 `/opt/cloudera/impalaodbc`를 사용합니다.

```
yum install -y ClouderaImpalaODBC-2.7.2.1011-1.x86_64.rpm
```

Impala JDBC Driver를 설치한 후 다음의 디렉토리에 파일을 확인합니다.
`/opt/cloudera/impalaodbc/lib` 디렉토리에는 Impala ODBC Driver가 있으므로 사용을 위해서 `LD_LIBRARY_PATH`에 설정해야 합니다.
`/opt/cloudera/impalaodbc/Setup`에는 ODBC 설정을 위한 샘플 환경설정 파일이 있습니다. 이 파일은 사용자 홈 디렉토리 또는 `/etc` 디렉토리에 환경설정 파일을 구성해야 합니다.

```
# tree /opt/cloudera/impalaodbc
/opt/cloudera/impalaodbc
├── Cloudera-ODBC-Connector-for-Impala-Install-Guide.pdf
├── EULA.txt
├── ErrorMessages
│   └── en-US
│       ├── DSMessages.xml
│       ├── DSOAuthMessages.xml
│       ├── ImpalaODBCMessages.xml
│       ├── KerberosSupportMessages.xml
│       ├── ODBCMessages.xml
│       ├── SQLEngineMessages.xml
│       └── ThriftExtensionMessages.xml
├── Release-Notes-Impala-ODBC.txt
├── Setup
│   ├── odbc.ini
│   └── odbcinst.ini
├── lib
│   └── 64
│       ├── ImpalaODBC.did
│       ├── cacerts.pem
│       ├── cloudera.impalaodbc.ini
│       └── libclouderaimpalaodbc64.so
└── third-party-licenses.txt

```

설치후 다음의 커맨드로 설정 파일의 위치 등에 대해서 확인하도록 합니다.

```
# odbcinst -j
unixODBC 2.3.9
DRIVERS............: /etc/odbcinst.ini
SYSTEM DATA SOURCES: /etc/odbc.ini
FILE DATA SOURCES..: /etc/ODBCDataSources
USER DATA SOURCES..: /home/developer/.odbc.ini
SQLULEN Size.......: 8
SQLLEN Size........: 8
SQLSETPOSIROW Size.: 8
```

* `odbcinst.ini`
  * 드라이버 정보
  * 애플리케이션이 드라이버를 찾을 때 사용
* `odbc.ini`
  * 데이터베이스의 연결 정보
  * 인증 및 각종 연결에 필요한 설정 정보 포함
  * 애플리케이션 특정 DNS로 연결할 때 사용
  * `odbc.ini`의 DNS 설정에서 `odbcinst.int` 파일에 정의되어 있는 드라이버를 참보함

다음은 `odbcinst.ini` 파일의 예시로써 이 파일에는 ODBC Driver 정보를 담고 있으므로 `/etc/odbcinst.ini`이 더 적합해 보입니다.

```bash
# vi /etc/odbcinst.ini
[ODBC Drivers]
Cloudera ODBC Driver for Impala=Installed

[Cloudera ODBC Driver for Impala]
Description=Cloudera ODBC Driver for Impala (64-bit)
Driver=/opt/cloudera/impalaodbc/lib/64/libclouderaimpalaodbc64.so

## The option below is for using unixODBC when compiled with -DSQL_WCHART_CONVERT.
## Execute 'odbc_config --cflags' to determine if you need to uncomment it.
# IconvEncoding=UCS-4LE

```

다음은 사용자 정보를 포함한 데이터베이스 연결 정보 및 인증 정보(UID, PWD)를 포함하고 있으므로 보안상 주의가 필요합니다.

```bash
# vi .odbc.ini 
[ODBC]

[ODBC Data Sources]
IMPALA_DSN=Cloudera ODBC Driver for Impala

[IMPALA_DSN]

# Description: DSN Description.
# This key is not necessary and is only to give a description of the data source.
Description=Cloudera ODBC Driver for Impala (64-bit) DSN

# Driver: The location where the ODBC driver is installed to.
Driver=/opt/cloudera/impalaodbc/lib/64/libclouderaimpalaodbc64.so

# The DriverUnicodeEncoding setting is only used for SimbaDM
# When set to 1, SimbaDM runs in UTF-16 mode.
# When set to 2, SimbaDM runs in UTF-8 mode.
#DriverUnicodeEncoding=2

# Values for HOST, PORT, KrbFQDN, and KrbServiceName should be set here.
# They can also be specified on the connection string.
HOST=[COORDINATOR_IP]
PORT=21050
Database=default

# The authentication mechanism.
# 0 - No authentication (NOSASL)
# 1 - Kerberos authentication (SASL)
# 2 - Username authentication (SASL)
# 3 - Username/password authentication (NOSASL or SASL depending on UseSASL configuration)
AuthMech=0

# Set to 1 to use SASL for authentication. 
# Set to 0 to not use SASL. 
# When using Kerberos authentication (SASL) or Username authentication (SASL) SASL is always used
# and this configuration is ignored. SASL is always not used for No authentication (NOSASL).
UseSASL=0

# Kerberos related settings.
KrbFQDN=_HOST
KrbRealm=
KrbServiceName=impala

# Username/password authentication with SASL settings.
UID=impala
PWD=impala

# Set to 0 to disable SSL.
# Set to 1 to enable SSL.
SSL=0
CAIssuedCertNamesMismatch=1
TrustedCerts=/opt/cloudera/impalaodbc/lib/64/cacerts.pem

# General settings
TSaslTransportBufSize=1000
RowsFetchedPerBlock=10000
SocketTimeout=0
StringColumnLength=32767
UseNativeQuery=0
```

ODBC Driver를 정상적으로 동작시키려면 모든 사용자는 다음과 같이 `LD_LIBRARY_PATH` 환경변수가 설정되어야 합니다.

```bash
# vi .bashrc
export LD_LIBRARY_PATH=/opt/cloudera/impalaodbc/lib/64:$LD_LIBRARY_PATH
```

`odbc.ini` 파일에 설정을 한 경우는 다음과 같이 DSN명을 인수로 넘기도록 합니다.

```python
import pyodbc
conn = pyodbc.connect('DSN=IMPALA_DSN')
cursor = conn.cursor()
cursor.execute('SELECT * FROM your_table LIMIT 5')
results = cursor.fetchall()
print(results)
```

그러나 `odbc.ini` 파일이 보안상 위험하다고 판단하면 다음과 같이 코드에서 connection string을 통해 모두 처리하도록 합니다.

```python
import pyodbc

# DSN 정보와 인증 정보를 직접 연결 문자열에 포함
conn_str = 'Driver=Cloudera ODBC Driver for Impala;Host=[COORDINATOR_IP];Port=21050;AuthMech=3;UID=imopala;PWD=impala;DelegationUID=honggilong;SocketTimeout=30;TransportMode=sasl;UseNativeQuery=1;UseSASL=1;SSP_MEM_LIMIT=1000000000;SSP_REQUEST_POOL=mypool;'

# 연결 생성
conn = pyodbc.connect(conn_str)

# 커서 생성
cursor = conn.cursor()

# 쿼리 실행
cursor.execute("SELECT * FROM your_table")

# 결과 출력
for row in cursor.fetchall():
    print(row)

# 연결 종료
cursor.close()
conn.close()

```
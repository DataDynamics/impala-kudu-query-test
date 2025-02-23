# Impala ODBC Driver

Impala ODBC Driver를 사용하기 위한 설정 방법을 설명합니다.

## 필요 버전

* iODBC 3.42.9 이후 버전
* unixODBC 2.2.14 이후 버전
  * RHEL 9.4 버전에는 2.3.9-4 버전을 OS에서 기본 제공
* libsasl 설치
  * cyrus-sasl-2.1.22-7 이후 버전
  * cyrus-sasl-gssapi-2.1.22-7 이후 버전
  * cyrus-sasl-plain-2.1.22-7 이후 버전
  * RHEL 9.4 버전에는 2.1.27-21 버전을 OS에서 기본 제공


## ODBC 설치

```
yum install -y unixODBC cyrus-sasl cyrus-sasl-gssapi cyrus-sasl-plain
```

## Impala ODBC Driver 설치

설치후 Impala ODBC Driver의 홈 디렉토리는 `/opt/cloudera/impalaodbc`를 사용합니다.

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


```bash
# vi .odbcinst.ini 
[ODBC Drivers]
Cloudera ODBC Driver for Impala=Installed

[Cloudera ODBC Driver for Impala]
Description=Cloudera ODBC Driver for Impala (64-bit)
Driver=/opt/cloudera/impalaodbc/lib/64/libclouderaimpalaodbc64.so

## The option below is for using unixODBC when compiled with -DSQL_WCHART_CONVERT.
## Execute 'odbc_config --cflags' to determine if you need to uncomment it.
# IconvEncoding=UCS-4LE

```

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
HOST=[HOST]
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


```bash
# vi .bashrc
export LD_LIBRARY_PATH=/opt/cloudera/impalaodbc/lib/64:$LD_LIBRARY_PATH
export ODBCINI=$HOME/.odbc.ini
export ODBCSYSINI=$HOME/.odbcinst.ini
```

```sql
import pyodbc
conn = pyodbc.connect('DSN=IMPALA_DSN')
cursor = conn.cursor()
cursor.execute('SELECT * FROM your_table LIMIT 5')
results = cursor.fetchall()
print(results)
```
# Impala Iceberg 테스트

테스트를 위한 기본 데이터셋을 생성합니다.

```sql
create external table mock_rows stored as parquet as 
select x from (
with v as (values (1 as x), (1), (1), (1), (1)) 
select v.x from v, v v2, v v3, v v4, v v5, v v6
) a;


create external table customer_demo stored as parquet as 
select 
FROM_TIMESTAMP(DAYS_SUB(now() , cast ( TRUNC(RAND(7)*365*1) as bigint)), 'yyyy-MM') as year_month,
DAYS_SUB(now() , cast ( TRUNC(RAND(7)*365*1) as bigint)) as ts,
CONCAT(
  cast ( TRUNC(RAND(1) * 250 + 2) as string), '.' , 
  cast ( TRUNC(RAND(2) * 250 + 2) as string), '.', 
  cast ( TRUNC(RAND(3) * 250 + 2) as string), '.',
  cast ( TRUNC(RAND(4) * 250 + 2) as string)
) as ip,
CONCAT("USER_", cast ( TRUNC(RAND(4) * 1000) as string),'@somedomain.com') as email,
CONCAT("USER_", cast ( TRUNC(RAND(5) * 1000) as string)) as username,
CONCAT("USER_", cast ( TRUNC(RAND(6) * 100) as string)) as country,
cast( RAND(8)*10000 as double) as metric_1,
cast( RAND(9)*10000 as double) as metric_2,
cast( RAND(10)*10000 as double) as metric_3,
cast( RAND(11)*10000 as double) as metric_4,
cast( RAND(12)*10000 as double) as metric_5
from mock_rows;


create external table customer_demo2 stored as parquet as 
select 
FROM_TIMESTAMP(DAYS_SUB(now() , cast ( TRUNC(RAND(7)*365*1) as bigint)), 'yyyy-MM') as year_month,
DAYS_SUB(now() , cast ( TRUNC(RAND(7)*365*1) as bigint)) as ts,
CONCAT(
  cast ( TRUNC(RAND(1) * 250 + 2) as string), '.' , 
  cast ( TRUNC(RAND(2) * 250 + 2) as string), '.', 
  cast ( TRUNC(RAND(3) * 250 + 2) as string), '.',
  cast ( TRUNC(RAND(4) * 250 + 2) as string)
) as ip,
CONCAT("USER_", cast ( TRUNC(RAND(4) * 1000) as string),'@somedomain.com') as email,
CONCAT("USER_", cast ( TRUNC(RAND(5) * 1000) as string)) as username,
CONCAT("USER_", cast ( TRUNC(RAND(6) * 100) as string)) as country,
cast( RAND(8)*10000 as double) as metric_1,
cast( RAND(9)*10000 as double) as metric_2,
cast( RAND(10)*10000 as double) as metric_3,
cast( RAND(11)*10000 as double) as metric_4,
cast( RAND(12)*10000 as double) as metric_5
from mock_rows ;
```
`customer_demo` 테이블로 Iceberg 테이블을 생성합니다.

```sql
CREATE TABLE customer_demo_iceberg STORED BY ICEBERG AS SELECT * FROM customer_demo;
```

`customer_demo2` 테이블의 모든 데이터를 조회해서 `customer_demo_iceberg` 테이블을 생성합니다.

```sql
INSERT INTO customer_demo_iceberg select * from customer_demo2;
INSERT INTO customer_demo_iceberg select * from customer_demo2;
INSERT INTO customer_demo_iceberg select * from customer_demo2; 
```

`customer_demo_iceberg` 테이블을 이용해서 `year_month`로 파티셔닝한 Iceberg 테이블을 생성합니다.

```sql
CREATE TABLE customer_demo_iceberg_part PARTITIONED BY(year_month) STORED BY ICEBERG 
AS SELECT ts, ip , email, username , country, metric_1 , metric_2 , metric_3 , metric_4 , metric_5, year_month 
FROM customer_demo_iceberg;
```

파티셔닝한 `customer_demo_iceberg_part` 테이블을 분할합니다.

```sql
ALTER TABLE customer_demo_iceberg_part SET PARTITION SPEC (year_month,BUCKET(15, country));
```

`customer_demo_iceberg` 테이블 조회 결과를 파티션한 테이블에 적재합니다.

```sql
INSERT INTO customer_demo_iceberg_part (year_month, ts, ip, email, username, country, metric_1, metric_2, metric_3, metric_4, metric_5) 
SELECT year_month, ts, ip, email, username, country, metric_1, metric_2, metric_3, metric_4, metric_5  
FROM customer_demo_iceberg;
```

테이블의 이력을 확인하여 Iceberg의 time travel로 데이터를 조회합니다.

```sql
SELECT * FROM customer_demo_iceberg FOR SYSTEM_TIME AS OF '2021-12-09 05:39:18.689000000' LIMIT 100;

DESCRIBE HISTORY customer_demo_iceberg;

SELECT * FROM customer_demo_iceberg FOR SYSTEM_VERSION AS OF <snapshot id> LIMIT 100;
```

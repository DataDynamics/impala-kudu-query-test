# Movielens

## Test Dataset

[MovieLens Latest Datasets](https://grouplens.org/datasets/movielens/latest/)을 다음의 URL에서 크기에 맞춰서 다운로드합니다.

* [100K dataset](https://files.grouplens.org/datasets/movielens/ml-latest-small.zip)
* [1M dataset](https://files.grouplens.org/datasets/movielens/ml-1m.zip)
* [25M dataset](https://files.grouplens.org/datasets/movielens/ml-25m.zip)
* [32M dataset](https://files.grouplens.org/datasets/movielens/ml-32m.zip)
* [Latest dataset](https://files.grouplens.org/datasets/movielens/ml-latest.zip)

### 데이터 적재를 위한 테이블 생성

테스트 데이터셋을 다운로드하여 HDFS의 경로에 업로드후 테이블을 생성합니다.

```sql
CREATE EXTERNAL TABLE ratings (
  user_id INT,
  movie_id INT,
  rating FLOAT,
  timestamp BIGINT
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '::'
STORED AS TEXTFILE
LOCATION '/movielens/ratings';

CREATE EXTERNAL TABLE movies (
  movie_id INT,
  title STRING,
  genres STRING
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '::'
STORED AS TEXTFILE
LOCATION '/movielens/movies';

CREATE EXTERNAL TABLE users (
  user_id INT,
  gender STRING,
  age INT,
  occupation INT,
  zip_code STRING
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '::'
STORED AS TEXTFILE
LOCATION '/movielens/users';
```

### Parquet 파일 기반 Impala 테이블 생성

```sql
CREATE EXTERNAL TABLE ratings_parquet
STORED AS PARQUET
LOCATION '/movielens/parquet/ratings'
AS
SELECT * FROM ratings;

CREATE EXTERNAL TABLE movies_parquet
STORED AS PARQUET
LOCATION '/movielens/parquet/movies'
AS
SELECT * FROM movies;

CREATE EXTERNAL TABLE users_parquet
STORED AS PARQUET
LOCATION '/movielens/parquet/users'
AS
SELECT * FROM users;
```

### Kudu 테이블 생성

```sql
CREATE TABLE ratings_kudu
PRIMARY KEY (user_id, movie_id)
PARTITION BY HASH(user_id) PARTITIONS 16
STORED AS KUDU
AS
SELECT * FROM ratings;

CREATE TABLE movies_kudu
PRIMARY KEY (movie_id)
PARTITION BY HASH(movie_id) PARTITIONS 8
STORED AS KUDU
AS
SELECT * FROM movies;

CREATE TABLE users_kudu
PRIMARY KEY (user_id)
PARTITION BY HASH(user_id) PARTITIONS 8
STORED AS KUDU
AS
SELECT * FROM users;
```

## 조회 쿼리

### 가장 인기 있는 영화 Top 10

```sql
SELECT m.title, COUNT(*) as rating_count, AVG(r.rating) as avg_rating
FROM ratings r
JOIN movies m ON r.movie_id = m.movie_id
GROUP BY m.title
ORDER BY rating_count DESC, avg_rating DESC
LIMIT 10;
```

### 령대별 선호 장르

```sql
SELECT 
  CASE 
    WHEN u.age < 18 THEN '10대 미만'
    WHEN u.age BETWEEN 18 AND 29 THEN '20대'
    WHEN u.age BETWEEN 30 AND 39 THEN '30대'
    WHEN u.age BETWEEN 40 AND 49 THEN '40대'
    ELSE '50대 이상'
  END AS age_group,
  SPLIT(m.genres, '|')[0] AS primary_genre,
  AVG(r.rating) AS avg_rating,
  COUNT(*) AS rating_count
FROM ratings r
JOIN users u ON r.user_id = u.user_id
JOIN movies m ON r.movie_id = m.movie_id
GROUP BY 1, 2
ORDER BY 1, 4 DESC
LIMIT 20;
```

### 시간대별 평점 분포

```sql
SELECT 
  HOUR(FROM_UNIXTIME(r.timestamp)) AS hour_of_day,
  AVG(r.rating) AS avg_rating,
  COUNT(*) AS rating_count
FROM ratings r
GROUP BY 1
ORDER BY 1;
```

### 성별에 따른 장르 선호도 차이

```sql
SELECT 
  u.gender,
  SPLIT(m.genres, '|')[0] AS primary_genre,
  AVG(r.rating) AS avg_rating,
  COUNT(*) AS rating_count
FROM ratings r
JOIN users u ON r.user_id = u.user_id
JOIN movies m ON r.movie_id = m.movie_id
GROUP BY 1, 2
HAVING rating_count > 1000
ORDER BY 1, 4 DESC;
```

### 연도별 영화 평점 트렌드

```sql
SELECT 
  YEAR(FROM_UNIXTIME(r.timestamp)) AS year,
  COUNT(*) AS movie_count,
  AVG(r.rating) AS avg_rating
FROM ratings r
GROUP BY 1
ORDER BY 1;
```

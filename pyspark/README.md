# PySpark 테스트 드라이버

## Movielens

### PySpark from HDFS

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, avg, desc

# SparkSession 생성
spark = SparkSession.builder \
    .appName("MovieLens Data Analysis") \
    .getOrCreate()

# MovieLens 데이터 파일 경로 설정
ratings_file = "path/to/ratings.dat"  # ratings.dat 파일 경로
movies_file = "path/to/movies.dat"    # movies.dat 파일 경로

# CSV 파일 로드 (구분자를 "::"로 설정)
ratings_df = spark.read.csv(ratings_file, sep="::", header=False, inferSchema=True) \
    .toDF("userId", "movieId", "rating", "timestamp")
movies_df = spark.read.csv(movies_file, sep="::", header=False, inferSchema=True) \
    .toDF("movieId", "title", "genres")

# 데이터 스키마 확인
print("Ratings Schema:")
ratings_df.printSchema()
print("Movies Schema:")
movies_df.printSchema()

# 데이터 확인
print("Sample Ratings Data:")
ratings_df.show(5)
print("Sample Movies Data:")
movies_df.show(5)

# 영화별 평균 평점 계산
average_ratings = ratings_df.groupBy("movieId") \
    .agg(avg("rating").alias("avg_rating"))

# 영화 제목과 평균 평점 조인
movies_with_ratings = movies_df.join(average_ratings, on="movieId")

# 상위 10개의 높은 평점 영화 출력
top_movies = movies_with_ratings.orderBy(desc("avg_rating")).limit(10)
print("Top 10 Movies by Average Rating:")
top_movies.show()

# 특정 사용자(userId=1)의 평가한 영화 목록 출력
user_1_movies = ratings_df.filter(col("userId") == 1) \
    .join(movies_df, on="movieId")
print("Movies Rated by User 1:")
user_1_movies.show()

# SparkSession 종료
spark.stop()
```

### PySpark from Kudu

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, avg, desc

# SparkSession 생성
spark = SparkSession.builder \
    .appName("MovieLens Data Analysis with Kudu") \
    .config("spark.kudu.master", "kudu-master:7051") \  # Kudu master 서버 주소
    .getOrCreate()

# Kudu 테이블 경로 설정
ratings_table = "impala::default.ratings_kudu"
movies_table = "impala::default.movies_kudu"

# Kudu 테이블 읽기
ratings_df = spark.read.format("kudu").option("kudu.table", ratings_table).load()
movies_df = spark.read.format("kudu").option("kudu.table", movies_table).load()

# 데이터 스키마 확인
print("Ratings Schema:")
ratings_df.printSchema()
print("Movies Schema:")
movies_df.printSchema()

# 데이터 확인
print("Sample Ratings Data:")
ratings_df.show(5)
print("Sample Movies Data:")
movies_df.show(5)

# 영화별 평균 평점 계산
average_ratings = ratings_df.groupBy("movieId") \
    .agg(avg("rating").alias("avg_rating"))

# 영화 제목과 평균 평점 조인
movies_with_ratings = movies_df.join(average_ratings, on="movieId")

# 상위 10개의 높은 평점 영화 출력
top_movies = movies_with_ratings.orderBy(desc("avg_rating")).limit(10)
print("Top 10 Movies by Average Rating:")
top_movies.show()

# 특정 사용자(userId=1)의 평가한 영화 목록 출력
user_1_movies = ratings_df.filter(col("userId") == 1) \
    .join(movies_df, on="movieId")
print("Movies Rated by User 1:")
user_1_movies.show()

# SparkSession 종료
spark.stop()
```

Kudu 테이블을 PySpark에서 이용하려면 다음과 같이 인자를 추가합니다.

```shell
# spark3-shell --packages org.apache.kudu:kudu-spark3_2.12:<kudu-cdp-version> --repositories https://repository.cloudera.com/artifactory/cloudera-repos/


# spark3-shell --jars /opt/cloudera/parcels/CDH/lib/kudu/kudu-spark3_2.12.jar
```

PySpark에서 Kudu 사용방법은 https://docs.cloudera.com/runtime/7.3.1/kudu-development/topics/kudu-integration-with-spark.html를 참고하십시오.

다음은 SparkSQL을 이용하여 Kudu 테이블의 데이터를 처리하는 추가 예제입니다.

```python
import org.apache.kudu.client._
import org.apache.kudu.spark.kudu.KuduContext
import collection.JavaConverters._

// Read a table from Kudu
val df = spark.read
  .options(Map("kudu.master" -> "kudu.master:7051", "kudu.table" -> "kudu_table"))
  .format("kudu").load

// Query using the Spark API...
df.select("key").filter("key >= 5").show()

// ...or register a temporary table and use SQL
df.createOrReplaceTempView("kudu_table")
val filteredDF = spark.sql("select key from kudu_table where key >= 5").show()

// Use KuduContext to create, delete, or write to Kudu tables
val kuduContext = new KuduContext("kudu.master:7051", spark.sparkContext)

// Create a new Kudu table from a DataFrame schema
// NB: No rows from the DataFrame are inserted into the table
kuduContext.createTable(
    "test_table", df.schema, Seq("key"),
    new CreateTableOptions()
        .setNumReplicas(1)
        .addHashPartitions(List("key").asJava, 3))

// Check for the existence of a Kudu table
kuduContext.tableExists("test_table")

// Insert data
kuduContext.insertRows(df, "test_table")

// Delete data
kuduContext.deleteRows(df, "test_table")

// Upsert data
kuduContext.upsertRows(df, "test_table")

// Update data
val updateDF = df.select($"key", ($"int_val" + 1).as("int_val"))
kuduContext.updateRows(updateDF, "test_table")

// Data can also be inserted into the Kudu table using the data source, though the methods on
// KuduContext are preferred
// NB: The default is to upsert rows; to perform standard inserts instead, set operation = insert
// in the options map
// NB: Only mode Append is supported
df.write
  .options(Map("kudu.master"-> "kudu.master:7051", "kudu.table"-> "test_table"))
  .mode("append")
  .format("kudu").save

// Delete a Kudu table
kuduContext.deleteTable("test_table")
```
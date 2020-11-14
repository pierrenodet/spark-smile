# Spark SMILE
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](https://github.com/pierrenodet/spark-smile/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/pierrenodet/spark-smile.svg?branch=master)](https://travis-ci.com/pierrenodet/spark-smile)
[![codecov](https://codecov.io/gh/pierrenodet/spark-smile/branch/master/graph/badge.svg)](https://codecov.io/gh/pierrenodet/spark-smile)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.pierrenodet/spark-smile_2.12.svg?label=maven-central&colorB=blue)](https://search.maven.org/search?q=g:%22com.github.pierrenodet%22%20AND%20a:%22spark-smile_2.12%22)

Deprecated repository, all features have been upstreamed to the official [SMILE repository](https://github.com/haifengl/smile).

Repository for better integration of Spark MLLib Pipelines and SMILE library.

## Setup

Download the dependency from Maven Central

**SBT**

```scala
libraryDependencies += "com.github.pierrenodet" %% "spark-smile" % "0.0.2"
```

**Maven**

```maven-pom
<dependency>
  <groupId>com.github.pierrenodet</groupId>
  <artifactId>spark-smile_2.12</artifactId>
  <version>0.0.2</version>
</dependency>
```

## What's inside

This repository contains :

*  Distributed GridSearch of SMILE trainer with Spark
*  Integration of SMILE with Spark MLLib Pipelines
*  Seamless interoperability between SMILE and Spark DataFrames

## How to use

**Distributed GridSearch**

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()

val mushrooms = read.arff("data/mushrooms.arff")

val x = mushrooms.select(1,22).toArray
val y = mushrooms("class").toIntArray

sparkgscv(spark)(5, x, y, Seq(new Accuracy()): _*) { (x, y) => knn(x, y, 3) }
```

**From Spark DataFrame to SMILE DataFrame**

```scala
import org.apache.spark.smile.implicits._

val mushrooms = spark.read.format("libsvm").load("data/mushrooms.svm")

val x = mushrooms.toSmileDF().select("features").map(t=>t.getArray[AnyRef](0).map(_.asInstanceOf[Double])).toArray
val y = mushrooms.toSmileDF().apply("label").toDoubleArray.map(_.toInt-1)

val res = classification(5, x, y, Seq(new Accuracy()): _*) { (x, y) => knn(x, y, 3) }

println(res(0))
```

**From SMILE DataFrame to Spark DataFrame**

```scala
import org.apache.spark.smile.implicits._

val spark = SparkSession.builder().master("local[*]").getOrCreate()

val mushrooms = read.arff("data/mushrooms.arff").omitNullRows().toSparkDF(spark)

mushrooms.show()
```

**Use SMILE Classifier (or Regressor) in Spark MLLib Pipeline**

```scala
val raw = spark.read.format("libsvm").load("data/mushrooms.svm")

val scl = new SmileClassifier()
  .setTrainer({ (x, y) => knn(x, y, 3) })

val bce = new BinaryClassificationEvaluator()
  .setLabelCol("label")
  .setRawPredictionCol("rawPrediction")

val model = scl.fit(data)

println(bce.evaluate(model.transform(data)))

model.write.overwrite().save("/tmp/bonjour")
val loaded = SmileClassificationModel.load("/tmp/bonjour")
println(bce.evaluate(loaded.transform(data)))
```

## Contributing

Feel free to open an issue or make a pull request to contribute to the repository.

## Authors

* **Pierre Nodet** - *Main developer* - [GitHub Profile](https://github.com/pierrenodet)

See also the list of
[contributors](https://github.com/pierrenodet/spark-smile/graphs/contributors)
who participated in this project.

## License

This project is licensed under the Apache License Version 2.0 - see the [LICENSE](LICENSE) file for details.

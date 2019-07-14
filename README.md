# Spark SMILE
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](https://github.com/pierrenodet/spark-smile/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/pierrenodet/spark-smile.svg?branch=master)](https://travis-ci.org/pierrenodet/spark-smile)
[![codecov](https://codecov.io/gh/pierrenodet/spark-smile/branch/master/graph/badge.svg)](https://codecov.io/gh/pierrenodet/spark-smile)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.pierrenodet/spark-smile_2.12.svg?label=maven-central&colorB=blue)](https://search.maven.org/search?q=g:%22com.github.pierrenodet%22%20AND%20a:%22spark-smile_2.12%22)

Repository for better integration of Spark MLLib Pipelines and SMILE
library

## Setup

Download the dependency from Maven Central

**SBT**

```scala
libraryDependencies += "com.github.pierrenodet" %% "spark-smile" % "0.0.1"
```

**Maven**

```maven-pom
<dependency>
  <groupId>com.github.pierrenodet</groupId>
  <artifactId>spark-smile_2.12</artifactId>
  <version>0.0.1</version>
</dependency>
```

## What's inside

This repository contains :

*  Distributed GridSearch of SMILE trainer with Spark
*  Integration of SMILE with Spark MLLib Pipelines (WIP)
 
## How to use

**Distributed GridSearch**

```scala
implicit val spark = SparkSession.builder().master("local[*]").getOrCreate()

val mushrooms = read.libsvm("data/mushrooms.svm")
val (x, y) = mushrooms.unzipInt

sparkgscv(x, y, 5, Seq(new Accuracy().asInstanceOf[ClassificationMeasure]): _*) { (x, y) => knn(x, y, 3) }
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

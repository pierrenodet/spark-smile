package org.apache.spark.smile

import java.util.stream.Collectors

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.apache.spark.smile.implicits._
import org.apache.spark.sql.functions._
import org.scalatest.FunSuite
import smile.classification._
import smile.data._
import smile.read
import smile.validation.Accuracy
import smile.validation.cv._

import collection.JavaConverters._

class ConversionsSuite extends FunSuite with DatasetSuiteBase {

  test("toSmileDF") {

    val mushrooms = spark.read.format("libsvm").load("data/mushrooms.svm")

    val x = mushrooms.toSmileDF().select("features").map(t=>t.getArray[AnyRef](0).map(_.asInstanceOf[Double])).toArray
    val y = mushrooms.toSmileDF().apply("label").toDoubleArray.map(_.toInt-1)

    val res = classification(5, x, y, Seq(new Accuracy()): _*) { (x, y) => knn(x, y, 3) }

    assert(res(0) == 1)

  }

  test("toSparkDF") {

    val mushrooms = read.arff("data/mushrooms.arff").omitNullRows().toSparkDF(spark)

    val x = mushrooms.toSmileDF().select(1,22).toArray
    val y = mushrooms.toSmileDF().apply("class").toIntArray

    val res = classification(5, x, y, Seq(new Accuracy()): _*) { (x, y) => knn(x, y, 3) }

    assert(res(0) == 1)

  }

  test("toSmileDataset with weights") {

    val mushrooms = spark.read.format("libsvm").load("data/mushrooms.svm").withColumn("weight",lit(1.0))

    val x = mushrooms.toSmileDF().select("features").map(t=>t.getArray[AnyRef](0).map(_.asInstanceOf[Double])).toArray
    val w = mushrooms.toSmileDF().apply("weight").toDoubleArray.map(_.toInt)
    val y = mushrooms.toSmileDF().apply("label").toDoubleArray.map(_.toInt-1)

    val res = classification(5, x, y, Seq(new Accuracy()): _*) { (x, y) => logit(x, y) }

    assert(res(0) == 1)

  }

}

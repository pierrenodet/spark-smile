package org.apache.spark.smile

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.scalatest.FunSuite
import smile.classification._
import smile.data._
import smile.validation.{Accuracy, ClassificationMeasure, _}
import org.apache.spark.sql.functions._

class ImplicitSuite extends FunSuite with DatasetSuiteBase {

  test("toSmileDataset") {

    import org.apache.spark.smile.implicits._

    val mushrooms = spark.read.format("libsvm").load("data/mushrooms.svm")

    val (x,y) = mushrooms.toSmileDataset().unzipInt

    val res = cv(x, y, 5, Seq(new Accuracy().asInstanceOf[ClassificationMeasure]): _*) { (x, y) => knn(x, y, 3) }

    assert(res(0) == 1)

  }

  test("toSmileDataset with weights") {

    import org.apache.spark.smile.implicits._

    val mushrooms = spark.read.format("libsvm").load("data/mushrooms.svm").withColumn("weight",lit(1.0))

    val (x,y) = mushrooms.toSmileDataset().unzipInt

    val res = cv(x, y, 5, Seq(new Accuracy().asInstanceOf[ClassificationMeasure]): _*) { (x, y) => logit(x, y) }

    assert(res(0) == 1)

  }

}

package org.apache.spark.smile

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.scalatest.FunSuite
import smile.classification.knn
import smile.data._
import smile.validation.{Accuracy, ClassificationMeasure, _}

class ImplicitSuite extends FunSuite with DatasetSuiteBase {

  test("toSmileDataset") {

    import org.apache.spark.smile.implicits._

    val mushrooms = spark.read.format("libsvm").load("data/mushrooms.svm")

    val (x,y) = mushrooms.toSmileDataset().unzipInt

    val res = cv(x, y, 5, Seq(new Accuracy().asInstanceOf[ClassificationMeasure]): _*) { (x, y) => knn(x, y, 3) }

    assert(res(0) == 1)

  }

}

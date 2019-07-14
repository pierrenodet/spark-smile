package smile.tuning

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite
import smile.classification.{Classifier, _}
import smile.data._
import smile.read
import smile.validation._

class SparkOperatorsSuite extends FunSuite with DatasetSuiteBase {

  test("sparkgscv") {

    implicit val sparkImplicit = spark

    val mushrooms = read.libsvm("data/mushrooms.svm")
    val (x, y) = mushrooms.unzipInt

    val res = sparkgscv(x, y, 5, Seq(new Accuracy().asInstanceOf[ClassificationMeasure]): _*) { (x, y) => knn(x, y, 3) }

    assert(res(0)(0) == 1)

  }

}

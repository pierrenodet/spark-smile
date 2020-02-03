package smile.tuning

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.scalatest.FunSuite
import smile.classification._
import smile.read
import smile.validation._

class OperatorsSuite extends FunSuite with DatasetSuiteBase {

  test("sparkgscv") {

    val mushrooms = read.arff("data/mushrooms.arff")
    val x = mushrooms.select(1,22).toArray
    val y = mushrooms("class").toIntArray

    val res = sparkgscv(spark)(5, x, y, Seq(new Accuracy()): _*) { (x, y) => knn(x, y, 3) }

    assert(res(0)(0) == 1)

  }

}

package org.apache.spark.ml.regression

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.scalatest.FunSuite
import smile.regression._

class SmileRegressorSuite extends FunSuite with DatasetSuiteBase {

  test("benchmark") {

    val raw = spark.read.format("libsvm").load("data/cpusmall.svm")

    val sr = new SmileRegressor()
      .setTrainer({ (x, y) => rbfnet(x, y, 3)})

    val re = new RegressionEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")

    val data = raw
    data.cache()

    time {

      val model = sr.fit(data)
      val res = re.evaluate(model.transform(data))

      println(res)

      model.write.overwrite().save("/tmp/bonjour")
      val loaded = SmileRegressionModel.load("/tmp/bonjour")
      assert(re.evaluate(loaded.transform(data)) == re.evaluate(model.transform(data)))

    }

  }

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

}
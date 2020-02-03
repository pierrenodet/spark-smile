package org.apache.spark.ml.classification

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.scalatest.FunSuite
import smile.classification.knn

class SmileClassifierSuite extends FunSuite with DatasetSuiteBase {

  test("benchmark") {

    val raw = spark.read.format("libsvm").load("data/mushrooms.svm")

    val scl = new SmileClassifier()
      .setTrainer({ (x, y) => knn(x, y, 3) })

    val bce = new BinaryClassificationEvaluator()
      .setLabelCol("label")
      .setRawPredictionCol("rawPrediction")

    val data = raw
    data.cache()

    time {

      val model = scl.fit(data)
      val res = bce.evaluate(model.transform(data))

      println(res)

      model.write.overwrite().save("/tmp/bonjour")
      val loaded = SmileClassificationModel.load("/tmp/bonjour")
      assert(bce.evaluate(loaded.transform(data)) == bce.evaluate(model.transform(data)))

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

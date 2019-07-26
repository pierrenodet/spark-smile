package smile.tuning

import org.apache.spark.sql.SparkSession
import smile.classification.Classifier
import smile.validation.{ClassificationMeasure, cv}

import scala.reflect.ClassTag

case class SerializableClassificationMeasure(@transient measure: ClassificationMeasure)

trait Operators {

  def sparkgscv[T <: Object: ClassTag](
      x: Array[T],
      y: Array[Int],
      k: Int,
      measures: ClassificationMeasure*)(trainers: ((Array[T], Array[Int]) => Classifier[T])*)(
      implicit spark: SparkSession): Array[Array[Double]] = {

    val sc = spark.sparkContext

    val xBroadcasted = sc.broadcast[Array[T]](x)
    val yBroadcasted = sc.broadcast[Array[Int]](y)

    val trainersRDD = sc.parallelize(trainers)

    val measuresBroadcasted = measures.map(SerializableClassificationMeasure).map(sc.broadcast)

    trainersRDD
      .map(trainer => {
        val x = xBroadcasted.value
        val y = yBroadcasted.value
        val measures = measuresBroadcasted.map(_.value.measure)
        cv(x, y, k, measures: _*)(trainer)
      })
      .collect()

  }

}

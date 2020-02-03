package smile.tuning

import org.apache.spark.sql.SparkSession
import smile.classification.Classifier
import smile.validation.ClassificationMeasure
import smile.validation.cv._

import scala.reflect.ClassTag

case class SerializableClassificationMeasure(@transient measure: ClassificationMeasure)

trait Operators {

  def sparkgscv[T <: Object: ClassTag](
      spark: SparkSession)(k: Int, x: Array[T], y: Array[Int], measures: ClassificationMeasure*)(
      trainers: ((Array[T], Array[Int]) => Classifier[T])*): Array[Array[Double]] = {

    val sc = spark.sparkContext

    val xBroadcasted = sc.broadcast[Array[T]](x)
    val yBroadcasted = sc.broadcast[Array[Int]](y)

    val trainersRDD = sc.parallelize(trainers)

    val measuresBroadcasted = measures.map(SerializableClassificationMeasure).map(sc.broadcast)

    val res = trainersRDD
      .map(trainer => {
        val x = xBroadcasted.value
        val y = yBroadcasted.value
        val measures = measuresBroadcasted.map(_.value.measure)
        classification(k, x, y, measures: _*)(trainer)
      })
      .collect()

    xBroadcasted.destroy()
    yBroadcasted.destroy()

    res

  }

}

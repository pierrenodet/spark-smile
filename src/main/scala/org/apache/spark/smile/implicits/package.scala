package org.apache.spark.smile

import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Row, Dataset => SparkDataset}
import smile.data.{NominalAttribute, SparseDataset}

package object implicits {

  implicit class BetterSmileDataset(dataset: SparkDataset[_]) {

    def toSmileDataset(
        featuresColName: String = "features",
        labelColName: String = "label",
        weightColName: String = "weight"): SparseDataset = {
      val classification =
        dataset.select(labelColName).take(20).forall { case Row(x: Double) => (x % 1) == 0 }
      val minClass =
        if (classification)
          dataset.select(labelColName).agg(min(labelColName)).head.getDouble(0).toInt
        else 0
      val res =
        if (classification) new SparseDataset(new NominalAttribute("class"))
        else new SparseDataset(new NominalAttribute("response"))
      if (dataset.columns.contains(weightColName)) {
        dataset
          .select(Array(featuresColName, labelColName, weightColName).map(col): _*)
          .collect()
          .toList
          .zipWithIndex
          .foreach {
            case (Row(features: Vector, label: Double, weight: Double), i: Int) =>
              features.toArray.toList.zipWithIndex.foreach {
                case (x: Double, j: Int) => res.set(i, j, x)
              }
              if (classification) res.set(i, label.toInt - minClass, weight)
              else res.set(i, label, weight)
          }
      } else {
        dataset
          .select(Array(featuresColName, labelColName).map(col): _*)
          .collect()
          .zipWithIndex
          .foreach {
            case (Row(features: Vector, label: Double), i: Int) =>
              features.toArray.zipWithIndex.foreach {
                case (x: Double, j: Int) => res.set(i, j, x)
              }
              if (classification) res.set(i, label.toInt - minClass) else res.set(i, label)
          }
      }
      res

    }

  }

}

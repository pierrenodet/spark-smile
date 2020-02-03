package org.apache.spark.smile.implicits

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.types.{
  MapType,
  NullType,
  ObjectType,
  StringType,
  ArrayType => SparkArrayType,
  DataType => SparkDataType,
  StructField => SparkStructField,
  StructType => SparkStructType,
  _
}
import org.apache.spark.sql.{Row, SparkSession, DataFrame => SparkDataFrame}
import smile.data._
import smile.data.`type`.{DataType, DataTypes, StructField, StructType}

import scala.collection.JavaConverters._
import scala.collection.immutable

trait Conversions {

  implicit class BetterSparkDataFrame(df: SparkDataFrame) {

    private def smiled(schema: SparkStructType): StructType =
      DataTypes.struct(schema.map(smiled): _*)

    private def smiled(sf: SparkStructField): StructField =
      new StructField(sf.name, smiled(sf.dataType))

    private def smiled(dt: SparkDataType): DataType = {
      dt match {
        case _: NullType => DataTypes.StringType
        case BooleanType => DataTypes.BooleanType
        case ByteType => DataTypes.ByteType
        case BinaryType => DataTypes.ByteArrayType
        case ShortType => DataTypes.ShortType
        case IntegerType => DataTypes.IntegerType
        case LongType => DataTypes.LongType
        case FloatType => DataTypes.FloatType
        case DoubleType => DataTypes.DoubleType
        case StringType => DataTypes.StringType
        case TimestampType => DataTypes.TimeType
        case DateType => DataTypes.DateType
        case SparkArrayType(elementType, _) => DataTypes.array(smiled(elementType))
        case MapType(keyType, valueType, _) =>
          DataTypes.array(
            DataTypes.struct(
              Seq(
                new StructField("key", smiled(keyType)),
                new StructField("value", smiled(valueType))): _*))
        case ObjectType(cls) => DataTypes.`object`(cls)
        case _: org.apache.spark.ml.linalg.VectorUDT => DataTypes.array(DataTypes.DoubleType)
        case _: org.apache.spark.mllib.linalg.VectorUDT => DataTypes.array(DataTypes.DoubleType)
        case definedType: UserDefinedType[_] => DataTypes.`object`(definedType.userClass)
        case SparkStructType(fields) => DataTypes.struct(fields.map(smiled): _*)
      }
    }

    def toSmileDF(): DataFrame =
      DataFrame.of(
        df.collect()
          .map(row =>
            Tuple.of(
              row.toSeq.map {
                case v: org.apache.spark.ml.linalg.SparseVector =>
                  v.toArray.map(_.asInstanceOf[AnyRef])
                case v: org.apache.spark.ml.linalg.DenseVector =>
                  v.toArray.map(_.asInstanceOf[AnyRef])
                case r => r.asInstanceOf[AnyRef]
              }.toArray,
              smiled(df.schema)))
          .toList
          .asJava)

  }

  implicit class BetterSmileDataFrame(df: DataFrame) {

    def sparked(dt: DataType): SparkDataType = {
      dt.id match {
        case DataType.ID.Boolean => BooleanType
        case DataType.ID.Byte => ByteType
        case DataType.ID.Char => StringType
        case DataType.ID.Short => ShortType
        case DataType.ID.Integer => IntegerType
        case DataType.ID.Long => LongType
        case DataType.ID.Float => FloatType
        case DataType.ID.Double => DoubleType
        case DataType.ID.Decimal => StringType
        case DataType.ID.String => StringType
        case DataType.ID.Date => DateType
        case DataType.ID.Time => TimestampType
        case DataType.ID.DateTime => StringType
        case DataType.ID.Object =>
          ExpressionEncoder
            .javaBean(dt.asInstanceOf[smile.data.`type`.ObjectType].getObjectClass)
            .schema
        case DataType.ID.Array =>
          new SparkArrayType(
            sparked(dt.asInstanceOf[smile.data.`type`.ArrayType].getComponentType),
            false)
        case DataType.ID.Struct =>
          SparkStructType(
            dt.asInstanceOf[smile.data.`type`.StructType]
              .fields()
              .map(f => SparkStructField(f.name, sparked(f.`type`)))
              .toSeq)
      }
    }

    def sparked(tuple: Tuple, schema: SparkStructType): immutable.Seq[AnyRef] = {
      schema.indices.map(i => tuple.get(i))
    }

    def toSparkDF(spark: SparkSession): SparkDataFrame = {
      val schema = new SparkStructType(
        df.names().zip(df.types()).map { case (n, t) => SparkStructField(n, sparked(t)) })
      spark.createDataFrame(df.map(d => Row.fromSeq(sparked(d, schema))).toList.asJava, schema)
    }
  }
}

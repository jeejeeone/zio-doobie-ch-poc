// This snippet works with both CE2 and CE3
import cats.data.NonEmptyList
import cats.effect.implicits.monadCancelOps_
import com.clickhouse.data.value.UnsignedByte
import com.clickhouse.jdbc.ClickHouseResultSet
import doobie._
import doobie.enumerated.JdbcType
import doobie.implicits._
import fs2.Stream
import zio.Task
import zio.interop.catz._

case class User(id: String, name: String, age: Int)

case class ArrayTuple[T, A](left: T, right: A)

object DoobieExample {
  implicit val zioRuntime: zio.Runtime[Any] = zio.Runtime.default
  import doobie.postgres.implicits._

  def xa: Transactor[Task] =
    Transactor.fromDriverManager[Task](
      "com.clickhouse.jdbc.ClickHouseDriver",
      "jdbc:clickhouse://localhost:8123/test?user=test&password=test"
    )

  def ok(name: String) = sql"""SELECT name FROM users where name = $name""".query[String].to[List]



  val zap: Task[List[String]] = ok("zap").transact(xa)

  val ok = (1, 'a')


  case class ChTuple2[A, T](one: A, two: T)
  import scala.jdk.CollectionConverters._

  implicit val tuple2 =
    Get.Basic.one(JdbcType.Array, List(), (rs, col) => {
      val array = rs.getArray(col).getArray.asInstanceOf[java.util.List[AnyRef]]
      ChTuple2(array.get(0).asInstanceOf[UnsignedByte].intValue(), array.get(1).asInstanceOf[String])
    })


  /*
  record
        .getValue(column)
        .asArray()
        .map(r => r.asInstanceOf[util.List[AnyRef]])
        .map(_.asScala.toList)
        .map(array => (array(0).asInstanceOf[T1], array(1).asInstanceOf[T2]))
        .toList
   */

  implicit val ArrayTuple2: Get[List[ChTuple2[Int, String]]] =
    tuple2Array[UnsignedByte, String].tmap(ok => ok.map(ch => ChTuple2(ch.one.intValue(), ch.two)))

  def tuple22[A, B] = Get.Basic.one(JdbcType.Array, List(), (rs, col) => {
    val array = rs.getArray(col).getArray.asInstanceOf[java.util.List[AnyRef]]
    ChTuple2(array.get(0).asInstanceOf[A], array.get(1).asInstanceOf[B])
  })


  val tupleArray = Get.Advanced.array[AnyRef](NonEmptyList.one(""))
  
  def tuple2Array[A, B] = Get.Basic.one(JdbcType.Array, List(), (rs, col) => {
    val array = rs.getArray(col).getArray.asInstanceOf[Array[AnyRef]]

    array
      .map(v => v.asInstanceOf[java.util.List[AnyRef]])
      .map(_.asScala.toList)
      .map(v => ChTuple2(v(0).asInstanceOf[A], v(1).asInstanceOf[B]))
      .toList
  })

  def tupledArray[A, T] = Get.Basic.one(JdbcType.Array, List(), (rs, col) => {
    val array = rs.getArray(col).getArray.asInstanceOf[Array[AnyRef]]

    array
      .map(v => v.asInstanceOf[java.util.List[AnyRef]])
      .map(_.asScala.toList)
      .map(v => ChTuple2(v(0).asInstanceOf[A], v(1).asInstanceOf[T]))
      .toList
  })

  case class Zap(zap: String, zup: List[ChTuple2[Int, String]])
  val array = sql"""select 'zap', [(1, 'a'), (2, 'b')]""".query[Zap].to[List]

  //val array = sql"""select (1, 'a')""".query[ChTuple2[Int, String]].to[List]
  val wat = array.transact(xa)
}
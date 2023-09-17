import java.sql.DriverManager
import com.clickhouse.jdbc.ClickHouseDataSource
import zio.ZLayer
import com.clickhouse.data.value.UnsignedByte
import org.checkerframework.checker.signedness.qual.Unsigned
import zio._

object AnormExample extends ZIOAppDefault {
    import anorm._
    import anorm.SqlParser._
    import io.github.gaelrenoux.tranzactio._
    import io.github.gaelrenoux.tranzactio.anorm._

    val chSource = new ClickHouseDataSource("jdbc:clickhouse://localhost:8123/test?user=test&password=test")

    val dbLayer = ZLayer.succeed(chSource) >>> Database.fromDatasource

    val sql = SQL("SELECT (1, 'a')")

    implicit val columnToUnsignedByte: Column[UnsignedByte] = 
        Column.nonNull { (value, meta) =>
            val MetaDataItem(qualified, nullable, clazz) = meta
            value match {
                case v: UnsignedByte => Right(v)
                case _             => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to Unsigned for column $qualified"))
            }
        }

    val unsignedByte = get[UnsignedByte]("ok")

    implicit val columnTuple: Column[(Int, String)] = 
        Column.nonNull { (value, meta) =>
            val MetaDataItem(qualified, nullable, clazz) = meta
            value match {
                case array: java.util.List[_]  => 
                    val value = (array.get(0).asInstanceOf[UnsignedByte].intValue(), array.get(1).asInstanceOf[String])
                    Right(value)
                case _             => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} for column $qualified"))
            }
        }

    val tuple = get[(Int, String)]("ok")

    val listTuple = get[List[(Int, String)]]("ok")

    val sql3 = tzio { implicit c => 
        SQL("SELECT [(1, 'a'), (1, 'b')] as ok").as(Wut.parser.*)
    }

    def query = Database.autoCommit(sql3)

    override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
        query.flatMap(v => Console.printLine(s"HEOO: $v")).provide(AnormExample.dbLayer)
}

object Wut {
    import anorm._
    import anorm.SqlParser._

    case class Info(ok: List[(Int, String)])

    implicit val columnTuple: Column[(Int, String)] = 
    Column.nonNull { (value, meta) =>
        val MetaDataItem(qualified, nullable, clazz) = meta
        value match {
            case array: java.util.List[_]  => 
                val value = (array.get(0).asInstanceOf[UnsignedByte].intValue(), array.get(1).asInstanceOf[String])
                Right(value)
            case _             => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} for column $qualified"))
        }
    }

    val tuple = get[(Int, String)]("ok")

    val parser: RowParser[Info] = Macro.namedParser[Info]
}

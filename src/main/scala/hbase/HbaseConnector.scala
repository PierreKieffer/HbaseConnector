package hbase

import org.apache.hadoop.hbase.client.{ConnectionFactory, Result, Scan}
import org.apache.hadoop.hbase.filter.{CompareFilter, FilterList, SingleColumnValueFilter}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{TableInputFormat, TableMapReduceUtil}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.SparkContext
import utils.AppConfiguration

object HbaseConnector {

def HbaseConnectorQuery(ID : String, sparkContext: SparkContext, inputTimestamp : Long): DataFrame = {

    System.setProperty("hadoop.home.dir", AppConfiguration.hadoopHomeDir)

    val sqlContext = new SQLContext(sparkContext)

    import sqlContext.implicits._

    val conf = HBaseConfiguration.create()

      conf.set("hbase.zookeeper.quorum", AppConfiguration.hbaseZooQuorum)
      conf.set("hbase.master", AppConfiguration.hbaseMaster)
      conf.set("hbase.zookeeper.property.clientPort", AppConfiguration.hbaseZooPort)
      conf.set("zookeeper.znode.parent", AppConfiguration.hbaseZooParentZnode)

    conf.set(TableInputFormat.INPUT_TABLE, AppConfiguration.TABLE_NAME)

    val connection = ConnectionFactory.createConnection(conf)
    val table = connection.getTable(TableName.valueOf(AppConfiguration.TABLE_NAME))
    val scan = new Scan

    val timestampFilter = new SingleColumnValueFilter(Bytes.toBytes("header"), Bytes.toBytes("eventTime"), CompareFilter.CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(String.valueOf(inputTimestamp)))
    val IDFilter = new SingleColumnValueFilter(Bytes.toBytes("header"), Bytes.toBytes("ID"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(String.valueOf(ID)))

    val filters = new FilterList(IDFilter, timestampFilter)
    scan.setFilter(filters)

    conf.set(TableInputFormat.SCAN, TableMapReduceUtil.convertScanToString(scan))

    val hBaseRDD = sparkContext.newAPIHadoopRDD(conf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result])

  val DATAFRAME = hBaseRDD.map(x => {
    (Bytes.toString(x._2.getValue(Bytes.toBytes("header"), Bytes.toBytes("ID"))),
      Bytes.toString(x._2.getValue(Bytes.toBytes("header"), Bytes.toBytes("eventTime"))),
      Bytes.toString(x._2.getValue(Bytes.toBytes("ColFamily1"), Bytes.toBytes("ColExample1"))),
      Bytes.toString(x._2.getValue(Bytes.toBytes("ColFamily2"), Bytes.toBytes("ColExample2"))),
     )}).toDF()

  DATAFRAME

  }

}


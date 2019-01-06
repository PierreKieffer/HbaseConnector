package utils

import java.io.{File, FileInputStream}
import java.util
import org.yaml.snakeyaml.Yaml

object AppConfiguration {
  private var props : java.util.HashMap[String,String] = new util.HashMap


  var hbaseZooQuorum = ""
  var hbaseZooPort = ""
  var hbaseMaster = ""
  var hbaseZooParentZnode = ""
  var TABLE_NAME =""
  var hadoopHomeDir = ""
  var hadoopCoreSite = ""


  def initializeAppConfig(configFilePath : String) : Unit = {

    val fileInputStream = new FileInputStream(new File(configFilePath))
    val confObj = new Yaml().load(fileInputStream)

    props = confObj.asInstanceOf[java.util.HashMap[String,String]]
    hbaseZooQuorum = props.get("hbase.zookeeper.quorum")
    hbaseZooPort = props.get("hbase.zookeeper.property.clientPort")
    hbaseMaster = props.get("hbase.master")
    hbaseZooParentZnode = props.get("zookeeper.znode.parent")
    TABLE_NAME = props.get("TABLE_NAME")
    hadoopHomeDir = props.get("HADOOP_HOME_DIR")
    hadoopCoreSite = props.get("HADOOP_CORE_SITE")

  }

}

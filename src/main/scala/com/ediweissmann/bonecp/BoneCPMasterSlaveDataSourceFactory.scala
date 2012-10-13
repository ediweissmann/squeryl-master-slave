package com.ediweissmann.bonecp

import com.jolbox.bonecp.BoneCPConfig
import java.util.Properties
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import com.ediweissmann.squeryl.MasterSlaveDataSourceFactory

/**
 * Creates master and slave BoneCP data sources, applying common bonecp configuration values supplied via properties
 */
class BoneCPMasterSlaveDataSourceFactory(val soProps:Properties) extends MasterSlaveDataSourceFactory {

  def masterConfig(): BoneCPConfig = {
    val masterConfig = new BoneCPConfig()
    masterConfig.setUsername(soProps.getProperty("db.master.username"))
    masterConfig.setPassword(soProps.getProperty("db.master.password"))
    masterConfig.setJdbcUrl(soProps.getProperty("db.master.url"))

    applyCommonBoneCPProps(masterConfig, soProps)
    masterConfig
  }

  def slaveConfig(): BoneCPConfig = {
    val slaveConfig = new BoneCPConfig()
    slaveConfig.setUsername(soProps.getProperty("db.slave.username"))
    slaveConfig.setPassword(soProps.getProperty("db.slave.password"))
    slaveConfig.setJdbcUrl(soProps.getProperty("db.slave.url"))

    slaveConfig.setDefaultReadOnly(true)

    applyCommonBoneCPProps(slaveConfig, soProps)
    slaveConfig
  }

  def applyCommonBoneCPProps(config: BoneCPConfig, soProps: Properties) {
    config.setMaxConnectionsPerPartition(soProps.getProperty("bonecp.maxConnectionsPerPartition", "5").toInt)
    config.setMinConnectionsPerPartition(soProps.getProperty("bonecp.minConnectionsPerPartition", "5").toInt)
    config.setPartitionCount(soProps.getProperty("bonecp.partitions", "1").toInt)
  }

  override def createMasterDataSource(): DataSource = {
    new com.jolbox.bonecp.BoneCPDataSource(masterConfig())
  }

  override def createSlaveDataSource(): DataSource = {
    new com.jolbox.bonecp.BoneCPDataSource(slaveConfig())
  }
}

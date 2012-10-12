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

  def applyCommonBoneCPProps(config: BoneCPConfig, soProps: Properties) {
    config.setMaxConnectionsPerPartition(soProps.getProperty("bonecp.maxConnectionsPerPartition").toInt)
    config.setMinConnectionsPerPartition(soProps.getProperty("bonecp.minConnectionsPerPartition").toInt)
    config.setPartitionCount(soProps.getProperty("bonecp.partitions").toInt)
    config.setAcquireIncrement(soProps.getProperty("bonecp.acquireIncrement").toInt)
    config.setIdleMaxAge(soProps.getProperty("bonecp.idleMaxAge").toLong, TimeUnit.MINUTES)
    config.setIdleConnectionTestPeriodInSeconds(soProps.getProperty("bonecp.idleConnectionTestPeriod").toLong)
    config.setConnectionTestStatement(soProps.getProperty("bonecp.connectionTestStatement"))
    config.setInitSQL(soProps.getProperty("bonecp.initSql"))
    config.setMaxConnectionAgeInSeconds(soProps.getProperty("bonecp.maxConnectionAgeInSeconds").toLong)
    config.setStatementsCacheSize(soProps.getProperty("bonecp.statementsCacheSize").toInt)
    config.setReleaseHelperThreads(soProps.getProperty("bonecp.releaseHelperThreads").toInt)
    config.setCloseConnectionWatch(soProps.getProperty("bonecp.closeConnectionWatch").toBoolean)
    config.setCloseConnectionWatchTimeoutInMs(soProps.getProperty("bonecp.closeConnectionWatchTimeoutInMs").toLong)
  }

  def masterConfig(): BoneCPConfig = {
    val masterConfig = new BoneCPConfig()
    masterConfig.setUsername(soProps.getProperty("nl.db.username"))
    masterConfig.setPassword(soProps.getProperty("nl.db.password"))
    masterConfig.setJdbcUrl(soProps.getProperty("nl.db.url"))

    applyCommonBoneCPProps(masterConfig, soProps)
    masterConfig
  }

  def slaveConfig(): BoneCPConfig = {
    val slaveConfig = new BoneCPConfig()
    slaveConfig.setUsername(soProps.getProperty("nl.db.slave.username"))
    slaveConfig.setPassword(soProps.getProperty("nl.db.slave.password"))
    slaveConfig.setJdbcUrl(soProps.getProperty("nl.db.slave.url"))

    slaveConfig.setDefaultReadOnly(true)

    applyCommonBoneCPProps(slaveConfig, soProps)
    slaveConfig
  }

  override def createMasterDataSource(): DataSource = {
    new com.jolbox.bonecp.BoneCPDataSource(masterConfig())
  }

  override def createSlaveDataSource(): DataSource = {
    new com.jolbox.bonecp.BoneCPDataSource(slaveConfig())
  }
}

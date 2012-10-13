package com.ediweissmann.squeryl

import com.ediweissmann.bonecp.BoneCPMasterSlaveDataSourceFactory
import com.jolbox.bonecp.BoneCPConfig
import com.jolbox.bonecp.hooks.AbstractConnectionHook
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._
import org.junit.Ignore
import java.util.Properties

/**
 * Builds a master-slave squeryl session factory using boneCP data sources
 */
@Ignore
object TestSessionFactoryBoot extends SquerylMasterSlaveSessionFactoryBoot {
  def dataSourceFactory: MasterSlaveDataSourceFactory = new TestBoneCPMasterSlaveDataSourceFactory
}

object SoProperties extends Properties {
  load(getClass.getResourceAsStream("/db.properties"))
}


/**
 * Adds spying capabilities on the connections provided
 * Useful for tests, to assert the number of executed statements on a connection, etc
 */
@Ignore
class TestBoneCPMasterSlaveDataSourceFactory
  extends BoneCPMasterSlaveDataSourceFactory(soProps = SoProperties) {


  override def masterConfig(): BoneCPConfig = {
    val masterConfig = super.masterConfig()
    masterConfig.setConnectionHook(MasterConnectionSpy)
    masterConfig
  }

  override def slaveConfig(): BoneCPConfig = {
    val slaveConfig = super.slaveConfig()
    slaveConfig.setConnectionHook(SlaveConnectionSpy)
    slaveConfig
  }
}

object MasterConnectionSpy extends ConnectionSpy
object SlaveConnectionSpy extends ConnectionSpy

trait ConnectionSpy extends AbstractConnectionHook {
  var executedStatementsCount = 0

  override def onBeforeStatementExecute(
                     conn:com.jolbox.bonecp.ConnectionHandle,
                     statement: com.jolbox.bonecp.StatementHandle,
                     sql: java.lang.String,
                     params: java.util.Map[java.lang.Object,java.lang.Object]) {

    executedStatementsCount += 1
  }

  def reset() { executedStatementsCount = 0 }

  def assertNoQueriesExecuted() = {
    assertQueriesExecuted(0)
  }

  def assertQueriesExecuted(count:Int) = {
    assertThat(executedStatementsCount, is(count))
  }
}
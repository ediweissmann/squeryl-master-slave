package com.ediweissmann.squeryl

import javax.sql.DataSource
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.MySQLInnoDBAdapter

/**
 * Configures the squeryl session factory using master/slave data sources
 * When context is marked as being a readOnly transaction, the session factory will create a session using the readOnly connection
 * Otherwise, the session factory will build a session using the master connection
 */
trait SquerylMasterSlaveSessionFactoryBoot {

  def dataSourceFactory: MasterSlaveDataSourceFactory

  def createSessionFactory() {
    // concrete factory already set? then our job is done here
    SessionFactory.concreteFactory match {
      case Some(x) => Unit
      case None => {
        // create data sources
        val masterDataSource: DataSource = dataSourceFactory.createMasterDataSource()
        val slaveDataSource: DataSource = dataSourceFactory.createSlaveDataSource()

        // create concrete session factory
        SessionFactory.concreteFactory = Some(() => {
          val conn = if (ReadOnlyTransactionSupport.isReadOnlyTransaction) {
            slaveDataSource.getConnection
          } else {
            masterDataSource.getConnection
          }
          Session.create(conn, new MySQLInnoDBAdapter)
        }
        )
      }
    }
  }
}

/**
 * Data Source factory that can create master/slave data sources
 */
trait MasterSlaveDataSourceFactory {

  def createMasterDataSource(): DataSource
  def createSlaveDataSource(): DataSource
}

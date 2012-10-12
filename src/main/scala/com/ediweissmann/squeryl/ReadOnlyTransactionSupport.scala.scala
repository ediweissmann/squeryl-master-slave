package com.ediweissmann.squeryl

import org.squeryl.PrimitiveTypeMode

/**
 * Read-only transaction that get executed on read (slave) connections
 * This marks transactions as read-only using a thread-local boolean. Works in combination with a Master/Slave session factory that gets a connection from the master or slave connection pool
 */
trait ReadOnlyTransactionSupport extends PrimitiveTypeMode {

  private val _currentTransactionReadOnlyThreadLocal = new ThreadLocal[Boolean] {
    override def initialValue = false
  }

  def inTransactionReadOnly[A](a: => A): A =
    try {
      // set read-only flag
      _currentTransactionReadOnlyThreadLocal.set(true)
      inTransaction(a)
    } finally {
      // remove read-only flag
      _currentTransactionReadOnlyThreadLocal.set(false)
    }

  def isReadOnlyTransaction = _currentTransactionReadOnlyThreadLocal.get
}

object ReadOnlyTransactionSupport extends ReadOnlyTransactionSupport
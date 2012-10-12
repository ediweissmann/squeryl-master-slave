package com.ediweissmann.squeryl

import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._
import com.ediweissmann.squeryl.test.RollbackOnPurposeException
import org.scalatest.{BeforeAndAfter, GivenWhenThen, FeatureSpec}
import com.ediweissmann.squeryl.test.repository.{OrangesRepository, ApplesRepository}
import com.ediweissmann.squeryl.ReadOnlyTransactionSupport._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * Created on: 6/9/12 2:08 PM
 */
@RunWith(classOf[JUnitRunner])
class DefaultSpecTest extends FeatureSpec with GivenWhenThen with BeforeAndAfter {
  val connectionIsReadOnly = "Connection is read-only"

  TestSessionFactoryBoot.createSessionFactory()
  val appleRepository = new ApplesRepository
  val orangeRepository = new OrangesRepository

  before {
    transaction {
      appleRepository.deleteAll
      orangeRepository.deleteAll
    }

    SlaveConnectionSpy.reset()
    MasterConnectionSpy.reset()
  }

  feature("Reading during a write transaction should be done from master") {
    scenario("inside a write transaction a readOnly method is called") {

      given("a write transaction method")
      transaction {

        and("inside it a call to a readOnly method")
        when("when readOnly method is executed")
        inTransactionReadOnly {
          appleRepository.contains("anything")
        }
      }

      then("the master connection should be used for the read")
      MasterConnectionSpy.assertQueriesExecuted(1)

      and("(no statements should be executed on the slave connection)")
      SlaveConnectionSpy.assertNoQueriesExecuted()
    }
  }

  feature("Accidental writing in a readOnly method should throw a safe-guard exception") {
    scenario("inside a readOnly method a write is performed") {

      val thrown = intercept[RuntimeException] {
        given("a readOnly method")
        inTransactionReadOnly {

          and("inside it a write statement is executed")
          when("when the write statement is executed")
          appleRepository.save("test")
        }
      }

      then("an exception should be raised about the connection being readOnly")
      assertThat(thrown.getMessage, containsString(connectionIsReadOnly))
    }
  }

  feature("Exception thrown in a transaction block should rollback") {
    scenario("inside a transactional method an exception is thrown") {

      intercept[RollbackOnPurposeException] {
        given("a transactional method")
        transaction {
          and("inside it a write statement is executed")
          appleRepository.save("test")
          when("an exception is raised")
          throw new RollbackOnPurposeException
        }
      }

      then("transaction should be rolled back")
      assertNotInDb("test")
    }
  }

  feature("Nested transactions") {
    scenario("rollback of outer transaction should not rollback inner transaction") {

      intercept[RollbackOnPurposeException] {
        given("an outer transactional method")
        transaction {
          and("a write statement performed in the outer block")
          appleRepository.save("outerApple")
          and("an inner transaction block")
          transaction {
            and("a write statement performed in the inner transaction block")
            orangeRepository.save("innerOrange")
          }
          when("an exception is raised in the outer transaction")
          throw new RollbackOnPurposeException
        }
      }

      then("outer transaction should be rolled back")
      assertNotInDb("outerApple")
      and("inner transaction should be commited")
      assertOrangeInDb("innerOrange")
    }
  }

  feature("ReadOnly goes to slave") {
    scenario("readOnly method executes statements only on slave connection") {

      given("a readOnly method")
      inTransactionReadOnly {
        when("a read statement performed")
        appleRepository.contains("anything")
        then("statement should use slave readOnly connection")
        SlaveConnectionSpy.assertQueriesExecuted(1)
        and("master write connection should not be used")
        MasterConnectionSpy.assertNoQueriesExecuted()
      }
    }
  }

  feature("Writes go to master") {
    scenario("write method executes statements only on master connection") {

      given("a write method")
      transaction {
        when("a write statement performed")
        appleRepository.save("apple")
        then("statement should use master write connection")
        MasterConnectionSpy.assertQueriesExecuted(1)
        and("slave readOnly connection should not be used")
        SlaveConnectionSpy.assertNoQueriesExecuted()
      }
    }
  }

  def assertNotInDb(name:String) = transaction {
    assertThat(appleRepository.contains(name), is(false))
  }

  def assertOrangeInDb(name: String) = transaction {
    assertThat(orangeRepository.contains(name), is(true))
  }
}

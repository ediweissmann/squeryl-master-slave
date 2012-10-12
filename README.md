Support for Master/Slave connection setup in [squeryl](http://squeryl.org)
===========================================================================

Usage:
------

First, setup your Squeryl SessionFactoryBoot

    /**
     * Builds a master-slave squeryl session factory using boneCP data sources
     */
    object SessionFactoryBoot {

      def createSessionFactory {
        val masterDS = BoneCPMasterSlaveDataSourceFactory.createMasterDataSource(SoProps)
        val slaveDS = BoneCPMasterSlaveDataSourceFactory.createSlaveDataSource(SoProps)

        SquerylMasterSlaveSessionFactoryBoot.createSessionFactory(masterDS, slaveDS)
      }
    }

    object SoProps extends MultipleSourcedProperties(
      "/db.properties",
      "/bonecp-default.properties")

Then create the session factory

    import com.ediweissmann.squeryl.SessionFactoryBoot

    class ApplesRepository {

      // create session factory
      SessionFactoryBoot.createSessionFactory

Use it in your services

    import com.ediweissmann.squeryl.ReadOnlyTransactionSupport._

    class ApplesOrangesService {

      def readOnlyOperation() = inTransactionReadOnly {
        // do some reading from the slave
      }

      def writeOperation(name:String) = transaction {
        // do some writing to the master
      }

[Read more about bootstrapping the session factory over at Squeryl](http://squeryl.org/sessions-and-tx.html)

Tests
------

Have a look at the tests [DefaultSpecTest.scala](http://github.com/ediweissmann/squeryl-master-slave/blob/master/src/test/scala/com/ediweissmann/squeryl/DefaultSpecTest.scala) for coverage and what is/isn't supported.

There's a <code>create-schema-for-tests.sql</code> file that you need to execute in your local mysql <code>test</code> db before you can run the tests.

    mysql -uroot -ptest123 test < create-schema-for-tests.sql




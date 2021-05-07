package models.db.UsersAndAuthentication

import models.db.DBTableDefinitions
import slick.jdbc.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider

/**
 * Trait that contains generic slick db handling code to be mixed in with DAOs
 */
trait DAOSlick extends DBTableDefinitions with HasDatabaseConfigProvider[JdbcProfile]
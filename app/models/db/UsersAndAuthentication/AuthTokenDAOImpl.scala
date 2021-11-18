package models.db.UsersAndAuthentication

import java.util.UUID
import models.AuthToken
//import models.db.UsersAndAuthentication.AuthTokenDAOImpl._
import java.time.Instant
import play.api.db.slick.DatabaseConfigProvider
import javax.inject.Inject
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit val executionContext: ExecutionContext)
  extends AuthTokenDAO with DAOSlick {

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID) = {
    val query = slickAuthTokens.filter(_.id === id).result.headOption
    db.run(query).map {  resultOption =>
      resultOption.map {
        case token => AuthToken(token.id, token.userID, token.expiry)
      }
    }
  }

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: Instant) = {
    val query = slickAuthTokens.filter(_.expiry >= dateTime).result
    db.run(query).map { resultSeq =>
      resultSeq.map {  tokenDB =>
        AuthToken(tokenDB.id, tokenDB.userID, tokenDB.expiry)
      }
    }
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken) = {
    val authTokenDB = AuthTokenDB(token.id, token.userID, token.expiry)
    val query = slickAuthTokenInserts += authTokenDB
    db.run(query)
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID) = {
    val query = slickAuthTokens.filter(_.id === id).delete
    db.run(query)
  }
}

/**
 * The companion object.
 */
//object AuthTokenDAOImpl {
//
//  /**
//   * The list of tokens.
//   */
//  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()
//}

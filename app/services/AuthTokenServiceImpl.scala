package services

import java.util.UUID

import com.mohiva.play.silhouette.api.util.Clock
import javax.inject.Inject
import models.AuthToken
import org.joda.time.DateTimeZone
import models.db.UsersAndAuthentication.AuthTokenDAO
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 *
 * @param authTokenDAO The auth token DAO implementation.
 * @param clock        The clock instance.
 * @param ex           The execution context.
 */
class AuthTokenServiceImpl @Inject()(
  authTokenDAO: AuthTokenDAO,
  clock: Clock
)(
  implicit
  ex: ExecutionContext
) extends AuthTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userID The user ID for which the token should be created.
   * @param expiry The duration a token expires.
   * @return The saved auth token.
   */
  def create(userID: UUID, expiry: FiniteDuration = 5 minutes): Future[AuthToken] = {
    val expireInstant = java.time.Instant.now().plusSeconds(expiry.toSeconds)
    val token = AuthToken(UUID.randomUUID(), userID, expireInstant)
    authTokenDAO.save(token).flatMap{ id =>
      Future.successful(token)
    }
  }

  /**
   * Validates a token ID.
   *
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(id: UUID):Future[Option[AuthToken]] = authTokenDAO.find(id)

  /**
   * Cleans expired tokens.
   *
   * @return The list of deleted tokens.
   */
  def clean: Future[Seq[AuthToken]] = authTokenDAO.findExpired(java.time.Instant.now()).flatMap { tokens =>
    Future.sequence(tokens.map { token =>
      authTokenDAO.remove(token.id).map(_ => token)
    })
  }
}

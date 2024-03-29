package services

import com.mohiva.play.silhouette.api.LoginInfo

import java.util.UUID
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User, newUser:Boolean): Future[User]

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[User]

  def findAllProfiles(userIDs: Seq[UUID]): Future[Seq[User]]

  /**
   * Retrieves a user with a specific login info
   * @param loginInfo User's loginInfo
   * @return The user, or none
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Retrieves a user with a specific ID
   * @param id User's id
   * @return The user, or none
   */
  def retrieve(id: UUID): Future[Option[User]]

  //for testing purposes only
  def clearAll(): Unit
}

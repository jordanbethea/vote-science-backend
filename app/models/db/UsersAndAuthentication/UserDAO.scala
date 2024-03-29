package models.db.UsersAndAuthentication

import java.util.UUID
import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[User]]

  /**
   * Takes list of user ids, returns list of users, minus login info
   * @param userIDs list of guids of users to find
   * @return  list of all users found from list
   */
  def findAllProfiles(userIDs: Seq[UUID]): Future[Seq[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User, newUser: Boolean): Future[User]

  //for testing purposes only
  def clearAll()
}

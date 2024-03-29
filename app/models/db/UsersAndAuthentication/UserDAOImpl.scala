package models.db.UsersAndAuthentication

import java.util.UUID
import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.H2Profile.api._

/**
 * Give access to the user object using Slick
 */
class UserDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit val executionContext: ExecutionContext)
  extends UserDAO with DAOSlick {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(user.userID, loginInfo, user.firstName, user.lastName, user.fullName,
          user.email, user.avatarURL, user.emailVerified)
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID) = {
    val query = for {
      dbUser <- slickUsers.filter(_.id === userID)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (user, loginInfo) =>
          User(
            user.userID,
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL,
            user.emailVerified)
      }
    }
  }

  def findAllProfiles(userIDs: Seq[UUID]): Future[Seq[User]] = {
    val query = for {
      dbUsers <- slickUsers.filter(_.id.inSet(userIDs))
    } yield (dbUsers)

    db.run(query.result).map{ resultOption =>
      resultOption.map {
        user =>
          User(
            user.userID,
            null,
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL,
            user.emailVerified
          )
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User, newUser: Boolean) = {
    val dbUser = DBUser(user.userID, user.firstName, user.lastName, user.fullName,
        user.email, user.avatarURL, user.emailVerified)
    val dbLoginInfo = DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.    
    val loginInfoAction = {
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerID === user.loginInfo.providerID &&
        info.providerKey === user.loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield loginInfo
    }
    // combine database actions to be run sequentially
    val actions = (for {
      //TODO - dont't think this update is correct, need to filter first
      _ <- if(newUser) slickUsers += dbUser else slickUsers.update(dbUser)
      loginInfo <- loginInfoAction
      _ <- slickUserLoginInfos += DBUserLoginInfo(dbUser.userID, loginInfo.id.get)
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }

  //for testing purposes only
  def clearAll() = {
    val query = DBIO.seq(slickLoginInfos.delete, slickUsers.delete, slickUserLoginInfos.delete)
    db.run(query)
  }
}

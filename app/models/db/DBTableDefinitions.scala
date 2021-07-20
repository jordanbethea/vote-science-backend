package models.db

import com.mohiva.play.silhouette.api.LoginInfo
import slick.jdbc.JdbcProfile

trait DBTableDefinitions {

  protected val driver: JdbcProfile
  import driver.api._

  case class DBUser(
                     userID: String,
                     firstName: Option[String],
                     lastName: Option[String],
                     fullName: Option[String],
                     email: Option[String],
                     avatarURL: Option[String]
                   )

  class Users(tag: Tag) extends Table[DBUser](tag, "user_data") {
    def id = column[String]("user_id", O.PrimaryKey)
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def fullName = column[Option[String]]("full_name")
    def email = column[Option[String]]("email")
    def avatarURL = column[Option[String]]("avatar_url")

    def * = (id, firstName, lastName, fullName, email, avatarURL) <> (DBUser.tupled, DBUser.unapply)
  }

  case class DBLoginInfo(
                          id: Option[Long],
                          providerID: String,
                          providerKey: String
                        )

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "login_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("provider_id")
    def providerKey = column[String]("provider_key")

    def * = (id.?, providerID, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo(
                              userID: String,
                              loginInfoId: Long
                            )

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "user_login_info") {
    def userID = column[String]("user_id")
    def loginInfoId = column[Long]("login_info_id")

    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBPasswordInfo(
                             hasher: String,
                             password: String,
                             salt: Option[String],
                             loginInfoId: Long
                           )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "password_info") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("login_info_id")

    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  case class DBOAuth1Info(
                           id: Option[Long],
                           token: String,
                           secret: String,
                           loginInfoId: Long
                         )

  class OAuth1Infos(tag: Tag) extends Table[DBOAuth1Info](tag, "oauth1_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def token = column[String]("token")
    def secret = column[String]("secret")
    def loginInfoId = column[Long]("login_info_id")

    def * = (id.?, token, secret, loginInfoId) <> (DBOAuth1Info.tupled, DBOAuth1Info.unapply)
  }

  case class DBOAuth2Info(
                           id: Option[Long],
                           accessToken: String,
                           tokenType: Option[String],
                           expiresIn: Option[Int],
                           refreshToken: Option[String],
                           loginInfoId: Long
                         )

  class OAuth2Infos(tag: Tag) extends Table[DBOAuth2Info](tag, "oauth2_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accessToken = column[String]("access_token")
    def tokenType = column[Option[String]]("token_type")
    def expiresIn = column[Option[Int]]("expires_in")
    def refreshToken = column[Option[String]]("refresh_token")
    def loginInfoId = column[Long]("login_info_id")

    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId) <> (DBOAuth2Info.tupled, DBOAuth2Info.unapply)
  }

  case class DBOpenIDInfo(
                           id: String,
                           loginInfoId: Long
                         )

  class OpenIDInfos(tag: Tag) extends Table[DBOpenIDInfo](tag, "open_id_info") {
    def id = column[String]("id", O.PrimaryKey)
    def loginInfoId = column[Long]("login_info_id")

    def * = (id, loginInfoId) <> (DBOpenIDInfo.tupled, DBOpenIDInfo.unapply)
  }

  case class DBOpenIDAttribute(
                                id: String,
                                key: String,
                                value: String
                              )

  class OpenIDAttributes(tag: Tag) extends Table[DBOpenIDAttribute](tag, "open_id_attributes") {
    def id = column[String]("id")
    def key = column[String]("key")
    def value = column[String]("value")

    def * = (id, key, value) <> (DBOpenIDAttribute.tupled, DBOpenIDAttribute.unapply)
  }

  case class Slate(id: Long, title: String, creator: String, anonymous: Boolean)

  class SlateTableDef(tag: Tag) extends Table[Slate](tag, "slates") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def creator = column[String]("creator")
    def anonymous = column[Boolean]("anonymous")

    override def * = (id, title, creator, anonymous).mapTo[Slate]
  }

  case class Question (id: Long, slateID: Long, text:String)

  class QuestionTableDef(tag: Tag) extends Table[Question](tag, "questions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def slateID = column[Long]("slate_id")
    def text = column[String]("text")

    override def * = (id, slateID, text).mapTo[Question]
    def slate = foreignKey("slate_question_fk", slateID, slates)(_.id)
  }

  case class Candidate (id: Long, name: String, description: String, slateID: Long, questionID:Long)

  class CandidateTableDef(tag: Tag) extends Table[Candidate](tag, "candidates") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def slateID = column[Long]("slate_id")
    def questionID = column[Long]("question_id")

    override def * = (id, name, description, slateID, questionID).mapTo[Candidate]
    def question = foreignKey("question_candidate_fk", questionID, questions)(_.id)
    def slate = foreignKey("slate_candidate_fk", slateID, slates)(_.id)
  }

  case class Ballot(id: Long, slateID: Long, voter: String, anonymous: Boolean)

  class BallotTableDef(tag: Tag) extends Table[Ballot](tag, "ballots") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def slateID = column[Long]("slate_id")
    def voter = column[String]("voter")
    def anonymous = column[Boolean]("anonymous")

    override def * = (id, slateID, voter, anonymous).mapTo[Ballot]
    def slate = foreignKey("ballot_slate_fk", slateID, slates)(_.id)
  }

  case class FPTPChoice(ballotID: Long, questionID: Long, candidateID:Long)

  class FPTPTableDef(tag: Tag) extends Table[FPTPChoice](tag, "fptp_choices") {
    def ballotID = column[Long]("ballot_id")
    def questionID = column[Long]("question_id")
    def candidateID = column[Long]("candidate_id")

    override def * = (ballotID, questionID, candidateID).mapTo[FPTPChoice]
    def ballotKey = foreignKey("fptp_ballot_fk", ballotID, ballots)(_.id)
    def questionKey = foreignKey("fptp_question_fk", questionID, questions)(_.id)
    def candidateKey = foreignKey("fptp_candididate_fk", candidateID, candidates)(_.id)
  }

  case class ApprovalChoice(ballotID:Long, questionID:Long, candidateID:Long)

  class ApprovalTableDef(tag: Tag) extends Table[ApprovalChoice](tag, "approval_choices"){
    def ballotID = column[Long]("ballot_id")
    def questionID = column[Long]("question_id")
    def candidateID = column[Long]("candidate_id")

    override def * = (ballotID, questionID, candidateID).mapTo[ApprovalChoice]
    def ballotKey = foreignKey("approval_ballot_fk", ballotID, ballots)(_.id)
    def questionKey = foreignKey("approval_question_fk", questionID, questions)(_.id)
    def candidateKey = foreignKey("approval_candidate_fk", candidateID, candidates)(_.id)
  }

  case class RankedChoice(ballotID:Long, questionID:Long, candidateID:Long, rank:Int)

  class RankedTableDef(tag: Tag) extends Table[RankedChoice](tag, "ranked_choices"){
    def ballotID = column[Long]("ballot_id")
    def questionID = column[Long]("question_id")
    def candidateID = column[Long]("candidate_id")
    def rank = column[Int]("rank")

    override def * = (ballotID, questionID, candidateID, rank).mapTo[RankedChoice]
    def ballotKey = foreignKey("rank_ballot_fk", ballotID, ballots)(_.id)
    def questionKey = foreignKey("rank_question_fk", questionID, questions)(_.id)
    def candidateKey = foreignKey("rank_candidate_fk", candidateID, candidates)(_.id)
  }

  case class RangeChoice(ballotID:Long, questionID:Long, candidateID:Long, score:Int)

  class RangeTableDef(tag: Tag) extends Table[RangeChoice](tag, "range_choices"){
    def ballotID = column[Long]("ballot_id")
    def questionID = column[Long]("question_id")
    def candidateID = column[Long]("candidate_id")
    def score = column[Int]("score")

    override def * = (ballotID, questionID, candidateID, score).mapTo[RangeChoice]
    def ballotKey = foreignKey("range_ballot_fk", ballotID, ballots)(_.id)
    def questionKey = foreignKey("range_question_fk", questionID, questions)(_.id)
    def candidateKey = foreignKey("range_candidate_fk", candidateID, candidates)(_.id)
  }

  // table query definitions
  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickOpenIDInfos = TableQuery[OpenIDInfos]
  val slickOpenIDAttributes = TableQuery[OpenIDAttributes]

  val slates = TableQuery[SlateTableDef]
  val slatesInserts = slates returning slates.map(_.id)
  val questions = TableQuery[QuestionTableDef]
  val questionsInserts = questions returning questions.map(_.id)
  val candidates = TableQuery[CandidateTableDef]
  val candidatesInsert = candidates returning candidates.map(_.id)
  val ballots = TableQuery[BallotTableDef]
  val ballotsInserts = ballots returning ballots.map(_.id)
  val fptpResults = TableQuery[FPTPTableDef]
  val approvalResults = TableQuery[ApprovalTableDef]
  val rankedResults = TableQuery[RankedTableDef]
  val rangeResults = TableQuery[RangeTableDef]


  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
}

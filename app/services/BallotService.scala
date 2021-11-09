package services

import models.User
import models.dto.BallotDTO

import java.util.UUID
import scala.concurrent.Future

trait BallotService {
  /**
   * Save full ballot including all possible voting models. Returns
   */
  def saveBallot(ballot: BallotDTO): Future[UUID]

  /**
   * Return list of all ballots cast by a given user
   */
  def ballotsByVoter(user: User): Future[Seq[BallotDTO]]
}

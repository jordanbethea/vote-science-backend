package services

import models.User
import models.dto.BallotDTO

import scala.concurrent.Future

trait BallotService {
  def saveBallot(ballot: BallotDTO): Future[Long]

  def ballotsByVoter(user: User): Future[Seq[BallotDTO]]
}

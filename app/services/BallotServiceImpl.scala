package services

import javax.inject.Inject
import models.User
import models.db.BallotRepository
import models.dto.BallotDTO

import scala.concurrent.{ExecutionContext, Future}

class BallotServiceImpl @Inject() (ballotRepo: BallotRepository) (implicit val ExecutionContext: ExecutionContext)
  extends BallotService {

  override def saveBallot(ballot: BallotDTO): Future[Long] = {
    ballotRepo.saveBallot(ballot)
  }

  override def ballotsByVoter(user: User): Future[Seq[BallotDTO]] = {
    ballotRepo.getBallotsForUser(user)
  }
}

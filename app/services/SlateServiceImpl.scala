package services

import javax.inject.Inject
import models.User
import models.db.SlateRepository
import models.dto.{SlateLoadDTO, SlateSaveDTO}

import scala.concurrent.{ExecutionContext, Future}

class SlateServiceImpl @Inject() (slateRepo: SlateRepository) (implicit val ExecutionContext: ExecutionContext)
  extends SlateService {

  def saveSlate(slateData: SlateSaveDTO): Future[Long] = {
    slateRepo.fullAdd(slateData)
  }

  def slateInfo(slateID: Long): Future[Option[SlateLoadDTO]] = {
    slateRepo.getSingleSlate(slateID)
  }

  def slateList(): Future[Seq[SlateLoadDTO]] = {
    slateRepo.getFullSlates()
  }

  override def slatesByUser(user: User): Future[Seq[SlateLoadDTO]] = {
    slateRepo.getSlatesMadeByUser(user)
  }

  override def slatesFromList(slateIDs: Seq[Long]): Future[Seq[SlateLoadDTO]] = {
    slateRepo.getSlatesFromList(slateIDs)
  }


}

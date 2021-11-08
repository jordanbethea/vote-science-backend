package services

import javax.inject.Inject
import models.User
import models.db.SlateRepository
import models.dto.{SlateLoadDTO, SlateSaveNewDTO}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SlateServiceImpl @Inject() (slateRepo: SlateRepository) (implicit val ExecutionContext: ExecutionContext)
  extends SlateService {

  def saveSlate(slateData: SlateSaveNewDTO): Future[UUID] = {
    slateRepo.fullAdd(slateData)
  }

  def slateInfo(slateID: UUID): Future[Option[SlateLoadDTO]] = {
    slateRepo.getSingleSlate(slateID)
  }

  def slateList(): Future[Seq[SlateLoadDTO]] = {
    slateRepo.getFullSlates()
  }

  override def slatesByUser(user: User): Future[Seq[SlateLoadDTO]] = {
    slateRepo.getSlatesMadeByUser(user)
  }

  override def slatesFromList(slateIDs: Seq[UUID]): Future[Seq[SlateLoadDTO]] = {
    slateRepo.getSlatesFromList(slateIDs)
  }


}

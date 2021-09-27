package services

import models.User
import play.api.i18n.Lang

trait MailService {
  def sendConfirmationEmail(user: User, uri: String)(implicit lang: Lang): String
}

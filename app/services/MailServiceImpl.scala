package services

import models.User
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl, MessagesProvider}
import play.api.libs.mailer.{Email, MailerClient}

import javax.inject.Inject

class MailServiceImpl @Inject() (mailerClient: MailerClient, messagesApi: MessagesApi) extends MailService {

  def sendConfirmationEmail(user: User, uri: String)(implicit lang: Lang) = {
    implicit val messagesProvider: MessagesProvider = MessagesImpl(lang, messagesApi)

    val email = Email(
      Messages("email.activate.account.subject"),
      "from_vote_science@doesnotexist.com",
      Seq(user.email.getOrElse("")), //TODO - replace with map to skip section if no email
      bodyText = Some(views.txt.emails.activateAccount(user, uri).body),
      bodyHtml = Some(views.html.emails.activateAccount(user, uri).body)
    )
    mailerClient.send(email)
  }
}

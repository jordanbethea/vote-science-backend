package controllers.userAuth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.{AbstractAuthController, SilhouetteControllerComponents}
import play.api.i18n.Messages
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AnyContent, Request}
import services.MailService
import utils.CommonRoutes

import java.net.URLDecoder
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActivateAccountController @Inject() (scc: SilhouetteControllerComponents)
                                          (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  /**
   * Activates account if you pass in previously generated token sent to email address
   * @param token
   */
  def activate(token: UUID) = silhouette.UserAwareAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => userService.retrieve(authToken.userID).flatMap{
        case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
          userService.save(user.copy(emailVerified = true), false).map { _ =>
            Redirect(CommonRoutes.home).flashing("success" -> Messages("account.activated"))
          }
        case _ => Future.successful(Redirect(CommonRoutes.signin).flashing("error" -> Messages("account.invalidLink")))
      }
      case None => Future.successful(Redirect(CommonRoutes.signin).flashing("error" -> Messages("account.invalidLink")))
    }
  }

  def send = silhouette.UserAwareAction.async {implicit request =>
    val emailOption = request.identity.flatMap(_.email)
    emailOption match {
      case None => Future(Redirect(CommonRoutes.home))
      case Some(email) => {

        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        val result = Redirect(CommonRoutes.userInfo).flashing("info" -> Messages("activation.email.sent", email))

        userService.retrieve(loginInfo).flatMap {
          case Some(user) if !user.emailVerified =>
            authTokenService.create(user.userID).map { authToken =>
              val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()

              mailService.sendConfirmationEmail(user, url)(request.lang)
              result
            }
          case None => Future.successful(result)
        }
      }
    }
  }
}

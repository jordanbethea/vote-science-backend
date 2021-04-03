package controllers.UserControllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}
import play.api.libs.mailer.Email
import utils.route.Calls
import forms.ForgotPasswordForm

import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Forgot Password` controller.
 */
//class ForgotPasswordController @Inject() (
//  components: SilhouetteControllerComponents,
//  forgotPassword: views.html.user.forgotPassword
//)(implicit ex: ExecutionContext) extends SilhouetteController(components) {
//
//  /**
//   * Views the `Forgot Password` page.
//   *
//   * @return The result to display.
//   */
//  def view = UnsecuredAction.async { implicit request: Request[AnyContent] =>
//    Future.successful(Ok(forgotPassword(ForgotPasswordForm.form)))
//  }
//
//  /**
//   * Sends an email with password reset instructions.
//   *
//   * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
//   * a notice for not existing email addresses to prevent the leak of existing email addresses.
//   *
//   * @return The result to display.
//   */
//  def submit = UnsecuredAction.async { implicit request: Request[AnyContent] =>
//    ForgotPasswordForm.form.bindFromRequest.fold(
//      form => Future.successful(BadRequest(forgotPassword(form))),
//      email => {
//        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
//        val result = Redirect(Calls.signin).flashing("info" -> Messages("reset.email.sent"))
//        userService.retrieve(loginInfo).flatMap {
//          case Some(user) if user.email.isDefined =>
//            authTokenService.create(user.userID).map { authToken =>
//              val url = routes.ResetPasswordController.view(authToken.id).absoluteURL()
//
//              mailerClient.send(Email(
//                subject = Messages("email.reset.password.subject"),
//                from = Messages("email.from"),
//                to = Seq(email),
//                bodyText = Option("body"), //Some(views.txt.emails.resetPassword(user, url).body),
//                bodyHtml = Some(views.html.user.emails.resetPassword(user, url).body)
//              ))
//              result
//            }
//          case None => Future.successful(result)
//        }
//      }
//    )
//  }
//}

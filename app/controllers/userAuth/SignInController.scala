package controllers.userAuth

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import controllers.{AbstractAuthController, SilhouetteControllerComponents}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AnyContent, Request}
import forms.SignInForm

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignInController @Inject() (scc: SilhouetteControllerComponents)
                                 (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  def view = UnsecuredAction.async{ implicit request: Request[AnyContent] =>
      Future.successful(Ok(views.html.userAuth.signIn(SignInForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async{ implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest().fold(
      form => Future.successful(BadRequest(views.html.userAuth.signIn(form))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) => authenticateUser(user, data.rememberMe)
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            Redirect(utils.CommonRoutes.signin).flashing("error" -> "Invalid Credentials")
        }
      }
    )
  }
}

package utils

import play.api.mvc.Call

object CommonRoutes {
  def signin: Call = controllers.userAuth.routes.SignInController.submit

  def home: Call = controllers.routes.HomeController.index

  def userInfo: Call = controllers.routes.UserController.userInfo()
}

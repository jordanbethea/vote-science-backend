package utils

import play.api.mvc.Call

object CommonRoutes {
  def signin: Call = new Call("", "") //controllers.userAuth.routes.SignInController.xxx

  def home: Call = controllers.routes.HomeController.index()
}

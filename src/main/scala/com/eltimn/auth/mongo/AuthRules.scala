package com.eltimn.auth.mongo

import net.liftweb._
import common._
import http.{Factory, SessionVar, S}
import sitemap.{LocPath, Menu}
import util.Helpers

object AuthRules extends Factory {
  /* config */
  val authUserMeta = new FactoryMaker[AuthUserMeta[_, _]](SimpleUser) {}
  //val authTokenMeta = new FactoryMaker[AuthTokenMeta[]](SimpleAuthToken) {}
  //val authAfterAuthToken = new FactoryMaker[String]("/set-password") {} // where to send user after logging in with an AuthToken

  //def menus: List[Menu] = List(shiro.sitemap.Locs.logoutMenu)

  //val baseURL = new FactoryMaker[Path](Nil) {}
  val indexURL = new FactoryMaker[Path](Path(Nil)) {}
  val loginURL = new FactoryMaker[Path](Path("login" :: Nil)) {}
  val logoutURL = new FactoryMaker[Path](Path("logout" :: Nil)) {}
  val authURL = new FactoryMaker[Path](Path("auth" :: Nil)) {}

  // site settings
  val siteName = new FactoryMaker[String]("Example") {}
  val systemEmail = new FactoryMaker[String]("info@example.com") {}
  val systemUsername = new FactoryMaker[String]("Example Staff") {}
  //val systemUser = new FactoryMaker[Box[AuthUser]](Empty) {}

  def systemFancyEmail = AuthUtil.fancyEmail(systemUsername.vend, systemEmail.vend)
}

/*
 * Wrapper for Menu locations
 */
case class MenuLoc(menu: Menu) {
  lazy val path: Path = Path(menu.loc.link.uriList)
  lazy val url: String = path.toString
  lazy val fullUrl: String = S.hostAndPath+url
}

case class Path(pathList: List[String]) {
  override def toString: String = pathList.mkString("/","/","")
}

object AuthUtil {
  def tryo[T](f: => T): Box[T] = {
    try {
      f match {
        case null => Empty
        case x => Full(x)
      }
    } catch {
      case e => Failure(e.getMessage, Full(e), Empty)
    }
  }

  def fancyEmail(name: String, email: String): String = "%s <%s>".format(name, email)
}

object LoginRedirect extends SessionVar[Box[String]](Empty){
  override def __nameSalt = Helpers.nextFuncName
}
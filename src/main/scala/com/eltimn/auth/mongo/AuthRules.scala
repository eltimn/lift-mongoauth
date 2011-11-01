package com.eltimn.auth.mongo

import org.joda.time.{Days, Hours, ReadablePeriod}

import net.liftweb._
import common._
import http.{Factory, SessionVar, S}
import sitemap.{LocPath, Menu}
import util.Helpers

object AuthRules extends Factory {
  // AuthUserMeta object
  val authUserMeta = new FactoryMaker[AuthUserMeta[_]](SimpleUser) {}

  // urls
  val indexUrl = new FactoryMaker[Path](Path(Nil)) {}
  val loginUrl = new FactoryMaker[Path](Path("login" :: Nil)) {}
  val logoutUrl = new FactoryMaker[Path](Path("logout" :: Nil)) {}

  // site settings
  val siteName = new FactoryMaker[String]("Example") {}
  val systemEmail = new FactoryMaker[String]("info@example.com") {}
  val systemUsername = new FactoryMaker[String]("Example Staff") {}

  def systemFancyEmail = AuthUtil.fancyEmail(systemUsername.vend, systemEmail.vend)

  // LoginToken
  val loginTokenUrl = new FactoryMaker[Path](Path("login-token" :: Nil)) {}
  val loginTokenAfterUrl = new FactoryMaker[Path](Path("set-password" :: Nil)) {}
  val loginTokenExpires = new FactoryMaker[ReadablePeriod](Hours.hours(48)) {}

  // ExtSession
  val extSessionExpires = new FactoryMaker[ReadablePeriod](Days.days(90)) {}
  val extSessionCookieName = new FactoryMaker[String]("EXTSESSID") {}
  val extSessionCookiePath = new FactoryMaker[String]("/") {}

  // Permission
  val permissionWilcardToken = new FactoryMaker[String]("*") {}
  val permissionPartDivider = new FactoryMaker[String](":") {}
  val permissionSubpartDivider = new FactoryMaker[String](",") {}
  //val permissionCaseSensitive = new FactoryMaker[Boolean](true) {}
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

/*
 * User gets sent here after a successful login.
 */
object LoginRedirect extends SessionVar[Box[String]](Empty) {
  override def __nameSalt = Helpers.nextFuncName
}
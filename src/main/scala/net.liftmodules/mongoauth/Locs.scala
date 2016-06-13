package net.liftmodules.mongoauth

import net.liftweb._
import common._
import http.{RedirectResponse, RedirectWithState, S, RedirectState}
import sitemap.{Loc, Menu}
import sitemap.Loc.{DispatchLocSnippets, EarlyResponse, If}

object Locs extends Locs {
  protected def userMeta = MongoAuth.authUserMeta.vend
}
trait Locs extends LifeCycleLocs with AuthLocs

trait LifeCycleLocs {
  protected def userMeta: UserLifeCycle[_]

  protected lazy val indexUrl = MongoAuth.indexUrl.vend
  protected lazy val loginUrl = MongoAuth.loginUrl.vend
  protected lazy val logoutUrl = MongoAuth.logoutUrl.vend
  protected lazy val loginTokenUrl = MongoAuth.loginTokenUrl.vend

  // redirects
  def RedirectToLoginWithReferrer = {
    val uri = S.uriAndQueryString
    RedirectWithState(loginUrl, RedirectState(() => { LoginRedirect.set(uri) }))
  }

  def RedirectToIndex = RedirectResponse(indexUrl)
  def RedirectToIndexWithCookies = RedirectResponse(indexUrl, S.responseCookies:_*)

  // Loc guards
  val RequireAuthentication = If(
    () => userMeta.isAuthenticated,
    () => RedirectToLoginWithReferrer)

  val RequireNoAuthentication = If(
    () => !userMeta.isAuthenticated,
    () => RedirectToIndex)

  val RequireLoggedIn = If(
    () => userMeta.isLoggedIn,
    () => RedirectToLoginWithReferrer)

  val RequireNotLoggedIn = If(
    () => !userMeta.isLoggedIn,
    () => RedirectToIndex)

  // Menus
  def buildLogoutMenu = Menu(Loc(
    "Logout",
    logoutUrl.split("/").filter(_.length > 0).toList,
    S.?("logout"), logoutLocParams
  ))

  protected def logoutLocParams = RequireLoggedIn ::
    EarlyResponse(() => {
      if (userMeta.isLoggedIn) { userMeta.logUserOut() }
      Full(RedirectToIndexWithCookies)
    }) :: Nil


  def buildLoginTokenMenu = Menu(Loc(
    "LoginToken", loginTokenUrl.split("/").filter(_.length > 0).toList,
    S ? "liftmodule-monogoauth.locs.loginToken", loginTokenLocParams
  ))

  protected def loginTokenLocParams = RequireNotLoggedIn ::
    EarlyResponse(() => userMeta.handleLoginToken) :: Nil

}

trait AuthLocs {
  protected def userMeta: AuthUserMeta[_]

  protected def DisplayError(message: String) = () =>
    RedirectWithState(MongoAuth.indexUrl.vend, RedirectState(() => S.error(S ? message)))

  // Loc guards
  def HasRole(role: String) =
    If(() => userMeta.hasRole(role),
      DisplayError("liftmodule-monogoauth.locs.hasRole"))

  def LacksRole(role: String) =
    If(() => userMeta.lacksRole(role),
      DisplayError("liftmodule-monogoauth.locs.lacksRole"))

  def HasPermission(permission: Permission) =
    If(() => userMeta.hasPermission(permission),
      DisplayError("liftmodule-monogoauth.locs.hasPermission"))

  def LacksPermission(permission: Permission) =
    If(() => userMeta.lacksPermission(permission),
      DisplayError("liftmodule-monogoauth.locs.overQualified"))

  def HasAnyRoles(roles: Seq[String]) =
    If(() => userMeta.hasAnyRoles(roles),
       DisplayError("liftmodule-monogoauth.locs.wrongRole"))
}

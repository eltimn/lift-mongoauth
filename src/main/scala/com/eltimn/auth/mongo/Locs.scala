package com.eltimn.auth.mongo

import net.liftweb._
import common._
import http.{RedirectResponse, RedirectWithState, S, RedirectState}
import sitemap.{Loc, Menu}
import sitemap.Loc.{DispatchLocSnippets, EarlyResponse, If}

object Locs {
  private lazy val userMeta = AuthRules.authUserMeta.vend

  private lazy val indexURL = AuthRules.indexURL.vend
  private lazy val loginURL = AuthRules.loginURL.vend
  private lazy val logoutURL = AuthRules.logoutURL.vend
  private lazy val authURL = AuthRules.authURL.vend

  // redirects
  def RedirectToLoginWithReferrer = {
    val uri = S.uriAndQueryString
    RedirectWithState(loginURL.toString, RedirectState(() => { LoginRedirect.set(uri) }))
  }

  def RedirectToIndexURL = RedirectResponse(indexURL.toString)

  private def DisplayError(message: String) = () =>
    RedirectWithState(indexURL.toString, RedirectState(() => S.error(message)))

  // Loc guards
  val RequireAuthentication = If(
    () => userMeta.isAuthenticated,
    () => RedirectToLoginWithReferrer)

  val RequireNoAuthentication = If(
    () => !userMeta.isAuthenticated,
    () => RedirectToIndexURL)

  val RequireLoggedIn = If(
    () => userMeta.isLoggedIn,
    () => RedirectToLoginWithReferrer)

  val RequireNotLoggedIn = If(
    () => !userMeta.isLoggedIn,
    () => RedirectToIndexURL)

  def HasRole(role: String) =
    If(() => userMeta.hasRole(role),
      DisplayError("You are the wrong role to access that resource."))

  def LacksRole(role: String) =
    If(() => userMeta.lacksRole(role),
      DisplayError("You lack the sufficient role to access that resource."))

  def HasPermission(permission: String) =
    If(() => userMeta.hasPermission(permission),
      DisplayError("Insufficient permissions to access that resource."))

  def LacksPermission(permission: String) =
    If(() => userMeta.lacksPermission(permission),
      DisplayError("Overqualified permissions to access that resource."))

  def HasAnyRoles(roles: Seq[String]) =
    If(() => userMeta.hasAnyRoles(roles),
       DisplayError("You are the wrong role to access that resource."))

  // MenuLocs
  def buildLogoutLoc = MenuLoc(Menu(Loc("Logout", logoutURL.pathList,
    S.??("logout"), logoutLocParams)))

  private def logoutLocParams = RequireLoggedIn ::
    EarlyResponse(() => {
      if (userMeta.isLoggedIn) { userMeta.logUserOut() }
      Full(RedirectToIndexURL)
    }) :: Nil


  def buildAuthLoc = MenuLoc(Menu(Loc("Auth", authURL.pathList,
      "Auth", authLocParams)))

  private def authLocParams = RequireNotLoggedIn ::
    EarlyResponse(() => userMeta.loginTokenMeta.map(_.handleToken)) :: Nil

  /*
  object DefaultLogin
    extends DispatchLocSnippets
    with shiro.snippet.DefaultUsernamePasswordLogin {
    def dispatch = {
      case "login" => render
    }
  }
  */
}
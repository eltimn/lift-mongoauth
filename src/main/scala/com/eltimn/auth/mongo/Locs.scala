package com.eltimn.auth.mongo

import net.liftweb._
import common._
import http.{RedirectResponse, RedirectWithState, S, RedirectState}
import sitemap.{Loc, Menu}
import sitemap.Loc.{DispatchLocSnippets, EarlyResponse, If}

object Locs extends Locs
trait Locs {
  private lazy val userMeta = AuthRules.authUserMeta.vend

  private lazy val indexUrl = AuthRules.indexUrl.vend
  private lazy val loginUrl = AuthRules.loginUrl.vend
  private lazy val logoutUrl = AuthRules.logoutUrl.vend
  private lazy val loginTokenUrl = AuthRules.loginTokenUrl.vend

  // redirects
  def RedirectToLoginWithReferrer = {
    val uri = S.uriAndQueryString
    RedirectWithState(loginUrl.toString, RedirectState(() => { LoginRedirect.set(uri) }))
  }

  def RedirectToIndex = RedirectResponse(indexUrl.toString)
  def RedirectToIndexWithCookies = RedirectResponse(indexUrl.toString, S.responseCookies:_*)

  protected def DisplayError(message: String) = () =>
    RedirectWithState(indexUrl.toString, RedirectState(() => S.error(message)))

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

  def HasRole(role: String) =
    If(() => userMeta.hasRole(role),
      DisplayError("You are the wrong role to access that resource."))

  def LacksRole(role: String) =
    If(() => userMeta.lacksRole(role),
      DisplayError("You lack the sufficient role to access that resource."))

  def HasPermission(permission: Permission) =
    If(() => userMeta.hasPermission(permission),
      DisplayError("Insufficient permissions to access that resource."))

  def LacksPermission(permission: Permission) =
    If(() => userMeta.lacksPermission(permission),
      DisplayError("Overqualified permissions to access that resource."))

  def HasAnyRoles(roles: Seq[String]) =
    If(() => userMeta.hasAnyRoles(roles),
       DisplayError("You are the wrong role to access that resource."))

  // Menus
  def buildLogoutMenu = Menu(Loc("Logout", logoutUrl.pathList,
    S.??("logout"), logoutLocParams))

  protected def logoutLocParams = RequireLoggedIn ::
    EarlyResponse(() => {
      if (userMeta.isLoggedIn) { userMeta.logUserOut() }
      Full(RedirectToIndexWithCookies)
    }) :: Nil


  def buildLoginTokenMenu = Menu(Loc("LoginToken", loginTokenUrl.pathList,
      "LoginToken", loginTokenLocParams))

  protected def loginTokenLocParams = RequireNotLoggedIn ::
    EarlyResponse(() => userMeta.handleLoginToken) :: Nil

}
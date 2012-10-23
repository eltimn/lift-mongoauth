/*******************************************************************************
 * <copyright file="Locs.scala">
 * Copyright (c) 2011 - 2012. Heirko
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Heirko and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Heirko
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Heirko.
 *
 * Heirko tout droit réservé -- Tout information contenue ici est la propriété
 * de la société Heirko.
 *
 * </copyright>
 *
 * <author>Alexandre Richonnier</author>
 * <lastUpdate>23/10/12 23:59</lastUpdate>
 ******************************************************************************/

package net.liftmodules.mongoauth

import net.liftweb._
import common._
import common.Full
import common.Full
import http._
import sitemap.Loc._
import sitemap.Loc.EarlyResponse
import sitemap.Loc.If
import sitemap.{Loc, Menu}
import util.Props

object Locs extends Locs
trait Locs {
  lazy val userMeta = MongoAuth.authUserMeta.vend

  private lazy val indexUrl = MongoAuth.indexUrl.vend
  private lazy val loginUrl = MongoAuth.loginUrl.vend
  private lazy val logoutUrl = MongoAuth.logoutUrl.vend
  private lazy val loginTokenUrl = MongoAuth.loginTokenUrl.vend

  // redirects
  def RedirectToLoginWithReferrer = {
    val uri = S.uriAndQueryString
    RedirectWithState(loginUrl, RedirectState(() => { LoginRedirect.set(uri) }))
  }

  def RedirectToIndex = RedirectResponse(indexUrl)
  def RedirectToIndexWithCookies = RedirectResponse(indexUrl, S.responseCookies:_*)

  protected def DisplayError(message: String) = () =>
    RedirectWithState(indexUrl, RedirectState(() => S.error(message)))

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

  /**
   * Always show in menu but redirect if false
   */
  val TestAccessLoggedIn = EarlyResponse(
    () => {
      if (userMeta.isLoggedIn)
        Empty
    else Full(RedirectToLoginWithReferrer)
    })

  val RequireNotLoggedIn = If(
    () => !userMeta.isLoggedIn,
    () => RedirectToIndex)

  def HasRole(role: String) =
    If(() => userMeta.hasRole(role),
      DisplayError("You are the wrong role to access that resource."))

  def HasRoleAdmin() =
    If(() => userMeta.hasRole("admin"),
      Props.mode match {
        case Props.RunModes.Development =>  DisplayError("You are the wrong role to access that resource.")
        case _ => () => NotFoundResponse()
      }
    )

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
    "LoginToken", loginTokenLocParams
  ))

  protected def loginTokenLocParams = RequireNotLoggedIn ::
    EarlyResponse(() => userMeta.handleLoginToken) :: Nil

}

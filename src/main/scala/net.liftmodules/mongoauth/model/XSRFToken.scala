/*******************************************************************************
 * <copyright file="XSRFToken.scala">
 * Copyright (c) 2011 - 2013. Heirko
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
 * <lastUpdate>25/04/13 00:43</lastUpdate>
 ******************************************************************************/

package net.liftmodules.mongoauth.model

 import net.liftweb.common._
import net.liftweb.http._
import js.JE.JsRaw
import js.JsCmds.{Run, Script}
import provider.HTTPCookie
import xml.NodeSeq
import net.liftmodules.mongoauth.MongoAuth
import net.liftweb.util.Helpers

object XSRFToken extends Loggable {
  private object curUserXSRFToken extends SessionVar[String](Helpers.nextFuncName)
  private val cookieName = MongoAuth.xsrfTokenCookieName.vend
  private val tokenHeaderName = MongoAuth.xsrfTokenHeaderName.vend

  /**
   * Add token to response
   */
  def setToken() {
    deleteCookie() // make sure existing cookie is removed
    val cookie = HTTPCookie(cookieName, curUserXSRFToken)
      .setDomain(ExtSession.cookieDomain)
      .setMaxAge(ExtSession.whenExpires.toPeriod.toStandardSeconds.getSeconds)
      .setPath(ExtSession.cookiePath)
    S.addCookie(cookie)
  }


  // delete the xsrf cookie
  private def deleteCookie() {
    for (cook <- S.findCookie(cookieName)) {
      S.deleteCookie(cookieName)
      logger.debug("S.deleteCookie XSRFToken called.")
    }
  }

  def jQueryAjaxTokenJs : NodeSeq => NodeSeq = ns => {
      Script(Run(JsRaw("""
        function getCookie (c_name) {
          var c_value = document.cookie;
          var c_start = c_value.indexOf(" " + c_name + "=");
          if (c_start == -1) {
            c_start = c_value.indexOf(c_name + "=");
          }
          if (c_start == -1) {
            c_value = null;
          }
          else {
            c_start = c_value.indexOf("=", c_start) + 1;
            var c_end = c_value.indexOf(";", c_start);
            if (c_end == -1) {
              c_end = c_value.length;
            }
            c_value = unescape(c_value.substring(c_start, c_end));
          }
          return c_value;
        }

    var xsrfValue = getCookie( """" + cookieName + """")

    $(document).ajaxSend(function(event, jqxhr, settings) {
      if (settings.type === "POST") {
        jqxhr.setRequestHeader( '""" + tokenHeaderName + """', xsrfValue);
     }
    });""").toJsCmd) )
  }


  /**
   * true if http request header has a good token id
   * @return
   */
  def isValidReq: Boolean = {
   val receptToken = S.getRequestHeader(tokenHeaderName).openOr(return false)
   receptToken == curUserXSRFToken.get
  }
}
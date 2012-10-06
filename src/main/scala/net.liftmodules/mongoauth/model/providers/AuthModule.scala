/*******************************************************************************
 * <copyright file="AuthModule.scala">
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
 * <lastUpdate>06/10/12 05:28</lastUpdate>
 ******************************************************************************/

package net.liftmodules.mongoauth
package model
package providers

import net.liftweb.common.{Full, Logger, Box, Empty}


/**
 * Heirko project
 * <creator>Alexandre Richonnier</creator>
 * <creationDate>20/04/12 13:28</creationDate>
 */

object AuthModuleType extends Enumeration {
  type AuthModuleType = Value
  val OpenID, UserPwd, OmniAuth = Value
}

trait AuthModule extends Logger  {
  def moduleName: AuthModuleType.AuthModuleType
  def isDefault = false
  def performInit(): Unit
}

object AuthModules extends  Logger {

  private var modules: Map[AuthModuleType.AuthModuleType, AuthModule] = Map()

  def register(module: AuthModule) {
    modules += (module.moduleName -> module)
    if (module.isDefault && defAuth.isEmpty) defAuth = Full(module)
    module.performInit()
  }


  private var defAuth: Box[AuthModule] = Empty

  //def defaultAuthModule: AuthModule = defAuth.open_!
}



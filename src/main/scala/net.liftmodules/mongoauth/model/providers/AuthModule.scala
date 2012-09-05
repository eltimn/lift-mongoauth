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

  def defaultAuthModule: AuthModule = defAuth.open_!
}



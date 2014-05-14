# Lift MongoAuth Module

Authentication and Authorization module for Lift-MongoDB-Record.

# Installation

Jars are available via the oss.sonatype.org repo.

For *Lift 2.5.x* (Scala 2.9 and 2.10):

    libraryDependencies += "net.liftmodules" %% "mongoauth_2.5" % "0.5"

For *Lift <= 2.6-M3* (Scala 2.9 and Scala 2.10):

    libraryDependencies += "net.liftmodules" %% "mongoauth_2.6" % "0.5"

For *Lift 2.6-SNAPSHOT* (Scala 2.10):

    libraryDependencies += "net.liftmodules" %% "mongoauth_2.6" % "0.6-SNAPSHOT"

For *Lift 3.0.x* (Scala 2.10):

    libraryDependencies += "net.liftmodules" %% "mongoauth_3.0" % "0.6-SNAPSHOT"


# Configuration

You must set the __MongoAuth.authUserMeta__ object that you will be using (see below). Most likely in boot:

    // init mongoauth
    MongoAuth.authUserMeta.default.set(User)
    MongoAuth.indexUrl.default.set(Sitemap.home.path)

See _MongoAuth_ for other settings that can be overriden.

You will also probably want to add the logout and login-token menus.

    LiftRules.setSiteMap(SiteMap(List(
      Locs.buildLogoutMenu,
      Locs.buildLoginTokenMenu
    ) :_*))

# Creating a User Data Model

This module provides several traits for constructing user model classes, which include roles and permissions.

There are several ways you can utilize this module:

## SimpleUser

_model.SimpleUser_ is a fully implemented user model, but is not extensible in any way. This is only useful for testing and demos.
This shows what is necessary to create a user from _ProtoAuthUser_.

## ProtoAuthUser

_ProtoAuthUser_ and _ProtoAuthUserMeta_ are a pair of traits that can be used to build a user model class and meta object.
_ProtoAuthUser_ has some standard fields. You can add
fields to it, but you can't modify the ones provided. This is a good place to start. If you find you need to modify
the provided fields, you can copy and paste them into your user class and use _MongoAuthUser_.

## MongoAuthUser

_MongoAuthUser_ is a trait for defining a MongoRecord of _AuthUser_ (provides authorization functionality).
This can be used to build a user class from scratch. It only requires id and email fields.

## ProtoAuthUserMeta

_ProtoAuthUserMeta_ is a combination of _AuthUserMeta_ and _UserLifeCycle_ traits. These provide authorization
functionality and login/logout functionality for MongoMetaRecord objects. No matter which version you use for the
MongoRecord user class, you can use this trait to define your MongoMetaRecord, if it provides sufficient functionality.

"Remember Me" functionality is provided by _ExtSession_.

_LoginToken_ provides a way for users that forgot their password to log in and change it. Users are sent a link with a token (an ObjectId)
on the url. When they click on it they can be handled appropriately. The implementation is left up to you.

# Roles and Permissions

Permissions are defined using a simple case class. They have three parts; domain, actions, entities. This was heavily
influenced by [Apache Shiro's](http://shiro.apache.org/) WildcardPermission.
Please see the [JavaDoc for WildcardPermission](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/authz/permission/WildcardPermission.html)
for detailed information.

See [PermissionSpec](https://github.com/eltimn/lift-mongoauth/blob/master/src/test/scala/net.liftmodules/mongoauth/PermissionSpec.scala) for examples.

PermissionListField provides a way to store permissions for a user. It stores them as a list of strings.

Example:

    user.permissions(List(Permission("printer", "print"), Permission("user", "edit", "123")))

    assert(User.hasPermission(Permission("printer", "manage")) == false)

Role is a MongoRecord that provides a way to group a set of permissions. A user's full set of permissions is calculated using the permissions
from any roles assigned to them and the individual permissions assigned to them. There are also LocParams as well as the User-Meta-Singleton that can be used to check for roles.

Example:

    val superuser = Role.createRecord.id("superuser").permissions(List(Permission.all)).save

    user.roles(List("superuser"))

    assert(User.hasRole("superuser")) == true)
    assert(User.lacksRole("superuser")) == false)
    assert(User.lacksRole("admin")) == true)


# SiteMap LocParams

The _Locs_ trait and companion object provide some useful _LocParams_ that use can use when defing your _SiteMap_.

This code was inspired by the [lift-shiro](https://github.com/timperrett/lift-shiro) module.

Examples:

    Meun.i("Settings") / "settings" >> RequireLoggedIn
    Meun.i("Password") / "password" >> RequireAuthentication
    Meun.i("Admin") / "admin" >> HasRole("admin")
    Meun.i("EditEntitiy") / "admin" / "entity" >> HasPermission(Permission("entity", "edit"))


"Authenticated" means the user logged in by supplying their password. "Logged In" means the user was logged in by either
an ExtSession or LoginToken, or they are Authenticated.

# Localization

A default localization is provided and can be found [here](https://github.com/jonoabroad/lift-mongoauth/tree/master/src/main/resources/toserve/mongoauth.resources.html). If you require another language or would prefer different text, copy the default and subtitute your values. See the [Localization](https://www.assembla.com/spaces/liftweb/wiki/Localization) page on the Liftweb wiki for more information.


# Example Implementation

The [lift-mongo](https://github.com/eltimn/lift-mongo.g8) giter8 template provides a fully functioning implementation of a basic user system.

# License

Apache v2.0. See LICENSE.txt


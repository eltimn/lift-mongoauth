# Lift MongoAuth Module

Authentication and Authorization module for Lift-MongoDB-Record.


# Creating a User Data Model

This module provides several traits for constructing user model classes, which includes roles and permissions.

There are several ways you can utilize this module:

## SimpleUser

_model.SimpleUser_ is a fully implemented user model, but is not extensible in any way. This is only useful for testing and demos.

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

## Roles and Permissions

Permissions are defined using a simple case class. They have three parts; domain, actions, entities. This was heavily
influenced by [Apache Shiro's](http://shiro.apache.org/ WildcardPermission).
Please see the [JavaDoc for WildcardPermission](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/authz/permission/WildcardPermission.html)
for detailed information.

Examples:

    val printer = Permission("printer")

    assert(printer.implies(Permission.all) == true)
    assert(printer.implies(Permission.none) == false)
    assert(printer.implies(Permission("printer")) == true)
    assert(printer.implies(Permission("a")) == false)

Role


## Sitemap LocParams


## Acknowledgments


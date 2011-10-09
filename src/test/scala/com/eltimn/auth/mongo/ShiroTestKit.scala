package com.eltimn
package auth.mongo

import org.apache.shiro.{SecurityUtils, UnavailableSecurityManagerException}
import org.apache.shiro.config.IniSecurityManagerFactory
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.support.SubjectThreadState
import org.apache.shiro.util.LifecycleUtils
import org.apache.shiro.util.ThreadState

import org.specs.Specification

trait ShiroTestKit {
  this: Specification =>

  private var subjectThreadState: ThreadState = _

  protected def securityManager: SecurityManager = SecurityUtils.getSecurityManager
  protected def subject: Subject = SecurityUtils.getSubject

  protected def setSubject(subject: Subject) {
    clearSubject()
    subjectThreadState = createThreadState(subject)
    subjectThreadState.bind()
  }

  protected def clearSubject() {
    doClearSubject()
  }

  protected def createThreadState(subject: Subject): ThreadState =
    new SubjectThreadState(subject)

  private def doClearSubject() {
    if (subjectThreadState != null) {
      subjectThreadState.clear()
      subjectThreadState = null
    }
  }

  protected def setSecurityManager(securityManager: SecurityManager) {
    SecurityUtils.setSecurityManager(securityManager)
  }

  doBeforeSpec {
    // set the SecurityManager
    val factory = new IniSecurityManagerFactory("classpath:shiro.ini")
    setSecurityManager(factory.getInstance)
  }

  doAfterSpec {
    doClearSubject()
    try {
      LifecycleUtils.destroy(securityManager)
    } catch {
      case e: UnavailableSecurityManagerException =>
        //we don't care about this when cleaning up the test environment
        //(for example, maybe the subclass is a unit test and it didn't
        // need a SecurityManager instance because it was using only
        // mock Subject instances)
    }
    setSecurityManager(null)
  }
}

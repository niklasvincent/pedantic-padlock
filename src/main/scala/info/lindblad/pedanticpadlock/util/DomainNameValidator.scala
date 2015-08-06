package info.lindblad.pedanticpadlock.util

import info.lindblad.pedanticpadlock.bootstrap.Configuration
import org.apache.commons.validator.routines.DomainValidator

import collection.JavaConversions._

object DomainNameValidator {

  def isValid(domainName: String, approvedDomains: Seq[String]): Boolean = {
    DomainValidator.getInstance.isValid(domainName) &&  approvedDomains.exists(approvedDomain =>
        {domainName == approvedDomain || domainName.endsWith(approvedDomain)}
    )
  }

  def validate(domainName: String, approvedDomains: Seq[String]): Option[String] = {
    isValid(domainName, approvedDomains) match {
      case true => Some(domainName)
      case false => None
    }
  }

  def apply(domainName: String): Option[String] = {
    validate(domainName, Configuration.values.getStringList("approved-domains"))
  }

 }

import info.lindblad.pedanticpadlock.util.DomainNameValidator
import org.scalatest.{Matchers, FlatSpec}

class DomainNameValidatorSpec extends FlatSpec with Matchers {

  it should "not accept invalid domain name" in {
    DomainNameValidator.validate("invalid-#£@domain", Seq("valid-top-domain.com")) should be (None)
  }

  it should "not accept domain name not in approved domain" in {
    DomainNameValidator.validate("valid-domain.com", Seq("valid-top-domain.com")) should be (None)
  }

  it should "accept valid domain name in approved domains" in {
    DomainNameValidator.validate("valid-domain.com", Seq("valid-domain.com")) should be (Some("valid-domain.com"))
  }

  it should "accept valid sub domain name of domain in approved domains" in {
    DomainNameValidator.validate("sub.valid-domain.com", Seq("valid-domain.com")) should be (Some("sub.valid-domain.com"))
  }

  it should "accept valid sub domain name of domain amongst multiple approved domains" in {
    DomainNameValidator.validate("sub.valid-domain.com", Seq("example.com", "somedomain.com", "valid-domain.com")) should be (Some("sub.valid-domain.com"))
  }

}
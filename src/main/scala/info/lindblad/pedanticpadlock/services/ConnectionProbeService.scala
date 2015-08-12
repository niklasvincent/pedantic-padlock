package info.lindblad.pedanticpadlock.services

import java.security.cert.X509Certificate
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.net.ssl.{SSLSession, SSLSocket, SSLSocketFactory, SSLContext}

import info.lindblad.pedanticpadlock.model.ConnectionProbeResult

object ConnectionProbeService {

  def probe(domainName: String): ConnectionProbeResult = {
    try {
      val sc: SSLContext = SSLContext.getInstance("TLS")
      sc.init(null, null, null)
      val ssf: SSLSocketFactory = sc.getSocketFactory
      val s: SSLSocket = ssf.createSocket(domainName, 443).asInstanceOf[SSLSocket]
      s.startHandshake
      val session: SSLSession = s.getSession
      val servercerts = session.getPeerCertificates
      val xc: X509Certificate = servercerts(0).asInstanceOf[X509Certificate]
      val expirationDate: Date = xc.getNotAfter
      val today: Date = new Date
      val millisecondsUntilExpiration: Long = expirationDate.getTime - today.getTime
      val daysUntilExpiration: Long = TimeUnit.DAYS.convert(millisecondsUntilExpiration, TimeUnit.MILLISECONDS)
      return ConnectionProbeResult(canConnect = true, daysUntilExpiration = Some(daysUntilExpiration))
    }
    catch {
      case e: Exception => {
      }
    }
    return ConnectionProbeResult(canConnect = false, daysUntilExpiration = None)
  }

}

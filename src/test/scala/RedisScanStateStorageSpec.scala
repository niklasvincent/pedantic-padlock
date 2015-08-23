import com.redis.RedisClient
import info.lindblad.pedanticpadlock.model.{ScanReport, AwaitingResult, NotProcessed, RedisScanStateStorage}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import redis.embedded.RedisServer

class RedisScanStateStorageSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  val redisServer = new RedisServer(16379)

  override def beforeAll() = {
    redisServer.start()
    super.beforeAll()
  }

  override def afterAll() = {
    redisClient.flushall
    redisServer.stop()
    super.afterAll()
  }

  def redisClient: RedisClient = {
    val client = new RedisClient("127.0.0.1", 16379)
    client.flushall
    client
  }

  it should "add new domain names to the database if it does not already exist" in {
    val client = redisClient
    val scanStateStorage = new RedisScanStateStorage(client)
    scanStateStorage.add("example.com")
    val allEntries = scanStateStorage.getAll
    allEntries.head._1 should be ("example.com")
    allEntries.head._2.isInstanceOf[NotProcessed] should be (true)
  }

  it should "update entry in database if domain name already exists" in {
    val client = redisClient
    val scanStateStorage = new RedisScanStateStorage(client)
    scanStateStorage.add("example.com")
    scanStateStorage.put("example.com", new AwaitingResult("example.com", ScanReport(canConnect = true)))
    val allEntries = scanStateStorage.getAll
    allEntries.head._1 should be ("example.com")
    allEntries.head._2.isInstanceOf[AwaitingResult] should be (true)
  }

}
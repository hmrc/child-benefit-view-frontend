package repositories

import base.BaseSpec
import config.FrontendAppConfig
import models.UserAnswers
import org.mockito.Mockito.when
import org.mongodb.scala.bson.{BsonInt64, BsonString}
import org.mongodb.scala.model.Filters
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext

class SessionRepositorySpec extends BaseSpec with DefaultPlayMongoRepositorySupport[UserAnswers] with BeforeAndAfterEach {
  val mockConfig = mock[FrontendAppConfig]
  val ttlSeconds = 123
  when(mockConfig.cacheTtl).thenReturn(ttlSeconds)

  val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  val stubClock = Clock.fixed(instant, ZoneId.systemDefault)

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override lazy val repository = new SessionRepository(mongoComponent, mockConfig, stubClock)

  override protected def beforeEach() = {
    deleteAll()
  }

  "SessionRepository" - {
    "Indices:" - {
      "SHOULD have an ascending index on the lastUpdated field" in {
        val indices = {
          await(repository.ensureIndexes())
          await(repository.collection.listIndexes().toFuture())
        }
        val lastUpdatedIndex = indices.find(_.get("name").contains(BsonString("lastUpdatedIdx")))

        lastUpdatedIndex.get("key").toString mustBe """{"lastUpdated": 1}"""
        lastUpdatedIndex.get("expireAfterSeconds") mustBe BsonInt64(ttlSeconds)
      }
    }
    "keepAlive" - {
      "GIVEN a record already exists in the collection" - {
        "WHEN keepAlive is called"- {
          "THEN lastUpdated is set to 'now' and the record updated" in {
            await(insert(userAnswers))
            val expectedResult = userAnswers.copy(lastUpdated = instant)

            whenReady(repository.keepAlive(userAnswers.id)) { result =>
              result mustBe true
            }
            whenReady(find(Filters.equal("_id", userAnswers.id))) { result =>
              result.headOption mustBe Some(expectedResult)
            }
          }
        }
      }
    }
    "get" - {
      "GIVEN a record with the requested id already exists in the collection" - {
        "THEN this record is returned" in {
          await(insert(userAnswers))

          whenReady(repository.get(userAnswers.id)) { result =>
            result mustBe Some(userAnswers.copy(lastUpdated = instant))
          }
        }
      }
      "GIVEN a record with the request id does not exist in the collection"- {
        "THEN None is returned" in {
          whenReady(repository.get(userAnswers.id)) { result =>
            result mustBe None
          }
        }
      }
    }
    "set" - {
      "GIVEN set is called with a UserAnswers" - {
        "THEN the record is saved AND the last updated time is set the 'now'" in {
          val expectedResult = userAnswers.copy(lastUpdated = instant)

          whenReady(repository.set(userAnswers)) { result =>
            result mustBe true
          }
          whenReady(find(Filters.equal("_id", userAnswers.id))) { result =>
            result.headOption.value mustBe expectedResult
          }
        }
        "AND set is called again with a second set of UserAnswers with the same Id" - {
          "THEN the value is upserted and replaces the original" in {
            repository.set(userAnswers)
            whenReady(find(Filters.equal("_id", userAnswers.id))) { result =>
              result.headOption mustBe Some(userAnswers.copy(lastUpdated = instant))
            }

            val newAnswers = UserAnswers(userAnswers.id, Json.obj("new" -> "value"), instant)
            whenReady(repository.set(newAnswers)) { result =>
              result mustBe true
            }
            whenReady(find(Filters.equal("_id", userAnswers.id))) { result =>
              result.headOption.value mustBe newAnswers
            }
          }
        }
      }
    }
    "clear" - {
      "GIVEN a record with the requested id already exists in the collection" - {
        "THEN this record is removed and true is returned" in {
          await(insert(userAnswers))
          whenReady(find(Filters.equal("_id", userAnswers.id))) { result =>
            result.headOption mustBe Some(userAnswers)
          }
          whenReady(repository.clear(userAnswers.id)) { result =>
            result mustBe true
          }
          whenReady(find(Filters.equal("_id", userAnswers.id))) { result =>
            result.headOption mustBe None
          }
        }
      }
      "GIVEN a record with the request id does not exist in the collection" - {
        "THEN true is still returned and though collection remain unchanged" in {
          val userAnswersA = userAnswers.copy(id = "idA")
          val userAnswersB = userAnswers.copy(id = "idB")
          val userAnswersC = userAnswers.copy(id = "idC")
          await(insert(userAnswersA))
          await(insert(userAnswersB))
          await(insert(userAnswersC))
          val expectedRecords = Seq(userAnswersA, userAnswersB, userAnswersC)
          whenReady(findAll()) { collection =>
            collection mustBe expectedRecords
          }

          whenReady(repository.clear(userAnswers.id)) { result =>
            result mustBe true
          }

          whenReady(findAll()) { collection =>
            collection mustBe expectedRecords
          }
        }
      }
    }
  }
}

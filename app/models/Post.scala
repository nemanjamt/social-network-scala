package models

import play.api.libs.json.{Format, Json}
import java.time.LocalDateTime
case class Post(
    id: Long,
    userId: Long,
    content: String,
    countLike: Int,
    deleted: Boolean,
    createDate: LocalDateTime,
    updateDate: Option[LocalDateTime]
)

object Post {
  implicit val postFormat: Format[Post] = Json.format[Post]
}

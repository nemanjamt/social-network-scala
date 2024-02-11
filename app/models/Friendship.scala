package models

import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

case class Friendship(id: Long, firstUserId: Long, secondUserId: Long, createdDate: LocalDateTime)

object Friendship {
  implicit val friendshipFormat: Format[Friendship] = Json.format[Friendship]
}

package models

import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

case class FriendshipRequest(id: Long, senderId: Long, receiverId: Long, createdDate: LocalDateTime)

object FriendshipRequest {
  implicit val friendshipRequestFormat: Format[FriendshipRequest] = Json.format[FriendshipRequest]
}

package dto

import play.api.libs.json.{Json, Reads}

case class NewFriendshipRequest(receiverId: Long, senderId: Long)

object NewFriendshipRequest {
  implicit val newPostReads: Reads[NewFriendshipRequest] = Json.reads[NewFriendshipRequest]
}

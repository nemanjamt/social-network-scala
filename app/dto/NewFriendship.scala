package dto

import play.api.libs.json.{Json, Reads}

case class NewFriendship(firstUserId: Long, secondUserId: Long)

object NewFriendship {
  implicit val newFriendshipReads: Reads[NewFriendship] = Json.reads[NewFriendship]
}

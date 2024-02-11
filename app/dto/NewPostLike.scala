package dto

import play.api.libs.json.{Json, Reads}

case class NewPostLike(userId: Long, postId: Long)

object NewPostLike {
  implicit val newLikeReads: Reads[NewPostLike] = Json.reads[NewPostLike]
}

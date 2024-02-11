package models

import play.api.libs.json.{Format, Json}

case class PostLike(id: Long, userId: Long, postId: Long)

object PostLike {
  implicit val postLikeFormat: Format[PostLike] = Json.format[PostLike]
}

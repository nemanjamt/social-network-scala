package dto

import play.api.libs.json.{Json, Reads}

case class NewPost(content: String, userId: Long)

object NewPost {
  implicit val newPostReads: Reads[NewPost] = Json.reads[NewPost]
}

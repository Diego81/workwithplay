package helpers

object TextHelper {

  def shortenText(text: String, maxLenght: Int = 25) = {
    text match {
      case someText if text.length > maxLenght => truncateText(text, maxLenght)
      case _ => text
    }
  }

  private def truncateText(text: String, maxLength: Int) = {
    val truncated = text.substring(0, maxLength)
    val withoutCutWords = if ((truncated.charAt(truncated.length - 1).toString != " ") && (text.charAt(maxLength).toString != " ")) {
      truncated.substring(0, truncated.lastIndexOf(" "))
    } else {
      truncated
    }
    withoutCutWords.trim + " ..."
  }
}
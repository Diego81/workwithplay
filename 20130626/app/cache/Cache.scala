package cache

trait Cache[T] {

  def getOrFetch(key: String)(fetch: () => T): T = {
    play.api.Logger.debug("Default empty cache always invoking the callback...")
    fetch()
  }

  def clear(key: String) {}
}
trait MapCache[T] extends Cache[T] {

  private var cache: Map[String, T] = Map()

  override def getOrFetch(key: String)(fetch: () => T): T = {
    cache.get(key) match {
      case Some(value) => {
        play.api.Logger.debug("getting value from cache ...")
        value
      }
      case None => {
        play.api.Logger.debug("invoking fetch callback ...")
        val value = fetch()
        cache = cache + (key -> value)
        cache.get(key).get
      }
    }
  }

  override def clear(key: String) {
    cache = Map()
  }

}

trait SingleElementCache[T] extends Cache[T] {
  private var cacheKey: String = null
  private var cacheValue: T = null.asInstanceOf[T]

  override def getOrFetch(key: String)(fetch: () => T): T = {
    if (cacheKey == key) {
      play.api.Logger.debug("single element cache hit ...")
      cacheValue
    } else {
      play.api.Logger.debug("single element cache miss, invoking the callback ...")
      val result = fetch()
      cacheValue = result
      cacheKey = key
      result
    }
  }

  override def clear(key: String) = {
    cacheKey = null
  }
}

trait ExpiryCache[T] extends Cache[T] {

  val timeout: Long = 5000

  var lastAccess: Map[String, Long] = Map()

  override def getOrFetch(key: String)(fetch: () => T): T = {
    if (lastAccess.contains(key) && System.currentTimeMillis - lastAccess(key) > timeout) {
      play.api.Logger.debug("Timeout expired, clearing the cache ...")
      clear(key)
    }
    lastAccess = lastAccess + (key -> System.currentTimeMillis)
    super.getOrFetch(key)(fetch)
  }
}

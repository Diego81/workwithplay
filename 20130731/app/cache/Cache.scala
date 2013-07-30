package cache

/** A cache allows to store data so that future accesses can be faster */
trait Cache[T] {
  /** 
   *  Returns the element with the specified key.
   *  If the element is not found in the cache, a callback is invoked to fetch it.
   *  
   *  @param key - the key of the element to fetch
   *  @param fetch - the callback to fetch the element if not found in the cache
   *  @return T - the element with the specified key
   */
  def getOrFetch(key: String)(fetch: () => T): T
  
  /**
   * Removes the element with the specified key from the cache.
   * 
   * @param key - the key of the element to remove
   */
  def clear(key: String)
}

trait EmptyCache[T] extends Cache[T]{

  override def getOrFetch(key: String)(fetch: () => T): T = {
    play.api.Logger.debug("Default empty cache always invoking the callback...")
    fetch()
  }

  override def clear(key: String) {}
}

trait MapCache[T] extends EmptyCache[T] {

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

trait NotRelatedTrait {
  def clear(key: String) {
    play.api.Logger.debug("Not related trait implementation ...")
  }
}

trait SingleElementCache[T] extends EmptyCache[T] {
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

  override def clear(key: String) {
    cacheKey = null
  }
}

trait ExpiryCache[T] extends EmptyCache[T] {

  val expirationTimeout: Int
  
  lazy val timeout: Long = expirationTimeout * 1000

  var lastAccess: Map[String, Long] = Map()
  
  private def init {
    play.api.Logger.debug("Init function. Timeout is " + timeout)
  }
  init
  
  override def getOrFetch(key: String)(fetch: () => T): T = {
    if (lastAccess.contains(key) && System.currentTimeMillis - lastAccess(key) > timeout) {
      play.api.Logger.debug("Entry older then " + timeout + " milliseconds. Timeout expired, clearing the cache ...")
      clear(key)
    }
    lastAccess = lastAccess + (key -> System.currentTimeMillis)
    super.getOrFetch(key)(fetch)
  }
}
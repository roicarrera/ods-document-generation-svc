package org.jooby

import org.jooby.Jooby
import org.jooby.Route

/** Example on how to hack Groovy so we can use groovy closure on script routes. */
class JoobyExtension {

  private static Route.Filter toHandler(Closure closure) {
    if (closure.maximumNumberOfParameters == 0) {
      Route.ZeroArgHandler handler = { closure() }
      return handler
    } else if (closure.maximumNumberOfParameters == 1) {
      Route.OneArgHandler handler = { req -> closure(req) }
      return handler
    } else if (closure.maximumNumberOfParameters == 2) {
      Route.Handler handler = { req, rsp -> closure(req, rsp) }
      return handler
    }

    Route.Filter handler = { req, rsp, chain -> closure(req, rsp, chain) }
    return handler
  }

  static Route.Definition get(Jooby self, String pattern, Closure closure) {
    return self.get(pattern, toHandler(closure));
  }

  static Route.Definition post(Jooby self, String pattern, Closure closure) {
    return self.post(pattern, toHandler(closure));
  }
}

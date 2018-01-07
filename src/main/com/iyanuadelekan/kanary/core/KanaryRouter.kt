/**
 * @author Iyanu Adelekan on 23/05/2017.
 */

package com.iyanuadelekan.kanary.core

import com.iyanuadelekan.kanary.interfaces.RouterInterface
import com.iyanuadelekan.kanary.libs.RouteList
import com.iyanuadelekan.kanary.constants.HttpConstants
import com.iyanuadelekan.kanary.exceptions.InvalidRouteException
import org.eclipse.jetty.server.Request
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @property basePath the root path of mapped HTTP request
 * @property routeController the controller used to handle routes within the [KanaryRouter]
 * @constructor Initializes KanaryRouter object
 */
class KanaryRouter(var basePath: String?= null, var routeController: KanaryController?= null): RouterInterface {

    val getRouteList: RouteList = RouteList()
    val postRouteList: RouteList = RouteList()
    val putRouteList: RouteList = RouteList()
    val patchRouteList: RouteList = RouteList()
    val deleteRouteList: RouteList = RouteList()
    val optionsRouteList: RouteList = RouteList()

    /**
     * Router function handling GET requests
     * @param path Specified route path
     * @param action Action to handle requests targeting specified route path
     * @param controller Controller handling the route
     * @return current instance of [KanaryRouter]
     */
    override fun get(path: String, action: (Request, HttpServletRequest, HttpServletResponse) -> Unit, controller: KanaryController?): KanaryRouter {
        assembleAndQueueRoute(HttpConstants.GET.name, path, action, controller)
        return this
    }

    /**
     * Router function handling POST requests
     * @param path Specified route path
     * @param action Action to handle requests targeting specified route path
     * @param controller Controller handling the route
     * @return current instance of [KanaryRouter]
     */
    override fun post(path: String, action: (Request, HttpServletRequest, HttpServletResponse) -> Unit, controller: KanaryController?): KanaryRouter {
        assembleAndQueueRoute(HttpConstants.POST.name, path, action, controller)
        return this
    }

    /**
     * Router function handling DELETE requests
     * @param path Specified route path
     * @param action Action to handle requests targeting specified route path
     * @param controller Controller handling the route
     * @return current instance of [KanaryRouter]
     */
    override fun delete(path: String, action: (Request, HttpServletRequest, HttpServletResponse) -> Unit, controller: KanaryController?): KanaryRouter {
        assembleAndQueueRoute(HttpConstants.DELETE.name, path, action, controller)
        return this
    }

    /**
     * Router function handling PUT requests
     * @param path Specified route path
     * @param action Action to handle requests targeting specified route path
     * @param controller Controller handling the route
     * @return current instance of [KanaryRouter]
     */
    override fun put(path: String, action: (Request, HttpServletRequest, HttpServletResponse) -> Unit, controller: KanaryController?): KanaryRouter {
        assembleAndQueueRoute(HttpConstants.GET.name, path, action, controller)
        return this
    }

    /**
     * Router function handling PATCH requests
     * @param path Specified route path
     * @param action Action to handle requests targeting specified route path
     * @param controller Controller handling the route
     * @return current instance of [KanaryRouter]
     */
    override fun patch(path: String, action: (Request, HttpServletRequest, HttpServletResponse) -> Unit, controller: KanaryController?): KanaryRouter {
        assembleAndQueueRoute(HttpConstants.PATCH.name, path, action, controller)
        return this
    }

    /**
     * Router function handling PATCH requests
     * @param path Specified route path
     * @param action Action to handle requests targeting specified route path
     * @param controller Controller handling the route
     * @return current instance of [KanaryRouter]
     */
    override fun options(path: String, action: (Request, HttpServletRequest, HttpServletResponse) -> Unit, controller: KanaryController?): KanaryRouter {
        assembleAndQueueRoute(HttpConstants.OPTIONS.name, path, action, controller)
        return this
    }

    /**
     * Function used for route queuing
     * adds a [route] based on its [method] to its appropriate route list
     * @param method HTTP method handled by route
     * @param route Instance of [Route]
     */
    private fun enqueueRoute(method: String, route: Route) {
        when(method) {
            HttpConstants.GET.name -> getRouteList.add(route)
            HttpConstants.POST.name -> postRouteList.add(route)
            HttpConstants.PUT.name -> putRouteList.add(route)
            HttpConstants.DELETE.name -> deleteRouteList.add(route)
            HttpConstants.PATCH.name -> patchRouteList.add(route)
            HttpConstants.OPTIONS.name -> optionsRouteList.add(route)
            else -> {
                println("Unrecognized HTTP method: '$method'")
            }
        }
    }

    /**
     * Used to set the base path for a set of routes
     * sets [basePath] to [path]
     * @param path Specified route path
     * @return current instance of [KanaryRouter]
     */
    infix fun on(path: String): KanaryRouter {
        if(path != "/" && isRoutePathValid(path)) {
            val formattedPath = formatPath(path)
            basePath = "/$formattedPath"
            return this
        }
        throw InvalidRouteException("The path '$path' is an invalid route path")
    }

    /**
     * Append / to a path, if necessary
     * Used to fix Issue #3 and keep it backwards compatible
     * @param path Route path with may not end with /
     * @return path that ends with /
     */
    private fun formatPath(path: String): String {
        if(path.endsWith("/")) {
            return path
        }
        return "$path/"
    }

    /**
     * Used to set the controller for a set of routes
     * sets [routeController] to [controller]
     * @param controller Specified controller
     * @return current instance of [KanaryRouter]
     */
    infix fun use(controller: KanaryController): KanaryRouter  {
        if(basePath != null) {
            this.routeController = controller
            return this
        }
        throw InvalidRouteException("Controller mount attempted without a set base path.")
    }

    /**
     * Tests if a route path is valid
     * @param path Specified route path
     * @return Boolean indicating if [path] is a valid route path
     */
    private fun isRoutePathValid(path: String): Boolean {
        return Regex("(\\w+/)*\\w+/?") matches path
    }

    /**
     * Prepends the current [basePath] to a [path]
     * @param path Specified route path
     * @return prepended path
     */
    private fun prependBasePath(path: String): String {
        return "$basePath$path"
    }

    /**
     * Used to create and queue a route
     * @param method the HTTP method accepted by the route
     * @param path the HTTP request path
     * @param action reference to a controller action
     * @param controller controller routed to by router
     */
    private fun assembleAndQueueRoute(method: String, path: String,
                                      action: (Request, HttpServletRequest, HttpServletResponse) -> Unit,
                                      controller: KanaryController?) {
        if (isRoutePathValid(path)) {
            val formattedPath = formatPath(path)
            if(controller == null && routeController == null) {
                throw InvalidRouteException("Null controller for route '$method $formattedPath' is not allowed.")

            } else {
                if (controller != null) {
                    routeController = controller
                }
                if (basePath == null) {
                    this.enqueueRoute(method, Route(formattedPath, routeController, action))
                } else {
                    this.enqueueRoute(method, Route(prependBasePath(formattedPath), routeController, action))
                }
            }
        } else {
            throw InvalidRouteException("The path '$path' is an invalid route path")
        }

    }

}
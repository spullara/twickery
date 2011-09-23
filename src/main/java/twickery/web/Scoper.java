package twickery.web;

import javax.servlet.http.HttpServletRequest;

import com.sampullara.mustache.Scope;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 2:53 PM
* To change this template use File | Settings | File Templates.
*/
public interface Scoper<T> {
  Scope newScope(HttpServletRequest request, T t);
}

package twickery.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 2:53 PM
* To change this template use File | Settings | File Templates.
*/
public interface Handler<T> {
  void handle(HttpServletRequest request, HttpServletResponse response, T t) throws ServletException;
}

package twickery.web;

import javax.servlet.http.HttpServletRequest;

public interface Scoper<T> {
  Object newScope(HttpServletRequest request, T t);
}

package hq.myhome.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import hq.myhome.utils.Definition;

public class LoginCheckInterceptor implements HandlerInterceptor {
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Object user = request.getSession().getAttribute(Definition.SESSION_ATTR_KEY_USER);
		System.out.println("requestURL ===> " + request.getRequestURI());
		if (user == null) {
			request.getSession().setAttribute(Definition.SESSION_ATTR_KEY_ALERT_MSG, "登录超时!!");
			if (request.getRequestURI().contains("/back/")) {
				response.sendRedirect(request.getContextPath() + "/sessionOut.jsp");
			} else {
				response.sendRedirect(request.getContextPath() + "/");
			}
			return false;
		}
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}
}

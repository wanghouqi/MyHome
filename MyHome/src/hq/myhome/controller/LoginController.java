package hq.myhome.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import hq.mydb.condition.CondSetBean;
import hq.mydb.dao.BaseDAO;
import hq.mydb.data.FormVO;
import hq.mydb.data.TableVO;
import hq.myhome.utils.Definition;
import hq.myhome.utils.MyHomeHelper;

/**
 * 用户登录控制器,包括前台和后台
 * @author Administrator
 *
 */
@Controller // 声明当前为Controller
public class LoginController {
	private BaseDAO baseDao;

	@Autowired
	public LoginController(BaseDAO baseDao) {
		this.baseDao = baseDao;
	}

	/**
	 * 后台的登录相应事件
	 * @return
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String cLogin(@RequestParam("u") String userName, @RequestParam("p") String passwordString, HttpServletRequest request) {
		String redirect = "redirect:";
		CondSetBean csbUser = new CondSetBean();
		csbUser.addCondBean_equal("CN_LOGIN_NAME", userName);
		csbUser.addCondBean_and_equal("CR_ACTIVE_FLAG", Definition.YES);
		TableVO tvoUser = this.baseDao.queryForTableVO("tn_user", csbUser);
		if (tvoUser.size() ==1  && StringUtils.equals(MyHomeHelper.encryptPassword(passwordString), tvoUser.get(0).getCellVOValue("CN_PASSWORD"))) {
			FormVO fvoUser = tvoUser.get(0).transformToFormVO();
			request.getSession().setAttribute(Definition.SESSION_ATTR_KEY_USER, fvoUser);
			redirect += "/generaledger";// 进入总账页面
		} else {
			redirect += "/";// 返回登录页面
			request.getSession().setAttribute(Definition.SESSION_ATTR_KEY_ALERT_MSG, "用户名或密码错误!");
		}
		return redirect;
	}

	
	/**
	 * 前台的登出相应事件
	 * @return
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String userloginDo(HttpServletRequest request) {
		request.getSession().removeAttribute(Definition.SESSION_ATTR_KEY_USER);
		return "redirect:/";
	}
}

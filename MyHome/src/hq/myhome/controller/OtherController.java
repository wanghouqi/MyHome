package hq.myhome.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;

import hq.mydb.dao.BaseDAO;
import hq.mydb.data.RowVO;
import hq.mydb.data.TableVO;
import hq.mydb.utils.MyDBHelper;

/**
 * 创建处理主页以外的内容的控制权
 * @author Administrator
 *
 */
@Controller // 声明当前为Controller
@RequestMapping(value = "/other") // 制定基础URL
public class OtherController {

	private BaseDAO baseDAO;

	/**
	 * 加载BaseDAO
	 * @param baseDAO
	 */
	@Autowired
	public OtherController(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	/**
	 *	收入类型管理 - 列表页
	 * @return
	 */
	@RequestMapping(value = "/incomeType/list", method = RequestMethod.GET)
	public ModelAndView incomeTypeList(HttpServletRequest request) {
		try {
			TableVO tvoReturn = this.baseDAO.queryForTableVO("tn_income_type");
			for (RowVO rv : tvoReturn.toRowVOs()) {
				rv.get("CN_ID").setKey("id");
				rv.get("CN_NAME").setKey("name");
				rv.get("CR_PERIODIC_FLAG").setKey("periodicFlag");
				rv.get("CN_EFFECTIVE_DAY").setKey("effectiveDay");
				rv.get("CN_AMOUNT").setKey("amount");
				rv.get("CN_FROZEN_FLAG").setKey("frozenFlag");
			}
			// 将汇总数据放入Request
			request.setAttribute("tvoReturn", tvoReturn);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// 跳转到[总账页面]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.incomeType.list");
		return mv;
	}

	/**
	 *	支出类型管理 - 列表页
	 * @return
	 */
	@RequestMapping(value = "/expenditureType/list", method = RequestMethod.GET)
	public ModelAndView expenditureTypeList(HttpServletRequest request) {
		try {
			TableVO tvoReturn = this.baseDAO.queryForTableVO("tn_expenditure_type");
			for (RowVO rv : tvoReturn.toRowVOs()) {
				rv.get("CN_ID").setKey("id");
				rv.get("CN_NAME").setKey("name");
				rv.get("CR_PERIODIC_FLAG").setKey("periodicFlag");
				rv.get("CN_EFFECTIVE_DAY").setKey("effectiveDay");
				rv.get("CN_AMOUNT").setKey("amount");
				rv.get("CN_START_DATE").setKey("startDate");
				rv.get("CN_END_DATE").setKey("endDate");

				rv.setCellVOValue("startDate", MyDBHelper.formatDate(Long.parseLong(rv.getCellVOValue("startDate"))));
				rv.setCellVOValue("endDate", MyDBHelper.formatDate(Long.parseLong(rv.getCellVOValue("endDate"))));
			}
			// 将汇总数据放入Request
			request.setAttribute("tvoReturn", tvoReturn);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// 跳转到[总账页面]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.expenditureType.list");
		return mv;
	}

	/**
	 *	借贷管理 - 列表页
	 * @return
	 */
	@RequestMapping(value = "/debitAndCredit/list", method = RequestMethod.GET)
	public ModelAndView debitAndCreditList(HttpServletRequest request) {
		try {
			TableVO tvoReturn = this.baseDAO.queryForTableVO("tn_debit_and_credit");
			for (RowVO rv : tvoReturn.toRowVOs()) {
				rv.get("CN_ID").setKey("id");
				rv.get("CR_IN_FLAG").setKey("inFlag");
				rv.get("CN_DESCRIPTION").setKey("desc");
				rv.get("CN_CREATE_DATE").setKey("createDate");
				rv.get("CN_AMOUNT").setKey("amount");

				rv.setCellVOValue("createDate", MyDBHelper.formatDate(Long.parseLong(rv.getCellVOValue("createDate"))));
			}
			// 将汇总数据放入Request
			request.setAttribute("tvoReturn", tvoReturn);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// 跳转到[总账页面]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.debitAndCredit.list");
		return mv;
	}
}
package hq.myhome.controller;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import hq.mydb.dao.BaseDAO;
import hq.mydb.data.CellVO;
import hq.mydb.data.FormVO;
import hq.mydb.data.RowVO;
import hq.mydb.data.TableVO;
import hq.mydb.orderby.OrderBy;
import hq.mydb.orderby.Sort;
import hq.mydb.utils.MyDBHelper;

/**
 * 创建处理主页以外的内容的控制权
 * 
 * @author Administrator
 *
 */
@Controller // 声明当前为Controller
@RequestMapping(value = "/housingFundWithdrawal") // 制定基础URL
public class HousingFundWithdrawalController {

	private BaseDAO baseDAO;

	/**
	 * 加载BaseDAO
	 * 
	 * @param baseDAO
	 */
	@Autowired
	public HousingFundWithdrawalController(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	/**
	 * 公积金提现 - 列表页
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ModelAndView list(HttpServletRequest request) {
		try {
			OrderBy ob = new OrderBy();
			ob.addSort(new Sort("CN_CREATE_DATE"));
			TableVO tvoReturn = this.baseDAO.queryForTableVO("tn_housing_fund_withdrawal", ob);
			for (RowVO rv : tvoReturn.toRowVOs()) {
				rv.get("CN_ID").setKey("id");
				rv.get("CN_DESCRIPTION").setKey("desc");
				rv.get("CN_CREATE_DATE").setKey("createDate");
				rv.get("CN_AMOUNT").setKey("amount");
				if (StringUtils.isNotEmpty(rv.getCellVOValue("createDate"))) {
					rv.setCellVOValue("createDate",
							MyDBHelper.formatDate(Long.parseLong(rv.getCellVOValue("createDate"))));
				}
			}
			// 将汇总数据放入Request
			request.setAttribute("tvoReturn", tvoReturn);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// 跳转到[公积金提现 - 列表页]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.housingFundWithdrawal.list");
		return mv;
	}

	/**
	 * 公积金提现 - 新增提现
	 * 
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public ModelAndView add(HttpServletRequest request) {

		// 跳转到[公积金提现 - 新增提现]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.housingFundWithdrawal.new");
		return mv;
	}

	/**
	 * 公积金提现 - 新增提现 - 保存
	 * 
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public @ResponseBody void save(HttpServletRequest request) {

		String createDate = request.getParameter("createDate");
		String amount = request.getParameter("amount");
		String desc = request.getParameter("desc");
		if (StringUtils.isEmpty(amount)) {
			return;
		}

		FormVO fvoSave = new FormVO("tn_housing_fund_withdrawal");
		fvoSave.addCellVO(new CellVO("CN_CREATE_DATE", "" + MyDBHelper.getDatetime(createDate, "yyyy-MM-dd")));
		fvoSave.addCellVO(
				new CellVO("CN_AMOUNT", MyDBHelper.formatNumber(Double.parseDouble(amount), "########.####")));
		fvoSave.addCellVO(new CellVO("CN_DESCRIPTION", desc));

		this.baseDAO.saveOrUpdateFormVO(fvoSave);
	}

	/**
	 * 公积金提现 - 新增提现 - 删除
	 * 
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public @ResponseBody void delete(HttpServletRequest request) {
		String delIds = request.getParameter("delIds");

		if (StringUtils.isNotEmpty(delIds)) {
			HashSet<String> hsDelId = new HashSet<String>();
			for (String delId : delIds.split(",")) {
				if (StringUtils.isNotEmpty(delId)) {
					hsDelId.add(delId);
				}
			}
			this.baseDAO.deleteInId("tn_housing_fund_withdrawal", hsDelId);
		}
	}
}

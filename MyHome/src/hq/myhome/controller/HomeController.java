package hq.myhome.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hq.mydb.condition.CondSetBean;
import hq.mydb.dao.BaseDAO;
import hq.mydb.data.CellVO;
import hq.mydb.data.DataVO;
import hq.mydb.data.FormVO;
import hq.mydb.data.RowVO;
import hq.mydb.data.TableVO;
import hq.mydb.orderby.Sort;
import hq.mydb.utils.MyDBHelper;
import hq.myhome.utils.Definition;
import hq.myhome.utils.exception.MyHomeException;

/**
 * 创建Homepage的控制器
 * 
 * @author Administrator
 *
 */
@Controller // 声明当前为Controller
@RequestMapping(value = "/") // 制定基础URL
public class HomeController {
	private BaseDAO baseDAO;

	/**
	 * 加载BaseDAO
	 * 
	 * @param baseDAO
	 */
	@Autowired
	public HomeController(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	/**
	 * 主页
	 * 
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "/home";// 跳转到home.jsp
	}

	/**
	 * 偷懒的写法.每次刷新页面就缓存这里.
	 * 如果把整个dvoCountDetail弄成JSONObject放给HTML文件太大,需要处理.
	 * 非常少人非常少数据,所以懒得仔细处理.
	 */
	private static DataVO dvoCountDetail = new DataVO();// 记录tvoCount中每一个CellVO的数据值有哪些[交易记录]组成的.

	/**
	 * 总账页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generaledger", method = RequestMethod.GET)
	public ModelAndView generaledger(HttpServletRequest request) {
		try {
			dvoCountDetail = new DataVO();
			String yearMonthRange = request.getParameter("yearMonthRange");
			if (StringUtils.isEmpty(yearMonthRange)) {
				yearMonthRange = MyDBHelper.formatDate(MyDBHelper.getFirstDayOfYear(new Date().getTime()), "yyyy/MM") + " - "
						+ MyDBHelper.formatDate(MyDBHelper.getLastDayOfYear(new Date().getTime()), "yyyy/MM");
				request.setAttribute("yearMonthRange", yearMonthRange);
			}
			String startMonth = yearMonthRange.split(" - ")[0];
			String endMonth = yearMonthRange.split(" - ")[1];
			SimpleDateFormat sdf_yearMonth = new SimpleDateFormat("yyyyMM");
			SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
			long currentTime_long = System.currentTimeMillis();// 当前系统时间

			Calendar calFirst = Calendar.getInstance();
			Calendar calStart = Calendar.getInstance();
			Calendar calEnd = Calendar.getInstance();
			try {
				calFirst.setTime(sdf_yearMonth.parse("201810"));// 从2018年10月开始记账
				calStart.setTime(sdf_yearMonth.parse(startMonth.replace("/", "")));// 页面查询的开始时间
				calEnd.setTime(sdf_yearMonth.parse(endMonth.replace("/", "")));// 页面查询的结束时间
			} catch (Exception e) {
				e.printStackTrace();
				throw new MyHomeException(e);
			}

			// 读取所有的支出记录
			TableVO tvoExpenditure = this.baseDAO.queryForTableVO("tn_expenditure");
			// 读取所有的收入记录
			TableVO tvoIncome = this.baseDAO.queryForTableVO("tn_income");
			// 读取所有的支出类型
			TableVO tvoExpenditureType = this.baseDAO.queryForTableVO("tn_expenditure_type");
			// 读取所有的收入类型
			TableVO tvoIncomeType = this.baseDAO.queryForTableVO("tn_income_type");
			// 读取所有的借贷
			TableVO tvoDebitAndCredit = this.baseDAO.queryForTableVO("tn_debit_and_credit");
			HashMap<String, String> hmTypeIdToName = tvoExpenditureType.toHashMapOfToCellVOValue("CN_ID", "CN_NAME");
			hmTypeIdToName.putAll(tvoIncomeType.toHashMapOfToCellVOValue("CN_ID", "CN_NAME"));
			/*
			 * 处理借贷
			 */
			HashMap<String, ArrayList<RowVO>> hmYearMonthToDACRowVO = new HashMap<String, ArrayList<RowVO>>();
			for (RowVO rowVO : tvoDebitAndCredit.toRowVOs()) {
				String createDate = rowVO.getCellVOValue("CN_CREATE_DATE");// 创建日期
				String yearMonth = sdf_yearMonth.format(new Date(Long.parseLong(createDate)));
				if (hmYearMonthToDACRowVO.containsKey(yearMonth)) {
					hmYearMonthToDACRowVO.get(yearMonth).add(rowVO);
				} else {
					ArrayList<RowVO> al = new ArrayList<RowVO>();
					al.add(rowVO);
					hmYearMonthToDACRowVO.put(yearMonth, al);
				}
			}
			// 得到现金收入的类型
			HashSet<String> hsCashInTypeId = tvoIncomeType.toHashMapOfToCellVOValueSet("CN_FROZEN_FLAG", "CN_ID").get(Definition.NO);
			if (hsCashInTypeId == null) {
				hsCashInTypeId = new HashSet<String>();
			}

			/*
			 * 将所有的收支合并到一个TableVO中,并且一句[创建时间][是否实际]进行排序
			 */
			TableVO tvoData = new TableVO();// 记录所有的收支(实际&预告)
			ArrayList<String> alKey_existPlan = new ArrayList<String>();// 记录已经有生成过[计划]记录的类型
			// 收入
			for (RowVO rowVO : tvoIncome.toRowVOs()) {
				String id = rowVO.getCellVOValue("CN_ID");
				String planFlag = rowVO.getCellVOValue("CR_PLAN_FLAG");// 是为计划支出,否为实际支出
				String createDate = rowVO.getCellVOValue("CN_CREATE_DATE");// 创建日期
				String typeId = rowVO.getCellVOValue("CR_INCOME_TYPE_ID");// 收入类型
				String amount = rowVO.getCellVOValue("CN_AMOUNT");// 金额
				String desc = rowVO.getCellVOValue("CN_DESCRIPTION");// 描述

				tvoData.addRowVO(this.generaledger_buildDataRowVO(id, planFlag, createDate, typeId, amount, Definition.YES, hmTypeIdToName.get(typeId), desc));

				if (StringUtils.equals(planFlag, Definition.YES)) {
					String key_existPlan = typeId + "_" + sdf_yearMonth.format(new Date(Long.parseLong(createDate)));// 类型 + 月份
					alKey_existPlan.add(key_existPlan);
				}
			}
			// 支出
			for (RowVO rowVO : tvoExpenditure.toRowVOs()) {
				String id = rowVO.getCellVOValue("CN_ID");
				String planFlag = rowVO.getCellVOValue("CR_PLAN_FLAG");// 是为计划支出,否为实际支出
				String createDate = rowVO.getCellVOValue("CN_CREATE_DATE");// 创建日期
				String typeId = rowVO.getCellVOValue("CR_EXPENDITURE_TYPE_ID");// 支出类型
				String amount = rowVO.getCellVOValue("CN_AMOUNT");// 金额
				String desc = rowVO.getCellVOValue("CN_DESCRIPTION");// 描述

				tvoData.addRowVO(this.generaledger_buildDataRowVO(id, planFlag, createDate, typeId, amount, Definition.NO, hmTypeIdToName.get(typeId), desc));

				if (StringUtils.equals(planFlag, Definition.YES)) {
					String key_existPlan = typeId + "_" + sdf_yearMonth.format(new Date(Long.parseLong(createDate)));
					alKey_existPlan.add(key_existPlan);
				}
			}

			RowVO rvoHead = new RowVO();// 根据用户选择的月份区间生成的头信息
			// 周期性预计支出和收入
			Calendar calLoop = Calendar.getInstance();
			calLoop.setTime(calFirst.getTime());
			while (calLoop.getTimeInMillis() <= calEnd.getTimeInMillis()) {
				String yearMonth = sdf_yearMonth.format(calLoop.getTime());

				// 处理Head
				if (calLoop.getTimeInMillis() >= calStart.getTimeInMillis()) {
					rvoHead.addCellVO(new CellVO(yearMonth, yearMonth));
				}
				// 收入
				for (RowVO rvo : tvoIncomeType.toRowVOs()) {
					String periodicFlag = rvo.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性收入
					String startDate = rvo.getCellVOValue("CN_START_DATE");
					String endDate = rvo.getCellVOValue("CN_END_DATE");
					if (!StringUtils.equals(Definition.YES, periodicFlag)) {
						continue;
					}
					if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
						continue;// 过滤 - 没到开始时间
					}
					if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
						continue;// 过滤 - 超过结束时间
					}

					String typeId = rvo.getCellVOValue("CN_ID");
					String effectiveDay = rvo.getCellVOValue("CN_EFFECTIVE_DAY");// 生效日期
					String amount = rvo.getCellVOValue("CN_AMOUNT");
					String createDate = "";// yyyyMMdd
					if (Integer.parseInt(effectiveDay) < 10) {
						createDate = yearMonth + "0" + effectiveDay;
					} else {
						createDate = yearMonth + effectiveDay;
					}
					String createDate_long = String.valueOf(sdf_date.parse(createDate).getTime());// 13位lang值
					String key_existPlan = typeId + "_" + sdf_yearMonth.format(new Date(Long.parseLong(createDate_long)));// 类型 + 月份
					if (alKey_existPlan.contains(key_existPlan)) {
						continue;// 过滤 - 已有实际交易记录
					}

					tvoData.addRowVO(this.generaledger_buildDataRowVO("", Definition.YES, createDate_long, typeId, amount, Definition.YES, hmTypeIdToName.get(typeId), ""));
				}

				// 支出
				for (RowVO rvo : tvoExpenditureType.toRowVOs()) {
					String periodicFlag = rvo.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性支出
					String startDate = rvo.getCellVOValue("CN_START_DATE");
					String endDate = rvo.getCellVOValue("CN_END_DATE");
					if (!StringUtils.equals(Definition.YES, periodicFlag)) {
						continue;
					}
					if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
						continue;// 过滤 - 没到开始时间
					}
					if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
						continue;// 过滤 - 超过结束时间
					}

					String typeId = rvo.getCellVOValue("CN_ID");
					String effectiveDay = rvo.getCellVOValue("CN_EFFECTIVE_DAY");// 生效日期
					String amount = rvo.getCellVOValue("CN_AMOUNT");
					String createDate = "";// yyyyMMdd
					if (Integer.parseInt(effectiveDay) < 10) {
						createDate = yearMonth + "0" + effectiveDay;
					} else {
						createDate = yearMonth + effectiveDay;
					}
					String createDate_long = String.valueOf(sdf_date.parse(createDate).getTime());// 13位lang值
					String key_existPlan = typeId + "_" + sdf_yearMonth.format(new Date(Long.parseLong(createDate_long)));// 类型 + 月份
					if (alKey_existPlan.contains(key_existPlan)) {
						continue;// 过滤 - 已有实际交易记录
					}

					tvoData.addRowVO(this.generaledger_buildDataRowVO("", Definition.YES, createDate_long, typeId, amount, Definition.NO, hmTypeIdToName.get(typeId), ""));
				}

				calLoop.add(Calendar.MONTH, 1);// 加一个月
			}

			// 对数据Table进行排序 [创建时间][是否实际]
			tvoData.sortByColumn(new Sort("createDate"), new Sort("planFlag"), new Sort("typeId"));

			// 开始进行汇总数的计算
			TableVO tvoCount = new TableVO();// 记录汇总信息
			tvoCount.setHeadRowVO(rvoHead);
			ArrayList<String> alKey_existActual = new ArrayList<String>();// 记录已经有生成过[实际]记录的类型
			for (RowVO rvoData : tvoData.toRowVOs()) {
				boolean isPlan = StringUtils.equals(rvoData.getCellVOValue("planFlag"), Definition.YES);
				boolean isIncome = StringUtils.equals(rvoData.getCellVOValue("isIncome"), Definition.YES);
				String createDate = rvoData.getCellVOValue("createDate");
				String typeId = rvoData.getCellVOValue("typeId");
				String yearMonth = sdf_yearMonth.format(new Date(Long.parseLong(createDate)));

				String key_yearMonth = typeId + "_" + yearMonth;// 类型 + 月份

				// 得到当前YearMonth的每行的CellVO
				CellVO cvoPlanOut = this.generaledger_getCellVO(tvoCount, "planOut", yearMonth); //计划支出
				CellVO cvoActualOut = this.generaledger_getCellVO(tvoCount, "actualOut", yearMonth); //实际支出
				CellVO cvoPlanIn = this.generaledger_getCellVO(tvoCount, "planIn", yearMonth); //计划收入
				CellVO cvoActualIn = this.generaledger_getCellVO(tvoCount, "actualIn", yearMonth); //实际收入
				CellVO cvoSurplusMonth = this.generaledger_getCellVO(tvoCount, "surplusMonth", yearMonth); //当月结余
				CellVO cvoSurplusMonthCash = this.generaledger_getCellVO(tvoCount, "surplusMonthCash", yearMonth); //当月结余(现金)

				if (isPlan) {
					if (isIncome) {
						// 汇总 - 计划收入
						this.generaledger_countCalculate(dvoCountDetail, cvoPlanIn, rvoData, true);
					} else {
						// 汇总 - 计划支出
						this.generaledger_countCalculate(dvoCountDetail, cvoPlanOut, rvoData, true);
					}
				} else {
					// 处理有[实际]类型的key
					alKey_existActual.add(key_yearMonth);

					if (isIncome) {
						// 汇总 - 实际收入
						this.generaledger_countCalculate(dvoCountDetail, cvoActualIn, rvoData, true);
					} else {
						// 汇总 - 实际支出
						this.generaledger_countCalculate(dvoCountDetail, cvoActualOut, rvoData, true);
					}
				}

				// 处理累计
				if (isPlan && (alKey_existActual.contains(key_yearMonth) || currentTime_long > Long.parseLong(createDate))) {
					continue;// 过滤计划记录,当前月份的计划已经生成了实际记录或当前已经已经过了计划日期的的记录都不参与汇总.
				}

				// 汇总 - 当月结余
				if (hsCashInTypeId.contains(typeId)) {
					// 收入 - 累积(现金)
					this.generaledger_countCalculate(dvoCountDetail, cvoSurplusMonthCash, rvoData, true);
				}
				if (isIncome) {
					// 收入 - 累积
					this.generaledger_countCalculate(dvoCountDetail, cvoSurplusMonth, rvoData, true);
				} else {
					// 支出 - 累积
					this.generaledger_countCalculate(dvoCountDetail, cvoSurplusMonth, rvoData, false);
					// 支出 - 累积(现金)
					this.generaledger_countCalculate(dvoCountDetail, cvoSurplusMonthCash, rvoData, false);
				}

				/*
				 * 处理借贷
				 */
				if (hmYearMonthToDACRowVO.containsKey(yearMonth)) {
					for (RowVO rvoDAC : hmYearMonthToDACRowVO.get(yearMonth)) {
						String id = rvoDAC.getCellVOValue("CN_ID");
						String inFlag = rvoDAC.getCellVOValue("CR_IN_FLAG");
						String amount = rvoDAC.getCellVOValue("CN_AMOUNT");
						String createDate_t = rvoDAC.getCellVOValue("CN_CREATE_DATE");// 创建日期
						String desc = rvoDAC.getCellVOValue("CN_DESCRIPTION");
						if (StringUtils.equals(inFlag, Definition.YES)) {
							// 借入,现金和累积都增加
							RowVO rvoDACData = this.generaledger_buildDataRowVO(id, Definition.NO, createDate_t, "借入", amount, inFlag, "借入", desc);
							// 借入 - 累积
							this.generaledger_countCalculate(dvoCountDetail, cvoSurplusMonth, rvoDACData, true);
							// 借入 - 累积(现金)
							this.generaledger_countCalculate(dvoCountDetail, cvoSurplusMonthCash, rvoDACData, true);
						} else {
							// 借出, 只减少现金,累积资产是不变的.
							RowVO rvoDACData = this.generaledger_buildDataRowVO(id, Definition.NO, createDate_t, "借出", amount, inFlag, "借出", desc);
							// 借出 - 累积(现金)
							this.generaledger_countCalculate(dvoCountDetail, cvoSurplusMonthCash, rvoDACData, false);
						}
					}
					hmYearMonthToDACRowVO.remove(yearMonth);
				}
			}

			// 支出偏差
			for (CellVO cvoPlanOut : tvoCount.get("planOut").toCellVOs()) {
				String yearMonth = cvoPlanOut.getKey();
				CellVO cvoActualOut = this.generaledger_getCellVO(tvoCount, "actualOut", yearMonth); //实际支出
				CellVO cvoOutDeviation = this.generaledger_getCellVO(tvoCount, "outDeviation", yearMonth); //支出偏差

				// 处理支出偏差
				String deviation = "0.0";
				if (Double.parseDouble(cvoPlanOut.getValue()) > 0) {
					deviation = MyDBHelper.doubleDiv(cvoActualOut.getValue(), cvoPlanOut.getValue());
					if (Double.parseDouble(deviation) == 1) {
						deviation = "0.0";
					}
				}
				cvoOutDeviation.setValue(deviation);
			}

			// 累积结余
			String surplusCount = "0";
			for (CellVO cvoSurplusMonth : tvoCount.get("surplusMonth").toCellVOs()) {
				String yearMonth = cvoSurplusMonth.getKey();
				CellVO cvoSurplusCount = this.generaledger_getCellVO(tvoCount, "surplusCount", yearMonth); //累积结余

				// 加总结余
				surplusCount = MyDBHelper.doubleAdd(surplusCount, cvoSurplusMonth.getValue());
				cvoSurplusCount.setValue(surplusCount);
			}

			// 累积结余(现金)
			String surplusCash = "0";
			for (CellVO cvoSurplusMonthCash : tvoCount.get("surplusMonthCash").toCellVOs()) {
				String yearMonth = cvoSurplusMonthCash.getKey();
				CellVO cvoSurplusCash = this.generaledger_getCellVO(tvoCount, "surplusCash", yearMonth); //累积结余(现金)

				// 加总结余
				surplusCash = MyDBHelper.doubleAdd(surplusCash, cvoSurplusMonthCash.getValue());
				cvoSurplusCash.setValue(surplusCash);
			}

			/*
			 * 处理数字的显示
			 */
			for (RowVO rvo : tvoCount.toRowVOs()) {
				for (CellVO cvo : rvo.toCellVOs()) {
					try {
						double v = Double.parseDouble(cvo.getValue());
						cvo.setValue(MyDBHelper.formatNumber(v, "#,###,##0.00"));
					} catch (Exception e) {

					}
				}
			}

			// 将汇总数据放入Request
			request.setAttribute("tvoCount", tvoCount);

			/*
			 * 处理页面上的内容显示
			 */
			tvoCount.changeRowVOIndex(tvoCount.get("outDeviation"), 2);

			this.generaledger_getCellVO(tvoCount, "planOut", "type").setValue("<span style='font-weight:bold;color:red;'>计划支出</span>");
			this.generaledger_getCellVO(tvoCount, "actualOut", "type").setValue("<span style='font-weight:bold;color:red;'>实际支出</span>");
			this.generaledger_getCellVO(tvoCount, "outDeviation", "type").setValue("<span style='font-weight:bold;color:blue;'>支出偏差</span>");
			this.generaledger_getCellVO(tvoCount, "planIn", "type").setValue("<span style='font-weight:bold;color:green;'>计划收入</span>");
			this.generaledger_getCellVO(tvoCount, "actualIn", "type").setValue("<span style='font-weight:bold;color:green;'>实际收入</span>");
			this.generaledger_getCellVO(tvoCount, "surplusMonth", "type").setValue("<span style='font-weight:bold;color:purple;'>当月结余</span>");
			this.generaledger_getCellVO(tvoCount, "surplusMonthCash", "type").setValue("<span style='font-weight:bold;color:purple;'>当月结余(现金)</span>");
			this.generaledger_getCellVO(tvoCount, "surplusCount", "type").setValue("<span style='font-weight:bold;color:purple;'>累积结余</span>");
			this.generaledger_getCellVO(tvoCount, "surplusCash", "type").setValue("<span style='font-weight:bold;color:purple;'>累积结余(现金)</span>");

			for (RowVO rvo : tvoCount.toRowVOs()) {
				String type = rvo.getCellVOValue("type");
				if (type.contains("计划支出") || type.contains("实际支出") || type.contains("计划收入") || type.contains("实际收入")) {
					String typeKey = "";
					if (type.contains("计划支出")) {
						typeKey = "planOut";
					} else if (type.contains("实际支出")) {
						typeKey = "actualOut";
					} else if (type.contains("计划收入")) {
						typeKey = "planIn";
					} else if (type.contains("实际收入")) {
						typeKey = "actualIn";
					}
					for (CellVO cvo : rvo.toCellVOs()) {
						if (StringUtils.equals("type", cvo.getKey())) {
							continue;
						}
						cvo.setValue("<div style='height: 100%;cursor:pointer;border-bottom: 1px dashed blue;' onclick=\"modifyDetailData('" + typeKey + "','" + cvo.hashCode()
								+ "')\">" + cvo.getValue() + "</div>");
					}
				} else if (type.contains("当月结余") || type.contains("当月结余(现金)")) {
					String typeKey = "";
					if (type.contains("当月结余(现金)")) {
						typeKey = "当月结余(现金)";
					} else if (type.contains("当月结余")) {
						typeKey = "当月结余";
					}
					for (CellVO cvo : rvo.toCellVOs()) {
						if (StringUtils.equals("type", cvo.getKey())) {
							continue;
						}
						cvo.setValue("<div style='height: 100%;cursor:pointer;border-bottom: 1px dashed blue;' onclick=\"surplusMonthDetail('" + typeKey + "','" + cvo.hashCode()
								+ "')\">" + cvo.getValue() + "</div>");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 跳转到[总账页面]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.generaledger");
		return mv;
	}

	private RowVO generaledger_buildDataRowVO(String id, String planFlag, String createDate, String typeId, String amount, String isIncome, String typeName, String desc) {
		RowVO rvoData = new RowVO();
		rvoData.addCellVO(new CellVO("id", id));// 如果有实际记录,则传入ID
		rvoData.addCellVO(new CellVO("planFlag", planFlag));// 是为计划支出,否为实际支出
		rvoData.addCellVO(new CellVO("createDate", createDate));// 创建日期
		rvoData.addCellVO(new CellVO("typeId", typeId));// 收支类型
		rvoData.addCellVO(new CellVO("typeName", typeName));// 收支类型
		rvoData.addCellVO(new CellVO("amount", amount));// 金额
		rvoData.addCellVO(new CellVO("isIncome", isIncome));// 是否为收入
		rvoData.addCellVO(new CellVO("desc", desc));// 描述
		return rvoData;
	}

	private CellVO generaledger_getCellVO(TableVO tvo, String rowVOKey, String cellVOkey) {
		RowVO rvo = tvo.get(rowVOKey);
		if (rvo == null) {
			rvo = new RowVO(rowVOKey);
			tvo.addRowVO(rvo);
		}
		CellVO cvo = rvo.get(cellVOkey);
		if (cvo == null) {
			cvo = new CellVO(cellVOkey, "0");
			rvo.addCellVO(cvo);
		}
		return cvo;
	}

	private void generaledger_countCalculate(DataVO dvoCountDetail, CellVO cvoCount, RowVO rvoData, boolean isPlus) {
		String amount = rvoData.getCellVOValue("amount");

		// 汇总金额
		if (isPlus) {
			cvoCount.setValue(MyDBHelper.doubleAdd(cvoCount.getValue(), amount));// + 
		} else {
			cvoCount.setValue(MyDBHelper.doubleSub(cvoCount.getValue(), amount));// - 
		}

		// 处理当前CellVO的汇总明细
		String cellVOHashCode = String.valueOf(cvoCount.hashCode());
		TableVO tvoCountDetial = dvoCountDetail.getTableVO(cellVOHashCode);
		if (tvoCountDetial == null) {
			tvoCountDetial = new TableVO(cellVOHashCode);
			dvoCountDetail.addTableVO(tvoCountDetial);
		}
		tvoCountDetial.addRowVO(rvoData);
	}

	/**
	 * 总账页面 - 明细维护页
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generaledger/modifyDetailData", method = RequestMethod.GET)
	public ModelAndView generaledger_modifyDetailData(HttpServletRequest request) {
		try {
			SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMM");
			String cvoHashCode = request.getParameter("cvoHashCode");// 当前Cell的HashCode,通过HashCode从dvoCountDetail读取对应的明细数据
			String typeKey = request.getParameter("typeKey");// planOut | actualOut | planOut | actualOut
			String type = "不知道";
			if (StringUtils.equals(typeKey, "planOut")) {
				type = "<span style='font-weight:bold;color:red;'>计划支出</span>";
			} else if (StringUtils.equals(typeKey, "actualOut")) {
				type = "<span style='font-weight:bold;color:red;'>实际支出</span>";
			} else if (StringUtils.equals(typeKey, "planIn")) {
				type = "<span style='font-weight:bold;color:green;'>计划收入</span>";
			} else if (StringUtils.equals(typeKey, "actualIn")) {
				type = "<span style='font-weight:bold;color:green;'>实际收入</span>";
			} else if (StringUtils.equals(typeKey, "surplusMonth")) {
				type = "<span style='font-weight:bold;color:purple;'>当月结余</span>";
			}
			String yearMonth = ""; // yyyyMM

			request.setAttribute("type", type);

			TableVO tvoCountDetail = dvoCountDetail.getTableVO(cvoHashCode);
			if(tvoCountDetail == null) {
				tvoCountDetail = new TableVO();
			}

			// 处理页面显示的TableVO
			TableVO tvoReturn = new TableVO();
			for (RowVO rvoCountDetail : tvoCountDetail.toRowVOs()) {
				String id = rvoCountDetail.getCellVOValue("id"); // 实际记录的ID,可能为空
				String createDate = rvoCountDetail.getCellVOValue("createDate"); // 创建日期
				String typeName = rvoCountDetail.getCellVOValue("typeName"); // 收支类型
				String amount = rvoCountDetail.getCellVOValue("amount"); // 金额
				String desc = rvoCountDetail.getCellVOValue("desc");// 描述

				RowVO rvoReturn = new RowVO();
				tvoReturn.addRowVO(rvoReturn);
				rvoReturn.addCellVO(new CellVO("id", id));
				rvoReturn.addCellVO(new CellVO("typeName", typeName));
				rvoReturn.addCellVO(new CellVO("amount", amount));
				rvoReturn.addCellVO(new CellVO("createDate", createDate));
				rvoReturn.addCellVO(new CellVO("desc", desc));
				if (StringUtils.isEmpty(yearMonth)) {
					yearMonth = sdf_date.format(new Date(Long.parseLong(createDate)));
				}
			}
			request.setAttribute("yearMonth", yearMonth);

			// 排序
			tvoReturn.sortByColumn(new Sort("createDate"), new Sort("typeName"));

			/*
			 * 处理页面显示
			 */
			tvoReturn.formatDate("yyyy-MM-dd", "createDate");
			for (int i = 0; i < tvoReturn.size(); i++) {
				RowVO rvo = tvoReturn.get(i);
				String id = rvo.getCellVOValue("id");
				String typeName = rvo.getCellVOValue("typeName");
				rvo.setCellVOValue("typeName", "<input type='hidden' name='id_" + i + "' value='" + id + "'>" + typeName + "");
				String amount = rvo.getCellVOValue("amount");
				rvo.setCellVOValue("amount", "<input type='text' name='amount_" + i + "' class='layui-input' value='" + amount + "'>");
				String createDate = rvo.getCellVOValue("createDate");
				rvo.setCellVOValue("createDate", "<input type='text' name='createDate_" + i + "' id='createDate_" + i
						+ "' placeholder='yyyy-MM-dd' autocomplete='off' class='layui-input' value='" + createDate + "'>");
			}
			request.setAttribute("tvoReturn", tvoReturn);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 跳转到[总账页面-明细数据修改页]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.layer.generaledger.modifyDetailData");
		return mv;
	}

	/**
	 * 总账页面 - 明细维护页 - 删除
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generaledger/modifyDetailData/delete", method = RequestMethod.POST)
	public @ResponseBody void generaledger_modifyDetailData_delete(HttpServletRequest request) {
		String delIdJSONArray = request.getParameter("delIdJSONArray");
		if (StringUtils.isNotEmpty(delIdJSONArray)) {
			HashSet<String> hsDelId = new HashSet<String>();
			for (String delId : delIdJSONArray.split(",")) {
				if (StringUtils.isNotEmpty(delId)) {
					hsDelId.add(delId);
				}
			}
			this.baseDAO.deleteInId("tn_income", hsDelId);
			this.baseDAO.deleteInId("tn_expenditure", hsDelId);
		}
	}

	/**
	 * 总账页面 - 明细维护页 - 保存
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generaledger/modifyDetailData/save", method = RequestMethod.POST)
	public @ResponseBody void generaledger_modifyDetailData_save(HttpServletRequest request) {
		String saveJSONArray = request.getParameter("saveJSONArray");
		JSONArray jaSave = JSONArray.parseArray(saveJSONArray);

		TableVO tvoSave = new TableVO();
		tvoSave.setOperation(TableVO.OPERATION_UPDATE);
		for (Object oSave : jaSave) {
			JSONObject joSave = (JSONObject) oSave;
			RowVO rvo = new RowVO();
			tvoSave.addRowVO(rvo);
			rvo.addCellVO(new CellVO("CN_ID", joSave.getString("id")));
			rvo.addCellVO(new CellVO("CN_CREATE_DATE", "" + MyDBHelper.getDatetime(joSave.getString("createDate"), "yyyy-MM-dd")));
			rvo.addCellVO(new CellVO("CN_AMOUNT", MyDBHelper.formatNumber(Double.parseDouble(joSave.getString("amount")), "########.####")));
		}
		tvoSave.setKey("tn_income");
		this.baseDAO.saveOrUpdateTableVO(tvoSave);
		tvoSave.setKey("tn_expenditure");
		this.baseDAO.saveOrUpdateTableVO(tvoSave);
	}

	/**
	 * 总账页面 - 新增收支
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generaledger/newDetailData", method = RequestMethod.GET)
	public ModelAndView generaledger_newDetailData(HttpServletRequest request) {
		// 读取所有的支出类型
		TableVO tvoExpenditureType = this.baseDAO.queryForTableVO("tn_expenditure_type");
		// 读取所有的收入类型
		TableVO tvoIncomeType = this.baseDAO.queryForTableVO("tn_income_type");

		tvoExpenditureType.sortByColumn(new Sort("CN_NAME"));
		tvoIncomeType.sortByColumn(new Sort("CN_NAME"));

		TableVO tvoReturn = new TableVO();
		for (RowVO rvo : tvoExpenditureType.toRowVOs()) {
			String name = rvo.getCellVOValue("CN_NAME");
			rvo.setCellVOValue("CN_NAME", "支出 - " + name);
			tvoReturn.addRowVO(rvo);
		}
		for (RowVO rvo : tvoIncomeType.toRowVOs()) {
			String name = rvo.getCellVOValue("CN_NAME");
			rvo.setCellVOValue("CN_NAME", "收入 - " + name);
			tvoReturn.addRowVO(rvo);
		}
		request.setAttribute("tvoReturn", tvoReturn);

		// 跳转到[总账页面-明细数据修改页]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.layer.generaledger.newDetailData");
		return mv;
	}

	/**
	 * 总账页面 - 新增收支 - 保存
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generaledger/newDetailData/save", method = RequestMethod.POST)
	public @ResponseBody void generaledger_newDetailData_save(HttpServletRequest request) {
		String typeId = request.getParameter("typeId");
		String createDate = request.getParameter("createDate");
		String planFlag = request.getParameter("planFlag");
		String amount = request.getParameter("amount");
		String desc = request.getParameter("desc");
		if (StringUtils.isEmpty(amount)) {
			return;
		}

		// 读取所有的收入类型
		TableVO tvoIncomeType = this.baseDAO.queryForTableVO("tn_income_type");
		ArrayList<String> alIncomeTypeId = tvoIncomeType.toArrayListOfCellVOValue("CN_ID");

		FormVO fvoSave = new FormVO();
		if (alIncomeTypeId.contains(typeId)) {
			fvoSave.setKey("tn_income");
			fvoSave.addCellVO(new CellVO("CR_INCOME_TYPE_ID", typeId));
		} else {
			fvoSave.setKey("tn_expenditure");
			fvoSave.addCellVO(new CellVO("CR_EXPENDITURE_TYPE_ID", typeId));
		}
		fvoSave.addCellVO(new CellVO("CN_CREATE_DATE", "" + MyDBHelper.getDatetime(createDate, "yyyy-MM-dd")));
		fvoSave.addCellVO(new CellVO("CN_AMOUNT", MyDBHelper.formatNumber(Double.parseDouble(amount), "########.####")));
		fvoSave.addCellVO(new CellVO("CN_DESCRIPTION", desc));
		if (StringUtils.equals(planFlag, Definition.YES)) {
			fvoSave.addCellVO(new CellVO("CR_PLAN_FLAG", Definition.YES));
		} else {
			fvoSave.addCellVO(new CellVO("CR_PLAN_FLAG", Definition.NO));
		}

		this.baseDAO.saveOrUpdateFormVO(fvoSave);
	}

	/**
	 * 总账页面 - 当月结余明细页
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generaledger/surplusMonthDetail", method = RequestMethod.GET)
	public ModelAndView generaledger_surplusMonthDetail(HttpServletRequest request) {
		try {
			SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMM");
			String typeKey = request.getParameter("typeKey");
			String cvoHashCode = request.getParameter("cvoHashCode");// 当前Cell的HashCode,通过HashCode从dvoCountDetail读取对应的明细数据

			String type = "<span style='font-weight:bold;color:purple;'>" + typeKey + "</span>";
			String yearMonth = "";
			TableVO tvoCountDetail = dvoCountDetail.getTableVO(cvoHashCode);

			// 处理页面显示的TableVO
			TableVO tvoReturn = new TableVO();
			for (RowVO rvoCountDetail : tvoCountDetail.toRowVOs()) {
				String planFlag = rvoCountDetail.getCellVOValue("planFlag"); // 是为计划支出,否为实际支出
				String createDate = rvoCountDetail.getCellVOValue("createDate"); // 创建日期
				String typeName = rvoCountDetail.getCellVOValue("typeName"); // 收支类型
				String amount = rvoCountDetail.getCellVOValue("amount"); // 金额
				String isIncome = rvoCountDetail.getCellVOValue("isIncome"); // 是否为收入
				String desc = rvoCountDetail.getCellVOValue("desc");// 描述

				if (StringUtils.equals(planFlag, Definition.YES)) {
					planFlag = "<span style='font-weight:bold;color:purple;'>" + "计划" + "</span>";
				} else {
					planFlag = "实际";
				}
				if (StringUtils.equals(isIncome, Definition.YES)) {
					isIncome = "<span style='font-weight:bold;color:green;'>" + "收入" + "</span>";
				} else {
					isIncome = "<span style='font-weight:bold;color:red;'>" + "支出" + "</span>";
				}

				RowVO rvoReturn = new RowVO();
				tvoReturn.addRowVO(rvoReturn);
				rvoReturn.addCellVO(new CellVO("typeName", typeName));
				rvoReturn.addCellVO(new CellVO("createDate", createDate));
				rvoReturn.addCellVO(new CellVO("inOut", isIncome));
				rvoReturn.addCellVO(new CellVO("amount", amount));
				rvoReturn.addCellVO(new CellVO("planOrActual", planFlag));
				rvoReturn.addCellVO(new CellVO("desc", desc));
				if (StringUtils.isEmpty(yearMonth)) {
					yearMonth = sdf_date.format(new Date(Long.parseLong(createDate)));
				}
			}

			// 排序
			tvoReturn.sortByColumn(new Sort("createDate"), new Sort("inOut"), new Sort("typeName"));
			tvoReturn.formatDate("yyyy-MM-dd", "createDate");

			request.setAttribute("tvoReturn", tvoReturn);
			request.setAttribute("yearMonth", yearMonth);
			request.setAttribute("type", type);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 跳转到[总账页面-明细数据修改页]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.layer.generaledger.surplusMonthDetail");
		return mv;
	}

}

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
import hq.mydb.data.FormVO;
import hq.mydb.data.RowVO;
import hq.mydb.data.TableVO;
import hq.mydb.orderby.Sort;
import hq.mydb.utils.MyDBHelper;
import hq.myhome.utils.Definition;
import hq.myhome.utils.exception.MyHomeException;

/**
 * 创建Homepage的控制器
 * @author Administrator
 *
 */
@Controller // 声明当前为Controller
@RequestMapping(value = "/") // 制定基础URL
public class HomeController {
	private BaseDAO baseDAO;

	/**
	 * 加载BaseDAO
	 * @param baseDAO
	 */
	@Autowired
	public HomeController(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	/**
	 * 主页
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "/home";// 跳转到home.jsp
	}

	/**
	 * 总账页面
	 * @return
	 */
	@RequestMapping(value = "/generaledger", method = RequestMethod.GET)
	public ModelAndView generaledger(HttpServletRequest request) {
		try {
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
			int currentYearMonth = Integer.parseInt(sdf_yearMonth.format(new Date()));// 当前年月的整数形201812,用于后面的月份比对
			long currentTime_long = System.currentTimeMillis();// 当前系统时间
			// 得到现金收入的类型 
			HashSet<String> hsCashInTypeId = tvoIncomeType.toHashMapOfToCellVOValueSet("CN_FROZEN_FLAG", "CN_ID").get(Definition.NO);
			if (hsCashInTypeId == null) {
				hsCashInTypeId = new HashSet<String>();
			}

			// group by Month
			HashMap<String, ArrayList<RowVO>> hmYearMonthToPlanInRowVOArray = new HashMap<String, ArrayList<RowVO>>();
			HashMap<String, ArrayList<RowVO>> hmYearMonthToActualInRowVOArray = new HashMap<String, ArrayList<RowVO>>();
			HashMap<String, ArrayList<RowVO>> hmYearMonthToPlanOutRowVOArray = new HashMap<String, ArrayList<RowVO>>();
			HashMap<String, ArrayList<RowVO>> hmYearMonthToActualOutRowVOArray = new HashMap<String, ArrayList<RowVO>>();
			for (RowVO rowVO : tvoIncome.toRowVOs()) {
				String planFlag = rowVO.getCellVOValue("CR_PLAN_FLAG");// 是为计划支出,否为实际支出
				String createDate = rowVO.getCellVOValue("CN_CREATE_DATE");// 创建日期
				String yearMonth = sdf_yearMonth.format(new Date(Long.parseLong(createDate)));
				if (StringUtils.equals(planFlag, Definition.YES)) {
					// 处理计划收入
					if (hmYearMonthToPlanInRowVOArray.containsKey(yearMonth)) {
						hmYearMonthToPlanInRowVOArray.get(yearMonth).add(rowVO);
					} else {
						ArrayList<RowVO> al = new ArrayList<RowVO>();
						al.add(rowVO);
						hmYearMonthToPlanInRowVOArray.put(yearMonth, al);
					}
				} else {
					// 处理计划收入
					if (hmYearMonthToActualInRowVOArray.containsKey(yearMonth)) {
						hmYearMonthToActualInRowVOArray.get(yearMonth).add(rowVO);
					} else {
						ArrayList<RowVO> al = new ArrayList<RowVO>();
						al.add(rowVO);
						hmYearMonthToActualInRowVOArray.put(yearMonth, al);
					}
				}
			}
			for (RowVO rowVO : tvoExpenditure.toRowVOs()) {
				String planFlag = rowVO.getCellVOValue("CR_PLAN_FLAG");// 是为计划支出,否为实际支出
				String createDate = rowVO.getCellVOValue("CN_CREATE_DATE");// 创建日期
				String yearMonth = sdf_yearMonth.format(new Date(Long.parseLong(createDate)));
				if (StringUtils.equals(planFlag, Definition.YES)) {
					// 处理计划支出
					if (hmYearMonthToPlanOutRowVOArray.containsKey(yearMonth)) {
						hmYearMonthToPlanOutRowVOArray.get(yearMonth).add(rowVO);
					} else {
						ArrayList<RowVO> al = new ArrayList<RowVO>();
						al.add(rowVO);
						hmYearMonthToPlanOutRowVOArray.put(yearMonth, al);
					}
				} else {
					// 处理计划支出
					if (hmYearMonthToActualOutRowVOArray.containsKey(yearMonth)) {
						hmYearMonthToActualOutRowVOArray.get(yearMonth).add(rowVO);
					} else {
						ArrayList<RowVO> al = new ArrayList<RowVO>();
						al.add(rowVO);
						hmYearMonthToActualOutRowVOArray.put(yearMonth, al);
					}
				}
			}

			TableVO tvoCount = new TableVO();// 记录汇总信息
			// 处理数据
			RowVO rvoPlanOut = new RowVO("planOut", RowVO.OPERATION_UNDEFINED); // 计划支出    
			RowVO rvoActualOut = new RowVO("actualOut", RowVO.OPERATION_UNDEFINED); // 实际支出    
			RowVO rvoOutDeviation = new RowVO("outDeviation", RowVO.OPERATION_UNDEFINED); // 支出偏差    
			RowVO rvoPlanIn = new RowVO("planIn", RowVO.OPERATION_UNDEFINED); // 计划收入    
			RowVO rvoActualIn = new RowVO("actualIn", RowVO.OPERATION_UNDEFINED); // 实际收入    
			RowVO rvoPlanInCash = new RowVO("planInCash", RowVO.OPERATION_UNDEFINED); // 计划现金收入    
			RowVO rvoActualInCash = new RowVO("actualInCash", RowVO.OPERATION_UNDEFINED); // 实际现金收入    
			RowVO rvoSurplusMonth = new RowVO("surplusMonth", RowVO.OPERATION_UNDEFINED); // 当月结余    
			RowVO rvoSurplusCount = new RowVO("surplusCount", RowVO.OPERATION_UNDEFINED); // 累积结余    
			RowVO rvoSurplusCash = new RowVO("surplusCash", RowVO.OPERATION_UNDEFINED); // 现金结余    
			tvoCount.addRowVO(rvoPlanOut);
			tvoCount.addRowVO(rvoActualOut);
			tvoCount.addRowVO(rvoOutDeviation);
			tvoCount.addRowVO(rvoPlanIn);
			tvoCount.addRowVO(rvoActualIn);
			//		tvoCount.addRowVO(rvoPlanInCash);
			//		tvoCount.addRowVO(rvoActualInCash);
			tvoCount.addRowVO(rvoSurplusMonth);
			tvoCount.addRowVO(rvoSurplusCount);
			tvoCount.addRowVO(rvoSurplusCash);

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

			/*
			 * 计算[支出偏差][当月结余][累积结余][现金结余]
			 */
			String surplusCount = "0";// 累积结余
			String surplusCash = "0";// 现金结余
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
			RowVO rvoHead = new RowVO();// 根据用户选择的月份区间生成的头信息
			tvoCount.setHeadRowVO(rvoHead);
			while (calFirst.getTimeInMillis() <= calEnd.getTimeInMillis()) {
				String yearMonth = sdf_yearMonth.format(calFirst.getTime());
				// 处理Head
				if (calFirst.getTimeInMillis() >= calStart.getTimeInMillis()) {
					rvoHead.addCellVO(new CellVO(yearMonth, yearMonth));
				}

				String planOut = "0"; // 计划支出
				String actualOut = "0";// 实际支出
				String planIn = "0";// 计划收入
				String actualIn = "0";// 实际收入
				String planInCash = "0";// 计划收入现金
				String actualInCash = "0";// 实际收入现金

				TableVO tvoMonth = new TableVO();// 记录当月所有的收支,用于计算结余

				/*
				 * 计划支出
				 */
				HashSet<String> hsExistOutTypeId = new HashSet<String>();// 记录当月已经生成计划支出记录的支出类型
				if (hmYearMonthToPlanOutRowVOArray.containsKey(yearMonth)) {
					for (RowVO rvo : hmYearMonthToPlanOutRowVOArray.get(yearMonth)) {
						String typeId = rvo.getCellVOValue("CR_EXPENDITURE_TYPE_ID");// 支出类型
						String amount = rvo.getCellVOValue("CN_AMOUNT");// 金额
						planOut = MyDBHelper.doubleAdd(planOut, amount);
						hsExistOutTypeId.add(typeId);
						// 当月所有的收支
						RowVO rvoMonth = new RowVO();
						tvoMonth.addRowVO(rvoMonth);
						rvoMonth.addCellVO(new CellVO("actualPlan", "2"));// 实际 1 | 计划 2
						rvoMonth.addCellVO(new CellVO("isOut", Definition.YES));
						rvoMonth.addCellVO(new CellVO("typeId", typeId));
						rvoMonth.addCellVO(new CellVO("amount", amount));
					}
				}
				for (RowVO rvo : tvoExpenditureType.toRowVOs()) {
					String periodicFlag = rvo.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性支出
					if (!StringUtils.equals(Definition.YES, periodicFlag)) {
						continue;
					}

					String typeId = rvo.getCellVOValue("CN_ID");
					String startDate = rvo.getCellVOValue("CN_START_DATE");
					String endDate = rvo.getCellVOValue("CN_END_DATE");
					if (hsExistOutTypeId.contains(typeId)) {
						continue;// 过滤 - 当前类型已经生成预告记录
					}
					if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
						continue;// 过滤 - 没到开始时间
					}
					if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
						continue;// 过滤 - 超过结束时间
					}

					String effectiveDay = rvo.getCellVOValue("CN_EFFECTIVE_DAY");// 生效日期
					String amount = rvo.getCellVOValue("CN_AMOUNT");
					if (Integer.parseInt(effectiveDay) < 10) {
						effectiveDay = yearMonth + "0" + effectiveDay;
					} else {
						effectiveDay = yearMonth + effectiveDay;
					}
					if (sdf_date.parse(effectiveDay).getTime() > currentTime_long) {
						// 将生效日期大于当前日期的周期行预告加入汇总
						planOut = MyDBHelper.doubleAdd(planOut, amount);
						// 当月所有的收支
						RowVO rvoMonth = new RowVO();
						tvoMonth.addRowVO(rvoMonth);
						rvoMonth.addCellVO(new CellVO("actualPlan", "2"));// 实际 1 | 计划 2
						rvoMonth.addCellVO(new CellVO("isOut", Definition.YES));
						rvoMonth.addCellVO(new CellVO("typeId", typeId));
						rvoMonth.addCellVO(new CellVO("amount", amount));
					}
				}

				/*
				 * 实际支出
				 */
				if (hmYearMonthToActualOutRowVOArray.containsKey(yearMonth)) {
					for (RowVO rvo : hmYearMonthToActualOutRowVOArray.get(yearMonth)) {
						String typeId = rvo.getCellVOValue("CR_EXPENDITURE_TYPE_ID");// 支出类型
						String amount = rvo.getCellVOValue("CN_AMOUNT");// 金额
						actualOut = MyDBHelper.doubleAdd(actualOut, amount);
						// 当月所有的收支
						RowVO rvoMonth = new RowVO();
						tvoMonth.addRowVO(rvoMonth);
						rvoMonth.addCellVO(new CellVO("actualPlan", "1"));// 实际 1 | 计划 2
						rvoMonth.addCellVO(new CellVO("isOut", Definition.YES));
						rvoMonth.addCellVO(new CellVO("typeId", typeId));
						rvoMonth.addCellVO(new CellVO("amount", amount));
					}
				}

				/*
				 * 计划收入
				 */
				HashSet<String> hsExistInTypeId = new HashSet<String>();// 记录当月已经生成计划收入记录的收入类型
				if (hmYearMonthToPlanInRowVOArray.containsKey(yearMonth)) {
					for (RowVO rvo : hmYearMonthToPlanInRowVOArray.get(yearMonth)) {
						String typeId = rvo.getCellVOValue("CR_INCOME_TYPE_ID");// 收入类型
						String amount = rvo.getCellVOValue("CN_AMOUNT");// 金额
						planIn = MyDBHelper.doubleAdd(planIn, amount);
						if (hsCashInTypeId.contains(typeId)) {
							planInCash = MyDBHelper.doubleAdd(planInCash, amount);
						}
						hsExistInTypeId.add(typeId);
						// 当月所有的收支
						RowVO rvoMonth = new RowVO();
						tvoMonth.addRowVO(rvoMonth);
						rvoMonth.addCellVO(new CellVO("actualPlan", "2"));// 实际 1 | 计划 2
						rvoMonth.addCellVO(new CellVO("isOut", Definition.NO));
						rvoMonth.addCellVO(new CellVO("typeId", typeId));
						rvoMonth.addCellVO(new CellVO("amount", amount));
					}
				}
				for (RowVO rvo : tvoIncomeType.toRowVOs()) {
					String periodicFlag = rvo.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性收入
					if (!StringUtils.equals(Definition.YES, periodicFlag)) {
						continue;
					}
					String typeId = rvo.getCellVOValue("CN_ID");
					String startDate = rvo.getCellVOValue("CN_START_DATE");
					String endDate = rvo.getCellVOValue("CN_END_DATE");
					if (hsExistInTypeId.contains(typeId)) {
						continue;// 过滤 - 当前类型已经生成预告记录
					}
					if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
						continue;// 过滤 - 没到开始时间
					}
					if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
						continue;// 过滤 - 超过结束时间
					}

					String effectiveDay = rvo.getCellVOValue("CN_EFFECTIVE_DAY");// 生效日期
					String amount = rvo.getCellVOValue("CN_AMOUNT");
					if (Integer.parseInt(effectiveDay) < 10) {
						effectiveDay = yearMonth + "0" + effectiveDay;
					} else {
						effectiveDay = yearMonth + effectiveDay;
					}
					if (sdf_date.parse(effectiveDay).getTime() > currentTime_long) {
						// 将生效日期大于当前日期的周期行预告加入汇总
						planIn = MyDBHelper.doubleAdd(planIn, amount);
						if (hsCashInTypeId.contains(typeId)) {
							planInCash = MyDBHelper.doubleAdd(planInCash, amount);
						}
						// 当月所有的收支
						RowVO rvoMonth = new RowVO();
						tvoMonth.addRowVO(rvoMonth);
						rvoMonth.addCellVO(new CellVO("actualPlan", "2"));// 实际 1 | 计划 2
						rvoMonth.addCellVO(new CellVO("isOut", Definition.NO));
						rvoMonth.addCellVO(new CellVO("typeId", typeId));
						rvoMonth.addCellVO(new CellVO("amount", amount));
					}
				}

				/*
				 * 实际收入
				 */
				if (hmYearMonthToActualInRowVOArray.containsKey(yearMonth)) {
					for (RowVO rvo : hmYearMonthToActualInRowVOArray.get(yearMonth)) {
						String typeId = rvo.getCellVOValue("CR_INCOME_TYPE_ID");// 收入类型
						String amount = rvo.getCellVOValue("CN_AMOUNT");// 金额
						actualIn = MyDBHelper.doubleAdd(actualIn, amount);
						if (hsCashInTypeId.contains(typeId)) {
							actualInCash = MyDBHelper.doubleAdd(actualInCash, amount);
						}
						// 当月所有的收支
						RowVO rvoMonth = new RowVO();
						tvoMonth.addRowVO(rvoMonth);
						rvoMonth.addCellVO(new CellVO("actualPlan", "1"));// 实际 1 | 计划 2
						rvoMonth.addCellVO(new CellVO("isOut", Definition.NO));
						rvoMonth.addCellVO(new CellVO("typeId", typeId));
						rvoMonth.addCellVO(new CellVO("amount", amount));
					}
				}

				// 处理支出偏差    
				String deviation = "0.0";
				if (Double.parseDouble(planOut) > 0) {
					deviation = MyDBHelper.doubleDiv(actualOut, planOut);
					if (Double.parseDouble(deviation) == 1) {
						deviation = "0.0";
					}
				}

				/*
				 * 结余计算
				 *  当前月份之前的月份使用实际数据,当前月份以及之后的月份使用预告数据
				 */
				String surplusMonth = "0";
				if (Integer.parseInt(yearMonth) < currentYearMonth) {
					// 处理当月结余
					surplusMonth = MyDBHelper.doubleSub(actualIn, actualOut);
					// 累积结余
					surplusCount = MyDBHelper.doubleAdd(surplusCount, surplusMonth);
					// 现金结余
					surplusCash = MyDBHelper.doubleAdd(surplusCash, MyDBHelper.doubleSub(actualInCash, actualOut));
				} else {
					tvoMonth.sortByColumn(new Sort("actualPlan"));// 排序,实际在前在计划后 实际 1 | 计划 2
					HashSet<String> actualTypeId = new HashSet<String>();// 记录有实际收支的收支类型,用于计算结余时过滤同类型计划收支记录.
					for (RowVO rvoMonth : tvoMonth.toRowVOs()) {
						String actualPlan = rvoMonth.getCellVOValue("actualPlan");// 实际 1 | 计划 2
						String isOut = rvoMonth.getCellVOValue("isOut");
						String typeId = rvoMonth.getCellVOValue("typeId");
						String amount = rvoMonth.getCellVOValue("amount");
						if (StringUtils.equals(actualPlan, "1")) {
							actualTypeId.add(typeId);
						} else if (actualTypeId.contains(typeId)) {
							continue;// 已经有实际的收支,则过滤掉计划
						}
						if (StringUtils.equals(isOut, Definition.YES)) {
							// 当月结余
							surplusMonth = MyDBHelper.doubleSub(surplusMonth, amount);
							// 累积结余
							surplusCount = MyDBHelper.doubleSub(surplusCount, amount);
							// 现金结余
							surplusCash = MyDBHelper.doubleSub(surplusCash, amount);
						} else {
							// 当月结余
							surplusMonth = MyDBHelper.doubleAdd(surplusMonth, amount);
							// 累积结余
							surplusCount = MyDBHelper.doubleAdd(surplusCount, amount);
							// 现金结余
							surplusCash = MyDBHelper.doubleAdd(surplusCash, amount);
						}
					}
				}

				/*
				 * 处理借贷
				 */
				if (hmYearMonthToDACRowVO.containsKey(yearMonth)) {
					for (RowVO rvoDAC : hmYearMonthToDACRowVO.get(yearMonth)) {
						String inFlag = rvoDAC.getCellVOValue("CR_IN_FLAG");
						String amount = rvoDAC.getCellVOValue("CN_AMOUNT");
						if (StringUtils.equals(inFlag, Definition.YES)) {
							// 借入
							surplusCount = MyDBHelper.doubleAdd(surplusCount, amount);
							surplusCash = MyDBHelper.doubleAdd(surplusCash, amount);
						} else {
							// 借出
							surplusCash = MyDBHelper.doubleSub(surplusCash, amount);
						}
					}
				}

				rvoPlanOut.addCellVO(new CellVO(yearMonth, planOut));
				rvoActualOut.addCellVO(new CellVO(yearMonth, actualOut));
				rvoOutDeviation.addCellVO(new CellVO(yearMonth, deviation));
				rvoPlanIn.addCellVO(new CellVO(yearMonth, planIn));
				rvoActualIn.addCellVO(new CellVO(yearMonth, actualIn));
				rvoPlanInCash.addCellVO(new CellVO(yearMonth, planInCash));
				rvoActualInCash.addCellVO(new CellVO(yearMonth, actualInCash));
				rvoSurplusMonth.addCellVO(new CellVO(yearMonth, surplusMonth));
				rvoSurplusCount.addCellVO(new CellVO(yearMonth, surplusCount));
				rvoSurplusCash.addCellVO(new CellVO(yearMonth, surplusCash));

				calFirst.add(Calendar.MONTH, 1);// 加一个月
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
			rvoPlanOut.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:red;'>计划支出</span>"));
			rvoActualOut.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:red;'>实际支出</span>"));
			rvoOutDeviation.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:blue;'>支出偏差</span>"));
			rvoPlanIn.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:green;'>计划收入</span>"));
			rvoActualIn.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:green;'>实际收入</span>"));
			rvoPlanInCash.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:green;'>计划现金收入</span>"));
			rvoActualInCash.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:green;'>实际现金收入</span>"));
			rvoSurplusMonth.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:purple;'>当月结余</span>"));
			rvoSurplusCount.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:purple;'>累积结余</span>"));
			rvoSurplusCash.addCellVO(new CellVO("type", "<span style='font-weight:bold;color:purple;'>现金结余</span>"));
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
					} else if (type.contains("当月结余")) {
						typeKey = "surplusMonth";
					}
					for (CellVO cvo : rvo.toCellVOs()) {
						if (StringUtils.equals("type", cvo.getKey())) {
							continue;
						}
						cvo.setValue("<div sytle='height: 100%;cursor:pointer;border-bottom: 1px dashed blue;' onclick=\"modifyDetailData('" + typeKey + "','" + cvo.getKey() + "')\">" + cvo.getValue() + "</div>");
					}
				} else if (type.contains("当月结余")) {
					for (CellVO cvo : rvo.toCellVOs()) {
						if (StringUtils.equals("type", cvo.getKey())) {
							continue;
						}
						cvo.setValue("<div sytle='height: 100%;cursor:pointer;border-bottom: 1px dashed blue;' onclick=\"surplusMonthDetail('" + cvo.getKey() + "')\">" + cvo.getValue() + "</div>");
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

	/**
	 * 总账页面 - 明细维护页
	 * @return
	 */
	@RequestMapping(value = "/generaledger/modifyDetailData", method = RequestMethod.GET)
	public ModelAndView generaledger_modifyDetailData(HttpServletRequest request) {
		try {
			SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
			long currentTime_long = System.currentTimeMillis();
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
			String yearMonth = request.getParameter("yearMonth"); // yyyyMM

			request.setAttribute("yearMonth", yearMonth);
			request.setAttribute("type", type);

			// 读取所有的支出类型
			TableVO tvoExpenditureType = this.baseDAO.queryForTableVO("tn_expenditure_type");
			// 读取所有的收入类型
			TableVO tvoIncomeType = this.baseDAO.queryForTableVO("tn_income_type");
			HashMap<String, String> hmTypeIdToName = tvoExpenditureType.toHashMapOfToCellVOValue("CN_ID", "CN_NAME");
			hmTypeIdToName.putAll(tvoIncomeType.toHashMapOfToCellVOValue("CN_ID", "CN_NAME"));

			TableVO tvoReturn = new TableVO();
			if (StringUtils.equals("planOut", typeKey) || StringUtils.equals("actualOut", typeKey)) {
				// 支出 - 有记录
				CondSetBean csb = new CondSetBean();
				csb.addCondBean_between("CN_CREATE_DATE", MyDBHelper.getFirstDayOfMonth(yearMonth, "yyyyMM"), MyDBHelper.getLastDayOfMonth(yearMonth, "yyyyMM"));
				if (StringUtils.equals("planOut", typeKey)) {
					csb.addCondBean_and_equal("CR_PLAN_FLAG", Definition.YES);
				} else {
					csb.addCondBean_and_equal("CR_PLAN_FLAG", Definition.NO);
				}
				TableVO tvoExpenditure = this.baseDAO.queryForTableVO("tn_expenditure", csb);
				HashSet<String> hsExistTypeId = new HashSet<String>();
				for (RowVO rvo : tvoExpenditure.toRowVOs()) {
					String id = rvo.getCellVOValue("CN_ID");
					String typeId = rvo.getCellVOValue("CR_EXPENDITURE_TYPE_ID");
					String amount = rvo.getCellVOValue("CN_AMOUNT");
					String createDate = rvo.getCellVOValue("CN_CREATE_DATE");
					String desc = rvo.getCellVOValue("CN_DESCRIPTION");
					String typeName = hmTypeIdToName.get(typeId);
					hsExistTypeId.add(typeId);

					RowVO rvoReturn = new RowVO();
					tvoReturn.addRowVO(rvoReturn);
					rvoReturn.addCellVO(new CellVO("id", id));
					rvoReturn.addCellVO(new CellVO("typeName", typeName));
					rvoReturn.addCellVO(new CellVO("amount", amount));
					rvoReturn.addCellVO(new CellVO("createDate", createDate));
					rvoReturn.addCellVO(new CellVO("desc", desc));
				}

				// 计划支出 - 无记录
				if (StringUtils.equals("planOut", typeKey)) {
					for (RowVO rvoExpenditureType : tvoExpenditureType.toRowVOs()) {
						String periodicFlag = rvoExpenditureType.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性支出
						if (!StringUtils.equals(Definition.YES, periodicFlag)) {
							continue;
						}
						String typeId = rvoExpenditureType.getCellVOValue("CN_ID");
						String startDate = rvoExpenditureType.getCellVOValue("CN_START_DATE");
						String endDate = rvoExpenditureType.getCellVOValue("CN_END_DATE");
						if (hsExistTypeId.contains(typeId)) {
							continue;// 过滤 - 当前类型已经生成预告记录
						}
						if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
							continue;// 过滤 - 没到开始时间
						}
						if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
							continue;// 过滤 - 超过结束时间
						}

						String typeName = rvoExpenditureType.getCellVOValue("CN_NAME");
						String effectiveDay = rvoExpenditureType.getCellVOValue("CN_EFFECTIVE_DAY");
						String amount = rvoExpenditureType.getCellVOValue("CN_AMOUNT");
						if (Integer.parseInt(effectiveDay) < 10) {
							effectiveDay = yearMonth + "0" + effectiveDay;
						} else {
							effectiveDay = yearMonth + effectiveDay;
						}
						if (sdf_date.parse(effectiveDay).getTime() > currentTime_long) {
							// 将周期行预告放入返回页面,只读显示.
							RowVO rvoReturn = new RowVO();
							tvoReturn.addRowVO(rvoReturn);
							rvoReturn.addCellVO(new CellVO("id", ""));
							rvoReturn.addCellVO(new CellVO("typeName", typeName));
							rvoReturn.addCellVO(new CellVO("amount", amount));
							rvoReturn.addCellVO(new CellVO("createDate", "" + MyDBHelper.getDatetime(effectiveDay, "yyyyMMdd")));
							rvoReturn.addCellVO(new CellVO("desc", ""));
						}
					}
				}

			} else if (StringUtils.equals("planIn", typeKey) || StringUtils.equals("actualIn", typeKey)) {
				// 收入
				CondSetBean csb = new CondSetBean();
				csb.addCondBean_between("CN_CREATE_DATE", MyDBHelper.getFirstDayOfMonth(yearMonth, "yyyyMM"), MyDBHelper.getLastDayOfMonth(yearMonth, "yyyyMM"));
				if (StringUtils.equals("planIn", typeKey)) {
					csb.addCondBean_and_equal("CR_PLAN_FLAG", Definition.YES);
				} else {
					csb.addCondBean_and_equal("CR_PLAN_FLAG", Definition.NO);
				}
				TableVO tvoIncome = this.baseDAO.queryForTableVO("tn_income", csb);
				HashSet<String> hsExistTypeId = new HashSet<String>();
				for (RowVO rvo : tvoIncome.toRowVOs()) {
					String id = rvo.getCellVOValue("CN_ID");
					String typeId = rvo.getCellVOValue("CR_INCOME_TYPE_ID");
					String amount = rvo.getCellVOValue("CN_AMOUNT");
					String createDate = rvo.getCellVOValue("CN_CREATE_DATE");
					String desc = rvo.getCellVOValue("CN_DESCRIPTION");
					String typeName = hmTypeIdToName.get(typeId);
					hsExistTypeId.add(typeId);

					RowVO rvoReturn = new RowVO();
					tvoReturn.addRowVO(rvoReturn);
					rvoReturn.addCellVO(new CellVO("id", id));
					rvoReturn.addCellVO(new CellVO("typeName", typeName));
					rvoReturn.addCellVO(new CellVO("amount", amount));
					rvoReturn.addCellVO(new CellVO("createDate", createDate));
					rvoReturn.addCellVO(new CellVO("desc", desc));
				}

				// 计划收入 - 无记录
				if (StringUtils.equals("planIn", typeKey)) {
					for (RowVO rvoIncomeType : tvoIncomeType.toRowVOs()) {
						String periodicFlag = rvoIncomeType.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性收入
						if (!StringUtils.equals(Definition.YES, periodicFlag)) {
							continue;
						}
						String typeId = rvoIncomeType.getCellVOValue("CN_ID");
						String startDate = rvoIncomeType.getCellVOValue("CN_START_DATE");
						String endDate = rvoIncomeType.getCellVOValue("CN_END_DATE");
						if (hsExistTypeId.contains(typeId)) {
							continue;// 过滤 - 当前类型已经生成预告记录
						}
						if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
							continue;// 过滤 - 没到开始时间
						}
						if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
							continue;// 过滤 - 超过结束时间
						}

						String typeName = rvoIncomeType.getCellVOValue("CN_NAME");
						String effectiveDay = rvoIncomeType.getCellVOValue("CN_EFFECTIVE_DAY");
						String amount = rvoIncomeType.getCellVOValue("CN_AMOUNT");
						if (Integer.parseInt(effectiveDay) < 10) {
							effectiveDay = yearMonth + "0" + effectiveDay;
						} else {
							effectiveDay = yearMonth + effectiveDay;
						}
						if (sdf_date.parse(effectiveDay).getTime() > currentTime_long) {
							// 将周期行预告放入返回页面,只读显示.
							RowVO rvoReturn = new RowVO();
							tvoReturn.addRowVO(rvoReturn);
							rvoReturn.addCellVO(new CellVO("id", ""));
							rvoReturn.addCellVO(new CellVO("typeName", typeName));
							rvoReturn.addCellVO(new CellVO("amount", amount));
							rvoReturn.addCellVO(new CellVO("createDate", "" + MyDBHelper.getDatetime(effectiveDay, "yyyyMMdd")));
							rvoReturn.addCellVO(new CellVO("desc", ""));
						}
					}
				}
			}

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
	 * @return
	 */
	@RequestMapping(value = "/generaledger/surplusMonthDetail", method = RequestMethod.GET)
	public ModelAndView generaledger_surplusMonthDetail(HttpServletRequest request) {
		try {
			SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
			long currentTime_long = System.currentTimeMillis();
			String type = "<span style='font-weight:bold;color:purple;'>当月结余</span>";
			String yearMonth = request.getParameter("yearMonth"); // yyyyMM
			request.setAttribute("yearMonth", yearMonth);
			request.setAttribute("type", type);

			// 读取所有的支出类型
			TableVO tvoExpenditureType = this.baseDAO.queryForTableVO("tn_expenditure_type");
			// 读取所有的收入类型
			TableVO tvoIncomeType = this.baseDAO.queryForTableVO("tn_income_type");
			HashMap<String, String> hmTypeIdToName = tvoExpenditureType.toHashMapOfToCellVOValue("CN_ID", "CN_NAME");
			hmTypeIdToName.putAll(tvoIncomeType.toHashMapOfToCellVOValue("CN_ID", "CN_NAME"));

			TableVO tvoMonthRecord = new TableVO();// 当月的收支记录集合
			// 支出 - 有记录
			CondSetBean csbExpenditure = new CondSetBean();
			csbExpenditure.addCondBean_between("CN_CREATE_DATE", MyDBHelper.getFirstDayOfMonth(yearMonth, "yyyyMM"), MyDBHelper.getLastDayOfMonth(yearMonth, "yyyyMM"));

			TableVO tvoExpenditure = this.baseDAO.queryForTableVO("tn_expenditure", csbExpenditure);
			HashSet<String> hsExistTypeId = new HashSet<String>();
			for (RowVO rvo : tvoExpenditure.toRowVOs()) {
				String planFlag = rvo.getCellVOValue("CR_PLAN_FLAG");
				String typeId = rvo.getCellVOValue("CR_EXPENDITURE_TYPE_ID");
				String amount = rvo.getCellVOValue("CN_AMOUNT");
				String createDate = rvo.getCellVOValue("CN_CREATE_DATE");
				String desc = rvo.getCellVOValue("CN_DESCRIPTION");
				String typeName = hmTypeIdToName.get(typeId);
				hsExistTypeId.add(typeId);

				RowVO rvoMonthRecord = new RowVO();
				tvoMonthRecord.addRowVO(rvoMonthRecord);
				rvoMonthRecord.addCellVO(new CellVO("planFlag", planFlag));
				rvoMonthRecord.addCellVO(new CellVO("typeName", typeName));
				rvoMonthRecord.addCellVO(new CellVO("amount", amount));
				rvoMonthRecord.addCellVO(new CellVO("createDate", createDate));
				rvoMonthRecord.addCellVO(new CellVO("desc", desc));
				rvoMonthRecord.addCellVO(new CellVO("inOut", "<span style='font-weight:bold;color:red;'>支出</span>"));
			}

			// 计划支出 - 无记录
			for (RowVO rvoExpenditureType : tvoExpenditureType.toRowVOs()) {
				String periodicFlag = rvoExpenditureType.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性支出
				if (!StringUtils.equals(Definition.YES, periodicFlag)) {
					continue;
				}
				String typeId = rvoExpenditureType.getCellVOValue("CN_ID");
				String startDate = rvoExpenditureType.getCellVOValue("CN_START_DATE");
				String endDate = rvoExpenditureType.getCellVOValue("CN_END_DATE");
				if (hsExistTypeId.contains(typeId)) {
					continue;// 过滤 - 当前类型已经生成预告记录
				}
				if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
					continue;// 过滤 - 没到开始时间
				}
				if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
					continue;// 过滤 - 超过结束时间
				}

				String typeName = rvoExpenditureType.getCellVOValue("CN_NAME");
				String effectiveDay = rvoExpenditureType.getCellVOValue("CN_EFFECTIVE_DAY");
				String amount = rvoExpenditureType.getCellVOValue("CN_AMOUNT");
				if (Integer.parseInt(effectiveDay) < 10) {
					effectiveDay = yearMonth + "0" + effectiveDay;
				} else {
					effectiveDay = yearMonth + effectiveDay;
				}
				if (sdf_date.parse(effectiveDay).getTime() > currentTime_long) {
					// 将周期行预告放入返回页面,只读显示.
					RowVO rvoMonthRecord = new RowVO();
					tvoMonthRecord.addRowVO(rvoMonthRecord);
					rvoMonthRecord.addCellVO(new CellVO("planFlag", Definition.YES));
					rvoMonthRecord.addCellVO(new CellVO("typeName", typeName));
					rvoMonthRecord.addCellVO(new CellVO("amount", amount));
					rvoMonthRecord.addCellVO(new CellVO("createDate", "" + MyDBHelper.getDatetime(effectiveDay, "yyyyMMdd")));
					rvoMonthRecord.addCellVO(new CellVO("desc", "计划支出 - 无记录"));
					rvoMonthRecord.addCellVO(new CellVO("inOut", "<span style='font-weight:bold;color:red;'>支出</span>"));
				}
			}

			// 收入
			CondSetBean csbIncome = new CondSetBean();
			csbIncome.addCondBean_between("CN_CREATE_DATE", MyDBHelper.getFirstDayOfMonth(yearMonth, "yyyyMM"), MyDBHelper.getLastDayOfMonth(yearMonth, "yyyyMM"));
			TableVO tvoIncome = this.baseDAO.queryForTableVO("tn_income", csbIncome);
			for (RowVO rvo : tvoIncome.toRowVOs()) {
				String planFlag = rvo.getCellVOValue("CR_PLAN_FLAG");
				String typeId = rvo.getCellVOValue("CR_INCOME_TYPE_ID");
				String amount = rvo.getCellVOValue("CN_AMOUNT");
				String createDate = rvo.getCellVOValue("CN_CREATE_DATE");
				String desc = rvo.getCellVOValue("CN_DESCRIPTION");
				String typeName = hmTypeIdToName.get(typeId);
				hsExistTypeId.add(typeId);

				RowVO rvoMonthRecord = new RowVO();
				tvoMonthRecord.addRowVO(rvoMonthRecord);
				rvoMonthRecord.addCellVO(new CellVO("planFlag", planFlag));
				rvoMonthRecord.addCellVO(new CellVO("typeName", typeName));
				rvoMonthRecord.addCellVO(new CellVO("amount", amount));
				rvoMonthRecord.addCellVO(new CellVO("createDate", createDate));
				rvoMonthRecord.addCellVO(new CellVO("desc", desc));
				rvoMonthRecord.addCellVO(new CellVO("inOut", "<span style='font-weight:bold;color:green;'>收入</span>"));
			}

			// 计划收入 - 无记录
			for (RowVO rvoIncomeType : tvoIncomeType.toRowVOs()) {
				String periodicFlag = rvoIncomeType.getCellVOValue("CR_PERIODIC_FLAG");// 是否为周期性收入
				if (!StringUtils.equals(Definition.YES, periodicFlag)) {
					continue;
				}
				String typeId = rvoIncomeType.getCellVOValue("CN_ID");
				String startDate = rvoIncomeType.getCellVOValue("CN_START_DATE");
				String endDate = rvoIncomeType.getCellVOValue("CN_END_DATE");
				if (hsExistTypeId.contains(typeId)) {
					continue;// 过滤 - 当前类型已经生成预告记录
				}
				if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
					continue;// 过滤 - 没到开始时间
				}
				if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
					continue;// 过滤 - 超过结束时间
				}

				String typeName = rvoIncomeType.getCellVOValue("CN_NAME");
				String effectiveDay = rvoIncomeType.getCellVOValue("CN_EFFECTIVE_DAY");
				String amount = rvoIncomeType.getCellVOValue("CN_AMOUNT");
				if (Integer.parseInt(effectiveDay) < 10) {
					effectiveDay = yearMonth + "0" + effectiveDay;
				} else {
					effectiveDay = yearMonth + effectiveDay;
				}
				if (sdf_date.parse(effectiveDay).getTime() > currentTime_long) {
					// 将周期行预告放入返回页面,只读显示.
					RowVO rvoMonthRecord = new RowVO();
					tvoMonthRecord.addRowVO(rvoMonthRecord);
					rvoMonthRecord.addCellVO(new CellVO("planFlag", Definition.YES));
					rvoMonthRecord.addCellVO(new CellVO("typeName", typeName));
					rvoMonthRecord.addCellVO(new CellVO("amount", amount));
					rvoMonthRecord.addCellVO(new CellVO("createDate", "" + MyDBHelper.getDatetime(effectiveDay, "yyyyMMdd")));
					rvoMonthRecord.addCellVO(new CellVO("desc", "计划收入 - 无记录"));
					rvoMonthRecord.addCellVO(new CellVO("inOut", "<span style='font-weight:bold;color:green;'>收入</span>"));
				}
			}

			TableVO tvoReturn = new TableVO();
			/*
			 *  将实际收支加入返回Table
			 */
			if (tvoMonthRecord.toHashMapOfToArrayRowVO("planFlag").containsKey(Definition.NO)) {
				for (RowVO rvo : tvoMonthRecord.toHashMapOfToArrayRowVO("planFlag").get(Definition.NO)) {
					String typeName = rvo.getCellVOValue("typeName");
					String amount = rvo.getCellVOValue("amount");
					String createDate = rvo.getCellVOValue("createDate");
					String desc = rvo.getCellVOValue("desc");
					String inOut = rvo.getCellVOValue("inOut");

					RowVO rvoReturn = new RowVO();
					tvoReturn.addRowVO(rvoReturn);
					rvoReturn.addCellVO(new CellVO("typeName", typeName));
					rvoReturn.addCellVO(new CellVO("createDate", createDate));
					rvoReturn.addCellVO(new CellVO("inOut", inOut));
					rvoReturn.addCellVO(new CellVO("amount_actual", amount));
					rvoReturn.addCellVO(new CellVO("amount_plan", "N/A"));
					rvoReturn.addCellVO(new CellVO("desc", desc));
				}
			}
			/*
			 *  处理计划支出
			 *  	1. 将有对应实际支出类型的,将计划对应到差异较小的实际记录上
			 *  	2. 如果没有对应的实际,则生成一条新的返回记录.实际为''.
			 */
			HashMap<String, ArrayList<RowVO>> hmTypeToActualRowVOArray = tvoReturn.toHashMapOfToArrayRowVO("typeName");// 记录类型对应的实际记录
			if (tvoMonthRecord.toHashMapOfToArrayRowVO("planFlag").containsKey(Definition.YES)) {
				for (RowVO rvo : tvoMonthRecord.toHashMapOfToArrayRowVO("planFlag").get(Definition.YES)) {
					String typeName = rvo.getCellVOValue("typeName");
					String amount = rvo.getCellVOValue("amount");
					String createDate = rvo.getCellVOValue("createDate");
					String desc = rvo.getCellVOValue("desc");
					String inOut = rvo.getCellVOValue("inOut");

					if (hmTypeToActualRowVOArray.containsKey(typeName)) {
						double min = 999999999;
						for (RowVO rvoReturn : hmTypeToActualRowVOArray.get(typeName)) {
							double t = Math.abs(Double.parseDouble(MyDBHelper.doubleSub(rvoReturn.getCellVOValue("amount_actual"), amount)));
							if (t < min) {
								min = t;
							}
						}
						for (RowVO rvoReturn : hmTypeToActualRowVOArray.get(typeName)) {
							double t = Math.abs(Double.parseDouble(MyDBHelper.doubleSub(rvoReturn.getCellVOValue("amount_actual"), amount)));
							if (t == min) {
								rvoReturn.setCellVOValue("amount_plan", amount);
							}
						}

					} else {
						RowVO rvoReturn = new RowVO();
						tvoReturn.addRowVO(rvoReturn);
						rvoReturn.addCellVO(new CellVO("typeName", typeName));
						rvoReturn.addCellVO(new CellVO("createDate", createDate));
						rvoReturn.addCellVO(new CellVO("inOut", inOut));
						rvoReturn.addCellVO(new CellVO("amount_actual", "N/A"));
						rvoReturn.addCellVO(new CellVO("amount_plan", amount));
						rvoReturn.addCellVO(new CellVO("desc", desc));
					}
				}
			}
			// 排序
			tvoReturn.sortByColumn(new Sort("createDate"), new Sort("inOut"), new Sort("typeName"));
			tvoReturn.formatDate("yyyy-MM-dd", "createDate");

			request.setAttribute("tvoReturn", tvoReturn);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 跳转到[总账页面-明细数据修改页]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.layer.generaledger.surplusMonthDetail");
		return mv;
	}

}

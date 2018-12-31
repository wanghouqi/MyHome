package hq.myhome.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import hq.mydb.dao.BaseDAO;
import hq.mydb.data.CellVO;
import hq.mydb.data.RowVO;
import hq.mydb.data.TableVO;
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
		String yearMonthRange = request.getParameter("yearMonthRange");
		if (StringUtils.isEmpty(yearMonthRange)) {
			yearMonthRange = MyDBHelper.formatDate(MyDBHelper.getFirstDayOfYear(new Date().getTime()), "yyyy/MM") + " - "
					+ MyDBHelper.formatDate(MyDBHelper.getLastDayOfYear(new Date().getTime()), "yyyy/MM");
			request.setAttribute("yearMonthRange", yearMonthRange);
		}
		String startMonth = yearMonthRange.split(" - ")[0];
		String endMonth = yearMonthRange.split(" - ")[1];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		// 读取所有的支出记录
		TableVO tvoExpenditure = this.baseDAO.queryForTableVO("tn_expenditure");
		// 读取所有的收入记录
		TableVO tvoIncome = this.baseDAO.queryForTableVO("tn_income");
		// 读取所有的支出类型
		TableVO tvoExpenditureType = this.baseDAO.queryForTableVO("tn_expenditure_type");
		// 读取所有的收入类型
		TableVO tvoIncomeType = this.baseDAO.queryForTableVO("tn_income_type");
		int currentYearMonth = Integer.parseInt(sdf.format(new Date()));// 当前年月的整数形201812,用于后面的月份比对

		/*
		 * 计算每个月的计划支出和计划收入,用于显示当前月份之后的月份的计划支出和收入
		 */
		HashMap<String, Boolean> hmTypeIdToActive = new HashMap<String, Boolean>();// 记录收支类型是否为[活动资金]
		String planInByMonth = "0";// 每个月的计划收入
		String planOutByMonth = "0";// 每个月的计划支出
		for (RowVO rvo : tvoExpenditureType.toRowVOs()) {
			String periodicFlag = rvo.getCellVOValue("CR_PERIODIC_FLAG");
			if (StringUtils.equals(Definition.YES, periodicFlag)) {
				String amount = rvo.getCellVOValue("CN_AMOUNT");
				planOutByMonth = MyDBHelper.doubleAdd(planOutByMonth, amount);
			}
		}
		for (RowVO rvo : tvoIncomeType.toRowVOs()) {
			String id = rvo.getCellVOValue("CN_ID");
			String periodicFlag = rvo.getCellVOValue("CR_PERIODIC_FLAG");
			String frozenFlag = rvo.getCellVOValue("CN_FROZEN_FLAG");
			if (StringUtils.equals(Definition.YES, periodicFlag)) {
				String amount = rvo.getCellVOValue("CN_AMOUNT");
				planInByMonth = MyDBHelper.doubleAdd(planInByMonth, amount);
			}
			hmTypeIdToActive.put(id, StringUtils.equals(Definition.NO, frozenFlag));
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

		/*
		 * 处理支出
		 */
		for (RowVO rowVO : tvoExpenditure.toRowVOs()) {
			String amount = rowVO.getCellVOValue("CN_AMOUNT");// 金额
			String planFlag = rowVO.getCellVOValue("CR_PLAN_FLAG");// 是为计划支出,否为实际支出
			String createDate = rowVO.getCellVOValue("CN_CREATE_DATE");// 创建日期
			String yearMonth = sdf.format(new Date(Long.parseLong(createDate)));
			if (StringUtils.equals(planFlag, Definition.YES)) {
				// 处理计划支出
				if (rvoPlanOut.containsKey(yearMonth)) {
					String countAmount = rvoPlanOut.getCellVOValue(yearMonth);
					rvoPlanOut.setCellVOValue(yearMonth, MyDBHelper.doubleAdd(countAmount, amount));
				} else {
					rvoPlanOut.addCellVO(new CellVO(yearMonth, amount));
				}
			} else {
				// 处理实际支出    
				if (rvoActualOut.containsKey(yearMonth)) {
					String countAmount = rvoActualOut.getCellVOValue(yearMonth);
					rvoActualOut.setCellVOValue(yearMonth, MyDBHelper.doubleAdd(countAmount, amount));
				} else {
					rvoActualOut.addCellVO(new CellVO(yearMonth, amount));
				}
			}
		}

		/*
		 * 处理收入    
		 */
		for (RowVO rowVO : tvoIncome.toRowVOs()) {
			String incomeTypeId = rowVO.getCellVOValue("CR_INCOME_TYPE_ID");// 收入类型
			String amount = rowVO.getCellVOValue("CN_AMOUNT");// 金额
			String planFlag = rowVO.getCellVOValue("CR_PLAN_FLAG");// 是为计划收入,否为实际收入
			String createDate = rowVO.getCellVOValue("CN_CREATE_DATE");// 创建日期
			String yearMonth = sdf.format(new Date(Long.parseLong(createDate)));
			if (StringUtils.equals(planFlag, Definition.YES)) {
				// 处理计划收入
				if (rvoPlanIn.containsKey(yearMonth)) {
					String countAmount = rvoPlanIn.getCellVOValue(yearMonth);
					rvoPlanIn.setCellVOValue(yearMonth, MyDBHelper.doubleAdd(countAmount, amount));
				} else {
					rvoPlanIn.addCellVO(new CellVO(yearMonth, amount));
				}
			} else {
				// 处理实际收入   
				if (rvoActualIn.containsKey(yearMonth)) {
					String countAmount = rvoActualIn.getCellVOValue(yearMonth);
					rvoActualIn.setCellVOValue(yearMonth, MyDBHelper.doubleAdd(countAmount, amount));
				} else {
					rvoActualIn.addCellVO(new CellVO(yearMonth, amount));
				}
			}
			// 处理现金
			if (hmTypeIdToActive.get(incomeTypeId)) {
				if (StringUtils.equals(planFlag, Definition.YES)) {
					// 处理计划收入现金
					if (rvoPlanInCash.containsKey(yearMonth)) {
						String countAmount = rvoPlanInCash.getCellVOValue(yearMonth);
						rvoPlanInCash.setCellVOValue(yearMonth, MyDBHelper.doubleAdd(countAmount, amount));
					} else {
						rvoPlanInCash.addCellVO(new CellVO(yearMonth, amount));
					}
				} else {
					// 处理实际收入现金
					if (rvoActualInCash.containsKey(yearMonth)) {
						String countAmount = rvoActualInCash.getCellVOValue(yearMonth);
						rvoActualInCash.setCellVOValue(yearMonth, MyDBHelper.doubleAdd(countAmount, amount));
					} else {
						rvoActualInCash.addCellVO(new CellVO(yearMonth, amount));
					}
				}
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
			calFirst.setTime(sdf.parse("201810"));// 从2018年10月开始记账
			calStart.setTime(sdf.parse(startMonth.replace("/", "")));// 从2018年10月开始记账
			calEnd.setTime(sdf.parse(endMonth.replace("/", "")));
		} catch (Exception e) {
			e.printStackTrace();
			throw new MyHomeException(e);
		}
		RowVO rvoHead = new RowVO();// 根据用户选择的月份区间生成的头信息
		tvoCount.setHeadRowVO(rvoHead);
		while (calFirst.getTimeInMillis() <= calEnd.getTimeInMillis()) {
			String yearMonth = sdf.format(calFirst.getTime());
			String planOut = rvoPlanOut.getCellVOValue(yearMonth);
			String actualOut = rvoActualOut.getCellVOValue(yearMonth);
			String planIn = rvoPlanIn.getCellVOValue(yearMonth);
			String actualIn = rvoActualIn.getCellVOValue(yearMonth);
			String planInCash = rvoPlanInCash.getCellVOValue(yearMonth);
			String actualInCash = rvoActualInCash.getCellVOValue(yearMonth);
			// 处理Head
			if (calFirst.getTimeInMillis() >= calStart.getTimeInMillis()) {
				rvoHead.addCellVO(new CellVO(yearMonth, yearMonth));
			}

			/*
			 * 将没有数据的月份补数
			 */
			if (StringUtils.isEmpty(planOut)) {
				planOut = planOutByMonth;// 预告支出
				rvoPlanOut.addCellVO(new CellVO(yearMonth, planOut));
			}
			if (StringUtils.isEmpty(actualOut)) {
				actualOut = "0";// 实际支出
				rvoActualOut.addCellVO(new CellVO(yearMonth, actualOut));
			}
			if (StringUtils.isEmpty(planIn)) {
				planIn = planInByMonth;// 预告收入
				rvoPlanIn.addCellVO(new CellVO(yearMonth, planIn));
			}
			if (StringUtils.isEmpty(actualIn)) {
				actualIn = "0";// 实际收入
				rvoActualIn.addCellVO(new CellVO(yearMonth, actualIn));
			}
			if (StringUtils.isEmpty(planInCash)) {
				planInCash = planInByMonth;// 预告收入现金
				rvoPlanInCash.addCellVO(new CellVO(yearMonth, planInCash));
			}
			if (StringUtils.isEmpty(actualInCash)) {
				actualInCash = "0";// 实际收入金
				rvoActualInCash.addCellVO(new CellVO(yearMonth, actualInCash));
			}

			// 处理支出偏差    
			String deviation = "0.0";
			if (Double.parseDouble(planOut) > 0) {
				deviation = MyDBHelper.doubleDiv(actualOut, planOut);
				if (Double.parseDouble(deviation) == 1) {
					deviation = "0.0";
				}
			}
			rvoOutDeviation.addCellVO(new CellVO(yearMonth, deviation));

			// 处理当月结余
			String inCash = planInCash;
			String in = planIn;
			String out = planOut;
			if (Integer.parseInt(yearMonth) < currentYearMonth) {
				// 当前月份之前的月份使用实际数据,当前月份以及之后的月份使用预告数据
				inCash = actualInCash;
				in = actualIn;
				out = actualOut;
			}
			String surplusMonth = MyDBHelper.doubleSub(in, out);
			rvoSurplusMonth.addCellVO(new CellVO(yearMonth, surplusMonth));

			// 累积结余
			surplusCount = MyDBHelper.doubleAdd(surplusCount, surplusMonth);
			rvoSurplusCount.addCellVO(new CellVO(yearMonth, surplusCount));

			// 现金结余
			surplusCash = MyDBHelper.doubleAdd(surplusCash, MyDBHelper.doubleSub(inCash, out));
			rvoSurplusCash.addCellVO(new CellVO(yearMonth, surplusCash));

			calFirst.add(Calendar.MONTH, 1);// 加一个月
		}

		// 将汇总数据放入Request
		request.setAttribute("tvoCount", tvoCount);

		// 跳转到[总账页面]
		ModelAndView mv = new ModelAndView();
		mv.setViewName("tiles.generaledger");
		return mv;
	}
}

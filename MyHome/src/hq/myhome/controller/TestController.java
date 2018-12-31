/**
 * 
 */
package hq.myhome.controller;

import java.util.Calendar;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import hq.mydb.dao.BaseDAO;
import hq.mydb.data.CellVO;
import hq.mydb.data.RowVO;
import hq.mydb.data.TableVO;
import hq.mydb.utils.MyDBHelper;
import hq.myhome.utils.Definition;

/**
 * 用于测试的Consoller
 * @author Administrator
 *
 */
@Controller // 声明当前为Controller
@RequestMapping(value = { "/test" })
public class TestController {
	private BaseDAO baseDAO;

	@Autowired
	public TestController(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	/*
	 * 测试JDBC是否连接成功
	 */
	@RequestMapping(value = { "/JDBC" }, method = RequestMethod.GET)
	public String testJDBC() {
		this.baseDAO.initDataBaseCache();
		return "home"; // 返回逻辑视图名称"home"
	}

	/*
	 * 测试JDBC是否连接成功
	 */
	@RequestMapping(value = { "/firstInitIncomeExpenditure" }, method = RequestMethod.GET)
	public String firstInitIncomeExpenditure() {
		Calendar cal = MyDBHelper.getCalendar("2018-10", "yyyy-MM");
		while (cal.getTimeInMillis() < System.currentTimeMillis()) {
			initIncomeExpenditure(cal.getTimeInMillis());
			cal.add(Calendar.DATE, 1);//增加一天   
		}
		return "home"; // 返回逻辑视图名称"home"
	}

	private void initIncomeExpenditure(long currentTime_long) {
		try {
			TableVO tvoExpenditure = new TableVO("tn_expenditure");// 支出
			TableVO tvoIncome = new TableVO("tn_income");// 收入
			String currentTime = String.valueOf(currentTime_long);

			// 读取所有的支出类型
			TableVO tvoExpenditureType = this.baseDAO.queryForTableVO("tn_expenditure_type");
			// 读取所有的收入类型
			TableVO tvoIncomeType = this.baseDAO.queryForTableVO("tn_income_type");
			/*
			 * 1. 每个月1号初始化所有计划的周期行支出和收入的记录
			 */
			if (StringUtils.equals("01", MyDBHelper.formatDate(currentTime_long, "dd"))) {
				// 支出
				for (RowVO rvoExpenditureType : tvoExpenditureType.toRowVOs()) {
					String expenditureTypeId = rvoExpenditureType.getCellVOValue("CN_ID");
					String periodicFlag = rvoExpenditureType.getCellVOValue("CR_PERIODIC_FLAG");
					if (StringUtils.equals(Definition.YES, periodicFlag)) {
						String amount = rvoExpenditureType.getCellVOValue("CN_AMOUNT");
						RowVO rvoExpenditure = new RowVO();
						tvoExpenditure.addRowVO(rvoExpenditure);
						rvoExpenditure.addCellVO(new CellVO("CR_EXPENDITURE_TYPE_ID", expenditureTypeId));
						rvoExpenditure.addCellVO(new CellVO("CN_AMOUNT", amount));
						rvoExpenditure.addCellVO(new CellVO("CR_PLAN_FLAG", Definition.YES));
						rvoExpenditure.addCellVO(new CellVO("CN_CREATE_DATE", currentTime));
					}
				}
				// 收入
				for (RowVO rvoIncomeType : tvoIncomeType.toRowVOs()) {
					String incomeTypeId = rvoIncomeType.getCellVOValue("CN_ID");
					String periodicFlag = rvoIncomeType.getCellVOValue("CR_PERIODIC_FLAG");
					if (StringUtils.equals(Definition.YES, periodicFlag)) {
						String amount = rvoIncomeType.getCellVOValue("CN_AMOUNT");
						RowVO rvoIncome = new RowVO();
						tvoIncome.addRowVO(rvoIncome);
						rvoIncome.addCellVO(new CellVO("CR_INCOME_TYPE_ID", incomeTypeId));
						rvoIncome.addCellVO(new CellVO("CN_AMOUNT", amount));
						rvoIncome.addCellVO(new CellVO("CR_PLAN_FLAG", Definition.YES));
						rvoIncome.addCellVO(new CellVO("CN_CREATE_DATE", currentTime));
					}
				}
			}

			/*
			 * 2. 将计划支出和收入的生效日期等于当前的计划生成对应的实际支出或收入.
			 */
			// 支出
			for (RowVO rvoExpenditureType : tvoExpenditureType.toRowVOs()) {
				String expenditureTypeId = rvoExpenditureType.getCellVOValue("CN_ID");
				String periodicFlag = rvoExpenditureType.getCellVOValue("CR_PERIODIC_FLAG");
				String effectiveDay = rvoExpenditureType.getCellVOValue("CN_EFFECTIVE_DAY");
				String amount = rvoExpenditureType.getCellVOValue("CN_AMOUNT");
				if (StringUtils.equals(Definition.YES, periodicFlag) && Integer.parseInt(effectiveDay) == Integer.parseInt(MyDBHelper.formatDate(currentTime_long, "dd"))) {
					RowVO rvoExpenditure = new RowVO();
					tvoExpenditure.addRowVO(rvoExpenditure);
					rvoExpenditure.addCellVO(new CellVO("CR_EXPENDITURE_TYPE_ID", expenditureTypeId));
					rvoExpenditure.addCellVO(new CellVO("CN_AMOUNT", amount));
					rvoExpenditure.addCellVO(new CellVO("CR_PLAN_FLAG", Definition.NO));
					rvoExpenditure.addCellVO(new CellVO("CN_CREATE_DATE", currentTime));
				}
			}

			// 收入
			for (RowVO rvoIncomeType : tvoIncomeType.toRowVOs()) {
				String encomeTypeId = rvoIncomeType.getCellVOValue("CN_ID");
				String periodicFlag = rvoIncomeType.getCellVOValue("CR_PERIODIC_FLAG");
				String effectiveDay = rvoIncomeType.getCellVOValue("CN_EFFECTIVE_DAY");
				String amount = rvoIncomeType.getCellVOValue("CN_AMOUNT");
				if (StringUtils.equals(Definition.YES, periodicFlag) && Integer.parseInt(effectiveDay) == Integer.parseInt(MyDBHelper.formatDate(currentTime_long, "dd"))) {
					RowVO rvoIncome = new RowVO();
					tvoIncome.addRowVO(rvoIncome);
					rvoIncome.addCellVO(new CellVO("CR_INCOME_TYPE_ID", encomeTypeId));
					rvoIncome.addCellVO(new CellVO("CN_AMOUNT", amount));
					rvoIncome.addCellVO(new CellVO("CR_PLAN_FLAG", Definition.NO));
					rvoIncome.addCellVO(new CellVO("CN_CREATE_DATE", currentTime));
				}
			}

			this.baseDAO.saveOrUpdateTableVO(tvoIncome);
			this.baseDAO.saveOrUpdateTableVO(tvoExpenditure);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

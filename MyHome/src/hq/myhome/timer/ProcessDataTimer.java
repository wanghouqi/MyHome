package hq.myhome.timer;

import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import hq.mydb.dao.BaseDAO;
import hq.mydb.data.CellVO;
import hq.mydb.data.RowVO;
import hq.mydb.data.TableVO;
import hq.mydb.utils.MyDBHelper;
import hq.myhome.utils.Definition;

/**
 * 每天启动
 * 	1. 每个月1号初始化所有计划的周期行支出和收入的记录
 *  2. 将计划支出和收入的生效日期等于当前的计划生成对应的实际支出或收入.
 * @author wanghq
 *
 */
public class ProcessDataTimer extends TimerTask {
	BaseDAO baseDAO;
	public static boolean isFirst = true;// 用于处理系统启动时,当前系统时间已经过了设定时间时,立刻执行的问题.

	public ProcessDataTimer(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	public void run() {
		try {
			if (ProcessDataTimer.isFirst) {
				ProcessDataTimer.isFirst = false;
				return;// 用于处理系统启动时,当前系统时间已经过了设定时间时,立刻执行的问题.
			}
			TableVO tvoExpenditure = new TableVO("tn_expenditure");// 支出
			TableVO tvoIncome = new TableVO("tn_income");// 收入
			long currentTime_long = System.currentTimeMillis();
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
			 * 2.生成周期性的实际收入或支出 记录.
			 * 	将计划支出和收入的生效日期等于当前的计划生成对应的实际支出或收入.
			 */
			// 支出
			for (RowVO rvoExpenditureType : tvoExpenditureType.toRowVOs()) {
				String startDate = rvoExpenditureType.getCellVOValue("CN_START_DATE");
				String endDate = rvoExpenditureType.getCellVOValue("CN_END_DATE");
				if (StringUtils.isNotEmpty(startDate) && Long.parseLong(startDate) > currentTime_long) {
					continue;
				}
				if (StringUtils.isNotEmpty(endDate) && Long.parseLong(endDate) < currentTime_long) {
					continue;
				}

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

package hq.myhome.utils;

import java.util.Date;
import java.util.Timer;

import org.apache.log4j.Logger;

import hq.mydb.dao.BaseDAO;
import hq.mydb.utils.MyDBHelper;
import hq.myhome.timer.ProcessDataTimer;

public class InitServlet {
	private Logger log = Logger.getLogger(this.getClass().getName());
	BaseDAO baseDAO;

	public void setBaseDAO(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	public void init() {
		log.info("/***********************************************");
		log.info("                Init Servlet               ");
		log.info("                                                ");
		try {
			Timer timer = new Timer();
			timer.schedule(new ProcessDataTimer(baseDAO), new Date(MyDBHelper.getFirstTimeOfDay(new Date().getTime())), 24 * 60 * 60 * 1000);

			log.info("成功启动ProcessDataTimer");
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("                                                ");
		log.info("***********************************************/");

	}
}

package hq.myhome.timer;

import java.util.TimerTask;

import hq.mydb.dao.BaseDAO;

/**
 * 因为MariaDB每隔8小时无访问会自动关闭,加入这个定时每3小时访问一次数据库.
 * @author wanghq
 *
 */
public class DataBaseKeepLiveTimer extends TimerTask {
	BaseDAO baseDAO;

	public DataBaseKeepLiveTimer(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	public void run() {
		try {
			this.baseDAO.queryForTableVO("tl_boolean");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
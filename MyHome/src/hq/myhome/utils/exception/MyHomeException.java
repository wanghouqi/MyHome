/**
 * 
 */
package hq.myhome.utils.exception;

import hq.myhome.utils.MyHomeHelper;

/**
 * 数据库操作出现的Exception的封装,用于在Controller中捕获.
 * @author Administrator
 *
 */
public class MyHomeException extends RuntimeException {
	private static final long serialVersionUID = -1603165073386349195L;
	private String errorMsg = "";
	private Exception exception = null;

	public MyHomeException(String errorMsg) {
		super();
		this.errorMsg = errorMsg;
	}

	public MyHomeException(Exception exception) {
		super();
		this.exception = exception;
	}

	public MyHomeException(String errorMsg, Exception exception) {
		super();
		this.errorMsg = errorMsg;
		this.exception = exception;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public String toString() {
		String msg = "";
		Exception e = this;
		while (e instanceof MyHomeException) {
			MyHomeException tbe = (MyHomeException) e;
			msg += tbe.getErrorMsg() + "  \r\n  >>>>>>";
			e = tbe.getException();
		}
		StringBuffer sbInfo = new StringBuffer();
		String exceptionString = MyHomeHelper.getTrace(e);
		sbInfo.append("/*************** DAO Exception ************ \r\n");
		sbInfo.append("DAO Message : " + msg + "\r\n");
		sbInfo.append("DAO ExceptionString : " + exceptionString + "\r\n");
		sbInfo.append("***********************************************/ \r\n");
		return sbInfo.toString();
	}

}

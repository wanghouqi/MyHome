package hq.myhome.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 通用的算法
 * 
 * @author Administrator
 *
 */
public class MyHomeHelper {
	private static int splitLength = 10;

	public static String createUUID() {
		String primaryId = UUID.randomUUID().toString();
		primaryId = primaryId.replace("-", "");
		return primaryId;
	}

	/**
	 * 将Exception的printStackTrace()转成字符窜.
	 * 
	 * @param t
	 * @return String
	 */
	public static String getTrace(Throwable t) {
		String traceMsg = "N/A";
		if (t != null) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);
			t.printStackTrace(writer);
			StringBuffer buffer = stringWriter.getBuffer();
			traceMsg = buffer.toString();
		}
		return traceMsg;
	}

	/**
	 * 密码加密
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static String encryptPassword(String data) {
		String password = "";
		try {
			if (StringUtils.isEmpty(data)) {
				return data;
			}
			byte[] srcBytes = data.getBytes();
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(Base64.encodeBase64(srcBytes));
			password = new String(new Hex().encode(md5.digest()));
		} catch (Exception e) {
			// TODO: handle exception
		}
		return password;
	}

	/**
	 * 首字母转小写
	 * @param s
	 * @return
	 */
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * 首字母转大写
	 * @param s
	 * @return
	 */
	public static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * 将传入的Number进行格式化
	 * @param number
	 * @param format
	 * @return String
	 */
	public static String formatNumber(double number, String format) {
		String number_format = "";
		try {
			DecimalFormat decimalFormat = new DecimalFormat("0.0000");
			if (StringUtils.isNotEmpty(format)) {
				decimalFormat = new DecimalFormat(format);
			}
			number_format = decimalFormat.format(number);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//继续转换得到秒数的long型
		return number_format;
	}

	/**
	 * 将传入的Number进行格式化
	 * @param number
	 * @param format
	 * @return String
	 */
	public static String formatNumber(long number, String format) {
		String number_format = "";
		try {
			DecimalFormat decimalFormat = new DecimalFormat("0.0000");
			if (StringUtils.isNotEmpty(format)) {
				decimalFormat = new DecimalFormat(format);
			}
			number_format = decimalFormat.format(number);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//继续转换得到秒数的long型
		return number_format;
	}

	/**
	 * 使用默认长度截取字符窜
	 * @param showValue
	 * @return String
	 */
	public static String splitShowValue(String showValue) {
		return splitShowValue(showValue, -1);
	}

	/**
	 * 根据传入的长度截取字符窜
	 * @param showValue
	 * @param length
	 * @return String
	 */
	public static String splitShowValue(String showValue, int length) {
		if (length < 0) {
			length = MyHomeHelper.splitLength;
		}
		String returnString = showValue;
		if (StringUtils.isBlank(showValue)) {
			returnString = "";
		} else {
			returnString = MyHomeHelper.substring(showValue, length);
			if (returnString.length() < showValue.length()) {
				returnString += "...";
			}
		}
		return returnString;
	}

	/**
	* 截取一段字符的长度,不区分中英文,如果数字不正好，则少取一个字符位
	*
	* @author patriotlml
	* @param origin  原始字符串
	* @param len, 截取长度(一个汉字长度按2算的)
	* @return String, 返回的字符串
	*/
	public static String substring(String origin, int len) {
		if (origin == null || origin.equals("") || len < 1) {
			return "";
		}
		try {
			if (len > MyHomeHelper.length(origin)) {
				return origin;
			}
			len = MyHomeHelper.getSplitLength(origin, len);
			origin = origin.substring(0, len);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return origin;
	}

	/**
	 *  得到一个用于拆分混合字符的长度
	 *
	 * @param s ,需要得到长度的字符串
	 * @param len 英文需要的长度
	 * @return int, 得到的字符串长度
	 */
	public static int getSplitLength(String s, int len) {
		if (s == null) {
			return 0;
		}
		int ret = 0;
		char[] c = s.toCharArray();
		//		System.out.println("c.length=" + c.length);
		for (int i = 0; i < c.length; i++) {
			len--;
			if (!isLetter(c[i])) {
				len--;
			}
			if (len < 0) {
				continue;
			}
			ret++;
		}
		return ret;
	}

	/**
	 * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为2,英文字符长度为1
	 *
	 * @param s 需要得到长度的字符串
	 * @return int 得到的字符串长度
	 */
	public static int length(String s) {
		if (s == null) {
			return 0;
		}
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
		}
		return len;
	}

	/**
	 * 判断一个字符是Ascill字符还是其它字符（如汉，日，韩文字符）
	 *
	 * @param c 需要判断的字符
	 * @return boolean 返回true,Ascill字符
	 */
	public static boolean isLetter(char c) {
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	/* 
	  * 判断是否为自然数, 123, -123, 12.3, -12.3
	  * @param str 传入的字符串  
	  * @return 是整数返回true,否则返回false  
	*/
	public static boolean isNumber(String str) {
		boolean isOk = false;
		if (str != null && !str.equals("")) {
			Pattern pattern = Pattern.compile("^([+-]?)\\d*\\.?\\d+$");
			isOk = pattern.matcher(str).matches();
		}
		return isOk;
	}

	public static HashMap<String, String> analyzeHTMLByContent(String contentHTML) {
		HashMap<String, String> hm = new HashMap<String, String>();
		Document doc = Jsoup.parse(contentHTML);
		Element image = doc.select("img").first();
		String firstImgSrc = "";
		if (image != null) {
			firstImgSrc = image.attr("src");
		}
		hm.put("firstImgSrc", firstImgSrc);
		hm.put("text", doc.text());
		return hm;
	}

}

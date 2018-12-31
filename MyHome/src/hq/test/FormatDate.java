package hq.test;

import java.text.SimpleDateFormat;

public class FormatDate {

	public static void main(String[] args) {
		try {
			// TODO Auto-generated method stub
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			System.out.println(sdf.parse("2018-10-10").getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

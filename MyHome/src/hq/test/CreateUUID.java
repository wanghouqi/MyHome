package hq.test;

import java.util.UUID;

public class CreateUUID {

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			// TODO Auto-generated method stub
			String primaryId = UUID.randomUUID().toString();
			primaryId = primaryId.replace("-", "");
			System.out.println(primaryId);
		}
	}

}

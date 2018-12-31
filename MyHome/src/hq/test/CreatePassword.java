package hq.test;

import java.security.NoSuchAlgorithmException;

import hq.myhome.utils.MyHomeHelper;

public class CreatePassword {

	public CreatePassword() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println(MyHomeHelper.encryptPassword("123456"));

	}

}

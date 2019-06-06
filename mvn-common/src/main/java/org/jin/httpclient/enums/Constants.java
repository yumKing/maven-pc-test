package org.jin.httpclient.enums;

import java.io.File;

/**
 * 常量类
 * 
 */
public abstract class Constants {

	private Constants() {
	}

	public static final String UTF8_ENCODING = "UTF-8";
	
	/**
	 * 文件分隔符（在linux里是“/”，windows里是“\”）
	 */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/**
	 * 用户的主目录，例如：/home/zhou
	 */
	public static final String USER_HOME = System.getProperty("user.home");

	/**
	 * 用户的主目录，例如：/home/zhou/
	 */
	public static final String USER_HOME_DIR = USER_HOME + FILE_SEPARATOR;

	/**
	 * 用户的当前工作目录
	 */
	public static final String USER_DIR = System.getProperty("user.dir");

	/**
	 * 用户的当前工作目录/
	 */
	public static final String USER_DIR_DIR = System.getProperty("user.dir") + FILE_SEPARATOR;

	/**
	 * 临时目录，例如：/home/zhou/tmp/
	 */
	public static final String USER_TEMP_DIR;

	static {
		String tmp = USER_HOME_DIR + "tmp" + FILE_SEPARATOR;
		File file = new File(tmp);
		if (!file.exists()) {
			file.mkdir();
		}else{
			if(!file.isDirectory()){//make sure it is a directory
				file.delete();
				file.mkdir();
			}
		}
		USER_TEMP_DIR = tmp;
	}

	public static void main(String... args) {
		System.out.println(FILE_SEPARATOR);
		System.out.println(USER_HOME);
		System.out.println(USER_DIR);
		System.out.println(USER_TEMP_DIR);
	}
}
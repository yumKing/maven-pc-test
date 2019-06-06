package org.jin.httpclient.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;


import org.jin.httpclient.enums.Constants;
import org.jin.httpclient.utils.CommonUtils;


public abstract class FileUtils {

	/**
	 * 这个API是为了那些只允许传递pathname的API准备的，如果将file打到jar里面，这个文件的绝对路径或者url路径是没有用的
	 * 只能创建一个临时文件来处理
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getRealPath(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return fileName;
		}
		String path = Constants.USER_DIR_DIR + fileName;
		file = new File(path);
		if (file.exists()) {
			return path;
		}
		path = "src/main/resources/" + fileName;
		file = new File(path);
		if (file.exists()) {
			return path;
		}
		ClassLoader cL = Thread.currentThread().getContextClassLoader();
		if (cL == null) {
			cL = FileUtils.class.getClassLoader();
		}
		URL url = cL.getResource(fileName);
		if (url != null) {// 这是个解压的过程
			String tmpFilePath = Constants.USER_TEMP_DIR + fileName;
			File tmpFile = new File(tmpFilePath);
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			writeFileFromStream(tmpFilePath, cL.getResourceAsStream(fileName));
			return tmpFilePath;
		}

		throw new RuntimeException("can find the file,please check!");
	}

	/**
	 * 默认先从工作目录找,如果没有去classpath里面读取文件 这么做的目的在于：
	 * 1:一般大家都优先修改工作目录里面的内容，classpath里的文件又打进了jar包，不可以修改
	 * 2:在多个工程存在的情况下，如果引用的jar包需要配置文件，但是使用者又不知道的情况下，可以默认打一个配置文件到classpath里
	 * 
	 * @param fileName
	 *            ！！！文件名
	 * @return
	 */
	public static InputStream getRealInputStream(String fileName) {

		File file = new File(fileName);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
			}
		}
		file = new File(Constants.USER_DIR_DIR + fileName);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
			}
		}
		file = new File("src/main/resources/" + fileName);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
			}
		}
		ClassLoader cL = Thread.currentThread().getContextClassLoader();
		if (cL == null) {
			cL = FileUtils.class.getClassLoader();
		}
		if (cL.getResource(fileName) != null) {
			return cL.getResourceAsStream(fileName);
		}
		return null;
	}

	/**
	 * 默认先从工作目录找,如果没有去classpath里面读取文件 这么做的目的在于：
	 * 1:一般大家都优先修改工作目录里面的内容，classpath里的文件又打进了jar包，不可以修改
	 * 2:在多个工程存在的情况下，如果引用的jar包需要配置文件，但是使用者又不知道的情况下，可以默认打一个配置文件到classpath里
	 * 
	 * @param fileName
	 * @return
	 */
	public static URL getFileURL(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return getUrl(file);
		}
		file = new File(Constants.USER_DIR_DIR + fileName);
		if (file.exists()) {
			return getUrl(file);
		}

		ClassLoader cL = Thread.currentThread().getContextClassLoader();
		if (cL == null) {
			cL = FileUtils.class.getClassLoader();
		}
		URL res = cL.getResource(fileName);
		if (res != null) {
			return res;
		}

		file = new File("src/main/resources/" + fileName);
		if (file.exists()) {
			return getUrl(file);
		}
		throw new RuntimeException("can find the file,please check!");
	}

	static URL getUrl(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
		}
		return null;
	}

	/**
	 * 将流写入一个文件
	 * 
	 * @param pathname
	 * @param in
	 */
	public static void writeFileFromStream(String pathname, InputStream in) {
		if (CommonUtils.isBlank(pathname)) {
			throw new IllegalArgumentException("filename is null!");
		}
		File file = new File(pathname);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream fou = null;
		try {
			fou = new FileOutputStream(file);
			byte[] buffer = new byte[1024 * 4];
			int len = -1;
			while ((len = in.read(buffer)) != -1) {
				fou.write(buffer, 0, len);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(in);
			close(fou);
		}
	}

	// 将字符串写进一个文件
	public static void writeFileFromString(String filename, String str, boolean append) {
		if (CommonUtils.isBlank(filename)) {
			throw new IllegalArgumentException("filename is null!");
		}
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter writer = null;
		BufferedReader reader = null;
		try {
			writer = new BufferedWriter(new FileWriter(file, append));
			reader = new BufferedReader(new StringReader(str));
			String tmp = null;
			StringBuffer buffer = new StringBuffer();
			while ((tmp = reader.readLine()) != null)
				buffer.append(tmp + "\n");
			writer.write(buffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(reader);
			close(writer);
		}

	}

	public static void close(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void toFile(byte[] bytes, String filePath) throws IOException {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);
		out.write(bytes);
		out.flush();
		out.close();
	}

	public static byte[] fromInputStream(InputStream in) throws IOException {
		if (in == null) {
			throw new IOException("InputStream is NULL");
		}
		UnSynByteArrayOutputStream swapStream = new UnSynByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = in.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();
		swapStream.close();
		in.close();
		return in2b;
	}

}

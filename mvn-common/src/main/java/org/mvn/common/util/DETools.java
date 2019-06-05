package org.mvn.common.util;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


/**
 * 加解密工具集 包括的算法模式 MD5 SHA-N AES
 * 
 * @author jinyang
 *
 */
public class DETools {

	/**
	 * md5加密
	 * 
	 * @param text 待加密的文本
	 * @return 加密文本
	 */
	public static String MD5(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(text.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			// Now we need to zero pad it if you actually want the full 32 chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * SHA-N 加密
	 * 
	 * @param type 各种SHA加密方式，如 SHA-1, SHA-256
	 * @param text 待加密的文本
	 * @return 加密文本
	 */
	public static String SHA_N(String type, String text) {
		try {
			MessageDigest md = MessageDigest.getInstance(type);
			md.update(text.getBytes(Charset.forName("UTF-8")));
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digest.length; i++) {
				int v = (digest[i] & 0xff) + 0x100;
				sb.append(Integer.toString(v, 16));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * AES加密 1.构造密钥生成器 2.根据ecnodeRules规则初始化密钥生成器 3.产生密钥 4.创建和初始化密码器 5.内容加密 6.返回字符串
	 * 
	 * @param aesKey 加解密key
	 * @param text   待加密的文本
	 * @return 加密后的文本
	 */
	public static String AES_Encrypt(String aesKey, String text) {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(aesKey.getBytes());
			keygen.init(128, random);
			SecretKey original_key = keygen.generateKey();

			byte[] raw = original_key.getEncoded();
			SecretKey key = new SecretKeySpec(raw, "AES");

			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] byte_encode = text.getBytes("UTF-8");
			byte[] byte_AES = cipher.doFinal(byte_encode);

			return BinaryTools.parseByte2HexStr(byte_AES);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * AES解密
	 * 
	 * @param aesKey 加解密key
	 * @param text   已加密文本
	 * @return 解密后的文本
	 */
	public static String AES_Decrypt(String aesKey, String text) {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(aesKey.getBytes());
			keygen.init(128, random);
			SecretKey original_key = keygen.generateKey();
			byte[] raw = original_key.getEncoded();
			SecretKey key = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			// byte [] byte_content= Base64.decodeBase64(content);
			byte[] byte_content = BinaryTools.parseHexStr2Byte(text);
			byte[] byte_decode = cipher.doFinal(byte_content);
			return new String(byte_decode, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, String> createKeys(int keySize){
        //为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg;
        try{
            kpg = KeyPairGenerator.getInstance("RSA");
        }catch(NoSuchAlgorithmException e){
            throw new IllegalArgumentException("No such algorithm-->[RSA]");
        }

        //初始化KeyPairGenerator对象,密钥长度
        kpg.initialize(keySize);
        //生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();
        //得到公钥
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.encodeBase64URLSafeString(publicKey.getEncoded());
        //得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.encodeBase64URLSafeString(privateKey.getEncoded());
        Map<String, String> keyPairMap = new HashMap<String, String>();
        keyPairMap.put("publicKey", publicKeyStr);
        keyPairMap.put("privateKey", privateKeyStr);

        return keyPairMap;
    }

	/**
	 * RSA 公钥加密
	 * @param publicKeyFilePath 公钥文件路径
	 * @param text 待加密的文本
	 * @return 公钥加密后的文本
	 */
	public static String RSA_Encrypt(String publicKeyFilePath, String text) {
		
		try {
			String publicKey = FileTools.readKeyFile(publicKeyFilePath);
			//构造公钥对象
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        byte[] buffer = Base64.decodeBase64(publicKey);
	        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
	        RSAPublicKey rsakey =  (RSAPublicKey) keyFactory.generatePublic(keySpec);
	        
	        Cipher cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.ENCRYPT_MODE, rsakey);
	        
	        return Base64.encodeBase64URLSafeString(cipher.doFinal(text.getBytes("UTF-8")));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}

	/**
	 * RSA 私钥解密
	 * @param privateKeyFilePath 私钥文件路径
	 * @param text 公钥加密后的文本
	 * @return 私钥解密后的文本
	 */
	public static String RSA_Dencrypt(String privateKeyFilePath, String text) {
		try {
			String privateKey = FileTools.readKeyFile(privateKeyFilePath);
			
			// 解密由base64编码的私钥
	        byte[] keyBytes = Base64.decodeBase64(privateKey);
	        // 构造PKCS8EncodedKeySpec对象
	        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
	        // KEY_ALGORITHM 指定的加密算法
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        // 取私钥匙对象
	        RSAPrivateKey rsa_key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
	        
	        Cipher cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.DECRYPT_MODE, rsa_key);
	        return new String(cipher.doFinal(Base64.decodeBase64(text)), "UTF-8");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * DES加密
	 * 
	 * @param des_key 加解密秘钥
	 * @param text 待加密文本
	 * @return
	 */
	public static String DES_Encrypt(String des_key, String text) {
		String result = null;
		
		if (text == null || text.length() == 0) {
			return text;
		}
		
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			// 从原始密匙数据创建DESKeySpec对象
			byte[] keyByte = des_key.getBytes("UTF-8");
			DESKeySpec dks = new DESKeySpec(keyByte);
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(dks);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
			
			byte[] encryptionByte = text.getBytes("UTF-8");
			
			result = BinaryTools.parseByte2HexStr(cipher.doFinal(encryptionByte));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * DES解密
	 * 
	 * @param des_key 加解密秘钥
	 * @param text 待解密的密文
	 * @return
	 */
	public static String DES_Decrypt(String des_key, String text) {
		String result = null;
		
		if (text == null || text.length() == 0) {
			return text;
		}
		
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			// 从原始密匙数据创建一个DESKeySpec对象
			byte[] keyByte = des_key.getBytes("UTF-8");
			DESKeySpec dks = new DESKeySpec(keyByte);
			// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(dks);
			// Cipher对象实际完成解密操作
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
			
			result = new String(cipher.doFinal(BinaryTools.parseHexStr2Byte(text)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
//		Map<String, String> keys = createKeys(1024);
//		String pub_key = keys.get("publicKey");
//		String priv_key = keys.get("privateKey");
//		FileUtils.writeStringToFile(new File("D:\\Documents\\pub_key"), pub_key, "UTF-8");
//		FileUtils.writeStringToFile(new File("D:\\Documents\\priv_key"), priv_key, "UTF-8");
//		
//		String rsa_Encrypt = RSA_Encrypt("D:\\Documents\\pub_key", "123456");
//		System.out.println(rsa_Encrypt);
//		String rsa_Dencrypt = RSA_Dencrypt("D:\\Documents\\priv_key", rsa_Encrypt);
//		System.out.println(rsa_Dencrypt);
		
	}
}

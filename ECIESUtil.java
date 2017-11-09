package cz.o2.smartbox.utility.security;

import android.util.Base64;

import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.KDF2BytesGenerator;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveSpec;
import org.spongycastle.jce.spec.IESParameterSpec;

import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;


public class ECIESUtil {

	public static void testEncrypt() throws Exception {
		String publicKeyValue = "BBaPTE9w7+XA0bH0bmoqBou7ieI/AP/Yzx8JoAYxB11XgpoiRqnlSySa9lF5dzU7meKvN8TlX1bybUZTtqljCJw=";
		byte[] byteKey = Base64.decode(publicKeyValue, Base64.DEFAULT);

		org.spongycastle.jce.spec.ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
		KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
		org.spongycastle.jce.spec.ECNamedCurveSpec curvedParams = new ECNamedCurveSpec("secp256r1", spec.getCurve(), spec.getG(), spec.getN());
		java.security.spec.ECPoint point = org.spongycastle.jce.ECPointUtil.decodePoint(curvedParams.getCurve(), byteKey);
		java.security.spec.ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, curvedParams);
		org.spongycastle.jce.interfaces.ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(pubKeySpec);

		byte[] inputBytes = "test".getBytes();

		org.spongycastle.jce.spec.IESParameterSpec params = new IESParameterSpec(null, null, 0, 128, new byte[16]);
		IESCipherGCM ownCipher = new IESCipherGCM(
				new IESEngineGCM(
						new ECDHBasicAgreement(),
						new KDF2BytesGenerator(new SHA256Digest()),
						new AESGCMBlockCipher()), 16);

		ownCipher.engineInit(Cipher.ENCRYPT_MODE, publicKey, params, new SecureRandom());


		byte[] cipherResult = ownCipher.engineDoFinal(inputBytes, 0, inputBytes.length);
		String result = Base64.encodeToString(cipherResult, Base64.DEFAULT);
	}


	public static void testDecrypt() throws Exception {
		String privateKeyValue = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg4eSgzxefv1TF8LRy3M0DI5MkQ6+HCqVBxLtJOEovV3ChRANCAAQLKgQxt5HmRvEWTZ9jMvZTNT2ANsI74wv2U6kdR1l7KxAqOckjpgbrWGfmKvkOkIMy001gRSafMV/X6mOkxjXo";
		byte[] byteKey = Base64.decode(privateKeyValue, Base64.DEFAULT);

		java.security.spec.PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(byteKey);

		KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
		org.spongycastle.jce.interfaces.ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(encodedKeySpec);

		byte[] inputBytes = Base64.decode("BDoUkNsU4RC8SSjrwOtDi8TEZuq09Zz/q7/YWKbBt44fLKDDlIm7Nq4OF66AiUIzX/sXpxuysdCHoEuINt2LAise8TbddzI3vbgLGaoD2ttj0O8LtA==", Base64.DEFAULT);

		IESParameterSpec params = new IESParameterSpec(null, null, 0, 128, new byte[16]);
		IESCipherGCM ownCipher = new IESCipherGCM(
				new IESEngineGCM(
						new ECDHBasicAgreement(),
						new KDF2BytesGenerator(new SHA256Digest()),
						new AESGCMBlockCipher()), 16);

		ownCipher.engineInit(Cipher.DECRYPT_MODE, privateKey, params, new SecureRandom());

		byte[] cipherResult = ownCipher.engineDoFinal(inputBytes, 0, inputBytes.length);
		String result = new String(cipherResult);
	}
}
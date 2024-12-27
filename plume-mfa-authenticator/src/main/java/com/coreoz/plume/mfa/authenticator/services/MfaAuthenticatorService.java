package com.coreoz.plume.mfa.authenticator.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.coreoz.plume.mfa.authenticator.db.daos.MfaAuthenticatorDao;
import com.coreoz.plume.mfa.authenticator.db.generated.MfaAuthenticator;
import com.coreoz.plume.mfa.authenticator.services.encryption.MfaSecretKeyEncryptionProvider;
import com.coreoz.plume.mfa.authenticator.webservices.data.UserCredentials;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Singleton
public class MfaAuthenticatorService {

    private static final Logger logger = LoggerFactory.getLogger(MfaAuthenticatorService.class);

    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();
    private final MfaSecretKeyEncryptionProvider mfaSecretKeyEncryptionProvider;
    private final MfaConfigurationService configurationService;
    private final MfaUserService userService;
    private final MfaAuthenticatorDao mfaAuthenticatorDao;

    @Inject
    private MfaAuthenticatorService(
        MfaSecretKeyEncryptionProvider mfaSecretKeyEncryptionProvider,
        MfaUserService userService,
        MfaAuthenticatorDao mfaAuthenticatorDao,
        MfaConfigurationService configurationService
    ) {
        this.mfaSecretKeyEncryptionProvider = mfaSecretKeyEncryptionProvider;
        this.configurationService = configurationService;
        this.mfaAuthenticatorDao = mfaAuthenticatorDao;
        this.userService = userService;
    }

    public String generateSecretKey() throws Exception {
        GoogleAuthenticatorKey key = authenticator.createCredentials();
        return key.getKey();
    }

    public String hashSecretKey(String secretKey) throws Exception {
        return mfaSecretKeyEncryptionProvider.get().encrypt(secretKey);
    }

    public String getQRBarcodeURL(UserCredentials user, String secret) {
        final String issuer = configurationService.appName();
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, user.getUserName(), secret, issuer);
    }

    public byte[] generateQRCode(UserCredentials user, String secret) {
        String qrBarcodeURL = getQRBarcodeURL(user, secret);
        try {
            return QRCodeGenerator.generateQRCodeImage(qrBarcodeURL, 200, 200);
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code", e);
            return null;
        }
    }

    public boolean verifyCode(String secret, int code) {
        try {
            return authenticator.authorize(mfaSecretKeyEncryptionProvider.get().decrypt(secret), code);
        } catch (Exception e) {
            logger.info("could not decrypt secret key", e);
            return false;
        }
    }

    public String createMfaAuthenticatorSecretKey(UserCredentials credentials) throws Exception {
        Long idUser = userService.authenticatedUserId(credentials.getUserName(), credentials.getPassword());
        String secretKey = generateSecretKey();
        MfaAuthenticator mfa = new MfaAuthenticator();
        mfa.setSecretKey(hashSecretKey(secretKey));
        mfa.setIdUser(idUser);
        mfaAuthenticatorDao.save(mfa);

        return secretKey;
    }

    public boolean isCredentialsValidForUser(String username, int code) {
        Long idUser = userService.userId(username);
        List<MfaAuthenticator> authenticators = mfaAuthenticatorDao.findByIdUser(idUser);
        return authenticators.stream().anyMatch(mfa -> verifyCode(mfa.getSecretKey(), code));
    }

    private static class QRCodeGenerator {
        public static byte[] generateQRCodeImage(String barcodeText, int width, int height) throws WriterException, IOException {
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }
}
